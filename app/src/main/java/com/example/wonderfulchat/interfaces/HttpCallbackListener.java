package com.example.wonderfulchat.interfaces;

/**
 * @Author wonderful
 * @Description Http应答接口
 * @Date 2019-8-29
 */
public interface HttpCallbackListener {

    void onFinish(String response);
    void onError(Exception e);

}
