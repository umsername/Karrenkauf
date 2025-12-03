package com.asw.karrenkauf.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private final Key key;
    public static Date expDate;

    public JWTUtil() {
        // generiert automatisch einen sicheren HS256 Key
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String username) {
    	this.expDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
    	return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(this.expDate) // 1 Stunde
                .signWith(key)
                .compact();
    }
    
    public static long getExpirationDate() {
        return expDate.getTime(); // milliseconds since epoch
    }
}