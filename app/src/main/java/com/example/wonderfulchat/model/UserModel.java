package com.example.wonderfulchat.model;

import org.litepal.crud.LitePalSupport;
import java.io.Serializable;

/**
 * @Author wonderful
 * @Description USER信息Model
 * @Date 2019-8-30
 */
public class UserModel extends LitePalSupport implements Serializable {

    private String account;
    private String password;
    private String nickname;
    private String lifeMotto;
    private String imageUrl;
    private String remark;

    public UserModel(){}

    public UserModel(UserModel userModel){
        this.account = userModel.getAccount();
        this.password = userModel.getPassword();
        this.nickname = userModel.getNickname();
        this.lifeMotto = userModel.getLifeMotto();
        this.imageUrl = userModel.getImageUrl();
        this.remark = userModel.getRemark();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLifeMotto() {
        return lifeMotto;
    }

    public void setLifeMotto(String lifeMotto) {
        this.lifeMotto = lifeMotto;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
