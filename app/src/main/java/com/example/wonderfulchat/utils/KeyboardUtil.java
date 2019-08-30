package com.example.wonderfulchat.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * @Author wonderful
 * @Description 软键盘工具
 * @Date 2019-8-30
 */
public class KeyboardUtil {

    //隐藏软键盘
    public static void hideSoftKeyboard(Context context, TextView editText){
        if (editText == null || context == null)return;
        InputMethodManager imm = (InputMethodManager) context. getSystemService(context.INPUT_METHOD_SERVICE);
        if (imm == null)return;
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘
    }
    //显示软键盘
    public static void showSoftKeyboard(Context context,TextView editText){
        if (editText == null || context == null)return;
        InputMethodManager imm = (InputMethodManager)context. getSystemService(context.INPUT_METHOD_SERVICE);
        if (imm == null)return;
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

}
