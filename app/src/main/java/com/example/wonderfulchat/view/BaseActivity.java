package com.example.wonderfulchat.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.wonderfulchat.viewmodel.BaseViewModel;

public abstract class BaseActivity <T extends BaseViewModel<Activity>>extends AppCompatActivity {

    protected T viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = bindViewModel();
        viewModel.attachView(this);
    }

    public T getViewModel(){
        return viewModel;
    }

    public abstract T bindViewModel();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(viewModel != null){
            viewModel.deTachView();
            viewModel = null;
        }
    }
}
