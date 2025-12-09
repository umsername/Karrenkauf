package com.asw.karrenkauf.backend.dto;

import java.util.List;

public class SharedUsersResponse {
    private String listId;
    private String owner;
    private List<String> sharedWith;

    public SharedUsersResponse() {}

    public SharedUsersResponse(String listId, String owner, List<String> sharedWith) {
        this.listId = listId;
        this.owner = owner;
        this.sharedWith = sharedWith;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }
}
