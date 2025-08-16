// load-test.js (k6)
// Usage examples:
//   # high-throughput key (no 429s expected)
//   k6 run -e KEY=api-key-load -e SECRET=secret-load load-test.js
//
//   # low limit key (429s expected, but ignored in failure rate)
//   k6 run -e KEY=api-key-limit -e SECRET=secret-limit -e IGNORE_429=true load-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';

// ----------- Config via env vars (with safe defaults) -----------
const KEY = __ENV.KEY || 'api-key-load';
const SECRET = __ENV.SECRET || 'secret-load';
const URL = __ENV.URL || 'http://localhost:8080/api/hello';
const IGNORE_429 = (__ENV.IGNORE_429 || 'true').toLowerCase() === 'true';

// Treat 429 as "expected" when IGNORE_429=true
if (IGNORE_429) {
    http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }, 429));
} else {
    http.setResponseCallback('none');
}

// ----------- k6 Options -----------
export const options = {
    discardResponseBodies: true,
    stages: [
        { duration: '30s', target: 100 }, // ramp up
        { duration: '1m', target: 100 },  // steady
        { duration: '10s', target: 0 },   // ramp down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],   // <1% unexpected errors
        http_req_duration: ['p(95)<500'], // 95% in <500ms
    },
};

// ----------- Test -----------
export default function () {
    const res = http.get(URL, {
        headers: {
            'X-API-KEY': KEY,
            'X-API-SECRET': SECRET,
        },
        tags: {
            expected_status: IGNORE_429 ? '200_or_429' : '200_only',
            api_key: KEY,
        },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'status is 429': (r) => r.status === 429,
    });

    sleep(1); // 1 req/VU/sec
}
