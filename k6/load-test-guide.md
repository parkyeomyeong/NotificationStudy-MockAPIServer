# Mock 서버 부하 테스트 실행 가이드

## 배경

알림 발송 Mock 서버(`localhost:8081`)의 동시 처리 한계를 찾는 부하 테스트입니다.
이 서버는 요청마다 지수 분포 기반 랜덤 지연(평균 ~4초, 최대 30초)을 적용하고, 1/3 확률로 500 에러를 반환합니다.

## 목적

**동시 요청(VU)을 단계별로 올려가며 Mock 서버가 버티는 한계점을 찾는다.**

### 한계점 판별 기준
- 평균 응답시간이 **~4초를 초과**하기 시작 → 요청이 Tomcat 쓰레드 대기열에서 줄 서고 있음
- 에러율(`http_req_failed`)이 **33%를 초과** → 서버가 요청을 거부하기 시작

두 지표가 동시에 나빠지는 직전의 VU 수 = Mock 서버의 동시 처리 한계 = 미들웨어 쓰레드 풀 상한선

## 사전 조건

- Mock 서버가 `localhost:8081`에서 실행 중이어야 함
- k6가 설치되어 있어야 함 (`brew install k6`)
- 부하 테스트 스크립트: `k6/load-test.js`

## 테스트 절차

k6 스크립트의 stages를 아래처럼 **한 단계씩** 바꿔가며 실행합니다.
각 단계에서 2분간 유지하여 충분한 데이터를 수집합니다.

### Round 1: VU 50
```javascript
export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '2m',  target: 50 },
    { duration: '10s', target: 0 },
  ],
};
```
```bash
k6 run k6/load-test.js
```
→ 결과에서 `http_req_duration`의 avg, `http_req_failed` 비율 기록

### Round 2: VU 100
```javascript
export const options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '2m',  target: 100 },
    { duration: '10s', target: 0 },
  ],
};
```

### Round 3: VU 150
stages의 target을 150으로 변경하여 동일하게 실행

### Round 4: VU 200
stages의 target을 200으로 변경하여 동일하게 실행

### Round 5: VU 250 (필요 시)
Round 4까지 에러 없으면 계속 50씩 올려가며 반복

## 결과 기록 형식

각 Round 실행 후 아래 값을 기록합니다:

| Round | VU | avg 응답시간 | p95 응답시간 | 에러율 | TPS (http_reqs/s) | 판정 |
|-------|-----|------------|------------|--------|-------------------|------|
| 1 | 50  | ?s | ?s | ?% | ? | ✅ or ❌ |
| 2 | 100 | ?s | ?s | ?% | ? | ✅ or ❌ |
| 3 | 150 | ?s | ?s | ?% | ? | ✅ or ❌ |
| 4 | 200 | ?s | ?s | ?% | ? | ✅ or ❌ |

### 판정 기준
- ✅ = avg 응답시간 ~4초 이하, 에러율 ~33% 이하
- ❌ = avg 응답시간 4초 초과 OR 에러율 33% 초과

### 최종 결론
처음으로 ❌가 나온 Round의 직전 VU 수 = **Mock 서버 동시 처리 한계**
