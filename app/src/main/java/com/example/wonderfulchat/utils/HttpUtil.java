package com.example.wonderfulchat.utils;

import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

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

    private static void sendOkHttpRequest(String address,String postParameters, String type, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        if(type.equals("GET")){
            request = new Request.Builder()
                    .url(address)
                    .get()
                    .build();
        }else if(type.equals("POST_FORM")){
            RequestBody requestBody = stringToBody(postParameters);

            request = new Request.Builder()
                    .url(address)
                    .post(requestBody)
                    .build();
        }else if(type.equals("POST_JSON")){
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , postParameters);
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
}
