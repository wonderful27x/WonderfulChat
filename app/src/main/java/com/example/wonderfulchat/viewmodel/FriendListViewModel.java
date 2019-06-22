package com.example.wonderfulchat.viewmodel;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ExpandableListViewAdapter;
import com.example.wonderfulchat.customview.CustomDialog;
import com.example.wonderfulchat.databinding.FriendListFragmentLayoutBinding;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.view.ChattingActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class FriendListViewModel extends BaseViewModel<Fragment> {

    private FriendListFragmentLayoutBinding layoutBinding;
    private ExpandableListViewAdapter adapter;
    private List<GroupModel> groupModels;
    private boolean friendExist = false;

    public void initView(){
        groupModels = new ArrayList<>();

        List<UserModel> userModels = getUserMessage();

        GroupModel groupModel = new GroupModel();
        groupModel.setTitle("我的亲密好友");
        groupModel.setNumber(userModels.size());
        groupModel.setChildModels(userModels);
        groupModels.add(groupModel);

        List<UserModel> otherChildren = new ArrayList<>();
        GroupModel otherGroup = new GroupModel();
        otherGroup.setTitle("其他分组 (待开发)");
        otherGroup.setNumber(0);
        otherGroup.setChildModels(otherChildren);
        groupModels.add(otherGroup);

        adapter = new ExpandableListViewAdapter(layoutBinding.getWonderfulViewModel(),groupModels);
        layoutBinding.friendList.setAdapter(adapter);
        layoutBinding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        layoutBinding.friendList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
                intent.putExtra("friendName",groupModels.get(i).getChildModels().get(i1).getNickname());
                getView().getActivity().startActivity(intent);
                return true;
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

    public void findAndaddFriend(){
        friendExist = false;
        final CustomDialog dialog = new CustomDialog(getView().getActivity());
        dialog.setConfirmClickListener(new CustomDialog.ConfirmClickListener() {
            @Override
            public void parameterPass(String parameter1, String parameter2) {
                if (friendExist){
                    friendExist = false;
                    dialog.dismiss();
                    addFriend();
                }
                findFriend(dialog);
            }
        });
        dialog.show();
        dialog.lineHideShow(true,false);
        dialog.imageHideShow(false,false);
        dialog.setParameterNote("账号：","");
        dialog.setConfirmText("搜索");
    }

    private void addFriend(){
        UserModel userModel = new UserModel();
        userModel.setNickname("德芙");
        userModel.setRemark("巧克力");
        userModel.setImageUrl("http://192.168.191.5:8080/file/girl.jpg");
        userModel.setLifeMotto("季后赛股票害怕么");
        List<UserModel> userModels = groupModels.get(0).getChildModels();
        userModels.add(userModels.size(),userModel);
        groupModels.get(0).setNumber(userModels.size());
        adapter.notifyDataSetChanged();
    }

    private void refresh(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addFriend();
                        layoutBinding.refreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void findFriend(CustomDialog dialog){
        friendExist = true;
        dialog.imageHideShow(true,false);
        dialog.setImage(R.mipmap.check_ok,-1);
        dialog.setConfirmText("添加");
    }

    public FriendListFragmentLayoutBinding getLayoutBinding() {
        return layoutBinding;
    }

    public void setLayoutBinding(FriendListFragmentLayoutBinding layoutBinding) {
        this.layoutBinding = layoutBinding;
    }
}
