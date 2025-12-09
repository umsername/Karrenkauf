package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.dto.LoginRequest;
import com.asw.karrenkauf.backend.model.TokenEntry;
import com.asw.karrenkauf.backend.model.User;
import com.asw.karrenkauf.backend.repository.TokenRepository;
import com.asw.karrenkauf.backend.repository.UserRepository;
import com.asw.karrenkauf.backend.security.JWTUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api")
public class AuthController {
	
	// BCrypt-PasswordEncoder erzeugen
    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final JWTUtil jwtUtil;
    protected static TokenRepository tokenRepo;
    protected static UserRepository userRepo;

    public AuthController(JWTUtil jwtUtil, TokenRepository tokenRepo, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        AuthController.tokenRepo = tokenRepo;
        AuthController.userRepo = userRepo;
    }
    
    @PostMapping("/user")
    public String createUser(@RequestParam String username, @RequestParam String password) {
        System.out.println("🔵 [REGISTER] Request received - Username: " + username);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ [REGISTER] Username is empty");
            return "❌ Username cannot be empty";
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("❌ [REGISTER] Password is empty");
            return "❌ Password cannot be empty";
        }

        // Check if username already exists
        Optional<User> existingUser = userRepo.findByUserName(username);
        if (existingUser.isPresent()) {
            System.out.println("❌ [REGISTER] Username already exists: " + username);
            return "❌ Username already exists";
        }

        // Hash password before saving
        String hashedPassword = this.encoder.encode(password);
        System.out.println("🔐 [REGISTER] Password hashed successfully");

        // Create new user
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, hashedPassword, username);
        userRepo.save(newUser);

        System.out.println("✅ [REGISTER] User registered successfully - ID: " + userId);
        return "👍 User registered successfully!";
    }

    // Check login status before showing login page
    @GetMapping("/status")
    public String status(@RequestParam(required = false) String token) {
        System.out.println("🔵 [STATUS] Request received - Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        if (token == null) {
            System.out.println("❌ [STATUS] No token provided");
            return "No token → Please login.";
        }

        Optional<TokenEntry> entry = tokenRepo.findById(token);

        if (entry.isEmpty()) {
            System.out.println("❌ [STATUS] Token unknown");
            return "Token unknown → Please login.";
        }
        if (entry.get().isExpired()) {
            System.out.println("❌ [STATUS] Token expired");
            return "Token expired → Please login again.";
        }

        String username = entry.get().getUsername();
        System.out.println("✅ [STATUS] User is logged in: " + username);
        return "Already logged in as: " + username;
    }

	@PostMapping("/login")
	public String login(@RequestBody LoginRequest request) {
	    String username = request.getUsername();
	    String password = request.getPassword();
	    
	    System.out.println("🔵 [LOGIN] Request received - Username: " + username);
	
	    Optional<User> userOpt = userRepo.findByUserName(username);
	    if (userOpt.isEmpty()) {
	        System.out.println("❌ [LOGIN] User does not exist: " + username);
	        return "❌ User does not exist";
	    }
	
	    User user = userOpt.get();
	    System.out.println("🔍 [LOGIN] User found - Verifying password...");
	    
	    if (!encoder.matches(password, user.getUserPassword())) {
	        System.out.println("❌ [LOGIN] Invalid password for user: " + username);
	        return "❌ Invalid password";
	    }
	
	    // Generate token
	    String token = jwtUtil.generateToken(user.getUserId());
	    long exp = System.currentTimeMillis() + 1000 * 60 * 60; // 1 hour
	    tokenRepo.save(new TokenEntry(token, user.getUserId(), exp));
	
	    System.out.println("✅ [LOGIN] Login successful - Token generated for user: " + username);
	    return "👍 Login successful!\n\nTOKEN:\n" + token;
	}
}
