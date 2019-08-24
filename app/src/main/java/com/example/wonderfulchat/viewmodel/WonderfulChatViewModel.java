package com.example.wonderfulchat.viewmodel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ViewPagerAdapter;
import com.example.wonderfulchat.customview.LoadingDialog;
import com.example.wonderfulchat.customview.TabGroupView;
import com.example.wonderfulchat.databinding.ActivityWonderfulChatBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.FriendModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.ParameterPass;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.ImageCompressUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.FriendListFragment;
import com.example.wonderfulchat.view.LoginActivity;
import com.example.wonderfulchat.view.LuckyTurntableFragment;
import com.example.wonderfulchat.view.MessageFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WonderfulChatViewModel extends BaseViewModel <AppCompatActivity> {

    private static final String TAG = "WonderfulChatViewModel";
    private ActivityWonderfulChatBinding chatBinding;
    private UserModel userModel;
    private LoadingDialog loadingDialog;

    public void initView(){
        ToastUtil.showToast("Welcome");

        loadingDialog = new LoadingDialog(getView());
        String model;
        if (getHostState()){
            model = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_USER_MODEL);
        }else {
            model = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_USER_MODEL);
        }
        Gson gson = new Gson();
        userModel = gson.fromJson(model, UserModel.class);

        final ViewPager viewPager = chatBinding.wonderfulChat.viewPager;
        final TabGroupView tabGroupView = chatBinding.wonderfulChat.tabGroupView;

        List<Fragment> fragments = new ArrayList<>();
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setLeftImageClickListener(new MessageFragment.LeftImageClickListener() {
            @Override
            public void leftImageClick() {
                chatBinding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        fragments.add(messageFragment);
        fragments.add(new LuckyTurntableFragment());
        fragments.add(new FriendListFragment());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getView().getSupportFragmentManager(),fragments);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
        tabGroupView.initChildren();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                tabGroupView.alphaChange(i,v);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        tabGroupView.setTabSelectedListener(new TabGroupView.TabSelectedListener() {
            @Override
            public void onSelect(int position) {
                viewPager.setCurrentItem(position,false);
            }
        });

        //getUserMessage();
    }

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String handleImageOnkitKat(Intent intent){
        String imagePath = null;
        Uri uri = intent.getData();
        if(DocumentsContract.isDocumentUri(getView(),uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    public String handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        return imagePath;
    }

    private String getImagePath(Uri uri,String selection){
        String path = null;
        Cursor cursor = getView().getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }
        return path;
    }

    public void statement(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getView());
        dialog.setTitle("声明：");
        dialog.setMessage("为了获得此软件的使用权您必须为国家繁荣富强而努力奋斗");
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    public void shoAuthor(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getView());
        dialog.setTitle("Author：德芙");
        dialog.setMessage("对于此软件有任何疑问请发送邮件至wonderful27x@126.com");
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    public void messageMoveToHost(){
        //清空HOST
        clearHost();
        //将sharedPreferences移动到HOST
        moveShare();
        //将文件移动到HOST
        moveFile();
        //将数据库信息移动到HOST
        moveDatabase();
        //清空OTHER
        clearOther();
        //HOST状态设为true
        MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,true);
    }

    private void clearHost(){
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,"");
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_MESSAGE_ACCOUNT,"");

        String path = FileUtil.getDiskPath(getView(),CommonConstant.HOST_READ_MESSAGE);
        File file = new File(path);
        FileUtil.dirDelete(file);

        path = FileUtil.getDiskPath(getView(),CommonConstant.HOST_UNREAD_MESSAGE);
        file = new File(path);
        FileUtil.dirDelete(file);

        LitePal.deleteAll(UserModel.class);
    }

    private void clearOther(){
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,"");
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_MESSAGE_ACCOUNT,"");

        String path = FileUtil.getDiskPath(getView(),CommonConstant.OTHER_READ_MESSAGE);
        File file = new File(path);
        FileUtil.dirDelete(file);

        path = FileUtil.getDiskPath(getView(),CommonConstant.OTHER_UNREAD_MESSAGE);
        file = new File(path);
        FileUtil.dirDelete(file);

        LitePal.deleteAll(FriendModel.class);

    }

    private void moveShare(){
        String userModelJson = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_USER_MODEL);
        String messageAccount = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_MESSAGE_ACCOUNT);
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,userModelJson);
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_MESSAGE_ACCOUNT,messageAccount);
        Gson gson = new Gson();
        UserModel userModel = gson.fromJson(userModelJson,UserModel.class);
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_ACCOUNT,userModel.getAccount());
    }

    private void moveFile(){
        FileUtil.moveFile(getView(),CommonConstant.OTHER_UNREAD_MESSAGE,CommonConstant.HOST_UNREAD_MESSAGE);
        FileUtil.moveFile(getView(),CommonConstant.OTHER_READ_MESSAGE,CommonConstant.HOST_READ_MESSAGE);
    }

    private void moveDatabase(){
        List<FriendModel> modelList = LitePal.findAll(FriendModel.class);
        for (FriendModel model:modelList){
            UserModel userModel = new UserModel(model);
            userModel.save();
        }
    }

    public void logoutOrSwitch(final String type, String account){
        loadingDialog.dialogShow();
        String url = InternetAddress.LOGOUT_URL + "?account=" + account;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if (response.equals("success")){
                            if (type.equals("switch")){
                                Intent intent = new Intent(getView(),LoginActivity.class);
                                getView().startActivity(intent);
                            }
                            getView().finish();
                        }else {
                            if (type.equals("switch")){
                                ToastUtil.showToast("切换异常！");
                            }else {
                                ToastUtil.showToast("退出异常！");
                            }
                            LogUtil.d(TAG,response);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if (type.equals("logout")){
                            ToastUtil.showToast("退出异常！");
                        }else {
                            ToastUtil.showToast("切换异常！");
                        }
                        LogUtil.e(TAG,"退出异常：" + e.getMessage());
                    }
                });
            }
        });
    }

    public void changePassword(String account,String oldPass,String newPass){
        loadingDialog.dialogShow();
        String postParameters = null;
        try {
            postParameters = "account=" + URLEncoder.encode(account, "UTF-8");
            postParameters += "&oldPass=" + URLEncoder.encode(oldPass, "UTF-8");
            postParameters += "&newPass=" + URLEncoder.encode(newPass, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtil.httpRequestForPost(InternetAddress.CHANGE_PASS_URL, postParameters, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if (response.equals("修改密码成功！")){
                            clearPass();
                            Intent intent = new Intent(getView(),LoginActivity.class);
                            getView().startActivity(intent);
                            getView().finish();
                        }
                        ToastUtil.showToast(response);
                        LogUtil.d(TAG,response);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("修改密码失败！");
                        LogUtil.e(TAG,"修改密码失败: "+e.getMessage());
                    }
                });
            }
        });
    }

    public void changeField(String account, final String field, final String content, final String oldContent, final TextView textView){
        String url = InternetAddress.CHANGE_FIELD_URL + "?account=" + account + "&field=" + field + "&content=" + content;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.equals("success")){
                            ToastUtil.showToast("修改成功！");
                            switch (field){
                                case "nickname":
                                    userModel.setNickname(content);
                                    break;
                                case "lifemotto":
                                    userModel.setLifeMotto(content);
                                    break;
                                default:
                                    break;
                            }
                            Gson gson = new Gson();
                            String jsonData = gson.toJson(userModel);
                            if (getHostState()){
                                MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,jsonData);
                            }else {
                                MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,jsonData);
                            }
                        }else {
                            textView.setText(oldContent);
                            ToastUtil.showToast("修改失败！");
                            LogUtil.d(TAG,response);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(oldContent);
                        ToastUtil.showToast("修改失败！");
                        LogUtil.e(TAG,"修改失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void clearPass(){
        MemoryUtil.sharedPreferencesSaveString("password","");
    }

    public void setNameOrLifeMotto(String type, String content, TextView textView){
        textView.setText(content);
    }

    public void firstStatement(){

        boolean statement = MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.STATEMENT);
        if (statement)return;

        AlertDialog.Builder dialog = new AlertDialog.Builder(getView());
        dialog.setTitle("声明：");
        dialog.setMessage("为了获得此软件的使用权您必须为国家繁荣富强而努力奋斗");
        dialog.setCancelable(true);
        dialog.setPositiveButton("我同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.STATEMENT,true);
            }
        });
        dialog.setNegativeButton("我不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ToastUtil.showToast("对不起，你没有资格使用此软件！");
                getView().finish();
            }
        });
        dialog.show();
    }

