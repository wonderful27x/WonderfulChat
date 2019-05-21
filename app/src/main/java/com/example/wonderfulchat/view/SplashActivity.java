package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivitySplashBinding;
import com.example.wonderfulchat.viewmodel.SplashViewModel;

public class SplashActivity extends BaseActivity<SplashViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        splashBinding.setSplash(getViewModel());
        FrameLayout frameLayout = splashBinding.splashFrameLayout;
        viewModel.animation(frameLayout);
    }

    @Override
    public SplashViewModel bindViewModel() {
        return new SplashViewModel();
    }
}
