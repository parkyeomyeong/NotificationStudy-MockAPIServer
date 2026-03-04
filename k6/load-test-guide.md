# Mock 서버 부하 테스트 실행 가이드

## 배경

알림 발송 Mock 서버의 동시 처리 한계를 찾는 부하 테스트입니다.
이 서버는 요청마다 지수 분포 기반 랜덤 지연(평균 ~4초, 최대 30초)을 적용하고, 1/3 확률로 500 에러를 반환합니다.

## 목적

**동시 요청(VU)을 단계별로 올려가며 Mock 서버가 버티는 한계점을 찾는다.**

### 한계점 판별 기준

> **주의**: 반드시 **k6(클라이언트) 측 응답시간**을 기준으로 판단해야 합니다.
> Grafana의 서버 측 메트릭(`http_server_requests_seconds`)은 쓰레드가 실제 처리한 시간만 측정하며,
> 대기열에서 기다린 시간은 포함하지 않으므로 과부하 상황에서도 정상으로 보일 수 있습니다.

- **TPS가 더 이상 올라가지 않는 VU 수** = 실질적 처리 한계 (가장 중요)
- k6 클라이언트 측 P95 응답시간 15초 초과 → 대기열에서 대기 시간이 쌓이고 있음
- 에러율이 Mock 서버 의도된 ~33% 를 초과 → 서버가 요청을 거부하기 시작

## 사전 조건

- Mock 서버가 **별도 머신**(윈도우 등)에서 실행 중이어야 함 (부하 생성기와 분리)
- k6 설치: `brew install k6`
- Prometheus + Grafana 실행 중

## 환경 구성 (Prometheus → Grafana에서 k6 메트릭 보기)

### 1. Prometheus 실행 (Remote Write 수신 활성화)

기존 실행 명령에 `--web.enable-remote-write-receiver` 플래그를 추가합니다.
이 플래그가 있어야 k6가 Prometheus에 데이터를 보낼 수 있습니다.

```bash
prometheus --config.file=prometheus.yml --web.enable-remote-write-receiver
```

### 2. Grafana 대시보드 등록

```bash
curl -X POST -H "Content-Type: application/json" \
  -d @grafana-dashboard.json \
  http://admin:비밀번호@localhost:3000/api/dashboards/db
```

## 테스트 시나리오 (breakpoint-test.js)

VU를 단계적으로 올리며 임계치를 탐색합니다.

**Test2 시나리오 (현재 활성):**
| 단계 | VU | 유지 시간 |
|------|-----|----------|
| 1 | 50 | 1분 |
| 2 | 100 | 1분 |
| 3 | 200 | 1분 |
| 4 | 300 | 1분 |
| 5 | 400 | 1분 |
| 쿨다운 | 0 | 30초 |

## 실행 방법

### k6만 실행 (Grafana 없이)
```bash
k6 run k6/breakpoint-test.js
```

### k6 + Grafana 연동 실행 (권장)

`--out experimental-prometheus-rw` 옵션을 추가해야 k6 메트릭(VU, 응답시간, 에러율 등)이 Prometheus로 전송됩니다.
이 옵션 없이 `k6 run`만 하면 터미널 출력만 되고 Grafana에서는 볼 수 없습니다.

#### Mac / Linux
```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
K6_PROMETHEUS_RW_TREND_STATS=avg,p(50),p(95),p(99) \
  k6 run --out experimental-prometheus-rw k6/breakpoint-test.js
```

#### Windows (PowerShell)
```powershell
$env:K6_PROMETHEUS_RW_SERVER_URL="http://localhost:9090/api/v1/write"
$env:K6_PROMETHEUS_RW_TREND_STATS="avg,p(50),p(95),p(99)"
k6 run --out experimental-prometheus-rw k6/breakpoint-test.js
```

#### Windows (CMD)
```cmd
set K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write
set K6_PROMETHEUS_RW_TREND_STATS=avg,p(50),p(95),p(99)
k6 run --out experimental-prometheus-rw k6/breakpoint-test.js
```

| 환경변수/옵션 | 필수 | 설명 |
|-------------|------|------|
| `--out experimental-prometheus-rw` | ✅ 필수 | 이걸 써야 k6 → Prometheus 전송 활성화 |
| `K6_PROMETHEUS_RW_SERVER_URL` | ✅ 필수 | 결과를 보낼 Prometheus 주소 |
| `K6_PROMETHEUS_RW_TREND_STATS` | 선택 (권장) | Grafana에서 avg/P50/P95/P99를 쉽게 조회하려면 필요 |

> `--out` 옵션을 쓰면 VU, 응답시간, **에러율** 등 k6 메트릭이 Prometheus로 자동 전송됩니다.
> Grafana 대시보드에서 서버 측과 k6 측 에러율을 한 그래프에서 비교할 수 있습니다.

## Grafana 대시보드 패널 구성

| 행 | 왼쪽 | 오른쪽 |
|----|------|--------|
| 1 | **k6 VU (동시 사용자)** | **k6 클라이언트 측 응답시간** |
| 2 | TPS | 서버 측 응답시간 (대기열 미포함) |
| 3 | Tomcat 쓰레드 | HTTP 에러율 |
| 4 | 동시 연결 수 | P95/P99 |
| 5 | CPU | JVM Heap |

> **1행(k6 메트릭)**: 사용자 입장에서 실제 체감하는 응답시간. 대기열 대기 시간 포함.
> **2행(서버 메트릭)**: 서버 내부에서 쓰레드가 처리한 시간만. 서버 병목 원인 파악용.

## 결과 판정

| 기준 | PASS | FAIL |
|------|------|------|
| TPS | 이전 단계보다 증가 | 이전 단계와 동일 or 감소 |
| k6 클라이언트 P95 | 15초 이하 | 15초 초과 |
| k6 에러율 | 서버 측 에러율과 동일 (~33%) | 서버 측보다 높음 (과부하 에러 추가 발생) |

> **에러율 판정 핵심**: 서버 측 에러율과 k6 측 에러율이 **벌어지는 순간**이 실제 과부하 시작.
> 서버 측만 보면 항상 ~33%로 정상 처럼 보이지만, k6 측에서 연결 거부·타임아웃이 발생하면 더 높아짐.

**최종 결론**: TPS가 더 이상 올라가지 않는 VU 수 = Mock 서버 동시 처리 한계

### 참고: P95/P99 threshold 설정 방법

Mock 서버의 지연은 지수 분포(평균 4초)를 따르며, 백분위수 공식은 `P값 = -평균 × ln(1 - P/100)` 입니다.

| 백분위 | 계산식 | 이론값 |
|--------|--------|--------|
| P50 | -4 × ln(0.5) | **2.8초** |
| P95 | -4 × ln(0.05) | **12.0초** |
| P99 | -4 × ln(0.01) | **18.4초** |

위 이론값은 참고용이며, **실제 threshold는 아래 절차로 설정해야 합니다:**

1. **베이스라인 측정**: VU 10 등 낮은 부하로 테스트하여 실제 P95/P99 값을 측정
2. **threshold 설정**: 베이스라인 실측값 × 1.2~1.3 (20~30% 마진) 으로 설정
3. **본 테스트**: 이 threshold를 기준으로 VU를 올려가며 PASS/FAIL 판정

예시:
```
베이스라인 측정 결과 P95 = 13초
→ threshold = 13 × 1.25 = 약 16초
→ breakpoint-test.js의 thresholds에 'p(95)<16000' 으로 설정
```

마진을 두는 이유: 지수 분포의 랜덤성 때문에 부하 없이도 순간적으로 이론값을 넘길 수 있다고 생각!