//    private void getUserMessage(){
//        String userModel = FileUtil.getJson(getView(), "HttpUserModel");
//        Gson gson = new Gson();
//        HttpUserModel httpUserModel = gson.fromJson(userModel, HttpUserModel.class);
//        MessageEvent event = new MessageEvent();
//        event.setUserModel(httpUserModel.getContent().get(0));
//
//        EventBus.getDefault().post(event);
//    }

    @SuppressLint("StaticFieldLeak")
    public void uploadHeadImage(final String url, final ImageView imageView){
        loadingDialog.dialogShow();

        Glide.with(getView()).load(url).into(imageView);

        final ParameterPass parameterPass = new ParameterPass();
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                Bitmap bitmap = null;
                bitmap = ImageCompressUtil.decodeBitmapFromFile(url,imageView.getWidth(),imageView.getHeight());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (bitmap != null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                }
                byte[] byteArr = stream.toByteArray();
                String bitmapString = Base64.encodeToString(byteArr, 0);

                String imageName = userModel.getImageUrl();
                if (imageName != null && imageName.length()>0){
                    imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
                }

                HashMap<String,String> map = new HashMap<>();
                map.put("account",userModel.getAccount());
                map.put("imageName",imageName);
                map.put("imageString",bitmapString);
                parameterPass.setType(HttpUtil.POST_FORM);
                parameterPass.setMap(map);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                HttpUtil.sendOkHttpRequest(InternetAddress.UPLOAD_IMAGE_URL, parameterPass, new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        getView().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dialogDismiss();
                                Glide.with(getView()).load(R.drawable.default_head_image).into(imageView);
                                ToastUtil.showToast("上传失败！");
//                                ToastUtil.showToast(e.getMessage());
                                LogUtil.e(TAG,e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        getView().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dialogDismiss();
                                try {
                                    String responseData = response.body().string();
                                    if(responseData.contains("$") && responseData.substring(0,responseData.indexOf("$")).equals("success")){
                                        ToastUtil.showToast("上传成功！");
                                        String imageUrl = responseData.substring(8);
                                        userModel.setImageUrl(imageUrl);
                                        Gson gson = new Gson();
                                        String jsonData = gson.toJson(userModel);
                                        if (getHostState()){
                                            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,jsonData);
                                        }else {
                                            MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,jsonData);
                                        }
                                    }else{
                                        Glide.with(getView()).load(R.drawable.default_head_image).into(imageView);
                                        ToastUtil.showToast("上传失败！");
//                                        ToastUtil.showToast(responseData);
                                        LogUtil.e(TAG,responseData);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            }
        }.execute();
    }

