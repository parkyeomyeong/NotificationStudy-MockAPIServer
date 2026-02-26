import http from 'k6/http';
import { check } from 'k6';

// 부하 대상 서버 주소 (Mock 서버가 띄워진 PC의 IP로 변경)
const BASE_URL = 'http://localhost:8081';

export const options = {
    stages: [
        { duration: '30s', target: 10 },   // 30초에 걸쳐 동시 10까지 올림
        { duration: '1m', target: 10 },   // 1분간 동시 10 유지
        { duration: '30s', target: 50 },   // 동시 50까지 올림
        { duration: '1m', target: 50 },   // 1분간 유지
        { duration: '30s', target: 100 },  // 동시 100까지 올림
        { duration: '1m', target: 100 },  // 1분간 유지
        { duration: '30s', target: 200 },  // 동시 200까지 올림
        { duration: '1m', target: 200 },  // 1분간 유지
        { duration: '30s', target: 0 },    // 종료
    ],
};

export default function () {
    const headers = { 'Content-Type': 'application/json' };

    const payload = Math.random() > 0.5
        ? JSON.stringify({ toEmail: `test${__VU}@test.com`, title: '부하테스트', content: '테스트' })
        : JSON.stringify({ toTeamsUserId: `user${__VU}`, title: '부하테스트', content: '테스트' });

    const endpoint = Math.random() > 0.5
        ? '/api/v1/notification/email'
        : '/api/v1/notification/teams';

    const res = http.post(`${BASE_URL}${endpoint}`, payload, { headers });

    check(res, {
        '정상 응답': (r) => r.status === 200 || r.status === 500,
    });
}
