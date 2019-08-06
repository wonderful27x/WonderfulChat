package com.example.wonderfulchat.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.example.wonderfulchat.adapter.ChattingListAdapter;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.CommonConstant;
import com.example.wonderfulchat.model.InternetAddress;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.FileUtil;
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

    public void initView(List<MessageModel> message,String friendAccount){
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

        List<MessageModel> unReadMessage = message;
        List<MessageModel> newMessage = getMessageFromNet(friendAccount);
        List<MessageModel> readMessage = getReadMessage(friendAccount);
        mergeMessage(readMessage,unReadMessage,newMessage);

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
    }

    private List<MessageModel> getMessageFromNet(String account){
        List<MessageModel> messages = new ArrayList<>();
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

        return messages;
    }

    private List<MessageModel> getReadMessage(String account){
        List<MessageModel> readMessage;
        String path = FileUtil.getDiskPath(getView(),"ReadedMessage");
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

        MessageModel model = buildMessage(MessageType.MESSAGE_SEND.getCode(),time,message,userModel.getNickname(),userModel.getAccount(),"",friendAccount,"");
        Message sendMessage = messageSendHandler.obtainMessage();
        sendMessage.what = MessageType.MESSAGE_SEND.getCode();
        sendMessage.obj = model;
        sendMessage.sendToTarget();

        messageModels.add(model);
        adapter.notifyItemInserted(messageModels.size());
        binding.recyclerView.scrollToPosition(messageModels.size()-1);
        binding.messageContent.setText("");
    }

    public void messageSave(){
        String path = FileUtil.getDiskPath(getView(),"ReadedMessage");
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
        Message sendMessage = messageSendHandler.obtainMessage();
        sendMessage.what = MessageType.SOCKET_CLOSE.getCode();
        sendMessage.obj = model;
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

    private void sendMessage(MessageModel messageModel){
        String messageData = gson.toJson(messageModel);
        try {
            writer.write(messageData + "\n");
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
                    reader = new BufferedReader(new InputStreamReader(input));
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    startTimes.set(0);

                    while(socketRun && (message = reader.readLine()) != null){
                        if(!socketRun)return;
                        messageModel = gson.fromJson(message,MessageModel.class);
                        switch (MessageType.getByValue(messageModel.getType())){
                            case ANSWER:
                                if (CommonConstant.IDENTITY_REQUEST.equals(messageModel.getMessage())){
                                    sendMessage(MessageType.ANSWER,"client","server",userModel.getAccount());
                                }else if (CommonConstant.ACCEPT.equals(messageModel.getMessage())){
                                    sendMessage(MessageType.SOCKET_KEY,"client","server",userModel.getAccount() + "$" + friendAccount);
                                    Message message = handler.obtainMessage();
                                    message.what = 0;
                                    message.sendToTarget();
                                }else if (CommonConstant.REFUSE.equals(messageModel.getMessage())){
                                    ToastUtil.showToast("未知的身份，请求被拒绝！");
                                    LogUtil.d(TAG,CommonConstant.REFUSE);
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
                    sendMessage((MessageModel) message.obj);
                    break;
                case SOCKET_CLOSE:
                    sendMessage((MessageModel) message.obj);
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
            }
            return true;
        }
    }

    @Override
    public void deTachView() {
        super.deTachView();
        if (binding != null){
            binding = null;
        }
    }
}
