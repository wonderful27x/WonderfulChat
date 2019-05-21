package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivityLoginBinding;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.viewmodel.LoginViewModel;

public class LoginActivity extends BaseActivity<LoginViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding loginBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        loginBinding.setLoginViewModel(getViewModel());
        getViewModel().editTextInit(loginBinding.account,loginBinding.password);

        String account = MemoryUtil.sharedPreferencesGetString("account");
        String password = MemoryUtil.sharedPreferencesGetString("password");
        boolean isChecked = MemoryUtil.sharedPreferencesGetBoolean("checkBox");
        getViewModel().setAccount(account);
        getViewModel().setPassword(password);
        getViewModel().setIschecked(isChecked);

    }

    @Override
    public LoginViewModel bindViewModel() {
        return new LoginViewModel();
    }
}
