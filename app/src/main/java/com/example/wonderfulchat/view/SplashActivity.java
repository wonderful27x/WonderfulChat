package com.example.wonderfulchat.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Window;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivitySplashBinding;
import com.example.wonderfulchat.utils.StatusUtil;
import com.example.wonderfulchat.viewmodel.SplashViewModel;

public class SplashActivity extends BaseActivity<SplashViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusUtil.setTranslucentStatus(this);
        ActivitySplashBinding splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        splashBinding.setSplash(getViewModel());
        getViewModel().setDataBinding(splashBinding);

        if (WonderfulChatActivity.isLogin){
            Intent intent = new Intent(this,WonderfulChatActivity.class);
            startActivity(intent);
            return;
        }

        getViewModel().animation();
    }

    @Override
    public SplashViewModel bindViewModel() {
        return new SplashViewModel();
    }
}
