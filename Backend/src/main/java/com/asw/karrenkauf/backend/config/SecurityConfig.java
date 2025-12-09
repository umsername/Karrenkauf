package com.asw.karrenkauf.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    System.out.println("🔧 [SECURITY] Configuring security filter chain...");
	    
	    http
	        .csrf(csrf -> {
	            csrf.disable();
	            System.out.println("🔧 [SECURITY] CSRF protection disabled");
	        })
	        .authorizeHttpRequests(auth -> {
	            auth.requestMatchers("/api/login", "/api/user/**", "/api/status", "/api/lists/**", "/api/hello", "/api/public/**").permitAll()
	                .anyRequest().authenticated();
	            System.out.println("🔧 [SECURITY] Public endpoints configured: /api/login, /api/user/**, /api/status, /api/lists/**, /api/hello, /api/public/**");
	        })
	        .cors(cors -> cors.configurationSource(request -> {
	            var config = new org.springframework.web.cors.CorsConfiguration();
	            // WARNING: Using wildcard (*) allows any origin - only for development!
	            // In production, replace with specific origins: List.of("http://localhost:5173", "https://your-domain.com")
	            config.setAllowedOrigins(List.of("*"));
	            config.setAllowedMethods(List.of("GET","POST","OPTIONS"));
	            config.setAllowedHeaders(List.of("*"));
	            System.out.println("🔧 [SECURITY] CORS configured - Origin: " + request.getHeader("Origin"));
	            return config;
	        }))
	        .httpBasic(httpBasic -> httpBasic.disable())
	        .formLogin(form -> form.disable());

	    System.out.println("✅ [SECURITY] Security filter chain configured successfully");
	    return http.build();
	}
}
