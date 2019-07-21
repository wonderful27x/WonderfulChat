package com.example.wonderfulchat.viewmodel;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.wonderfulchat.adapter.ViewPagerAdapter;
import com.example.wonderfulchat.customview.TabGroupView;
import com.example.wonderfulchat.databinding.ActivityWonderfulChatBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.FriendListFragment;
import com.example.wonderfulchat.view.LoginActivity;
import com.example.wonderfulchat.view.LuckyTurntableFragment;
import com.example.wonderfulchat.view.MessageFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WonderfulChatViewModel extends BaseViewModel <AppCompatActivity> {

    private static final String TAG = "WonderfulChatViewModel";
    private ActivityWonderfulChatBinding chatBinding;

    public void initView(){
        ToastUtil.showToast("Welcome");

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
                viewPager.setCurrentItem(position);
            }
        });

        //getUserMessage();
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

    public void logoutOrSwitch(final String type, String account){
        String url = InternetAddress.LOGOUT_URL + "?account=" + account;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        ToastUtil.showToast("修改密码失败！");
                        LogUtil.e(TAG,"修改密码失败: "+e.getMessage());
                    }
                });
            }
        });
    }

    public void changeField(String account, String field, final String content, final String oldContent, final TextView textView){
        String url = InternetAddress.CHANGE_FIELD_URL + "?account=" + account + "&field=" + field + "&content=" + content;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.equals("success")){
                            ToastUtil.showToast("修改成功！");
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

        boolean statement = MemoryUtil.sharedPreferencesGetBoolean("Statement");
        if (statement)return;

        AlertDialog.Builder dialog = new AlertDialog.Builder(getView());
        dialog.setTitle("声明：");
        dialog.setMessage("为了获得此软件的使用权您必须为国家繁荣富强而努力奋斗");
        dialog.setCancelable(true);
        dialog.setPositiveButton("我同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MemoryUtil.sharedPreferencesSaveBoolean("Statement",true);
            }
        });
        dialog.setNegativeButton("我不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ToastUtil.showToast("对不起，非爱国人士不能使用此软件！");
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

    public void uploadHeadImage(String url, ImageView imageView){
        Glide.with(getView()).load(url).into(imageView);
    }

    public ActivityWonderfulChatBinding getChatBinding() {
        return chatBinding;
    }

    public void setChatBinding(ActivityWonderfulChatBinding chatBinding) {
        this.chatBinding = chatBinding;
    }

    @Override
    public void deTachView() {
        super.deTachView();
        if (chatBinding != null){
            chatBinding = null;
        }
    }
}
