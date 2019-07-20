package com.example.itemsplanner.models;

public class Booking {
    private String descriere;
    private String interval = "dummy";
    private String user;

    public Booking(String descriere, String user) {
        this.descriere = descriere;
        this.user = user;
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
        return "{" +
                "\"descriere\":\"" + descriere + "\"" +
                ",\"user\":\"" + user + "\"" +
                "}";
    }
}