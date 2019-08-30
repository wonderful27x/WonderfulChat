package com.example.wonderfulchat.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.wonderfulchat.viewmodel.BaseViewModel;

/**
 * @Author wonderful
 * @Description 基础Activity,采用MVVM架构，同时引入了DataBinding,这里有点生搬硬套，
 * 有些混乱，并且大部分逻辑都移动到了ViewModel中，使得Activity几乎成为空壳，
 * 显得头重脚轻，并且还加剧了代码的耦合性，这是一个失败的架构
 * 至于会不会造成内存泄漏有待考证
 * @Date 2019-8-30
 */
public abstract class BaseActivity <T extends BaseViewModel<? super AppCompatActivity>>extends AppCompatActivity {

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
            viewModel.detachView();
            viewModel = null;
        }
    }
}
