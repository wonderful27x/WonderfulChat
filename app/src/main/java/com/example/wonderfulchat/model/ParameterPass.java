package com.example.wonderfulchat.model;

import java.util.HashMap;

/**
 * @Author wonderful
 * @Description 参数传递类，用于Http的参数传递
 * @Date 2019-8-30
 */
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
