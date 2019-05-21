package com.example.wonderfulchat.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.example.wonderfulchat.view.LoginActivity;
import com.example.wonderfulchat.view.MainActivity;
import com.example.wonderfulchat.view.MyApplication;

public class SplashViewModel extends BaseViewModel <Activity> {

    public void animation(FrameLayout frameLayout){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(5000);
        alphaAnimation.setFillAfter(true);
        frameLayout.setAnimation(alphaAnimation);
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

}
