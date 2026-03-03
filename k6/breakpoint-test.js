import http from 'k6/http';
import { check, sleep } from 'k6';

// 부하 대상 서버 주소
const BASE_URL = 'http://192.168.0.2:8081';

/**
 * Breakpoint Test (임계치 탐색)
 *
 * 목적: Mock 서버가 정상 응답을 유지하는 최대 동시 요청 수(임계치)를 찾는다.
 *
 * 전략:
 *  Test1:
 *  - 10 VU부터 시작해서 1분마다 10씩 증가
 *  - 각 단계에서 1분간 유지하면서 응답시간/에러율 관찰
 *  - 총 10단계(10→20→30→...→100)로 약 10분 소요
 *  - 에러율이 급등하거나 응답시간이 급격히 느려지는 지점이 임계치
 *  Test2:
 *  - 50 VU부터 시작해서 30초마다 50씩 증가
 *  - 각 단계에서 1분간 유지하면서 응답시간/에러율 관찰
 *  - 총 10단계(50→100→150→...→500)로 약 10분 소요
 *  - 에러율이 급등하거나 응답시간이 급격히 느려지는 지점이 임계치
 *
 * Grafana에서 모니터링할 지표:
 *  - http_req_duration (P95, P99)
 *  - http_req_failed rate
 *  - Tomcat 쓰레드 사용량
 */
export const options = {
    stages: [
        // test1 : 10 VU 단위로 단계적 증가 (각 단계 1분 유지)
        // { duration: '30s', target: 10 },
        // { duration: '1m', target: 10 },

        // { duration: '15s', target: 20 },
        // { duration: '1m', target: 20 },

        // { duration: '15s', target: 30 },
        // { duration: '1m', target: 30 },

        // { duration: '15s', target: 40 },
        // { duration: '1m', target: 40 },

        // { duration: '15s', target: 50 },
        // { duration: '1m', target: 50 },

        // { duration: '15s', target: 60 },
        // { duration: '1m', target: 60 },

        // { duration: '15s', target: 70 },
        // { duration: '1m', target: 70 },

        // { duration: '15s', target: 80 },
        // { duration: '1m', target: 80 },

        // { duration: '15s', target: 90 },
        // { duration: '1m', target: 90 },

        // { duration: '15s', target: 100 },
        // { duration: '1m', target: 100 },

        // { duration: '30s', target: 0 },   // 쿨다운

        //test2 :최대 400까지
        { duration: '30s', target: 50 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '1m', target: 200 },   // 여기서 avg가 4s 초과하는지 확인
        { duration: '30s', target: 300 },
        { duration: '1m', target: 300 },
        { duration: '30s', target: 400 },
        { duration: '1m', target: 400 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        // 임계치 판단 기준
        // Mock 서버 지연: 지수분포 평균 4초, 최대 30초
        // 부하 없을 때 이론값: P50≈2.8s, P95≈12s, P99≈18s
        // → 이보다 넉넉하게 잡아서, 초과하면 "서버 과부하"로 판단
        http_req_duration: [
            'p(95)<15000',   // P95 응답시간 15초 이내
            'p(99)<25000',   // P99 응답시간 25초 이내
        ],
        http_req_failed: [
            'rate<0.05',     // 에러율 5% 미만
        ],
    },
};

export default function () {
    const headers = { 'Content-Type': 'application/json' };

    // email / teams 엔드포인트 랜덤 호출
    const isEmail = Math.random() > 0.5;
    const endpoint = isEmail ? '/api/v1/notification/email' : '/api/v1/notification/teams';
    const payload = isEmail
        ? JSON.stringify({ toEmail: `test${__VU}@test.com`, title: '부하테스트', content: '테스트' })
        : JSON.stringify({ toTeamsUserId: `user${__VU}`, title: '부하테스트', content: '테스트' });

    const res = http.post(`${BASE_URL}${endpoint}`, payload, {
        headers,
        tags: { endpoint: endpoint },  // Grafana에서 엔드포인트별 분류 가능
    });

    check(res, {
        '정상 응답 (2xx)': (r) => r.status >= 200 && r.status < 300,
        '응답시간 < 15초': (r) => r.timings.duration < 15000,
    });
}
