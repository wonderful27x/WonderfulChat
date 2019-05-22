package com.example.wonderfulchat.model;

public class MessageModel {

    public static final int TYPE_RECEIVE = 0;
    public static final int TYPE_SEND = 1;
    private String message;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
