package com.godmother.itemsplanner.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class ImageUpload {

    private Uri filePath;
    private Bitmap bitmap;

    public ImageUpload(Uri filePath, Bitmap bitmap) {
        this.filePath = filePath;
        this.bitmap = bitmap;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public void setFilePath(Uri filePath) {
        this.filePath = filePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
