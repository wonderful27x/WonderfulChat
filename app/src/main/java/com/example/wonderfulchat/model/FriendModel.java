package com.example.wonderfulchat.model;

/**
 * @Author wonderful
 * @Description 好友信息模型，与USER区别开来为了使用LitePal
 * @Date 2019-8-30
 */
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
