package com.godmother.itemsplanner.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class ImageUpload {

    private Uri filePath;
    private Bitmap bitmap;
    private byte[] byteArray;
    private boolean isNoImage;

    public ImageUpload(Uri filePath, byte[] byteArray, Bitmap bitmap) {
        this.filePath = filePath;
        this.byteArray = byteArray;
        this.bitmap = bitmap;
        this.isNoImage = false;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public void setFilePath(Uri filePath) {
        this.filePath = filePath;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isNoImage() {
        return isNoImage;
    }

    public void setNoImage(boolean noImage) {
        isNoImage = noImage;
    }
}
