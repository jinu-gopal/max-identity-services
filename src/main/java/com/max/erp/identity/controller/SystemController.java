package com.max.erp.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class SystemController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "max-identity-service",
                "status", "UP",
                "timestamp", Instant.now()
        );
    }
}