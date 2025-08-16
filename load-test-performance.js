import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 50,               // 50 virtual users
    duration: '1m',        // run for 1 minute
    thresholds: {
        http_req_failed: ['rate<0.05'],   // allow up to 5% failed requests
        http_req_duration: ['p(95)<200'], // 95% requests < 200ms
    },
};

const API_URL = 'http://localhost:8080/api/hello';
const API_KEY = 'api-key-load';
const API_SECRET = 'secret-load';

export default function () {
    const headers = {
        'X-API-KEY': API_KEY,
        'X-API-SECRET': API_SECRET,
    };

    let res = http.get(API_URL, { headers });

    check(res, {
        'status 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 0.2); // small random pause to simulate real traffic
}
