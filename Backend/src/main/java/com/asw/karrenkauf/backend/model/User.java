package com.asw.karrenkauf.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;

@Entity
@Table(
    name = "user",
    uniqueConstraints = @UniqueConstraint(columnNames = "userName") // THIS MUST BE jakarta.persistence.UniqueConstraint
)
public class User {

    @Id
    private String userId;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false, unique = true)
    private String userName;

    private long createdAt;
    private long updatedAt;

    public User() {}

    public User(String userId, String userPassword, String userName) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUserPassword() {
    	return this.userPassword;
    }
    
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
