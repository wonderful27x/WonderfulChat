package com.example.wonderfulchat.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.example.wonderfulchat.adapter.ChattingListAdapter;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.interfaces.HttpCallbackListener;
import com.example.wonderfulchat.model.CommonConstant;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChattingViewModel extends BaseViewModel<AppCompatActivity> {

    private static final String TAG = "ChattingViewModel";

    private ActivityChattingBinding binding;
    private ChattingListAdapter adapter;
    private List<MessageModel> messageModels;
    private String friendAccount;
    private UserModel userModel;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private AtomicInteger startTimes;
    private boolean socketRun = true;
    private Gson gson;
    private Handler handler;
    private Handler messageSendHandler;

    public void initView(List<MessageModel> unReadMessage,String friendAccount){
        this.friendAccount = friendAccount;
        messageModels = new ArrayList<>();
        startTimes = new AtomicInteger(3);
        gson = new Gson();

        binding.messageSend.setEnabled(false);
        String userString = MemoryUtil.sharedPreferencesGetString("UserModel");
        Gson gson = new Gson();
        userModel = gson.fromJson(userString,UserModel.class);

        Looper looper = Looper.getMainLooper();
        MessageCallback callback = new MessageCallback();
        handler = new Handler(looper,callback);

        SocketRunnable runnable = new SocketRunnable();
        Thread receiveThread = new Thread(runnable);
        receiveThread.start();

        MessageSendRunnable messageSendRunnable = new MessageSendRunnable();
        Thread sendThread = new Thread(messageSendRunnable);
        sendThread.start();

        List<MessageModel> readMessage = getReadMessage(friendAccount);
        mergeMessage(readMessage,unReadMessage,null);

//        List<String> messageList = new ArrayList<>();
//        messageList.add("接哦结果就哦我");
//        for (int i=0; i<20; i++){
//            MessageModel model= new MessageModel();
//            model.setMessage(messageList);
//            model.setSenderImage("http://192.168.191.4:8080/file/girl.jpg");
//            if (i%3 == 0){
//                model.setType(MessageModel.TYPE_RECEIVE);
//            }else {
//                model.setType(MessageModel.TYPE_SEND);
//            }
//            messageModels.add(model);
//        }

        adapter = new ChattingListAdapter(messageModels,this);
        LinearLayoutManager manager = new LinearLayoutManager(getView());
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.scrollToPosition(messageModels.size()-1);

        getMessageFromNet();
    }

    //进入是再请求一次网络，拿到最新消息
    private void getMessageFromNet(){
//        List<MessageModel> messages = new ArrayList<>();
//        String content = "经费世界公司哦手机覅韩国就送大奖哦挤公交感觉颇为烦恼";
//        for (int i=0; i<1; i++){
//            MessageModel message = new MessageModel();
//            message.setMessage(content+i);
//            message.setSender(account);
//            message.setSenderAccount("wonderful"+i);
//            message.setSenderImage("http://172.16.169.97:8080/file/girl.jpg");
//            message.setReceiver("机构鞥我");
//            message.setReceiverAccount("thisismyse");
//            message.setTime("2019-06-12 12:07");
//            message.setType(MessageType.MESSAGE_RECEIVE.getCode());
//            messages.add(message);
//        }

        String url = InternetAddress.GET_NEWEST_MESSAGE_URL + "?account=" + userModel.getAccount() + "&friendAccount=" + friendAccount;
        HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        HttpMessageModel httpMessageModel = gson.fromJson(response, HttpMessageModel.class);
                        if(httpMessageModel == null)return;
                        if("success".equals(httpMessageModel.getResult())){
                            if(httpMessageModel.getContent() != null && httpMessageModel.getContent().size()>0){
                                List<MessageModel> messageList = httpMessageModel.getContent().get(0);
                                mergeMessage(null,messageList,null);
                                adapter.notifyDataSetChanged();
                                binding.recyclerView.scrollToPosition(messageModels.size()-1);
                            }
                        }else{
                            LogUtil.e(TAG,"消息获取失败：" + httpMessageModel.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.e(TAG,"消息获取失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    //获取已读消息，因为传递进来的只是未读消息
    private List<MessageModel> getReadMessage(String account){
        List<MessageModel> readMessage;
        String path = FileUtil.getDiskPath(getView(),"ReadMessage");
        File file = new File(path,account);
        if (!file.exists()){
            return null;
        }
        String jsonData = FileUtil.fileRead(file);
        if ("".equals(jsonData)){
            return null;
        }
        readMessage = gson.fromJson(jsonData,new TypeToken<List<MessageModel>>(){}.getType());
        return readMessage;
    }

    private void mergeMessage(List<MessageModel> readMessage,List<MessageModel> unReadMessage,List<MessageModel> newMessage){
        if (readMessage != null && readMessage.size()>0){
            messageModels.addAll(readMessage);
        }
        if (unReadMessage != null && unReadMessage.size()>0){
            messageModels.addAll(unReadMessage);
        }
        if (newMessage != null && newMessage.size()>0){
            messageModels.addAll(newMessage);
        }
    }

    public void sendMessage(View view){
        String message = binding.messageContent.getText().toString();
        if (message.isEmpty())return;
//        MessageModel model = new MessageModel();
//        model.setType(MessageType.MESSAGE_SEND.getCode());
//        model.setMessage(message);

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = format.format(date);

        MessageModel model = buildMessage(MessageType.MESSAGE_RECEIVE.getCode(),time,message,userModel.getNickname(),userModel.getAccount(),"",friendAccount,userModel.getImageUrl());

        String messageData = gson.toJson(model);
        Message sendMessage = messageSendHandler.obtainMessage();
        sendMessage.what = MessageType.MESSAGE_SEND.getCode();
        sendMessage.obj = messageData;
        sendMessage.sendToTarget();

        model.setType(MessageType.MESSAGE_SEND.getCode());
        messageModels.add(model);
        adapter.notifyItemInserted(messageModels.size());
        binding.recyclerView.scrollToPosition(messageModels.size()-1);
        binding.messageContent.setText("");
    }

    public void messageSave(){
        if(messageModels == null || messageModels.size()<=0)return;
        String path = FileUtil.getDiskPath(getView(),"ReadMessage");
        Gson gson = new Gson();
        File file = new File(path, friendAccount);
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

//    //清空好友消息,将此好友的未读消息清空
//    public void clearUnreadMessage(){
//        String path = FileUtil.getDiskPath(getView(),"UnReadMessage");
//        File file = new File(path, friendAccount);
//        if (!file.exists()){
//            return;
//        }else {
//            FileUtil.fileClear(file);
//        }
//    }

    //拿到有消息记录的所有账号
    private String[] getOldMessageAccounts(){
        String s = MemoryUtil.sharedPreferencesGetString("OldMessageAccounts");
        if (s.equals("")){
            return null;
        }
        String[] accounts = s.split(",");
        return accounts;
    }

    //将此好友账号添加到记录账号里，否则消息列表在某些特殊情况下将无法展示已读消息
    public void saveMessageAccounts() {
        String[] accountAll;
        accountAll = getOldMessageAccounts();
        if (accountAll == null){
            MemoryUtil.sharedPreferencesSaveString("OldMessageAccounts", friendAccount);
            return;
        }
        for (String account : accountAll) {
            if (friendAccount.equals(account)) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String account : accountAll) {
            builder.append(account);
            builder.append(",");
        }
        builder.append(friendAccount);
        MemoryUtil.sharedPreferencesSaveString("OldMessageAccounts", builder.toString());
    }

    public void setBinding(ActivityChattingBinding binding){
        this.binding = binding;
    }

    private void stopSocket(){
        socketRun = false;
        String message = "Bye !";
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = format.format(date);

        MessageModel model = buildMessage(MessageType.SOCKET_CLOSE.getCode(),time,message,"client","client","server","server","");
        String messageData = gson.toJson(model);
        Message sendMessage = messageSendHandler.obtainMessage();
        sendMessage.what = MessageType.SOCKET_CLOSE.getCode();
        sendMessage.obj = messageData;
        sendMessage.sendToTarget();
    }

    private void stopLoop(){
        messageSendHandler.getLooper().quit();
    }

    public void exit(){
        stopSocket();
    }

    private MessageModel sendMessage(MessageType type,String sender,String receiver,String message){
        MessageModel messageModel = new MessageModel();
        messageModel.setType(type.getCode());
        messageModel.setSender(sender);
        messageModel.setReceiver(receiver);
        messageModel.setMessage(message);
        String messageData = gson.toJson(messageModel);
        try {
            writer.write(messageData + "\n");
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ChattingViewModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return messageModel;
    }

//    private MessageModel buildMessage(MessageType type,String sender,String receiver,String message){
//        MessageModel messageModel = new MessageModel();
//        messageModel.setType(type.getCode());
//        messageModel.setSenderAccount(sender);
//        messageModel.setReceiverAccount(receiver);
//        messageModel.setMessage(message);
//
//        return messageModel;
//    }

    private MessageModel buildMessage(int Type,String time,String message,String sender,String senderAccount,String receiver,String receiverAccount,String senderImage){

        MessageModel messageModel = new MessageModel();
        messageModel.setType(Type);
        messageModel.setTime(time);
        messageModel.setMessage(message);
        messageModel.setSender(sender);
        messageModel.setSenderAccount(senderAccount);
        messageModel.setReceiver(receiver);
        messageModel.setReceiverAccount(receiverAccount);
        messageModel.setSenderImage(senderImage);

        return messageModel;
    }

    private void sendMessage(String message){
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ChattingViewModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class SocketRunnable implements Runnable{
        String message;
        MessageModel messageModel;

        @Override
        public void run() {
            while (socketRun && startTimes.get() >0){
                try {
                    socket = new Socket(InternetAddress.HOST_IP,8888);
                    InputStream input = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();
                    reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                    writer = new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
                    startTimes.set(0);

                    while(socketRun && (message = reader.readLine()) != null){
                        if(!socketRun)return;
                        messageModel = gson.fromJson(message,MessageModel.class);
                        switch (MessageType.getByValue(messageModel.getType())){
                            case ANSWER:
                                if (CommonConstant.IDENTITY_REQUEST.equals(messageModel.getMessage())){
                                    sendMessage(MessageType.ANSWER,"client","server",userModel.getAccount() + "$" + friendAccount);
                                }else if (CommonConstant.ACCEPT.equals(messageModel.getMessage())){
                                    sendMessage(MessageType.SOCKET_KEY,"client","server",userModel.getAccount() + "$" + friendAccount);
                                    Message message = handler.obtainMessage();
                                    message.what = 0;
                                    message.sendToTarget();
                                }else if (CommonConstant.REFUSE.equals(messageModel.getMessage())){
                                    Message message = handler.obtainMessage();
                                    message.what = 2;
                                    message.sendToTarget();
                                }else if(CommonConstant.REFUSE_FRIEND.equals(messageModel.getMessage())){
                                    Message message = handler.obtainMessage();
                                    message.what = 3;
                                    message.sendToTarget();
                                }
                                break;
                            case MESSAGE_RECEIVE:
                                Message message = handler.obtainMessage();
                                message.what = 1;
                                message.obj = messageModel;
                                message.sendToTarget();
                                break;
                            case ERROR:
                                LogUtil.e(TAG,messageModel.getMessage());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (IOException e) {
                    startTimes.getAndDecrement();
                    e.printStackTrace();
                }finally {
                    try {
                        if (reader != null){
                            reader.close();
                        }
                        if (writer != null){
                            writer.close();
                        }
                        if (socket != null){
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class MessageSendRunnable implements Runnable{

        @Override
        public void run() {
            Looper.prepare();
            messageSendHandler = new Handler(new MessageSendCallback());
            Looper.loop();
        }
    }

    class MessageSendCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message message) {
            switch (MessageType.getByValue(message.what)){
                case MESSAGE_SEND:
                    sendMessage((String)message.obj);
                    break;
                case SOCKET_CLOSE:
                    sendMessage((String) message.obj);
                    messageSendHandler.getLooper().quit();
                    break;
            }
            return true;
        }
    }

    class MessageCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case 0:
                    binding.messageSend.setEnabled(true);
                    break;
                case 1:
                    messageModels.add((MessageModel) message.obj);
                    adapter.notifyItemInserted(messageModels.size());
                    binding.recyclerView.scrollToPosition(messageModels.size()-1);
                    break;
                case 2:
                    ToastUtil.showToast("未知的身份，请求被拒绝！");
                    LogUtil.d(TAG,CommonConstant.REFUSE);
                    break;
                case 3:
                    ToastUtil.showToast("对方未添加好友，请求被拒绝！");
                    LogUtil.d(TAG,CommonConstant.REFUSE_FRIEND);
                    break;
            }
            return true;
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        if (binding != null){
            binding = null;
        }
    }
}
