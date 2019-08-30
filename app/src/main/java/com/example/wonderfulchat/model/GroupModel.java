package com.example.wonderfulchat.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description 二级菜单父级
 * @Date 2019-8-30
 */
public class GroupModel {

    private String title;
    private int number;
    private String content;
    private List<UserModel> childModels = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<UserModel> getChildModels() {
        return childModels;
    }

    public void setChildModels(List<UserModel> childModels) {
        this.childModels = childModels;
    }
}
