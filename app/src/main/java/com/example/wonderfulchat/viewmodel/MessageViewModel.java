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
import com.example.wonderfulchat.model.HttpMessageModel;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageViewModel extends BaseViewModel <Fragment> {

    private static final String TAG = "MessageViewModel";
    private MessageFragmentLayoutBinding layoutBinding;
    private List<List<MessageModel>> unReadMessageList;
    private List<List<MessageModel>> readedMessageList;
    private MessageListAdapter adapter;
    private UserModel userModel;

    public void initView(){
        String modelString = MemoryUtil.sharedPreferencesGetString("UserModel");
        Gson gson = new Gson();
        userModel = gson.fromJson(modelString, UserModel.class);

        unReadMessageList = new ArrayList<>();
        readedMessageList = new ArrayList<>();
        List<List<MessageModel>> unRead = new ArrayList<>();
        List<List<MessageModel>> readed = new ArrayList<>();
        getMessageList(unRead,readed);
        mergeReadedMessage(unReadMessageList,readed);
        mergeUnReadMessageList(unRead);
        saveMessage(unReadMessageList,"UnReadMessage");
        saveMessage(readedMessageList,"ReadedMessage");
        saveMessageAccounts();
        adapter = new MessageListAdapter(layoutBinding.getWonderfulViewModel(),unReadMessageList,readedMessageList);
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

        getMessageFromNet();
    }

    //将消息存入文件系统，分为已读和未读两大文件，每个文件中每个好友账号都存入独立的消息列表Json数据
    private void saveMessage(List<List<MessageModel>> messageList,String messageState){
        String path = FileUtil.getDiskPath(getView().getActivity(),messageState);
        Gson gson = new Gson();
        for (List<MessageModel> messageModels:messageList){
            File file = new File(path, messageModels.get(0).getSenderAccount());
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

    //清空好友消息
    public void clearUnreadMessage(String name,String messageState){
        String path = FileUtil.getDiskPath(getView().getActivity(),messageState);
        File file = new File(path, name);
        if (!file.exists()){
            return;
        }else {
            FileUtil.fileClear(file);
        }
    }

    private void getMessageFromNet(){
//        List<MessageModel> messages = new ArrayList<>();
//        List<List<MessageModel>> messageList = new ArrayList<>();
//        String content = "经费世界公司哦手机覅韩国就送大奖哦挤公交感觉颇为烦恼";
//        for (int i=0; i<5; i++){
//            MessageModel message = new MessageModel();
//            message.setMessage(content);
//            message.setSender("黄世豪"+i);
//            message.setSenderAccount("wonderful"+i);
//            message.setSenderImage("http://172.16.169.97:8080/file/girl.jpg");
//            message.setReceiver("机构鞥我");
//            message.setReceiverAccount("thisismyse");
//            message.setTime("2019-06-12 12:07");
//            message.setType(MessageModel.TYPE_RECEIVE);
//            messages.add(message);
//        }
//
//        for (MessageModel model:messages){
//            List<MessageModel> models = new ArrayList<>();
//            for (int i=0; i<5; i++){
//                models.add(model);
//            }
//            messageList.add(models);
//        }
//
//        HttpMessageModel messageModel = new HttpMessageModel();
//        messageModel.setResult("success");
//        messageModel.setContent(messageList);
//        Gson gson = new Gson();
//        String jsonData = gson.toJson(messageModel);
//        LogUtil.d(TAG,jsonData);

//        HttpMessageModel messageModel;
//        Gson gson = new Gson();
//        String jsonData = FileUtil.getJson(getView().getActivity(),"HttpMessageList");
//        messageModel = gson.fromJson(jsonData,HttpMessageModel.class);
//        return messageModel.getContent();

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

    //拿到有消息记录的所有账号
    private String[] getOldMessageAccounts(){
        String s = MemoryUtil.sharedPreferencesGetString("OldMessageAccounts");
        if (s.equals("")){
            return null;
        }
        String[] accounts = s.split(",");
        return accounts;
    }

    //将有消息存入的所有账号记录下来
    private void saveMessageAccounts(){
        String accountAll = "";
        StringBuilder account = new StringBuilder();
        for (List<MessageModel> unRead:unReadMessageList){
            account.append(unRead.get(0).getSenderAccount());
            account.append(",");
        }
        for (List<MessageModel> readed:readedMessageList){
            account.append(readed.get(0).getSenderAccount());
            account.append(",");
        }
        if (account.length()>0){
            account.deleteCharAt(account.length()-1);
            accountAll = account.toString();
        }
        MemoryUtil.sharedPreferencesSaveString("OldMessageAccounts",accountAll);
    }

    //读取好友消息
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

//    private void getMessageList(){
//        String [] messageAccounts = getOldMessageAccounts();
//        int size = unReadMessageList.size();
//        boolean same = false;
//        for (String account:messageAccounts){
//            same = false;
//            for (int i=0; i<size; i++){
//                List<MessageModel> models = unReadMessageList.get(i);
//                if (models.get(0).getSenderAccount().equals(account)){
//                    same = true;
//                    models.addAll(unReadMessageList.size(),getMessageListFromPhone("UnReadMessage",messageAccounts[i]));
//                    break;
//                }
//            }
//            if (!same){
//
//            }
//        }
//    }

    //读取未读消息和已读消息（二者互斥，即同一个好友的消息状态只能是未读或已读），
    // 注意这是指获取的消息我们在代码中控制令其唯一，但是在已读和未读文件系统中有可能有相同好友的数据，即有未读和已读的消息。
    private void getMessageList(List<List<MessageModel>> unRead,List<List<MessageModel>> readed){
        String [] messageAccounts = getOldMessageAccounts();
        if (messageAccounts == null)return;
        for (String account:messageAccounts){
            List<MessageModel> messageModels = getMessageListFromPhone("UnReadMessage",account);
            //从"UnReadMessage"文件中读到了说明是未读消息
            if (messageModels != null){
                unRead.add(messageModels);
            }
            //否则从"ReadedMessage"文件中读取
            else {
                messageModels = getMessageListFromPhone("ReadedMessage",account);
                if (messageModels != null){
                    readed.add(messageModels);
                }
            }

        }
    }

//    private void mergeReadedMessage(List<List<MessageModel>> unReaded,List<List<MessageModel>> readed){
//        for (List<MessageModel> messageModels:unReaded){
//            for (int i=0; i<readed.size(); i++){
//                List<MessageModel> readedList = readed.get(i);
//                if (messageModels.get(0).getSenderAccount().equals(readedList.get(0).getSenderAccount())){
//                    readed.remove(i);
//                    break;
//                }
//            }
//        }
//        readedMessageList.addAll(readed);
//    }

    //将最新的消息（从网络获取，一定是未读状态）和已读消息合并，得出最终的已读消息，即如果已读消息和未读消息是相同的好友则不作处理，
    // 注意虽然没做处理但是这条已读消息并没有被删除，因此在跳转到聊天Activity时仍然能够从"ReadedMessage"文件中读取到。
    private void mergeReadedMessage(List<List<MessageModel>> unReadList,List<List<MessageModel>> readedList){
        boolean theSame = false;
        for (List<MessageModel> readed:readedList){
            theSame = false;
            for (List<MessageModel> unRead:unReadList){
                if (readed.get(0).getSenderAccount().equals(unRead.get(0).getSenderAccount())){
                    theSame = true;
                    break;
                }
            }
            if (!theSame){
                readedMessageList.add(readed);
            }
        }
    }

    //将最新的消息（从网络获取，一定是未读状态）和未读消息合并，得出最终的未读消息，即如果两个未读消息是同一个好友则将消息合并。
    private void mergeUnReadMessageList(List<List<MessageModel>> unRead){
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

    private void removeEmpty(List<List<MessageModel>> arrayList){
        for (int i=0; i<arrayList.size(); i++){
            List list = arrayList.get(i);
            if (list.size() <=0){
                arrayList.remove(i);
                i--;
            }
        }
    }

    public void refresh(){
        getMessageFromNet();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    Thread.sleep(1000);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//                getView().getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        List<List<MessageModel>> messageModels = getMessageFromNet();
//                        List<List<MessageModel>> readed = new ArrayList<>();
//                        readed.addAll(readedMessageList);
//                        readedMessageList.clear();
//                        mergeReadedMessage(messageModels,readed);
//                        mergeUnReadMessageList(messageModels);
//                        adapter.notifyData();
//                        layoutBinding.refreshLayout.setRefreshing(false);
//                        if(messageModels != null && messageModels.size()>0){
//                            saveMessage(unReadMessageList,"UnReadMessage");
//                            saveMessage(readedMessageList,"ReadedMessage");
//                            saveMessageAccounts();
//                        }
//                    }
//                });
//            }
//        }).start();
    }

    private void analyzeMessage(List<List<MessageModel>> messageModels){
        List<List<MessageModel>> readed = new ArrayList<>();
        readed.addAll(readedMessageList);
        readedMessageList.clear();
        mergeReadedMessage(messageModels,readed);
        mergeUnReadMessageList(messageModels);
        adapter.notifyData();
        layoutBinding.refreshLayout.setRefreshing(false);
        if(messageModels != null && messageModels.size()>0){
            saveMessage(unReadMessageList,"UnReadMessage");
            saveMessage(readedMessageList,"ReadedMessage");
            saveMessageAccounts();
        }
    }

//    private void addMessage(List<MessageModel> messageModels){
//        boolean sameSender = false;
//        int oldSize = messages.size();
//        for (MessageModel newMessage:messageModels){
//            sameSender = false;
//            for (int i=0; i<oldSize; i++){
//                MessageModel oldMessage = messages.get(i);
//                if (newMessage.getSenderAccount().equals(oldMessage.getSenderAccount())){
//                    oldMessage.getMessage().addAll(newMessage.getMessage());
//                    sameSender = true;
//                    break;
//                }
//            }
//            if (!sameSender){
//                messages.add(messages.size(),newMessage);
//            }
//        }
//    }

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

    private void deleteMessage(int position){
        List<MessageModel> messageModels = null;
        String path = null;
        String account = null;

        if (position <= (unReadMessageList.size()-1)) {
            messageModels = unReadMessageList.get(position);
            unReadMessageList.remove(messageModels);
            account = messageModels.get(0).getSenderAccount();
        }else {
            messageModels = readedMessageList.get(position - unReadMessageList.size());
            readedMessageList.remove(messageModels);
            account = messageModels.get(0).getSenderAccount();
        }

        path = FileUtil.getDiskPath(getView().getActivity(),"UnReadMessage");
        File file = new File(path,account);
        FileUtil.fileDelete(file);

        path = FileUtil.getDiskPath(getView().getActivity(),"ReadedMessage");
        file = new File(path,account);
        FileUtil.fileDelete(file);

        saveMessageAccounts();
        adapter.notifyData();
    }

    //跳转到聊天Activity,如果点击的是未读消息则清空，这时在聊天Activity退出的时候会把所有聊天信息存为已读
    //如果点击的是已读消息则不作处理，同样在聊天Activity退出的时候会把所有聊天信息存为已读
    private void jumpToChatting(int position){
        List<MessageModel> messages = null;
        String friendName = null;
        String friendAccount = null;
        if (position <= (unReadMessageList.size()-1)) {
            messages = unReadMessageList.get(position);
            friendName = messages.get(0).getSender();
            friendAccount = messages.get(0).getSenderAccount();
            clearUnreadMessage(messages.get(0).getSenderAccount(),"UnReadMessage");
        }else {
            friendName = messages.get(0).getSender();
            friendAccount = messages.get(0).getSenderAccount();
        }
        Intent intent = new Intent(getView().getActivity(), ChattingActivity.class);
        intent.putExtra("friendName",friendName);
        intent.putExtra("friendAccount",friendAccount);
        intent.putParcelableArrayListExtra("message", (ArrayList<? extends Parcelable>) messages);
        getView().startActivity(intent);
    }

    public MessageFragmentLayoutBinding getLayoutBinding() {
        return layoutBinding;
    }

    public void setLayoutBinding(MessageFragmentLayoutBinding layoutBinding) {
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
