package com.asw.karrenkauf.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "list_items")
public class ListItem {

    @Id
    private String id; // UUID as string

    private String name;
    private String beschreibung;
    private int menge;
    private String unit;
    private long preis;
    private boolean done;
    private boolean checked;
    private String category;
    private long createdAt;
    private long updatedAt;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    private ListData list;

    public ListItem() {}

    // full constructor
    public ListItem(String id, String name, String beschreibung, int menge, String unit,
                    long preis, boolean done, boolean checked, String category,
                    long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
        this.menge = menge;
        this.unit = unit;
        this.preis = preis;
        this.done = done;
        this.checked = checked;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBeschreibung() { return beschreibung; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }

    public int getMenge() { return menge; }
    public void setMenge(int menge) { this.menge = menge; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getPreis() { return preis; }
    public void setPreis(long preis) { this.preis = preis; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public ListData getList() { return list; }
    public void setList(ListData list) { this.list = list; }
}
