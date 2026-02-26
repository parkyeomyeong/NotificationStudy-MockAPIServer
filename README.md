# Notification Dispatch Mock Server

알림 Front 서버 개발 시 연동할 **알림 발송 API의 Mock Server**입니다.  
실제 이메일/Teams 발송 없이, 랜덤 지연과 랜덤 결과를 반환하여 발송 서버와의 통신을 시뮬레이션합니다.

## 제공 API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/notification/email` | 이메일 발송 |
| POST | `/api/v1/notification/teams` | Teams 메시지 발송 |

### 요청 예시

**이메일:**
```json
{
  "toEmail": "test@example.com",
  "title": "계좌 이체 알림",
  "content": "100,000원이 출금되었습니다."
}
```

**Teams:**
```json
{
  "toTeamsUserId": "user123",
  "title": "시스템 장애 알림",
  "content": "DB 커넥션 풀 부족"
}
```

### 응답 예시

**성공 (200):**
```json
{
  "status": "SUCCESS",
  "message": "Email dispatch completed in 4523ms",
  "timestamp": "2026-02-25T15:00:00"
}
```

**실패 (200):**
```json
{
  "status": "FAILURE",
  "message": "Email dispatch completed in 12300ms",
  "timestamp": "2026-02-25T15:00:12"
}
```

**에러 (500):**
```json
{
  "status": "ERROR",
  "message": "[EMAIL] Unexpected dispatch error occurred during notification delivery",
  "timestamp": "2026-02-25T15:00:05"
}
```

## 내부 동작 방식

1. API 요청이 들어오면 `NotificationController`에서 수신합니다.
2. `NotificationDispatchService`가 발송 시뮬레이션을 수행합니다.
3. `LatencySimulator`가 **지수 분포 기반 랜덤 지연**을 발생시킵니다. (상한 30초)
4. `DispatchStatus`가 **SUCCESS / FAILURE / ERROR** 중 하나를 동일한 확률로 랜덤 선택합니다.
5. ERROR가 선택되면 `DispatchException`이 발생하고, `GlobalExceptionHandler`가 500 응답을 반환합니다.
6. SUCCESS 또는 FAILURE가 선택되면 200 응답을 반환합니다.

### 지연 시간 분포

단순 균등 분포(0~30)로 하려 했지만 실제 API 응답 패턴과 맞지 않다고 판단하여 실제 네트워크 지연을 따른다는 지수 분포를 적용했습니다. ([참고] https://en.wikipedia.org/wiki/Exponential_distribution)

> 30초는 클라우드 로드밸런서의 기본 타임아웃(30~60초)을 고려한 값입니다.

지수분포 지연시간 테스트 (10000건)
| 구간 | 비율 | 설명 |
|------|------|------|
| 0~4초 | 6345건 (63.5%) | 대부분의 요청 |
| 4~10초 | 2883건 (28.8%) | 간헐적 지연 |
| 10~30초 | 772건 (7.7%) | 드문 고지연 |

> 평균 응답시간: 약 **4초** (λ=0.25), 상한: **30초**

## 서버 실행 (Windows)

```bash
gradlew.bat bootRun
```

> `gradle.properties`에 JDK 17 경로가 설정되어 있어, 시스템 기본 Java가 다른버전 (ex. 1.8)이어도 빌드 및 실행이 가능합니다.  
> 서버는 **8081 포트**를 사용합니다. 해당 포트를 사용 중인 프로세스가 있다면 종료 후 실행해주세요.

## 모니터링 (Actuator)

부하 테스트 시 서버 상태를 확인하기 위해 Spring Boot Actuator를 적용했습니다.

```bash
# 서버 상태 확인
curl http://localhost:8081/actuator/health

# Prometheus 메트릭 조회 (Tomcat 쓰레드, JVM, HTTP 요청 통계 등)
curl http://localhost:8081/actuator/prometheus
```
