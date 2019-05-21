package com.example.wonderfulchat.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.example.wonderfulchat.viewmodel.BaseViewModel;

public abstract class BaseFragment <T extends BaseViewModel<Fragment>> extends Fragment{

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
}