//    @SuppressLint("StaticFieldLeak")
//    public void uploadHeadImage(final String url, final ImageView imageView){
//        Glide.with(getView()).load(url).into(imageView);
//
//        final ParameterPass parameterPass = new ParameterPass();
//        new AsyncTask<Void,Void,Void>(){
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                Bitmap bitmap = null;
//                bitmap = ImageCompressUtil.decodeBitmapFromFile(url,imageView.getWidth(),imageView.getHeight());
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
//                byte[] byteArr = stream.toByteArray();
//                String bitmapString = Base64.encodeToString(byteArr, 0);
//
//                String imageUrlLastState;
//                if (userModel.getImageUrl() != null && userModel.getImageUrl().length()>=6){
//                    imageUrlLastState = userModel.getImageUrl().substring(userModel.getImageUrl().length()-6,userModel.getImageUrl().length()-4);
//                }else {
//                    imageUrlLastState = "$0";
//                }
//
//                HashMap<String,String> map = new HashMap<>();
//                map.put("account",userModel.getAccount());
//                map.put("imageString",bitmapString);
//                map.put("imageUrlLastState",imageUrlLastState);
//                parameterPass.setType(HttpUtil.POST_FORM);
//                parameterPass.setMap(map);
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                HttpUtil.sendOkHttpRequest(InternetAddress.UPLOAD_IMAGE_URL, parameterPass, new Callback() {
//                    @Override
//                    public void onFailure(Call call, final IOException e) {
//                        getView().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Glide.with(getView()).load(R.drawable.default_head_image).into(imageView);
//                                ToastUtil.showToast("上传失败！");
//                                LogUtil.e(TAG,e.getMessage());
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(Call call, final Response response) throws IOException {
//                        getView().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    String responseData = response.body().string();
//                                    if(responseData.substring(0,7).equals("success")){
//                                        ToastUtil.showToast("上传成功！");
//                                        String imageUrl = responseData.substring(7);
//                                        userModel.setImageUrl(imageUrl);
//                                        Gson gson = new Gson();
//                                        String jsonData = gson.toJson(userModel);
//                                        if (getHostState()){
//                                            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,jsonData);
//                                        }else {
//                                            MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,jsonData);
//                                        }
//                                    }else{
//                                        ToastUtil.showToast("上传失败！");
//                                        LogUtil.e(TAG,responseData);
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//                        });
//                    }
//                });
//            }
//        }.execute();
//    }

    public ActivityWonderfulChatBinding getChatBinding() {
        return chatBinding;
    }

    public void setChatBinding(ActivityWonderfulChatBinding chatBinding) {
        this.chatBinding = chatBinding;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (chatBinding != null){
            chatBinding = null;
        }
    }
}
