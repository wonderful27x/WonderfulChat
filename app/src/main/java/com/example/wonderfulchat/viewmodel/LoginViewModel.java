package com.example.wonderfulchat.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.ObservableField;
import android.text.InputType;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.customview.DefuEditText;
import com.example.wonderfulchat.databinding.ActivityLoginBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.WonderfulChatActivity;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginViewModel extends BaseViewModel<Activity> {

    private static final String TAG = "LoginViewModel";
    private ActivityLoginBinding loginBinding;

    private ObservableField<Boolean> isChecked = new ObservableField<>(false);
    private ObservableField<String> account = new ObservableField<>();
    private ObservableField<String> password = new ObservableField<>();
    private ObservableField<Integer> showHide = new ObservableField<>(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    public void login(View view){
        String postParameters = null;
        try {
            postParameters = "account=" + URLEncoder.encode(account.get(), "UTF-8");
            postParameters += "&password=" + URLEncoder.encode(password.get(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpUtil.sendHttpRequest(InternetAddress.LOGIN_URL, postParameters, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(response.equals("登录成功")){
                            accountPassSave(isChecked.get());
                            Intent intent = new Intent(getView(), WonderfulChatActivity.class);
                            getView().startActivity(intent);
                            ToastUtil.showToast("Welcome");
                            //getView().finish();
                            getUserMessage();
                        }else {
                            ToastUtil.showToast("账号或密码错误");
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("登录失败");
                        LogUtil.d(TAG,"登录失败: "+e.getMessage());
                    }
                });
            }
        });

        LogUtil.d(TAG, "account: "+account+" "+"password: "+password);
    }

    private void getUserMessage(){
        String httpUserModelString = FileUtil.getJson(getView(), "HttpUserModel");
        Gson gson = new Gson();
        HttpUserModel httpUserModel = gson.fromJson(httpUserModelString, HttpUserModel.class);
        UserModel userModel = httpUserModel.getContent().get(0);
        String userModelString = gson.toJson(userModel);
        MemoryUtil.sharedPreferencesSaveString("UserModel",userModelString);
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

    public void register(View view){
        LogUtil.d(TAG,"register");
    }

    public void checkBoxCheckedChanged(CompoundButton compoundButton, boolean isChecked){
        this.isChecked.set(isChecked);
        LogUtil.d(TAG, "isChecked:"+isChecked);
    }

    public void passwordShow(View view){
        ImageView imageView = (ImageView) view;
        if(showHide.get() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            showHide.set(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.mipmap.icon_eye_close);
        }else {
            showHide.set(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.mipmap.icon_eye_open);
        }
    }

    public void init(){
        loginBinding.account.setDefuTextWatcher(new DefuEditText.DefuTextWatcher() {
            @Override
            public void onTextChanged(String text) {
                account.set(text);
                LogUtil.d(TAG,"account change!");
            }
        });
        loginBinding.password.setDefuTextWatcher(new DefuEditText.DefuTextWatcher() {
            @Override
            public void onTextChanged(String text) {
                password.set(text);
                LogUtil.d(TAG,"password change!");
            }
        });

        String account = MemoryUtil.sharedPreferencesGetString("account");
        String password = MemoryUtil.sharedPreferencesGetString("password");
        boolean isChecked = MemoryUtil.sharedPreferencesGetBoolean("checkBox");
        setAccount(account);
        setPassword(password);
        setIschecked(isChecked);
    }

    public ObservableField<Integer> getShowHide() {
        return showHide;
    }
    public ObservableField<String> getAccount() {
        return account;
    }

    public void setAccount(ObservableField<String> account) {
        this.account = account;
    }

    public ObservableField<String> getPassword() {
        return password;
    }

    public void setPassword(ObservableField<String> password) {
        this.password = password;
    }

    public void setAccount(String account) {
        this.account.set(account);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public ObservableField<Boolean> getIschecked() {
        return isChecked;
    }

    public void setIschecked(ObservableField<Boolean> ischecked) {
        this.isChecked = ischecked;
    }

    public void setIschecked(boolean ischecked) {
        this.isChecked.set(ischecked);
    }

    private void accountPassSave(boolean isChecked){
        String accountSave = "";
        String passwordSave = "";
        if(isChecked){
            accountSave = account.get();
            passwordSave = password.get();
        }
        MemoryUtil.sharedPreferencesSaveString("account",accountSave);
        MemoryUtil.sharedPreferencesSaveString("password",passwordSave);
        MemoryUtil.sharedPreferencesSaveBoolean("checkBox",isChecked);
    }

    public ActivityLoginBinding getLoginBinding() {
        return loginBinding;
    }

    public void setLoginBinding(ActivityLoginBinding loginBinding) {
        this.loginBinding = loginBinding;
    }

    @Override
    public void deTachView() {
        super.deTachView();
        if (loginBinding != null){
            loginBinding = null;
        }
    }
}
