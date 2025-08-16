import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: __ENV.TEST_TYPE === 'performance' ? 50 : 10, // More VUs for performance
    duration: __ENV.TEST_TYPE === 'performance' ? '1m' : '30s', // Longer for performance
    thresholds: {
        http_req_failed: ['rate<0.05'],   // allow up to 5% failed requests
        http_req_duration: ['p(95)<200'], // p95 latency
    },
};

// Configurable via environment variables
const API_URL = 'http://localhost:8080/api/hello';
const API_KEY = __ENV.KEY || 'api-key-load';
const API_SECRET = __ENV.SECRET || 'secret-load';

export default function () {
    const headers = {
        'X-API-KEY': API_KEY,
        'X-API-SECRET': API_SECRET,
    };

    let res = http.get(API_URL, { headers });

    // Checks depend on test type
    if (__ENV.TEST_TYPE === 'functional') {
        check(res, {
            'status 200': (r) => r.status === 200,
            'status 429': (r) => r.status === 429,
        });
    } else {
        // performance test: expect all 200
        check(res, {
            'status 200': (r) => r.status === 200,
        });
    }

    // Random small sleep for realistic traffic
    sleep(Math.random() * 0.2);
}



// k6 run -e TEST_TYPE=functional -e KEY=api-key-limit -e SECRET=secret-limit load-test-unified.js
//k6 run -e TEST_TYPE=performance -e KEY=api-key-load -e SECRET=secret-load load-test-unified.js