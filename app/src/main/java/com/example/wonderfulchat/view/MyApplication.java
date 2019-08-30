package com.example.wonderfulchat.view;

import android.app.Application;
import android.content.Context;
import org.litepal.LitePal;

/**
 * @Author wonderful
 * @Description MyApplication
 * @Date 2019-8-30
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(this);
    }

    public static Context getContext(){
        return context;
    }
}
