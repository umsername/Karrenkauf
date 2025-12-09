package com.asw.karrenkauf.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "list_shares")
public class ListShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String listId; // ID of the shared list

    @Column(nullable = false)
    private String sharedWithUsername; // Username of the user the list is shared with

    @Column(nullable = false)
    private long sharedAt; // Timestamp when the list was shared

    public ListShare() {}

    public ListShare(String listId, String sharedWithUsername, long sharedAt) {
        this.listId = listId;
        this.sharedWithUsername = sharedWithUsername;
        this.sharedAt = sharedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getSharedWithUsername() { return sharedWithUsername; }
    public void setSharedWithUsername(String sharedWithUsername) { this.sharedWithUsername = sharedWithUsername; }

    public long getSharedAt() { return sharedAt; }
    public void setSharedAt(long sharedAt) { this.sharedAt = sharedAt; }
}
