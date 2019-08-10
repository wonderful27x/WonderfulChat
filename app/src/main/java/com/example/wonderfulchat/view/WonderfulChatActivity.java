package com.example.wonderfulchat.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.customview.SimpleDialog;
import com.example.wonderfulchat.customview.DefuEditText;
import com.example.wonderfulchat.databinding.ActivityWonderfulChatBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.KeyboardUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.viewmodel.WonderfulChatViewModel;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WonderfulChatActivity extends BaseActivity <WonderfulChatViewModel> implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener{

    private static final String TAG = "WonderfulChatActivity";
    private ActivityWonderfulChatBinding chatBinding;
    private ImageView headImage;
    private TextView userName;
    private TextView lifeMotto;
    private DefuEditText userNameEdit;
    private DefuEditText lifeMottoEdit;
    private LinearLayout headLayout;
    private Uri imageUri;
    private String takePhotoPath;
    private UserModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        chatBinding = DataBindingUtil.setContentView(this,R.layout.activity_wonderful_chat);
        chatBinding.setWonderfulViewModel(getViewModel());
        getViewModel().setChatBinding(chatBinding);

//        EventBus.getDefault().register(this);
        initLeftDrawer(chatBinding);
        getViewModel().initView();

    }


    private void initLeftDrawer(ActivityWonderfulChatBinding chatBinding){

        getViewModel().firstStatement();

        View headView = chatBinding.wonderfulMenu.getHeaderView(0);
        headImage = headView.findViewById(R.id.head_image);
        userName = headView.findViewById(R.id.user_name);
        lifeMotto = headView.findViewById(R.id.life_motto);
        userNameEdit = headView.findViewById(R.id.user_name_edit);
        lifeMottoEdit = headView.findViewById(R.id.life_motto_edit);
        headLayout = headView.findViewById(R.id.headLayout);

        chatBinding.wonderfulMenu.setNavigationItemSelectedListener(this);
        headImage.setOnClickListener(this);
        userName.setOnClickListener(this);
        lifeMotto.setOnClickListener(this);

        userNameEdit.setIconClickListener(new DefuEditText.IconClickListener() {
            @Override
            public void IconLeftOnClick() {

            }

            @Override
            public void IconRightOnClick() {
                String oldUserName = userName.getText().toString().trim();
                String newUserName = userNameEdit.getText().toString().trim();
                userName.setText(newUserName);
                userName.setVisibility(View.VISIBLE);
                userNameEdit.setVisibility(View.GONE);
                KeyboardUtil.hideSoftKeyboard(WonderfulChatActivity.this,userNameEdit);
                getViewModel().changeField(model.getAccount(),"nickname",newUserName,oldUserName,userName);
            }
        });

        lifeMottoEdit.setIconClickListener(new DefuEditText.IconClickListener() {
            @Override
            public void IconLeftOnClick() {

            }

            @Override
            public void IconRightOnClick() {
                String oldMotto = lifeMotto.getText().toString().trim();
                String newMotto = lifeMottoEdit.getText().toString().trim();
                lifeMotto.setText(newMotto);
                lifeMotto.setVisibility(View.VISIBLE);
                lifeMottoEdit.setVisibility(View.GONE);
                KeyboardUtil.hideSoftKeyboard(WonderfulChatActivity.this,lifeMottoEdit);
                getViewModel().changeField(model.getAccount(),"lifemotto",newMotto,oldMotto,lifeMotto);
            }
        });

        headLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName.setVisibility(View.VISIBLE);
                userNameEdit.setVisibility(View.GONE);
                userNameEdit.setText(userName.getText().toString());

                lifeMotto.setVisibility(View.VISIBLE);
                lifeMottoEdit.setVisibility(View.GONE);
                lifeMottoEdit.setText(lifeMotto.getText().toString());
            }
        });

        initUserMessage();
    }

    private void initUserMessage(){
        String userModel = MemoryUtil.sharedPreferencesGetString("UserModel");
        Gson gson = new Gson();
        model = gson.fromJson(userModel, UserModel.class);
        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.default_head_image)
                .fallback(R.mipmap.default_head_image)
                .error(R.mipmap.default_head_image);
        Glide.with(this)
                .load(model.getImageUrl())
                .apply(options)
                .into(headImage);
        if (model.getNickname() != null && !model.getNickname().equals("")){
            userName.setText(model.getNickname());
            userNameEdit.setText(model.getNickname());
        }else{
            userName.setText("未设置昵称");
            userNameEdit.setText("未设置昵称");
        }
        if (model.getLifeMotto() != null && !model.getLifeMotto().equals("")){
            lifeMotto.setText(model.getLifeMotto());
            lifeMottoEdit.setText(model.getLifeMotto());
        }else{
            lifeMotto.setText("为国家繁荣富强而努力奋斗！");
            lifeMottoEdit.setText("为国家繁荣富强而努力奋斗！");
        }

        Menu menu = chatBinding.wonderfulMenu.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setTitle("切换账号  (" + model.getAccount() + ")");
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(MessageEvent event){
//        LogUtil.d(TAG,"onEvent");
//        Glide.with(this).load(event.getUserModel().getImageUrl()).into(headImage);
//        userName.setText(event.getUserModel().getNickname());
//        lifeMotto.setText(event.getUserModel().getLifeMotto());
//    }

    @Override
    public WonderfulChatViewModel bindViewModel() {
        return new WonderfulChatViewModel();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_switchAccount:
                getViewModel().logoutOrSwitch("switch",model.getAccount());
                return true;
//            case R.id.menu_register:
//                SimpleDialog dialog1 = new SimpleDialog(this);
//                dialog1.setConfirmClickListener(new SimpleDialog.ConfirmClickListener() {
//                    @Override
//                    public void parameterPass(String parameter1, String parameter2) {
//                        LogUtil.d(TAG, "parameterPass: " + parameter1 + "-" + parameter2);
//                    }
//                });
//                dialog1.show();
//                dialog1.setParameterNote("账号：","密码：");
//                return true;
            case R.id.menu_upload:
                ToastUtil.showToast("待开发！");
                return true;
            case R.id.menu_file_manager:
                ToastUtil.showToast("待开发！");
                return true;
            case R.id.menu_change_password:
                SimpleDialog dialog2 = new SimpleDialog(this);
                dialog2.setConfirmClickListener(new SimpleDialog.ConfirmClickListener() {
                    @Override
                    public void parameterPass(String parameter1, String parameter2) {
                        if (parameter2.length()<5){
                            ToastUtil.showToast("密码长度不能小于5！");
                            return;
                        }
                        getViewModel().changePassword(model.getAccount(),parameter1,parameter2);
                        LogUtil.d(TAG, "parameterPass: " + parameter1 + "-" + parameter2);
                    }
                });
                dialog2.show();
                dialog2.setParameterNote("原密码：","新密码：");
                return true;
            case R.id.menu_logout:
                getViewModel().logoutOrSwitch("logout",model.getAccount());
                return true;
            case R.id.menu_backup:
                chatBinding.drawerLayout.closeDrawers();
                return true;
            case R.id.menu_about_software:
                getViewModel().statement();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.head_image:
                setHeadImage();
                break;
            case R.id.user_name:
                userNameEdit.setText(userName.getText().toString().trim());
                userName.setVisibility(View.GONE);
                userNameEdit.setVisibility(View.VISIBLE);
                userNameEdit.setFocusable(true);
                userNameEdit.setFocusableInTouchMode(true);
                userNameEdit.requestFocus();
                KeyboardUtil.showSoftKeyboard(this,userNameEdit);
                break;
            case R.id.life_motto:
                lifeMottoEdit.setText(lifeMotto.getText().toString().trim());
                lifeMotto.setVisibility(View.GONE);
                lifeMottoEdit.setVisibility(View.VISIBLE);
                lifeMottoEdit.setFocusable(true);
                lifeMottoEdit.setFocusableInTouchMode(true);
                lifeMottoEdit.requestFocus();
                KeyboardUtil.showSoftKeyboard(this,lifeMottoEdit);
                break;
        }
    }

    private void setHeadImage(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("选择图片来源");
        dialog.setCancelable(true);
        dialog.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File outputImage = new File(getExternalCacheDir(),"outputImage.jpg");
                takePhotoPath = outputImage.getPath();
                try{
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(WonderfulChatActivity.this,
                            "com.example.wonderfulchat.fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,1);
            }
        });
        dialog.setNegativeButton("相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(ContextCompat.checkSelfPermission(WonderfulChatActivity.this, Manifest.permission.
                        WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(WonderfulChatActivity.this,new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });
        dialog.show();
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    ToastUtil.showToast("拒绝权限将无法使用此功能");
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    getViewModel().uploadHeadImage(takePhotoPath,headImage);
                }
                break;
            case 2:
                if(resultCode==RESULT_OK){
                    if(Build.VERSION.SDK_INT>=19){
                        takePhotoPath = getViewModel().handleImageOnkitKat(data);
                    }else{
                        takePhotoPath = getViewModel().handleImageBeforeKitKat(data);
                    }
                    getViewModel().uploadHeadImage(takePhotoPath,headImage);
                }
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
//        EventBus.getDefault().unregister(this);
        logoutBeforeDestroy();
        LogUtil.d(TAG,"destroy");
        super.onDestroy();
    }

    public void logoutBeforeDestroy(){
        String url = InternetAddress.LOGOUT_URL + "?account=" + model.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.equals("fail")){
                            ToastUtil.showToast("退出异常！");
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("退出异常！");
                        LogUtil.e(TAG,"退出异常：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void gsonToJson(){
        Gson gson = new Gson();

        HttpUserModel loginModel = new HttpUserModel();
        UserModel userModel = new UserModel();
        List<UserModel> modelList = new ArrayList<>();

        userModel.setAccount("wonderful");
        userModel.setPassword("123456");
        userModel.setNickname("德芙");
        userModel.setRemark("巧克力");
        userModel.setLifeMotto("为国家繁荣富强而努力奋斗");
        userModel.setImageUrl("http://192.168.191.5:8080/file/girl.jpg");

        for (int i=0; i<10; i++){
            modelList.add(userModel);
        }

        loginModel.setResult("success");
        loginModel.setContent(modelList);

        String jsonString = gson.toJson(loginModel);
        Log.d(TAG, "gsonToJson: " + jsonString);

    }

}
