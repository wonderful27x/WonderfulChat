package com.example.wonderfulchat.model;

import java.util.HashMap;

public class ParameterPass {

    private String type;
    private String string;
    private HashMap<String ,String> map;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }
}
