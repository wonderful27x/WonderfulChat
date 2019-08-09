package com.example.wonderfulchat.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import com.example.wonderfulchat.databinding.ActivitySplashBinding;
import com.example.wonderfulchat.view.LoginActivity;

public class SplashViewModel extends BaseViewModel <Activity> {

    private ActivitySplashBinding dataBinding;

    public void animation(){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setFillAfter(true);
        dataBinding.splashFrameLayout.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(getView(),LoginActivity.class);
                getView().startActivity(intent);
                getView().finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        alphaAnimation.start();
    }

    public ActivitySplashBinding getDataBinding() {
        return dataBinding;
    }

    public void setDataBinding(ActivitySplashBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    @Override
    public void detachView() {
        super.detachView();
        if(dataBinding != null){
            dataBinding = null;
        }
    }

}
