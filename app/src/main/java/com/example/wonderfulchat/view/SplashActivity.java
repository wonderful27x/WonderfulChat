package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Window;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivitySplashBinding;
import com.example.wonderfulchat.utils.StatusUtil;
import com.example.wonderfulchat.viewmodel.SplashViewModel;

/**
 * @Author wonderful
 * @Description 动画播放界面
 * @Date 2019-8-30
 */
public class SplashActivity extends BaseActivity<SplashViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusUtil.setTranslucentStatus(this);
        ActivitySplashBinding splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        splashBinding.setSplash(getViewModel());
        getViewModel().setDataBinding(splashBinding);

        getViewModel().animation();
    }

    @Override
    public SplashViewModel bindViewModel() {
        return new SplashViewModel();
    }
}
