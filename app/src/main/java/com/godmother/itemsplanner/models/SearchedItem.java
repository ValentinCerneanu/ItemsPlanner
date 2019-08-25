package com.godmother.itemsplanner.models;

public class SearchedItem extends Item {

    private String categoryId;
    private String categoryName;

    public SearchedItem(String id, String name, String descriere, int cantitate, String categoryId, String categoryName){
        super(id, name, descriere, cantitate);
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }


    @Override
    public String toString() {

        return "Categorie: " + categoryName + "\n" + this.getName();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
