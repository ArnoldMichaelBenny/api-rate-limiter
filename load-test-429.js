import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,               // 10 virtual users
    duration: '30s',       // run for 30 seconds
    thresholds: {
        http_req_failed: ['rate<0.1'], // fail if >10% of requests fail
        http_req_duration: ['p(95)<500'], // 95% requests < 500ms
    },
};

const API_URL = 'http://localhost:8080/api/hello';
const API_KEY = 'api-key-limit';
const API_SECRET = 'secret-limit';

export default function () {
    const headers = {
        'X-API-KEY': API_KEY,
        'X-API-SECRET': API_SECRET,
    };

    let res = http.get(API_URL, { headers });

    check(res, {
        'status 200': (r) => r.status === 200,
        'status 429': (r) => r.status === 429,
    });

    sleep(0.1); // short pause to simulate realistic traffic
}
