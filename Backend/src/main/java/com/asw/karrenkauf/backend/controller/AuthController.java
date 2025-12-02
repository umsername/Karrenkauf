package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.security.JWTUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JWTUtil jwtUtil;

    public AuthController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // LOGIN ÜBER BROWSER / URL
    @GetMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        if ("user".equals(username) && "password".equals(password)) {
            String token = jwtUtil.generateToken(username);
            return "👍 Login successful! \n\nTOKEN:\n" + token;
        } else {
            return "❌ Invalid username/password";
        }
    }
}
