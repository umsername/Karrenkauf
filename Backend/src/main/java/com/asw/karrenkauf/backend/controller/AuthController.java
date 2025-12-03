package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.model.TokenEntry;
import com.asw.karrenkauf.backend.model.TokenRepository;
import com.asw.karrenkauf.backend.security.JWTUtil;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepo;

    public AuthController(JWTUtil jwtUtil, TokenRepository tokenRepo) {
        this.jwtUtil = jwtUtil;
        this.tokenRepo = tokenRepo;
    }

    // Check login status before showing login page
    @GetMapping("/status")
    public String status(@RequestParam(required = false) String token) {
        if (token == null) {
            return "No token → Please login.";
        }

        Optional<TokenEntry> entry = tokenRepo.findById(token);

        if (entry.isEmpty()) {
            return "Token unknown → Please login.";
        }

        if (entry.get().isExpired()) {
            return "Token expired → Please login again.";
        }

        return "Already logged in as: " + entry.get().getUsername();
    }

    // Login
    @GetMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        if ("user".equals(username) && "password".equals(password)) {

            String token = jwtUtil.generateToken(username);
            long exp = JWTUtil.getExpirationDate();

            tokenRepo.save(new TokenEntry(token, username, exp));

            return "Login successful!\n\nTOKEN:\n" + token;
        } else {
            return "❌ Invalid username/password";
        }
    }
}
