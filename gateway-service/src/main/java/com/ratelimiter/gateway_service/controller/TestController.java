package com.ratelimiter.gateway_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class TestController {

    @GetMapping("/hello")
    public String sayHello() {
        log.info("Hello endpoint called successfully");
        return "Hello! Your request was allowed.";
    }

    @GetMapping("/**") // This will match any path, like /any/path
    public ResponseEntity<String> handleAll() {
        return ResponseEntity.ok("Request was successful!");
    }

    @GetMapping("/test")
    public String testEndpoint() {
        log.info("Test endpoint called successfully");
        return "Test endpoint - request processed successfully!";
    }
}
