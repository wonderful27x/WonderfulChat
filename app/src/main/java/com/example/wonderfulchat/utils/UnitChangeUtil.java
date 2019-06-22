package com.example.wonderfulchat.utils;

import com.example.wonderfulchat.view.MyApplication;

public class UnitChangeUtil {

    public static int dp2px(float dx){
        float density = MyApplication.getContext().getResources().getDisplayMetrics().density;
        int px = (int)(dx*density+0.5f);
        return px;
    }

    public static float px2dp(int px){
        float density = MyApplication.getContext().getResources().getDisplayMetrics().density;
        float dp = px/density;
        return dp;
    }

    public static int px2sp(float pxValue) {
        final float fontScale = MyApplication.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = MyApplication.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
