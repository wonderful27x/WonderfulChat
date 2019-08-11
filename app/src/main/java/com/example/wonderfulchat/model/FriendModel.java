package com.example.wonderfulchat.model;

public class FriendModel extends UserModel {

    public void changeToFriendModel(UserModel userModel){
        this.setAccount(userModel.getAccount());
        this.setPassword(userModel.getPassword());
        this.setNickname(userModel.getNickname());
        this.setLifeMotto(userModel.getLifeMotto());
        this.setImageUrl(userModel.getImageUrl());
        this.setRemark(userModel.getRemark());
    }
}
