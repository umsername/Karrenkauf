package com.asw.karrenkauf.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api")
public class HelloController {
	@PermitAll
	@GetMapping("/hello")
    public String hello() {
        System.out.println("🔵 [HELLO] GET /api/hello - Request received");
        return "Hello from backend!";
    }
    
    @PermitAll
    @GetMapping("/public/ping")
    public String ping() {
        System.out.println("🔵 [PING] GET /api/public/ping - Request received");
        return "🟢 Backend is reachable! Server time: " + System.currentTimeMillis();
    }
}