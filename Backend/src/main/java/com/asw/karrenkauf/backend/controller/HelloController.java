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
        return "Hello from backend!";
    }
}