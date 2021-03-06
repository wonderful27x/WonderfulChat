package com.example.wonderfulchat.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.content.LocalBroadcastManager;
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
import com.example.wonderfulchat.model.CommonConstant;
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

/**
 * @Author wonderful
 * @Description 主ACTIVITY
 * @Date 2019-8-30
 */
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
    private long lastBackPressedTime = 0;
    private static final int BACK_PRESSED_INTERVAL = 2000;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
     /** 登录锁，避免此Activity被多次启动创建多个实例**/
    public static boolean loginLock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        chatBinding = DataBindingUtil.setContentView(this,R.layout.activity_wonderful_chat);
        chatBinding.setWonderfulViewModel(getViewModel());
        getViewModel().setChatBinding(chatBinding);

        /**
         * 注册本地广播，用于接收消息
         */
        localReceiver = new LocalReceiver();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(CommonConstant.LOCAL_BROADCAST_NOTE);
        broadcastManager.registerReceiver(localReceiver,intentFilter);

        initLeftDrawer(chatBinding);
        getViewModel().initView(broadcastManager);

        LogUtil.d(TAG,"onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d(TAG,"onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getViewModel().getNotice();
        LogUtil.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        loginLock = false;
        broadcastManager.unregisterReceiver(localReceiver);
        logoutBeforeDestroy();
        super.onDestroy();
        LogUtil.d(TAG,"destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.d(TAG,"onRestart");
    }

    /**
     * @description 初始化抽屉
     * @param chatBinding
     */
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
        String userModel;
        boolean host;
        if (getHostState()){
            host = true;
            userModel = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_USER_MODEL);
        }else {
            host = false;
            userModel = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_USER_MODEL);
        }

        Gson gson = new Gson();
        model = gson.fromJson(userModel, UserModel.class);
        /**
         * 图片加载策略
         */
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);

        Glide.with(this)
                .load(model.getImageUrl())
                .apply(options)
                .into(headImage);

        if (model.getNickname() != null && !model.getNickname().isEmpty()){
            userName.setText(model.getNickname());
            userNameEdit.setText(model.getNickname());
        }else{
            userName.setText("点击设置昵称");
            userNameEdit.setText("点击设置昵称");
        }
        if (model.getLifeMotto() != null && !model.getLifeMotto().isEmpty()){
            lifeMotto.setText(model.getLifeMotto());
            lifeMottoEdit.setText(model.getLifeMotto());
        }else{
            lifeMotto.setText("点击设置座右铭");
            lifeMottoEdit.setText("点击设置座右铭");
        }

        Menu menu = chatBinding.wonderfulMenu.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setTitle("切换账号  (" + model.getAccount() + ")");

        MenuItem menuHost = menu.getItem(1);
        if (host){
            menuHost.setTitle("设为Host √");
        }else {
            menuHost.setTitle("设为Host");
        }

    }

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
            case R.id.menu_set_host:
                setToHost(menuItem);
                return true;
            case R.id.menu_author:
                getViewModel().showAuthor();
                return true;
            case R.id.menu_note:
                getViewModel().showNote();
                return true;
            default:
                break;
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

    private void setToHost(final MenuItem menuItem){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("温馨提示：");
        dialog.setMessage("将此账号设为Host,可以获得数据持久化，但上一个Host账号将被覆盖！");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!getHostState()) {
                    getViewModel().messageMoveToHost();
                    menuItem.setTitle("设为Host √");
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
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

    /**
     * @description Activity销毁前退出登录，这看起来似乎有安全隐患
     */
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

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressedTime > BACK_PRESSED_INTERVAL) {
            lastBackPressedTime = System.currentTimeMillis();
            ToastUtil.showToast("再次点击退出程序！");
        }else {
            finish();
        }
    }

    class LocalReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean note = intent.getBooleanExtra("note",false);
            Menu menu = chatBinding.wonderfulMenu.getMenu();
            MenuItem menuItem = menu.getItem(5);
            if (note){
                menuItem.setTitle("通知 (1)");
            }else{
                menuItem.setTitle("通知");
            }
        }
    }
}
