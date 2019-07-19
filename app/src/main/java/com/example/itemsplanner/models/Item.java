package com.example.itemsplanner.models;

public class Item {
    private String id;
    private String name;
    private String imageUrl;
    private String descriere;

    public Item(String id, String name, String imageUrl, String descriere){
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.descriere = descriere;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    @Override
    public String toString() {
        return name;
    }
}
