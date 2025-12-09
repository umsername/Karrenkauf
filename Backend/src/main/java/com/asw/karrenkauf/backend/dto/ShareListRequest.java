package com.asw.karrenkauf.backend.dto;

public class ShareListRequest {
    private String username;

    public ShareListRequest() {}

    public ShareListRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
