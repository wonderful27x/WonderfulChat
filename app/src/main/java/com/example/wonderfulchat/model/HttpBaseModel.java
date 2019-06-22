package com.example.wonderfulchat.model;

public class HttpBaseModel<T>{

    private String result;
    private T content;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

}
