package com.example.wonderfulchat.viewmodel;

import java.lang.ref.WeakReference;

/**
 * @Author wonderful
 * @Description 基础ViewModel,采用MVVM架构，同时引入了DataBinding,这里有点生搬硬套，有些混乱，
 * 有些混乱，并且大部分逻辑都移动到了ViewModel中，使得Fragment和Activity几乎成为空壳，
 * 显得头重脚轻，并且还加剧了代码的耦合性，这是一个失败的架构
 * ViewModel中能够持有Fragment和Activity的binding并随时都能拿到控件，并且ViewModel还会被adapter持有，关系错综复杂，高耦合
 * 至于会不会造成内存泄漏又是一大问题
 * @Date 2019-8-30
 */
public abstract class BaseViewModel <T>{

    private boolean isViewPause = false;
    protected WeakReference<T> weakView;

    public void attachView(T view){
        weakView = new WeakReference<T>(view);
    }

    public T getView(){
        if (weakView == null)return null;
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

    public void detachView(){
        if(weakView != null){
            weakView.clear();
            weakView = null;
        }
    }
}
