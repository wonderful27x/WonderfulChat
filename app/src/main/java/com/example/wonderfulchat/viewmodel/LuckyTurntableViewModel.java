package com.example.wonderfulchat.viewmodel;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.example.wonderfulchat.customview.DefuTurntable;
import com.example.wonderfulchat.databinding.LuckyTurntableFragmentLayoutBinding;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.view.ChattingActivity;
import com.google.gson.Gson;

import java.util.List;

public class LuckyTurntableViewModel extends BaseViewModel<Fragment> {

    private LuckyTurntableFragmentLayoutBinding layoutBinding;
    private List<UserModel> friendList;

    public void initView(){
        friendList = getUserMessage();
        layoutBinding.luckTurntable.setList(friendList);
        layoutBinding.luckTurntable.setCircleClickListener(new DefuTurntable.CircleClickListener() {
            @Override
            public void circleClick(int position) {
                Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
                intent.putExtra("friendName",friendList.get(position).getRemark());
                intent.putExtra("friendAccount",friendList.get(position).getAccount());
                getView().getActivity().startActivity(intent);
            }
        });
    }

    private List<UserModel> getUserMessage(){
        String userModel = FileUtil.getJson(getView().getActivity(), "HttpFriendList");
        Gson gson = new Gson();
        HttpUserModel httpUserModel = gson.fromJson(userModel, HttpUserModel.class);
        List<UserModel> userModels = httpUserModel.getContent();

        return userModels;
    }

    public void setBinding(LuckyTurntableFragmentLayoutBinding layoutBinding){
        this.layoutBinding = layoutBinding;
    }

    @Override
    public void deTachView() {
        super.deTachView();
        if (layoutBinding != null){
            layoutBinding = null;
        }
    }
}
