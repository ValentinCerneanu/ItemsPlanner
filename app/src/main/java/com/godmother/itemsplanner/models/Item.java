package com.godmother.itemsplanner.models;

import java.io.Serializable;

public class Item implements Serializable {
    private String id;
    private String name;
    private String descriere;
    private int cantitate;

    public Item(String id, String name, String descriere, int cantitate){
        this.id = id;
        this.name = name;
        this.descriere = descriere;
        this.cantitate = cantitate;
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

    public int getCantitate() {
        return cantitate;
    }

    public void setCantitate(int cantitate) {
        this.cantitate = cantitate;
    }

    @Override
    public String toString() {
        return name;
    }
}
