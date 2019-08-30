package com.example.wonderfulchat.viewmodel;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ExpandableListViewAdapter;
import com.example.wonderfulchat.adapter.FriendRequestAdapter;
import com.example.wonderfulchat.customview.FriendRequestDialog;
import com.example.wonderfulchat.customview.LoadingDialog;
import com.example.wonderfulchat.customview.SimpleDialog;
import com.example.wonderfulchat.customview.UserMessageDialog;
import com.example.wonderfulchat.databinding.FriendListFragmentLayoutBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.FriendModel;
import com.example.wonderfulchat.model.FriendRequestModel;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.model.HttpFriendRequest;
import com.example.wonderfulchat.model.HttpUserModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.example.wonderfulchat.utils.HttpUtil;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.ChattingActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.litepal.LitePal;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description FriendListViewModel
 * @Date 2019-8-30
 */
public class FriendListViewModel extends BaseViewModel<Fragment> {

    private static final String TAG = "FriendListViewModel";

    private FriendListFragmentLayoutBinding layoutBinding;
    private ExpandableListViewAdapter adapter;
    private FriendRequestAdapter friendRequestAdapter;
    private List<GroupModel> groupModels;
    private List<UserModel> userModels;
    private List<FriendRequestModel> friendRequestModels;
    private UserModel user;
    private UserModel friend;
    private boolean friendExist = false;
    private int childPosition;
    private LoadingDialog loadingDialog;

    public void initView(){
        loadingDialog = new LoadingDialog(getView().getActivity());

        user = getUserModel();

        groupModels = new ArrayList<>();
        userModels = new ArrayList<>();
        friendRequestModels = new ArrayList<>();

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

        adapter = new ExpandableListViewAdapter(this,groupModels);
        layoutBinding.friendList.setAdapter(adapter);

        friendRequestAdapter = new FriendRequestAdapter(this,friendRequestModels);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getView().getActivity());
        layoutBinding.friendRequest.setLayoutManager(layoutManager);
        layoutBinding.friendRequest.setAdapter(friendRequestAdapter);

