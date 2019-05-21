package com.example.wonderfulchat.interfaces;

public interface HttpCallbackListener {

    void onFinish(String response);
    void onError(Exception e);

}
