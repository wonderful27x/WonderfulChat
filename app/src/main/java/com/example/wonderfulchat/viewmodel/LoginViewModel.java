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
import com.example.wonderfulchat.customview.LoadingDialog;
import com.example.wonderfulchat.databinding.ActivityLoginBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.FriendModel;
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
import org.litepal.LitePal;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description LoginViewModel
 * @Date 2019-8-30
 */
public class LoginViewModel extends BaseViewModel<Activity> {

    private static final String TAG = "LoginViewModel";
    private ActivityLoginBinding loginBinding;
    private LoadingDialog loadingDialog;

    /**
     * dataBinding变量监听，当值改变是与值绑定的控件状态也会改变
     */
    private ObservableField<Boolean> isChecked = new ObservableField<>(false);
    private ObservableField<String> account = new ObservableField<>();
    private ObservableField<String> password = new ObservableField<>();
    private ObservableField<Integer> showHide = new ObservableField<>(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    public void init(){
        loadingDialog = new LoadingDialog(getView());
        loginBinding.account.setDefuTextWatcher(new DefuEditText.DefuTextWatcher() {
            @Override
            public void onTextChanged(String text) {
                account.set(text);
                loginBinding.account.setSelection(text.length());
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

    /**
     * @description 登录，这是dataBinding的用法
     * @param view
     */
    public void login(View view){
        if (!WonderfulChatActivity.loginLock){
            WonderfulChatActivity.loginLock = true;
        }else {
            return;
        }
        loadingDialog.dialogShow();
        String postParameters = null;
        try {
            postParameters = "account=" + URLEncoder.encode(account.get(), "UTF-8");
            postParameters += "&password=" + URLEncoder.encode(password.get(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpUtil.httpRequestForPost(InternetAddress.LOGIN_URL, postParameters, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        getUserMessage(response);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WonderfulChatActivity.loginLock = false;
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("登录失败！");
                        LogUtil.e(TAG,"登录失败: "+e.getMessage());
                    }
                });
            }
        });

        LogUtil.d(TAG, "account: "+account+" "+"password: "+password);
    }

    /**
     * @description 注册，严格控制了输入字符和长度，避免后台字符串处理带来麻烦
     * @param view
     */
    public void registerClick(View view){
        ToastUtil.showToast("请长按注册");
    }

    public boolean register(View view){
        if (account.get().length()<5){
            ToastUtil.showToast("账号长度不得小于5！");
            return true;
        }
        if (password.get().length()<5){
            ToastUtil.showToast("密码长度不得小于5！");
            return true;
        }
        String postParameters = null;
        try {
            postParameters = "account=" + URLEncoder.encode(account.get(), "UTF-8");
            postParameters += "&password=" + URLEncoder.encode(password.get(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        loadingDialog.dialogShow();
        HttpUtil.httpRequestForPost(InternetAddress.REGISTER_URL, postParameters, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if(response.equals("注册成功！")){
                            ToastUtil.showToast("注册成功，请直接登录");
                        }else {
                            ToastUtil.showToast(response);
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
                        ToastUtil.showToast("注册失败！");
                        LogUtil.e(TAG,"注册失败: "+e.getMessage());
                    }
                });
            }
        });

        return true;
    }

    /**
     * @description 登录成功返回一个列表，第一项是登录者信息，剩下的是好友信息
     * @param jsonData
     */
    private void getUserMessage(String jsonData){

        Gson gson = new Gson();
        HttpUserModel httpUserModel = gson.fromJson(jsonData, HttpUserModel.class);
        if(httpUserModel == null){
            WonderfulChatActivity.loginLock = false;
            return;
        }
        if ("success".equals(httpUserModel.getResult())){
            WonderfulChatActivity.loginLock = true;
            accountPassSave(isChecked.get());

            List<UserModel> userModels = httpUserModel.getContent();
            messageSave(userModels);
            Intent intent = new Intent(getView(), WonderfulChatActivity.class);
            getView().startActivity(intent);
            getView().finish();
        }else if("fail".equals(httpUserModel.getResult())){
            WonderfulChatActivity.loginLock = false;
            ToastUtil.showToast(httpUserModel.getMessage());
            LogUtil.d(TAG,httpUserModel.getMessage());
        }else if("error".equals(httpUserModel.getResult())){
            WonderfulChatActivity.loginLock = false;
            ToastUtil.showToast("登录失败！");
            LogUtil.d(TAG,httpUserModel.getMessage());
        }
    }

    /**
     * @description 重要的消息存储策略，实现不同登录用户的数据持久化，
     * 根据账号是否为HOST账号和是否和上一个登录的账号相同执行不同的消息存储策略，
     * 这里设计了HOST和非HOST两套策略
     * @param userModels
     */
    private void messageSave(List<UserModel> userModels){
        Gson gson = new Gson();
        String hostAccount = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_ACCOUNT);
        String lastAccount = MemoryUtil.sharedPreferencesGetString(CommonConstant.LAST_ACCOUNT);
        if (hostAccount == null || hostAccount.isEmpty()){
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_ACCOUNT,userModels.get(0).getAccount());
        }
        MemoryUtil.sharedPreferencesSaveString(CommonConstant.LAST_ACCOUNT,userModels.get(0).getAccount());

        //情况一：第一次使用此软件，没有任何记录，将账号设为HOST账号，数据存入HOST
        if (hostAccount == null || hostAccount.isEmpty() || lastAccount == null || lastAccount.isEmpty()){
            String userModelString = gson.toJson(userModels.get(0));
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,userModelString);
            MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,true);
            saveToDatabase(userModels,true);
        //情况二：这次登录的账号和上次登录的一致，且是HOST账号，数据存入HOST
        }else if (lastAccount.equals(userModels.get(0).getAccount()) && hostAccount.equals(userModels.get(0).getAccount())){
            String userModelString = gson.toJson(userModels.get(0));
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,userModelString);
            MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,true);
            saveToDatabase(userModels,true);
        //情况三：这次登录的账号和上次登录的一致，但不是HOST账号，这种情况属于有相同的非HOST账号连续登录，则直接将数据存入非HOST
        }else if (lastAccount.equals(userModels.get(0).getAccount()) && !hostAccount.equals(userModels.get(0).getAccount())){
            String userModelString = gson.toJson(userModels.get(0));
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,userModelString);
            MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,false);
            saveToDatabase(userModels,false);
        //情况四：这次登录的账号和上次登录的不一致，但是HOST账号，这种情况属从非HOST账号切回了HOST账号，则将数据存入HOST，并清除非HOST数据
        }else if (!lastAccount.equals(userModels.get(0).getAccount()) && hostAccount.equals(userModels.get(0).getAccount())){
            String userModelString = gson.toJson(userModels.get(0));
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_USER_MODEL,userModelString);
            MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,true);
            saveToDatabase(userModels,true);
            clearAllOtherMessage();
        //情况五：这次登录的账号和上次登录的不一致，且不是HOST账号，这种情况属于从HOST切到非HOST或从非HOST切到非HOST，则先清除非HOST数据，并将数据存入非HOST
        }else {
            clearAllOtherMessage();
            String userModelString = gson.toJson(userModels.get(0));
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_USER_MODEL,userModelString);
            MemoryUtil.sharedPreferencesSaveBoolean(CommonConstant.HOST_STATE,false);
            saveToDatabase(userModels,false);
        }
    }

    /**
     * @description 好友信息存储策略，为数据库方式存储，
     * 获取好友消息后存入数据库，根据账号类型执行不同的策略
     * @param userModelList,hostAccount
     */
    private void saveToDatabase(List<UserModel> userModelList,boolean hostAccount){
        List<UserModel> userModels = new ArrayList<>();
        if (!hostAccount){
            for (UserModel model:userModelList){
                FriendModel friendModel = new FriendModel();
                friendModel.changeToFriendModel(model);
                userModels.add(friendModel);
            }
        }else {
            userModels.addAll(userModelList);
        }
        for (int i=1; i<userModels.size(); i++){
            UserModel userModel = userModels.get(i);
            int num = userModel.updateAll("account=?",userModel.getAccount());
            if (num<=0){
                userModel.save();
            }
        }
    }

    /**
     * @description 数据清除，防止登录账号获取到其他曾经登录过的账号信息，
     * 这里清除的是非Host账号所有信息
     */
    private void clearAllOtherMessage(){
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

    public void checkBoxCheckedChanged(CompoundButton compoundButton, boolean isChecked){
        this.isChecked.set(isChecked);
        LogUtil.d(TAG, "isChecked:"+isChecked);
    }

    public void passwordShow(View view){
        ImageView imageView = (ImageView) view;
        if(showHide.get() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            showHide.set(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.icon_eye_close);
        }else {
            showHide.set(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.icon_eye_open);
        }
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
    public void detachView() {
        super.detachView();
        if (loginBinding != null){
            loginBinding = null;
        }
    }
}