        layoutBinding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        getFriendList();
        getFriendRequest();
    }

    /**
     * @description 获取好友请求
     */
    public void getFriendRequest(){
        String url = InternetAddress.GET_FRIEND_REQUEST_URL + "?account=" + user.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        HttpFriendRequest httpFriendRequest = gson.fromJson(response, HttpFriendRequest.class);
                        if(httpFriendRequest == null)return;
                        if ("success".equals(httpFriendRequest.getResult())){
                            if(httpFriendRequest.getContent() != null){
                                List<FriendRequestModel> requestModelList = httpFriendRequest.getContent();
                                friendRequestModels.clear();
                                friendRequestModels.addAll(requestModelList);
                                friendRequestAdapter.notifyDataSetChanged();
                            }
                        }else if("fail".equals(httpFriendRequest.getResult())){
                            ToastUtil.showToast(httpFriendRequest.getMessage());
                        }else if("error".equals(httpFriendRequest.getResult())){
                            LogUtil.e(TAG,httpFriendRequest.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.e(TAG,"好友申请获取失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * @description 好友请求处理，同意或拒绝
     */
    private void friendRequestDeal(final String type, final UserModel userModel, final int position){
        loadingDialog.dialogShow();
        String url = InternetAddress.FRIEND_REQUEST_DEAL_URL + "?account=" + user.getAccount() + "&friendAccount=" + userModel.getAccount() + "&type=" + type;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if ("exist".equals(response)){
                            friendRequestModels.remove(position);
                            friendRequestAdapter.notifyDataSetChanged();
                        }else if("success".equals(response)){
                            if ("agree".equals(type)){
                                userModels.add(userModels.size(),userModel);
                                groupModels.get(0).setNumber(userModels.size());
                                adapter.notifyDataSetChanged();
                                saveToDatabase(userModel);
                            }
                            friendRequestModels.remove(position);
                            friendRequestAdapter.notifyDataSetChanged();
                        }else if("fail".equals(response)){
                            ToastUtil.showToast("操作失败！");
                        }else if("invalid".equals(response)){
                            friendRequestModels.remove(position);
                            friendRequestAdapter.notifyDataSetChanged();
                            if ("agree".equals(type)){
                                ToastUtil.showToast("请求已失效！");
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("操作失败！");
                        LogUtil.e(TAG,e.getMessage());
                    }
                });
            }
        });
    }

    public void showFriendRequestMessage(int position){
        UserModel userModel = new UserModel(friendRequestModels.get(position));
        FriendRequestDialog friendRequestDialog = new FriendRequestDialog(getView().getActivity(),userModel,position);
        friendRequestDialog.setDialogClickListener(new FriendRequestDialog.DialogClickListener() {
            @Override
            public void agreeClick(UserModel model, int position) {
                friendRequestDeal("agree",model,position);
            }

            @Override
            public void refuseClick(UserModel model, int position) {
                friendRequestDeal("refuse",model,position);
            }
        });
        friendRequestDialog.show();
        LogUtil.d(TAG,"friendRequestItem click");
    }

    public void showFriendMessage(int group,int child){
        childPosition = child;
        UserMessageDialog dialog = new UserMessageDialog(getView().getActivity(),groupModels.get(group).getChildModels().get(child));
        dialog.setDialogClickListener(new UserMessageDialog.DialogClickListener() {
            @Override
            public void save(UserModel model) {
                changeRemark(user.getAccount(),model);
            }

            @Override
            public void deleteClick(UserModel model) {
                ToastUtil.showLongToast("确认要删除好友吗，请长按删除！");
            }

            @Override
            public void deleteLongClick(UserModel model) {
                deleteFriendFromService(user.getAccount(),model);
            }
        });
        dialog.show();
    }

    public void jumpToChatting(int group,int child){
        String unreadState;
        if (getHostState()){
            unreadState = CommonConstant.HOST_UNREAD_MESSAGE;
        }else {
            unreadState = CommonConstant.OTHER_UNREAD_MESSAGE;
        }
        UserModel friendModel = groupModels.get(group).getChildModels().get(child);
        List<MessageModel> unReadMessage = getMessageListFromPhone(unreadState,friendModel.getAccount());
        clearUnreadMessage(friendModel.getAccount(),unreadState);

        Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
        intent.putExtra("friendModel",friendModel);
        intent.putParcelableArrayListExtra("message", (ArrayList<? extends Parcelable>) unReadMessage);
        getView().getActivity().startActivity(intent);

    }

    private UserModel getUserModel(){
        String userModelJson;
        UserModel userModel;
        if (getHostState()){
            userModelJson = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_USER_MODEL);
        }else {
            userModelJson = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_USER_MODEL);
        }
        Gson gson = new Gson();
        userModel = gson.fromJson(userModelJson, UserModel.class);

        return userModel;
    }

    public void refreshUserModel(){
        user = getUserModel();
    }

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    /**
     * @description 清空好友消息,点击跳转后信息即为已读，则将未读消息清空
     * @param name,messageState
     */
    public void clearUnreadMessage(String name,String messageState){
        String path = FileUtil.getDiskPath(getView().getActivity(),messageState);
        File file = new File(path, name);
        if (!file.exists()){
            return;
        }else {
            FileUtil.fileClear(file);
        }
    }

    /**
     * @description 读取好友消息,根据类型可读取已读消息或未读消息
     * @param readState,account
     * @return List<MessageModel>
     */
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

    private void getFriendList(){
        layoutBinding.refreshLayout.setRefreshing(true);
        String url = InternetAddress.FRIEND_LIST_URL + "?account=" + user.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        HttpUserModel httpUserModel = gson.fromJson(response, HttpUserModel.class);
                        if(httpUserModel == null)return;
                        if ("success".equals(httpUserModel.getResult())){
                            if(httpUserModel.getContent() != null){
                                List<UserModel> userList = httpUserModel.getContent();
                                userModels.clear();
                                userModels.addAll(userList);
                                groupModels.get(0).setNumber(userModels.size());
                                adapter.notifyDataSetChanged();
                                saveToDatabase();
                            }
                        }else if("fail".equals(httpUserModel.getResult())){
                            ToastUtil.showToast(httpUserModel.getMessage());
                        }else if("error".equals(httpUserModel.getResult())){
                            LogUtil.e(TAG,httpUserModel.getMessage());
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

    /**
     * @description 获取好友列表后存入数据库，如果已存在则更新，否则添加
     */
    private void saveToDatabase(){
        List<UserModel> userList = new ArrayList<>();
        if (!getHostState()){
            for (UserModel model:userModels){
                FriendModel friendModel = new FriendModel();
                friendModel.changeToFriendModel(model);
                userList.add(friendModel);
            }
        }else {
            userList.addAll(userModels);
        }
        for (int i=0; i<userList.size(); i++){
            UserModel userModel = userList.get(i);
            int num = userModel.updateAll("account=?",userModel.getAccount());
            if (num<=0){
                userModel.save();
            }
        }
    }

    /**
     * @description 存入单条数据，如果已存在则更新，否则添加
     */
    private void saveToDatabase(UserModel userModel){
        UserModel model;
        if (getHostState()){
            model = new UserModel(userModel);
        }else {
            model = new FriendModel();
            ((FriendModel) model).changeToFriendModel(userModel);
        }
        int num = model.updateAll("account=?",model.getAccount());
        if (num<=0){
            model.save();
        }
    }

    public void findAddFriend(){
        friendExist = false;
        final SimpleDialog dialog = new SimpleDialog(getView().getActivity());
        dialog.setConfirmClickListener(new SimpleDialog.ConfirmClickListener() {
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

        dialog.setTextChangeListener(new SimpleDialog.TextChangeListener() {
            @Override
            public void textChanged(String text) {
                friendExist = false;
                dialog.setImage(R.drawable.check_false,-1);
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
        loadingDialog.dialogShow();
        String url = InternetAddress.ADD_FRIEND_URL + "?account=" + user.getAccount() + "&friendAccount=" + friendAccount;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if ("success".equals(response)){
                            userModels.add(userModels.size(),friend);
                            groupModels.get(0).setNumber(userModels.size());
                            adapter.notifyDataSetChanged();
                            saveToDatabase(friend);
                        }else if("fail".equals(response)){
                            ToastUtil.showToast("添加失败！");
                            LogUtil.e(TAG,"添加失败：" + response);
                        }else if ("exist".equals(response)){

                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("添加失败！");
                        LogUtil.e(TAG,"添加失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void refresh(){
        getFriendList();
        getFriendRequest();
    }

    private void findFriend(final SimpleDialog dialog, String account){
        loadingDialog.dialogShow();
        String url = InternetAddress.FIND_FRIEND_URL + "?account=" + account;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        Gson gson = new Gson();
                        HttpUserModel httpUserModel = gson.fromJson(response, HttpUserModel.class);
                        if (httpUserModel == null)return;
                        if ("success".equals(httpUserModel.getResult())){
                            if(httpUserModel.getContent() != null && httpUserModel.getContent().size() >0){
                                friend = httpUserModel.getContent().get(0);
                                friendExist = true;
                                dialog.imageHideShow(true,false);
                                dialog.setImage(R.drawable.check_ok,-1);
                                dialog.setConfirmText("添加");
                            }else {
                                dialog.imageHideShow(true,false);
                                dialog.setImage(R.drawable.check_false,-1);
                                ToastUtil.showToast("未找到，请检查账号是否正确！");
                            }
                        }else if("fail".equals(httpUserModel.getResult())){
                            dialog.imageHideShow(true,false);
                            dialog.setImage(R.drawable.check_false,-1);
                            ToastUtil.showToast(httpUserModel.getMessage());
                        }else if("error".equals(httpUserModel.getResult())){
                            dialog.imageHideShow(true,false);
                            dialog.setImage(R.drawable.check_false,-1);
                            LogUtil.e(TAG,httpUserModel.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("搜索失败！");
                        LogUtil.e(TAG,"搜索失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void changeRemark(String account, final UserModel model){
        loadingDialog.dialogShow();
        String url = InternetAddress.CHANGE_REMARK_URL + "?account=" + account + "&friendAccount=" + model.getAccount() + "&content=" + model.getRemark();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if ("success".equals(response)){
                            ToastUtil.showToast("修改成功！");
                            userModels.set(childPosition,model);
                            groupModels.get(0).setNumber(userModels.size());
                            adapter.notifyDataSetChanged();
                            saveToDatabase(model);
                        }else{
                            ToastUtil.showToast("修改失败！");
                            LogUtil.d(TAG,response);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("修改失败！");
                        LogUtil.e(TAG,"修改失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * @description 从服务器中删除好友，成功后删除本地数据
     * @param account,userModel
     */
    private void deleteFriendFromService(final String account, final UserModel userModel){
        loadingDialog.dialogShow();
        String url = InternetAddress.DELETE_FRIEND_URL + "?account=" + account + "&friendAccount=" + userModel.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        if ("success".equals(response)){
                            ToastUtil.showToast("删除成功！");
                            userModels.remove(childPosition);
                            groupModels.get(0).setNumber(userModels.size());
                            adapter.notifyDataSetChanged();
                            deleteFriendFromLocal(userModel.getAccount());
                            deleteFriendMessage(userModel.getAccount());
                            deleteMessageAccount(userModel.getAccount());
                        }else{
                            ToastUtil.showToast("删除失败！");
                            LogUtil.d(TAG,response);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dialogDismiss();
                        ToastUtil.showToast("删除失败！");
                        LogUtil.e(TAG,"删除失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    private void deleteFriendFromLocal(String friendAccount){
        if (getHostState()){
            LitePal.deleteAll(UserModel.class,"account=?",friendAccount);
        }else {
            LitePal.deleteAll(FriendModel.class,"account=?",friendAccount);
        }
    }

    private void deleteFriendMessage(String account){
        if (account == null)return;
        String unreadState;
        String readState;
        if (getHostState()){
            unreadState = CommonConstant.HOST_UNREAD_MESSAGE;
            readState = CommonConstant.HOST_READ_MESSAGE;
        }else {
            unreadState = CommonConstant.OTHER_UNREAD_MESSAGE;
            readState = CommonConstant.OTHER_READ_MESSAGE;
        }
        String path;
        path = FileUtil.getDiskPath(getView().getActivity(),unreadState);
        File file = new File(path,account);
        FileUtil.fileDelete(file);

        path = FileUtil.getDiskPath(getView().getActivity(),readState);
        file = new File(path,account);
        FileUtil.fileDelete(file);
    }

    private void deleteMessageAccount(String account){
        if (account == null)return;
        String[] accounts = getOldMessageAccounts();
        if (accounts == null || accounts.length <=0)return;
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<accounts.length; i++){
            String child = accounts[i];
            if (!account.equals(child)){
                builder.append(child);
                if(i<accounts.length-1){
                    builder.append(",");
                }
            }
        }

        if (builder.length()<=0)return;

        String key;
        if (getHostState()){
            key = CommonConstant.HOST_MESSAGE_ACCOUNT;
        }else {
            key = CommonConstant.OTHER_MESSAGE_ACCOUNT;
        }
        MemoryUtil.sharedPreferencesSaveString(key,builder.toString());
    }

    /**
     * @description 拿到有消息记录的所有账号
     * @return String[]
     */
    private String[] getOldMessageAccounts(){
        String key;
        if (getHostState()){
            key = CommonConstant.HOST_MESSAGE_ACCOUNT;
        }else {
            key = CommonConstant.OTHER_MESSAGE_ACCOUNT;
        }
        String s = MemoryUtil.sharedPreferencesGetString(key);
        if (s == null || s.equals("")){
            return null;
        }
        String[] accounts = s.split(",");
        return accounts;
    }

    public FriendListFragmentLayoutBinding getLayoutBinding() {
        return layoutBinding;
    }

    public void setLayoutBinding(FriendListFragmentLayoutBinding layoutBinding) {
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
