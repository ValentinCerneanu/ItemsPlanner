package com.example.itemsplanner.models;

public class Booking {
    private String descriere;
    private String interval = "dummy";
    private String user;
    private String itemName;
    private String itemId;

    public Booking(String descriere, String user, String itemName, String itemId) {
        this.descriere = descriere;
        this.user = user;
        this.itemName = itemName;
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    @Override
    public String toString() {
        return itemName + "\n" + descriere;
    }
}