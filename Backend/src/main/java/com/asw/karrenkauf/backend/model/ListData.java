package com.asw.karrenkauf.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "list_data")
public class ListData {

    @Id
    private String listId;

    private String userId;
    private String listOwner; // CSV of UUIDs
    private String data;      // JSON blob
    private long createdAt;
    private long updatedAt;

    public ListData() {}

    public ListData(String listId, String userId, String listOwner, String data) {
        this.listId = listId;
        this.userId = userId;
        this.listOwner = listOwner;
        this.data = data;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

 // Getters
    public String getListId() { return listId; }
    public String getUserId() { return userId; }
    public String getListOwner() { return listOwner; }
    public String getData() { return data; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setListId(String listId) { this.listId = listId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setListOwner(String listOwner) { this.listOwner = listOwner; }
    public void setData(String data) { this.data = data; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
