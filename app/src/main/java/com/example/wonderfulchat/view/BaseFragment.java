package com.example.wonderfulchat.view;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.wonderfulchat.viewmodel.BaseViewModel;

import java.util.List;

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
            viewModel.deTachView();
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
