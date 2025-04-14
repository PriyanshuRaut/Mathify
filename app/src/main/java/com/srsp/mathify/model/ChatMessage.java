package com.srsp.mathify.model;

import android.graphics.Bitmap;

public class ChatMessage {
    public String type;
    public String message;
    public Bitmap image;

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
