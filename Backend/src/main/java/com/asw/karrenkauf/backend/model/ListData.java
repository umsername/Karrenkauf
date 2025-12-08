package com.asw.karrenkauf.backend.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "lists")
public class ListData {

    @Id
    private String id; // UUID as string

    private String name;
    private String owner;
    private long createdAt;
    private long updatedAt;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListItem> items = new ArrayList<>();

    public ListData() {}

    public ListData(String id, String name, String owner, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public List<ListItem> getItems() { return items; }
    public void setItems(List<ListItem> items) { this.items = items; }

    public void addItem(ListItem item) {
        item.setList(this);
        items.add(item);
        this.updatedAt = System.currentTimeMillis();
    }
}
