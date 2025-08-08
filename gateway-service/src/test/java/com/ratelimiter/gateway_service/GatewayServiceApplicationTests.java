package com.ratelimiter.gateway_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.redis.host=localhost",
		"spring.redis.port=6379",
		"rate-limiter.requests-per-minute=10"
})
class GatewayServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test will verify that the Spring context loads correctly
	}
}
