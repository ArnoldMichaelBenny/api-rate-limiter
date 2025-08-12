package com.ratelimiter.gateway_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    @GetMapping("/**")
    public ResponseEntity<String> catchAll() {
        return ResponseEntity.ok("Request processed successfully by the gateway.");
    }
}