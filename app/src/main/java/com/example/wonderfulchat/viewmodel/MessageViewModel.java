package com.example.wonderfulchat.viewmodel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import com.example.wonderfulchat.adapter.MessageListAdapter;
import com.example.wonderfulchat.databinding.MessageFragmentLayoutBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.FriendModel;
import com.example.wonderfulchat.model.HttpMessageModel;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description 消息ViewModel,这里实现了重要的消息存储策略，
 * 消息存储采用文件系统+JSON的方式，分为已读和未读两大类，
 * 每次请求到最新消息后，先从本地读出消息，然后合并消息，再存入本地
 * 这也许并不是好的实现方案
 * @Date 2019-8-30
 */
public class MessageViewModel extends BaseViewModel <Fragment> {

    private static final String TAG = "MessageViewModel";
    private MessageFragmentLayoutBinding layoutBinding;
    private List<List<MessageModel>> unReadMessageList;
    private List<List<MessageModel>> readMessageList;
    private MessageListAdapter adapter;
    private UserModel userModel;

    public void initView(){
        userModel = getUserModel();

        unReadMessageList = new ArrayList<>();
        readMessageList = new ArrayList<>();
        List<List<MessageModel>> unRead = new ArrayList<>();
        List<List<MessageModel>> readed = new ArrayList<>();
        getMessageList(unRead,readed);
        unReadMessageList.addAll(unRead);
        readMessageList.addAll(readed);

        adapter = new MessageListAdapter(this,unReadMessageList,readMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getView().getActivity());
        layoutBinding.recyclerView.setLayoutManager(layoutManager);
        layoutBinding.recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(new MessageListAdapter.ItemClickListener() {
            @Override
            public void itemClick(int position) {
                jumpToChatting(position);
            }

            @Override
            public void itemLongClick(int position) {
                messageDelete(position);
            }
        });

        layoutBinding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

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

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    /**
     * @description 将消息存入文件系统，分为已读和未读两大文件，每个文件中每个好友账号都存入独立的消息列表Json数据
     * 注意：这种将整个文件重新存一遍的方式会造成性能问题
     * @param messageList,messageState
     */
    private void saveMessage(List<List<MessageModel>> messageList,String messageState){
        if (messageList == null || messageList.size() <=0)return;
        String path = FileUtil.getDiskPath(getView().getActivity(),messageState);
        Gson gson = new Gson();
        for (List<MessageModel> messageModels:messageList){
            String account = accountFilter(messageModels.get(0));
            File file = new File(path, account);
            if (!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String content = gson.toJson(messageModels);
            FileUtil.fileSave(file,content,false);
        }
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
     * @description 从网络获取新的好友消息
     */
    private void getMessageFromNet(){
        String url = InternetAddress.GET_MESSAGE_URL + "?account=" + userModel.getAccount();
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutBinding.refreshLayout.setRefreshing(false);
                        Gson gson = new Gson();
                        HttpMessageModel httpMessageModel = gson.fromJson(response, HttpMessageModel.class);
                        if(httpMessageModel == null)return;
                        if("success".equals(httpMessageModel.getResult())){
                            if(httpMessageModel.getContent() != null){
                                List<List<MessageModel>> arrayList = httpMessageModel.getContent();
                                removeEmpty(arrayList);
                                analyzeMessage(arrayList);
                            }
                        }else if("fail".equals(httpMessageModel.getResult())){
                            ToastUtil.showToast(httpMessageModel.getMessage());
                        }else if("error".equals(httpMessageModel.getResult())){
                            ToastUtil.showToast("消息获取失败！");
                            LogUtil.e(TAG,"消息获取失败：" + httpMessageModel.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutBinding.refreshLayout.setRefreshing(false);
                        ToastUtil.showToast("消息获取失败！");
                        LogUtil.e(TAG,"消息获取失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * @description 拿到有消息记录的所有账号
     */
    private String[] getOldMessageAccounts(){
        String s;
        if (getHostState()){
            s = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_MESSAGE_ACCOUNT);
        }else {
            s = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_MESSAGE_ACCOUNT);
        }
        if (s == null || s.equals("")){
            return null;
        }
        String[] accounts = s.split(",");
        return accounts;
    }

    /**
     * @description 将有消息存入的所有账号记录下来
     */
    private void saveMessageAccounts(){
        String accountAll = "";
        StringBuilder account = new StringBuilder();
        for (List<MessageModel> unRead:unReadMessageList){
            account.append(unRead.get(0).getSenderAccount());
            account.append(",");
        }
        for (List<MessageModel> read:readMessageList){
            String senderAccount = accountFilter(read.get(0));
            account.append(senderAccount);
            account.append(",");
        }
        if (account.length()>0){
            account.deleteCharAt(account.length()-1);
            accountAll = account.toString();
        }
        if (getHostState()){
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.HOST_MESSAGE_ACCOUNT,accountAll);
        }else {
            MemoryUtil.sharedPreferencesSaveString(CommonConstant.OTHER_MESSAGE_ACCOUNT,accountAll);
        }
    }

    /**
     * @description 读取好友消息,根据类型可读取已读消息或未读消息
     * @param readState,account
     * @return List<MessageModel>
     */
    private List<MessageModel> getMessageListFromPhone(String readState,String account){
        if (account == null)return null;
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

    /**
     * @description 读取未读消息和已读消息（二者互斥，即同一个好友的消息状态只能是未读或已读），
     * 注意这是指获取的消息我们在代码中控制令其唯一，但是在已读和未读文件系统中有可能有相同账号的数据，即有未读和已读的消息。
     * @param unRead,read
     */
    private void getMessageList(List<List<MessageModel>> unRead,List<List<MessageModel>> read){
        String [] messageAccounts = getOldMessageAccounts();
        if (messageAccounts == null)return;
        String unreadState;
        String readState;
        if (getHostState()){
            unreadState = CommonConstant.HOST_UNREAD_MESSAGE;
            readState = CommonConstant.HOST_READ_MESSAGE;
        }else {
            unreadState = CommonConstant.OTHER_UNREAD_MESSAGE;
            readState = CommonConstant.OTHER_READ_MESSAGE;
        }
        for (String account:messageAccounts){
            List<MessageModel> messageModels = getMessageListFromPhone(unreadState,account);
            //从"UnReadMessage"文件中读到了说明是未读消息
            if (messageModels != null){
                unRead.add(messageModels);
            }
            //否则从"ReadMessage"文件中读取
            else {
                messageModels = getMessageListFromPhone(readState,account);
                if (messageModels != null){
                    read.add(messageModels);
                }
            }

        }
    }

    /**
     * @description 读取指定账号的所有已读消息
     * @param accounts
     * @return List<List<MessageModel>>
     */
    private List<List<MessageModel>> getReadMessageList(List<String> accounts){
        if(accounts == null || accounts.size() <=0) return null;
        List<List<MessageModel>> messageList = new ArrayList<>();
        String readState;
        if (getHostState()){
            readState = CommonConstant.HOST_READ_MESSAGE;
        }else {
            readState = CommonConstant.OTHER_READ_MESSAGE;
        }
        for (String account:accounts){
            List<MessageModel> readMessage = getMessageListFromPhone(readState,account);
            messageList.add(readMessage);
        }
        if (messageList.size() <=0){
            messageList = null;
        }
        return messageList;
    }

    /**
     * @description 将最新的消息（从网络获取，一定是未读状态）和已读消息合并，得出最终的已读消息，
     * 即如果已读消息和未读消息是相同的好友则不作处理，
     * 注意：虽然没做处理但是这条已读消息并没有被删除，因此在跳转到聊天Activity时仍然能够从"ReadMessage"文件中读取到。
     * @param unRead,read
     */
    private void mergeReadMessage(List<List<MessageModel>> unRead,List<List<MessageModel>> read){
        if (unRead == null && read == null){
            return ;
        }else if(read == null){
            return ;
        }else if(unRead == null){
            readMessageList.addAll(read);
            return;
        }

        for (List<MessageModel> messageModels:unRead){
            for (int i=0; i<read.size(); i++){
                List<MessageModel> readList = read.get(i);
                String account = accountFilter(readList.get(0));
                if (messageModels.get(0).getSenderAccount().equals(account)){
                    read.remove(i);
                    break;
                }
            }
        }
        readMessageList.addAll(read);
    }

    /**
     * @description 将最新的消息（从网络获取，一定是未读状态）和未读消息合并，得出最终的未读消息，
     * 即如果两个未读消息是同一个好友则将消息合并。
     * @param unRead
     */
    private void mergeUnReadMessageList(List<List<MessageModel>> unRead){
        if (unRead == null)return;
        for (List<MessageModel> messageModels:unReadMessageList){
            for (int i=0; i<unRead.size(); i++){
                List<MessageModel> unReadList = unRead.get(i);
                if (messageModels.get(0).getSenderAccount().equals(unReadList.get(0).getSenderAccount())){
                    messageModels.addAll(unReadList);
                    unRead.remove(i);
                    break;
                }
            }
        }
        unReadMessageList.addAll(unRead);
    }

    /**
     * @description 空项移除函数，将列表内的空列表移除
     * @param arrayList
     */
    private void removeEmpty(List<List<MessageModel>> arrayList){
        for (int i=0; i<arrayList.size(); i++){
            List list = arrayList.get(i);
            if (list == null || list.size() <=0){
                arrayList.remove(i);
                i--;
            }
        }
    }

    public void refreshUserModel(){
        userModel = getUserModel();
    }

    public void refresh(){
        LogUtil.d(TAG,"refresh");
        layoutBinding.refreshLayout.setRefreshing(true);
        getMessageFromNet();
    }

    /**
     * @description 获取新消息后，将其与原先的数据进行比较分析，再展示
     * 先读取本地已读和未读消息，已读与新消息合并得到最终已读消息，未读与消息合并得到最终未读消息
     * @param messageModels
     */
    private void analyzeMessage(List<List<MessageModel>> messageModels){
        String unreadSate;
        String readSate;
        if (getHostState()){
            unreadSate = CommonConstant.HOST_UNREAD_MESSAGE;
            readSate = CommonConstant.HOST_READ_MESSAGE;
        }else {
            unreadSate = CommonConstant.OTHER_UNREAD_MESSAGE;
            readSate = CommonConstant.OTHER_READ_MESSAGE;
        }

        unReadMessageList.clear();
        readMessageList.clear();
        List<List<MessageModel>> read = new ArrayList<>();
        getMessageList(unReadMessageList,read);
        mergeReadMessage(messageModels,read);
        mergeUnReadMessageList(messageModels);
        adapter.notifyData();
        saveMessage(unReadMessageList,unreadSate);
        saveMessage(readMessageList,readSate);
        saveMessageAccounts();
    }

    /**
     * @description 账号过滤，拿到朋友的账号，因为消息包含了本人发送的消息
     * @param model
     * @return String
     */
    private String accountFilter(MessageModel model){
        String account = "";
        if (model.getType() == MessageType.MESSAGE_SEND.getCode()){
            account = model.getReceiverAccount();
        }else if (model.getType() == MessageType.MESSAGE_RECEIVE.getCode()){
            account = model.getSenderAccount();
        }
        return account;
    }

    /**
     * @description 永久删除消息
     * @param position
     */
    private void messageDelete(final int position){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getView().getActivity());
        dialog.setTitle("温馨提示：");
        dialog.setMessage("删除信息将无法恢复");
        dialog.setCancelable(true);
        dialog.setPositiveButton("继续删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMessage(position);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    /**
     * @description 删除好友的消息记录，包括已读和未读
     * @param position 消息列表的位置，根据位置获取指定好友的信息
     */
    private void deleteMessage(int position){
        List<MessageModel> messageModels = null;
        String path = null;
        String account = null;

        if (position <= (unReadMessageList.size()-1)) {
            messageModels = unReadMessageList.get(position);
            unReadMessageList.remove(messageModels);
            account = messageModels.get(0).getSenderAccount();
        }else {
            messageModels = readMessageList.get(position - unReadMessageList.size());
            readMessageList.remove(messageModels);
            account = accountFilter(messageModels.get(0));
        }

        String unreadSate;
        String readSate;
        if (getHostState()){
            unreadSate = CommonConstant.HOST_UNREAD_MESSAGE;
            readSate = CommonConstant.HOST_READ_MESSAGE;
        }else {
            unreadSate = CommonConstant.OTHER_UNREAD_MESSAGE;
            readSate = CommonConstant.OTHER_READ_MESSAGE;
        }

        path = FileUtil.getDiskPath(getView().getActivity(),unreadSate);
        File file = new File(path,account);
        FileUtil.fileDelete(file);

        path = FileUtil.getDiskPath(getView().getActivity(),readSate);
        file = new File(path,account);
        FileUtil.fileDelete(file);

        saveMessageAccounts();
        adapter.notifyData();
    }
    /**
     * @description 跳转到聊天Activity,如果点击的是未读消息则清空，
     * 这时在聊天Activity退出的时候会把所有聊天信息存为已读
     * 如果点击的是已读消息则不作处理，同样在聊天Activity退出的时候会把所有聊天信息存为已读
     * @param position 消息列表的位置，根据位置获取指定好友的信息
     */
    private void jumpToChatting(int position){
        List<MessageModel> messages;
        UserModel friendModel = null;
        String unreadSate;
        if (getHostState()){
            unreadSate = CommonConstant.HOST_UNREAD_MESSAGE;
        }else {
            unreadSate = CommonConstant.OTHER_UNREAD_MESSAGE;
        }
        if (position <= (unReadMessageList.size()-1)) {
            messages = unReadMessageList.get(position);
            String account = messages.get(0).getSenderAccount();
            friendModel = getUserModelFromDatabase(account);
            clearUnreadMessage(messages.get(0).getSenderAccount(),unreadSate);
        }else {
            messages = readMessageList.get(position - unReadMessageList.size());
            if (messages.get(0).getType() == MessageType.MESSAGE_SEND.getCode()){
                String account = messages.get(0).getReceiverAccount();
                friendModel = getUserModelFromDatabase(account);
            }else if (messages.get(0).getType() == MessageType.MESSAGE_RECEIVE.getCode()){
                String account = messages.get(0).getSenderAccount();
                friendModel = getUserModelFromDatabase(account);
            }
            messages = null;
        }

        Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
        intent.putExtra("friendModel",friendModel);
        intent.putParcelableArrayListExtra("message", (ArrayList<? extends Parcelable>) messages);
        getView().startActivity(intent);
    }

    public UserModel getUserModelFromDatabase(String account){
        List userModel;
        if (getHostState()){
            userModel = LitePal.where("account=?",account).find(UserModel.class);
        }else {
            userModel = LitePal.where("account=?",account).find(FriendModel.class);
        }
        if (userModel == null || userModel.size()<=0)return null;
        return (UserModel) userModel.get(0);
    }

    public MessageFragmentLayoutBinding getLayoutBinding() {
        return layoutBinding;
    }

    public void setLayoutBinding(MessageFragmentLayoutBinding layoutBinding) {
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
