package com.example.wonderfulchat.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ChildModel {

    private Bitmap bitmap;
    private String title;
    private String content;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
