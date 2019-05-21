package com.example.wonderfulchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.wonderfulchat.view.MyApplication;

public class MemoryUtil {

    public static final String FILE_NAME = "comExampleWonderfulChat";

    public static void sharedPreferencesSaveString(String key, String msg){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,msg);
        editor.apply();
    }

    public static String sharedPreferencesGetString(String key){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key,"");
    }

    public static void sharedPreferencesSaveBoolean(String key, boolean msg){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key,msg);
        editor.apply();
    }

    public static boolean sharedPreferencesGetBoolean(String key){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key,false);
    }
}
