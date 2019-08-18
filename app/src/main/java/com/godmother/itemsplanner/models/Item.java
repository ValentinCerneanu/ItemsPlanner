package com.godmother.itemsplanner.models;

public class Item {
    private String id;
    private String name;
    private String descriere;

    public Item(String id, String name, String descriere){
        this.id = id;
        this.name = name;
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
