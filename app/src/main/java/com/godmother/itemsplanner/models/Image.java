package com.godmother.itemsplanner.models;

public class Image {
    private String uid;
    private String url;

    public Image(String uid, String url){
        this.uid = uid;
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
