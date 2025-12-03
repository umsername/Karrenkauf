package com.asw.karrenkauf.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class TokenEntry {

    @Id
    private String token;

    private String username;
    private long expiration; // epoch millis

    public TokenEntry() {} // JPA needs a default constructor

    public TokenEntry(String token, String username, long expiration) {
        this.token = token;
        this.username = username;
        this.expiration = expiration;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }

    public boolean isExpired() {
        return expiration < System.currentTimeMillis();
    }
}
