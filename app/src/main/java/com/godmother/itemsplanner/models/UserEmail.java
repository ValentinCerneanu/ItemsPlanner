package com.godmother.itemsplanner.models;

public class UserEmail {
    private String email;
    private String key;

    public UserEmail(String email, String key) {
        this.email = email;
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
