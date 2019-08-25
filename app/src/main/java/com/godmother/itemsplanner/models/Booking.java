package com.godmother.itemsplanner.models;

public class Booking {
    private String descriere;
    private String interval = "dummy";
    private String user;
    private String itemName;
    private String itemId;
    private String bookingId;
    private String categoryId;
    private String categoryName;
    private String userName;
    private int cantitate;

    public Booking(String descriere, String user, String itemName, String itemId, String categoryId, String categoryName, int cantitate) {
        this.descriere = descriere;
        this.user = user;
        this.itemName = itemName;
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.cantitate = cantitate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public int getCantitate() {
        return cantitate;
    }

    public void setCantitate(int cantitate) {
        this.cantitate = cantitate;
    }

    @Override
    public String toString() {
        if(userName == null){
            return "Item: " + itemName + "\n" + "Scop rezervare: " + descriere + "\n" + "Cantitate: " + cantitate;
        }
        return "Item: " + itemName + "\n" + "Scop rezervare: " + descriere + "\n" + "Cantitate: " + cantitate + "\n" + "User: " + userName;
    }

    public String toEmail() {
        if(userName == null){
            return "Item: " + itemName + "<br />" + "Scop rezervare: " + descriere + "<br />" + "Cantitate: " + cantitate;
        }
        return "Item: " + itemName + "<br />" + "Scop rezervare: " + descriere + "<br />" + "Cantitate: " + cantitate + "<br />" + "User: " + userName;
    }
}