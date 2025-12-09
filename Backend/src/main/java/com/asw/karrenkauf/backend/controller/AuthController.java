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
    	if (userRepo.findByUserName(username).isPresent()) {
            return "❌ Username already exists. Pick a different one.";
        }
    	
        String hashedPassword = this.encoder.encode(password);

        String id = UUID.randomUUID().toString();
        User u = new User(id, hashedPassword, username); // Passwort jetzt gehashed
        userRepo.save(u);

        return "Created user " + username + " with id " + id;
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
	    PasswordEncoder encoder = new BCryptPasswordEncoder();
	    System.out.println(user.getUserPassword());
	    System.out.println(encoder.encode(password));
	    if (!encoder.matches(password, user.getUserPassword())) {
	        return "❌ Invalid password";
	    }
	
	    // Generate token
	    String token = jwtUtil.generateToken(user.getUserId());
	    long exp = System.currentTimeMillis() + 1000 * 60 * 60; // 1 hour
	    tokenRepo.save(new TokenEntry(token, user.getUserId(), exp));
	
	    return "👍 Login successful!\n\nTOKEN:\n" + token;
	}
}
