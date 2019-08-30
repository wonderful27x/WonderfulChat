package com.example.wonderfulchat.model;

/**
 * @Author wonderful
 * @Description 好友请求Model
 * @Date 2019-8-30
 */
public class FriendRequestModel extends UserModel{

    private String requestTime;

    public void changeToFriendRequestModel(UserModel userModel){
        this.setAccount(userModel.getAccount());
        this.setNickname(userModel.getNickname());
        this.setLifeMotto(userModel.getLifeMotto());
        this.setImageUrl(userModel.getImageUrl());
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
