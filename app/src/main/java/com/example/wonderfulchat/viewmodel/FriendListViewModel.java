package com.example.wonderfulchat.viewmodel;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ExpandableListViewAdapter;
import com.example.wonderfulchat.customview.CustomDialog;
import com.example.wonderfulchat.databinding.FriendListFragmentLayoutBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.ChattingActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class FriendListViewModel extends BaseViewModel<Fragment> {

    private static final String TAG = "FriendListViewModel";

    private FriendListFragmentLayoutBinding layoutBinding;
    private ExpandableListViewAdapter adapter;
    private List<GroupModel> groupModels;
    private List<UserModel> userModels;
    private UserModel model;
    private UserModel friend;
    private boolean friendExist = false;

    public void initView(){
        String userModel = MemoryUtil.sharedPreferencesGetString("UserModel");
        Gson gson = new Gson();
        model = gson.fromJson(userModel, UserModel.class);

        groupModels = new ArrayList<>();
        userModels = new ArrayList<>();

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
                intent.putExtra("friendName",groupModels.get(i).getChildModels().get(i1).getRemark());
                intent.putExtra("friendAccount",groupModels.get(i).getChildModels().get(i1).getAccount());
                getView().getActivity().startActivity(intent);
                return true;
            }
        });

        getFriendList();
    }

//    private List<UserModel> getUserMessage(){
//        String userModel = FileUtil.getJson(getView().getActivity(), "HttpFriendList");
//        Gson gson = new Gson();
//        HttpUserModel httpUserModel = gson.fromJson(userModel, HttpUserModel.class);
//        List<UserModel> userModels = httpUserModel.getContent();
//
//        return userModels;
//    }

    private void getFriendList(){

        String url = InternetAddress.FRIEND_LIST_URL + "?account=" + model.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        HttpUserModel httpUserModel = gson.fromJson(response, HttpUserModel.class);
                        if (httpUserModel != null && httpUserModel.getContent() != null){
                            List<UserModel> userList = httpUserModel.getContent();
                            userModels.clear();
                            userModels.addAll(userList);
                            groupModels.get(0).setNumber(userModels.size());
                            adapter.notifyDataSetChanged();
                        }else {
                            ToastUtil.showToast("无数据！");
                        }
                        layoutBinding.refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutBinding.refreshLayout.setRefreshing(false);
                        ToastUtil.showToast("好友列表获取失败！");
                        LogUtil.e(TAG,"好友列表获取失败：" + e.getMessage());
                    }
                });
            }
        });

    }



    public void findAddFriend(){
        friendExist = false;
        final CustomDialog dialog = new CustomDialog(getView().getActivity());
        dialog.setConfirmClickListener(new CustomDialog.ConfirmClickListener() {
            @Override
            public void parameterPass(String parameter1, String parameter2) {
                if (friendExist){
                    friendExist = false;
                    dialog.dismiss();
                    addFriend(parameter1);
                }else {
                    findFriend(dialog,parameter1);
                }
            }
        });

        dialog.setTextChangeListener(new CustomDialog.TextChangeListener() {
            @Override
            public void textChanged(String text) {
                friendExist = false;
                dialog.setImage(R.mipmap.check_false,-1);
                dialog.setConfirmText("搜索");
            }
        });

        dialog.show();
        dialog.lineHideShow(true,false);
        dialog.imageHideShow(false,false);
        dialog.setParameterNote("账号：","");
        dialog.setConfirmText("搜索");
    }

    private void addFriend(String friendAccount){
//        UserModel userModel = new UserModel();
//        userModel.setAccount("wonderful");
//        userModel.setNickname("德芙");
//        userModel.setRemark("巧克力");
//        userModel.setImageUrl("http://192.168.191.5:8080/file/girl.jpg");
//        userModel.setLifeMotto("季后赛股票害怕么");
//        List<UserModel> userModels = groupModels.get(0).getChildModels();

        String url = InternetAddress.ADD_FRIEND_URL + "?account=" + model.getAccount() + "&friendAccount=" + friendAccount;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.equals("success")){
                            userModels.add(userModels.size(),friend);
                            groupModels.get(0).setNumber(userModels.size());
                            adapter.notifyDataSetChanged();
                        }else if(response.equals("fail")){
                            ToastUtil.showToast("添加失败！");
                            LogUtil.e(TAG,"添加失败：" + response);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("添加失败！");
                        LogUtil.e(TAG,"添加失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void refresh(){
        getFriendList();
    }


//    private void refresh(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    Thread.sleep(2000);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//                getView().getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        addFriend();
//                        layoutBinding.refreshLayout.setRefreshing(false);
//                    }
//                });
//            }
//        }).start();
//    }

    private void findFriend(final CustomDialog dialog,String account){
        String url = InternetAddress.FIND_FRIEND_URL + "?account=" + account;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        HttpUserModel httpUserModel = gson.fromJson(response, HttpUserModel.class);
                        if (httpUserModel != null && httpUserModel.getContent() != null && httpUserModel.getContent().size() >0){
                            friend = httpUserModel.getContent().get(0);
                            friendExist = true;
                            dialog.imageHideShow(true,false);
                            dialog.setImage(R.mipmap.check_ok,-1);
                            dialog.setConfirmText("添加");
                        }else {
                            dialog.imageHideShow(true,false);
                            dialog.setImage(R.mipmap.check_false,-1);
                            ToastUtil.showToast("未找到，请检查账号是否正确！");
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("搜索失败！");
                        LogUtil.e(TAG,"搜索失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    public FriendListFragmentLayoutBinding getLayoutBinding() {
        return layoutBinding;
    }

    public void setLayoutBinding(FriendListFragmentLayoutBinding layoutBinding) {
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
