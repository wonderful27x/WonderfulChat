package com.example.wonderfulchat.utils;

import android.text.TextUtils;
import android.widget.Toast;
import com.example.wonderfulchat.view.MyApplication;

public class ToastUtil {

    public static void showToast(String msg){
        if(!TextUtils.isEmpty(msg)) {
            Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showLongToast(String msg){
        if(!TextUtils.isEmpty(msg)) {
            Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
