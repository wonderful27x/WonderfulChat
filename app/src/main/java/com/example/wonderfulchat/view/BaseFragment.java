package com.example.wonderfulchat.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.example.wonderfulchat.viewmodel.BaseViewModel;

/**
 * @Author wonderful
 * @Description 基础Fragment,采用MVVM架构，同时引入了DataBinding,这里有点生搬硬套，有些混乱,
 * 有些混乱，并且大部分逻辑都移动到了ViewModel中，使得Fragment几乎成为空壳，
 * 显得头重脚轻，并且还加剧了代码的耦合性，这是一个失败的架构
 * 至于会不会造成内存泄漏有待考证
 * 在没有嵌套的情况下能够实现懒加载
 * @Date 2019-8-30
 */
public abstract class BaseFragment <T extends BaseViewModel<? super Fragment>> extends Fragment{

    protected T viewModel;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = bindViewModel();
        viewModel.attachView(this);
    }

    public T getViewModel(){
        return viewModel;
    }

    public abstract T bindViewModel();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewModel != null){
            viewModel.detachView();
            viewModel = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if ((isVisibleToUser && isResumed())) {
            onResume();
        } else if (!isVisibleToUser) {
            onPause();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            //TODO give the signal that the fragment is visible
            dataLoad();
        }
    }

    public abstract void dataLoad();
}
