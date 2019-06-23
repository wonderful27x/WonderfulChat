package com.example.wonderfulchat.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {

    public static String getJson(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String getDiskPath(Context context, String name){
        String path;
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(externalStorageAvailable){
            path = context.getExternalFilesDir(name).getAbsolutePath();
        }else {
            path = context.getFilesDir() + File.separator + name;
        }
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        return path;
    }

    public static String fileRead(File file){
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        StringBuilder builder = new StringBuilder();
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String s = null;
            while ((s = bufferedReader.readLine()) != null){
                builder.append(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null){
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    public static void fileSave(File file,String text,boolean append){
        FileWriter writer = null;
        try {
            writer = new FileWriter(file,append);
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fileClear(File file){
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fileDelete(File file){
        if (file.exists() && file.isFile()){
            file.delete();
        }
    }

}
