package com.example.wonderfulchat.utils;

import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.ParameterPass;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    public static final String GET = "get";
    public static final String POST_FORM = "postForm";
    public static final String POST_JSON = "postJson";

    public static void httpRequestForPost(final String address,final String postParameters,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    connection.connect();

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(postParameters);
                    out.flush();
                    out.close();

                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void httpRequestForGet(final String address, final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoInput(true);
                    connection.setDoOutput(false);

                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void sendOkHttpRequest(String address, ParameterPass pass,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        if(pass.getType().equals(GET)){
            request = new Request.Builder()
                    .url(address)
                    .get()
                    .build();
        }else if(pass.getType().equals(POST_FORM)){
            RequestBody requestBody = stringToBody(pass.getMap());

            request = new Request.Builder()
                    .url(address)
                    .post(requestBody)
                    .build();
        }else if(pass.getType().equals(POST_JSON)){
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , pass.getString());
            request = new Request.Builder()
                    .url(address)
                    .post(requestBody)
                    .build();
        }
        client.newCall(request).enqueue(callback);
    }

    private static RequestBody stringToBody(String data){
        FormBody.Builder builder = new FormBody.Builder();
        String [] dataArray = data.split(",");
        for (int i=0; i<dataArray.length; i+=2){
            builder.add(dataArray[i],dataArray[i+1]);
        }
        return builder.build();
    }

    private static RequestBody stringToBody(HashMap<String ,String> map){
        FormBody.Builder builder = new FormBody.Builder();
        for (String key:map.keySet()){
            builder.add(key,map.get(key));
        }
        return builder.build();
    }
}
