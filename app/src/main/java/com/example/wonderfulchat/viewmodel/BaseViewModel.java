package com.example.wonderfulchat.viewmodel;

import java.lang.ref.WeakReference;

public abstract class BaseViewModel <T>{

    private boolean isViewPause = false;
    protected WeakReference<T> weakView;

    public void attachView(T view){
        weakView = new WeakReference<T>(view);
    }

    public T getView(){
        return weakView.get();
    }

    public boolean isViewAttached(){
        return weakView != null && weakView.get() != null;
    }

    public void notifyViewPause(boolean isViewPause){
        this.isViewPause = isViewPause;
    }

    public boolean isViewPause() {
        return isViewPause;
    }

    public void deTachView(){
        if(weakView != null){
            weakView.clear();
            weakView = null;
        }
    }
}
