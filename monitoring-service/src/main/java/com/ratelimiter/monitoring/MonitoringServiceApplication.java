package com.ratelimiter.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // IMPORT THIS

// THE FIX: This annotation explicitly tells Spring Boot to scan for and
// enable your JPA repositories in this package and its sub-packages.
@EnableJpaRepositories
@SpringBootApplication
public class MonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringServiceApplication.class, args);
	}

}