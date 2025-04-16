package com.srsp.mathify.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public String type;
    public String message;
    public transient Bitmap image;

    public ChatMessage(String type, String message) {
        this.type = type;
        this.message = message;
        this.image = null;
    }

    public ChatMessage(String type, String message, Bitmap image) {
        this.type = type;
        this.message = message;
        this.image = image;
    }
}
