package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.dto.LoginRequest;
import com.asw.karrenkauf.backend.model.TokenEntry;
import com.asw.karrenkauf.backend.model.User;
import com.asw.karrenkauf.backend.repository.TokenRepository;
import com.asw.karrenkauf.backend.repository.UserRepository;
import com.asw.karrenkauf.backend.security.JWTUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepo;
    private final UserRepository userRepo;

    public AuthController(JWTUtil jwtUtil, TokenRepository tokenRepo, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    // Check login status before showing login page
    @GetMapping("/status")
    public String status(@RequestParam(required = false) String token) {
        if (token == null) return "No token → Please login.";

        Optional<TokenEntry> entry = tokenRepo.findById(token);

        if (entry.isEmpty()) return "Token unknown → Please login.";
        if (entry.get().isExpired()) return "Token expired → Please login again.";

        return "Already logged in as: " + entry.get().getUsername();
    }

	@PostMapping("/login")
	public String login(@RequestBody LoginRequest request) {
	    String username = request.getUsername();
	    String password = request.getPassword();
	
	    Optional<User> userOpt = userRepo.findByUserName(username);
	    if (userOpt.isEmpty()) return "❌ User does not exist";
	
	    User user = userOpt.get();
	    if (!user.getUserPassword().equals(password)) {
	        return "❌ Invalid password";
	    }
	
	    // Generate token
	    String token = jwtUtil.generateToken(user.getUserId());
	    long exp = System.currentTimeMillis() + 1000 * 60 * 60; // 1 hour
	    tokenRepo.save(new TokenEntry(token, user.getUserId(), exp));
	
	    return "👍 Login successful!\n\nTOKEN:\n" + token;
	}

	@PostMapping("/user")
	public String createUser(@RequestParam String username, @RequestParam String password) {
	    // Validate input
	    if (username == null || username.trim().isEmpty()) {
	        return "❌ Username cannot be empty";
	    }
	    if (password == null || password.trim().isEmpty()) {
	        return "❌ Password cannot be empty";
	    }

	    // Check if username already exists
	    Optional<User> existingUser = userRepo.findByUserName(username);
	    if (existingUser.isPresent()) {
	        return "❌ Username already exists";
	    }

	    // Create new user
	    String userId = java.util.UUID.randomUUID().toString();
	    User newUser = new User(userId, password, username);
	    userRepo.save(newUser);

	    return "👍 User registered successfully!";
	}
}
