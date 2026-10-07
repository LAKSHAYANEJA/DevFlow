import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 50,
    duration: '60s',
};

export function setup() {
    const loginPayload = JSON.stringify({
        email: 'test@devflow.com',
        password: 'newpassword123',
    });

    const loginResponse = http.post(
        'http://localhost:8080/api/v1/auth/login',
        loginPayload,
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(loginResponse, {
        'login successful': (r) => r.status === 200,
    });

    if (loginResponse.status !== 200) {
        throw new Error(`Login failed: ${loginResponse.status}`);
    }

    return {
        token: loginResponse.json('accessToken'),
    };
}

export default function (data) {
    const response = http.get(
        'http://localhost:8080/api/v1/projects',
        {
            headers: {
                Authorization: `Bearer ${data.token}`,
            },
        }
    );

    check(response, {
        'projects request successful': (r) => r.status === 200,
    });
}