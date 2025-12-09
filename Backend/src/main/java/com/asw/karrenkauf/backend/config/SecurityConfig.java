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
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/login", "/api/user/**", "/api/status", "/api/lists/**").permitAll()
	            .anyRequest().authenticated()
	        )
	        .cors(cors -> cors.configurationSource(request -> {
	            var config = new org.springframework.web.cors.CorsConfiguration();
	            config.setAllowedOrigins(List.of("*"));
	            config.setAllowedMethods(List.of("GET","POST","OPTIONS"));
	            config.setAllowedHeaders(List.of("*"));
	            return config;
	        }))
	        .httpBasic(httpBasic -> httpBasic.disable())
	        .formLogin(form -> form.disable());

	    return http.build();
	}
}
