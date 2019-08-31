package com.example.wonderfulchat.viewmodel;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import com.example.wonderfulchat.R;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author wonderful
 * @Description 重要的即时聊天实现，这里没有使用服务，说实话感觉没有必要，
 * 从某些方面来说又感觉有必要，这里留给大家思考吧
 * @Date 2019-8-30
 */
public class ChattingViewModel extends BaseViewModel<AppCompatActivity> {

    private static final String TAG = "ChattingViewModel";

    private ActivityChattingBinding binding;
    private ChattingListAdapter adapter;
    private List<MessageModel> messageModels;
    private UserModel friendModel;
    private UserModel userModel;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private AtomicInteger startTimes;
    private boolean socketRun;
    private Gson gson;
    private Handler handler;
    private Handler messageSendHandler;
    private Thread receiveThread;
    private Thread sendThread;
    private Thread messageThread;

    public void initView(List<MessageModel> unReadMessage,UserModel friendModel){
        this.friendModel = friendModel;

        messageModels = new ArrayList<>();
        gson = new Gson();
        startTimes = new AtomicInteger(3);//三次重连机会（中途断开无效）
        socketRun = true;

        binding.messageSend.setEnabled(false);
        binding.messageSend.setTextColor(ContextCompat.getColor(getView(),R.color.gray));

        String userString;
        if (getHostState()){
            userString = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_USER_MODEL);
        }else {
            userString = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_USER_MODEL);
        }

        Gson gson = new Gson();
        userModel = gson.fromJson(userString,UserModel.class);

        Looper looper = Looper.getMainLooper();
        MessageCallback callback = new MessageCallback();
        handler = new Handler(looper,callback);

        SocketRunnable runnable = new SocketRunnable();
        receiveThread = new Thread(runnable);
        receiveThread.start();

        MessageSendRunnable messageSendRunnable = new MessageSendRunnable();
        sendThread = new Thread(messageSendRunnable);
        sendThread.start();

        List<MessageModel> readMessage = getReadMessage(friendModel.getAccount());
        mergeMessage(readMessage,unReadMessage,null);

        adapter = new ChattingListAdapter(userModel,friendModel,messageModels,this);
        LinearLayoutManager manager = new LinearLayoutManager(getView());
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.scrollToPosition(messageModels.size()-1);

        getMessageFromNet();
    }

    private boolean getHostState(){
        return MemoryUtil.sharedPreferencesGetBoolean(CommonConstant.HOST_STATE);
    }

    /**
     * @description 进入时再请求一次网络，拿到最新消息，并且与上一次已读消息合并，再展示
     */
    private void getMessageFromNet(){
        String url = InternetAddress.GET_NEWEST_MESSAGE_URL + "?account=" + userModel.getAccount() + "&friendAccount=" + friendModel.getAccount();
        messageThread = HttpUtil.httpRequestForGet(url, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if (getView() == null)return;
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
                if (getView() == null)return;
                getView().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.e(TAG,"消息获取失败：" + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * @description 获取已读消息，因为传递进来的只是未读消息
     * @param account
     * @return List<MessageModel>
     */
    private List<MessageModel> getReadMessage(String account){
        if (account == null)return null;
        String name;
        if (getHostState()){
            name = CommonConstant.HOST_READ_MESSAGE;
        }else {
            name = CommonConstant.OTHER_READ_MESSAGE;
        }
        List<MessageModel> readMessage;
        String path = FileUtil.getDiskPath(getView(),name);
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

    /**
     * @description 获取已读消息，因为传递进来的只是未读消息
     * @param readMessage,unReadMessage,newMessage
     */
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

    /**
     * @description 发送消息，这里需要切换到子线程中，为了简化服务端的处理，
     * 发出的消息类型为MESSAGE_RECEIVE，而自己显示的为MESSAGE_SEND
     * @param view
     */
    public void sendMessage(View view){
        String message = binding.messageContent.getText().toString();
        if (message.isEmpty())return;

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);

        MessageModel model = buildMessage(MessageType.MESSAGE_RECEIVE.getCode(),time,message,userModel.getNickname(),userModel.getAccount(),friendModel.getNickname(),friendModel.getAccount(),"");

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

    /**
     * @description 消息存入文件系统，当Activity退出时将所有消息存入已读
     */
    public void messageSave(){
        if (friendModel == null || friendModel.getAccount() == null)return;
        if(messageModels == null || messageModels.size()<=0)return;
        String name;
        if (getHostState()){
            name = CommonConstant.HOST_READ_MESSAGE;
        }else {
            name = CommonConstant.OTHER_READ_MESSAGE;
        }
        String path = FileUtil.getDiskPath(getView(),name);
        Gson gson = new Gson();
        File file = new File(path, friendModel.getAccount());
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

    /**
     * @description 拿到有消息记录的所有账号
     * @return String[]
     */
    private String[] getOldMessageAccounts(){
        String s;
        if (getHostState()){
            s = MemoryUtil.sharedPreferencesGetString(CommonConstant.HOST_MESSAGE_ACCOUNT);
        }else {
            s = MemoryUtil.sharedPreferencesGetString(CommonConstant.OTHER_MESSAGE_ACCOUNT);
        }
        if (s.equals("")){
            return null;
        }
        String[] accounts = s.split(",");
        return accounts;
    }

    /**
     * @description 将此好友账号添加到记录账号里，否则消息列表在某些特殊情况下将无法展示已读消息
     * 表现为第一次主动与好友聊天时，未通过消息列表点击进入
     */
    public void saveMessageAccounts() {
        if (friendModel == null)return;
        String[] accountAll;
        accountAll = getOldMessageAccounts();
        String key;
        if (getHostState()){
            key = CommonConstant.HOST_MESSAGE_ACCOUNT;
        }else {
            key = CommonConstant.OTHER_MESSAGE_ACCOUNT;
        }
        if (accountAll == null){
            MemoryUtil.sharedPreferencesSaveString(key, friendModel.getAccount());
            return;
        }
        for (String account : accountAll) {
            if (account.equals(friendModel.getAccount())) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String account : accountAll) {
            builder.append(account);
            builder.append(",");
        }
        builder.append(friendModel.getAccount());
        MemoryUtil.sharedPreferencesSaveString(key, builder.toString());
    }

    public void setBinding(ActivityChattingBinding binding){
        this.binding = binding;
    }

    /**
     * @description 重要的连接释放方法，因为在线程中使用了带while循环的阻塞式读取，
     * 因此没有有效的方式来停止Socket和线程，只有向服务器发送请求，当服务器调用close时，
     * 客户端的socket才能响应到null,以此来退出线程并释放资源，但是这仍是一种不可靠的方式，
     * 在后期我们将考虑自己写一套通信协议，采用非阻塞式读取方式。
     */
    private void stopSocket(){
        socketRun = false;
        String message = "Bye !";
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        }catch (NullPointerException ex){
            Logger.getLogger(ChattingViewModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return messageModel;
    }

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
        }catch (NullPointerException ex){
            Logger.getLogger(ChattingViewModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @description 核心线程之socket通信，开始聊天前进行了简单的身份验证
     */
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
                                    sendMessage(MessageType.ANSWER,"client","server",userModel.getAccount() + "$" + friendModel.getAccount());
                                }else if (CommonConstant.ACCEPT.equals(messageModel.getMessage())){
                                    sendMessage(MessageType.SOCKET_KEY,"client","server",userModel.getAccount() + "$" + friendModel.getAccount());
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

    /**
     * @description 消息发送线程
     */
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

    /**
     * @description 切换至UI线程
     */
    class MessageCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case 0:
                    setButtonSelector(binding.messageSend);
                    binding.messageSend.setEnabled(true);
                    break;
                case 1:
                    messageModels.add((MessageModel) message.obj);
                    adapter.notifyItemInserted(messageModels.size());
                    binding.recyclerView.scrollToPosition(messageModels.size()-1);
                    break;
                case 2:
                    ToastUtil.showLongToast("未知的身份，请求被拒绝！");
                    LogUtil.d(TAG,CommonConstant.REFUSE);
                    break;
                case 3:
                    ToastUtil.showLongToast("对方尚未同意你的好友申请或已将你删除！");
                    LogUtil.d(TAG,CommonConstant.REFUSE_FRIEND);
                    break;
            }
            return true;
        }
    }

    /**
     * @description 设置状态选择器
     */
    public void setButtonSelector(Button button){
        int[] colors = {Color.parseColor("#FFFFFF"), Color.parseColor("#000000")};
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_pressed};
        states[1] = new int[]{};
        ColorStateList colorStateList = new ColorStateList(states,colors);
        button.setTextColor(colorStateList);
    }

    /**
     * @description 强行杀掉线程，释放资源,这种方法已过时，并且会抛出异常，而且并没有什么卵用，
     * 试图用这种方式来结束阻塞的socket但是失败了
     */
    public void threadKill(){
        try{
            if (receiveThread != null){
                receiveThread.stop();
            }
            if (sendThread != null){
                sendThread.stop();
            }
            if (messageThread != null){
                messageThread.stop();
            }
        }catch (UnsupportedOperationException e){
            e.printStackTrace();
        }finally {
            receiveThread = null;
            sendThread = null;
            messageThread = null;
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
