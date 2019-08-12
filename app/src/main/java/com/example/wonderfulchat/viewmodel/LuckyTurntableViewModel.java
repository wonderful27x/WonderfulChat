package com.example.wonderfulchat.viewmodel;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.example.wonderfulchat.customview.DefuTurntable;
import com.example.wonderfulchat.databinding.LuckyTurntableFragmentLayoutBinding;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.FriendModel;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.view.ChattingActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LuckyTurntableViewModel extends BaseViewModel<Fragment> {

    private LuckyTurntableFragmentLayoutBinding layoutBinding;
    private List<? extends UserModel> friendList;

    public void initView(){
        friendList = getFriendList();
        layoutBinding.luckTurntable.setList(friendList);
        layoutBinding.luckTurntable.setCircleClickListener(new DefuTurntable.CircleClickListener() {
            @Override
            public void circleClick(int position) {
//                Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
//                intent.putExtra("friendName",friendList.get(position).getRemark());
//                intent.putExtra("friendAccount",friendList.get(position).getAccount());
//                getView().getActivity().startActivity(intent);
                jumpToChatting(position);
            }
        });
    }

    public void refresh(){
        friendList = getFriendList();
        layoutBinding.luckTurntable.setList(friendList);
    }

    private void jumpToChatting(int position){
        String unreadState;
        if (getHostState()){
            unreadState = CommonConstant.HOST_UNREAD_MESSAGE;
        }else {
            unreadState = CommonConstant.OTHER_UNREAD_MESSAGE;
        }
        UserModel friendModel = friendList.get(position);
        List<MessageModel> unReadMessage = getMessageListFromPhone(unreadState,friendModel.getAccount());
        clearUnreadMessage(friendModel.getAccount(),unreadState);

        Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
        intent.putExtra("friendModel",friendModel);
        intent.putParcelableArrayListExtra("message", (ArrayList<? extends Parcelable>) unReadMessage);
        getView().getActivity().startActivity(intent);
    }

    //读取好友消息,根据类型可读取已读消息或未读消息
    private List<MessageModel> getMessageListFromPhone(String readState,String account){
        List<MessageModel> messageModels;
        String path = FileUtil.getDiskPath(getView().getActivity(),readState);
        File file = new File(path,account);
        if (!file.exists()){
            return null;
        }
        String jsonData = FileUtil.fileRead(file);
        if ("".equals(jsonData)){
            return null;
        }
        Gson gson = new Gson();
        messageModels = gson.fromJson(jsonData,new TypeToken<List<MessageModel>>(){}.getType());
        return messageModels;
    }

    //清空好友消息,点击跳转后信息即为已读，则将未读消息清空
    public void clearUnreadMessage(String name,String messageState){
        String path = FileUtil.getDiskPath(getView().getActivity(),messageState);
        File file = new File(path, name);
        if (!file.exists()){
            return;
        }else {
            FileUtil.fileClear(file);
        }
    }

//    private List<UserModel> getUserMessage(){
//        String userModel = FileUtil.getJson(getView().getActivity(), "HttpFriendList");
//        Gson gson = new Gson();
//        HttpUserModel httpUserModel = gson.fromJson(userModel, HttpUserModel.class);
//        List<UserModel> userModels = httpUserModel.getContent();
//
//        return userModels;
//    }

    private List<? extends UserModel> getFriendList(){
        List<? extends UserModel>friendList;
        if (getHostState()){
            friendList = LitePal.findAll(UserModel.class);
        }else {
            friendList = LitePal.findAll(FriendModel.class);
        }
        if (friendList == null){
            friendList = new ArrayList<>();
        }

        return friendList;
    }

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    public void setBinding(LuckyTurntableFragmentLayoutBinding layoutBinding){
        this.layoutBinding = layoutBinding;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (layoutBinding != null){
            layoutBinding = null;
        }
    }
}
