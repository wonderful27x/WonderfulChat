package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Window;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivityLoginBinding;
import com.example.wonderfulchat.viewmodel.LoginViewModel;

/**
 * @Author wonderful
 * @Description 登录界面
 * @Date 2019-8-30
 */
public class LoginActivity extends BaseActivity<LoginViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityLoginBinding loginBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        loginBinding.setLoginViewModel(getViewModel());
        getViewModel().setLoginBinding(loginBinding);

        getViewModel().init();
    }

    @Override
    public LoginViewModel bindViewModel() {
        return new LoginViewModel();
    }
}
