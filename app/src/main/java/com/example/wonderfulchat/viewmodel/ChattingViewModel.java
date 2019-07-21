package com.example.wonderfulchat.viewmodel;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.example.wonderfulchat.adapter.ChattingListAdapter;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.HttpMessageModel;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.utils.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChattingViewModel extends BaseViewModel<AppCompatActivity> {

    private ActivityChattingBinding binding;
    private ChattingListAdapter adapter;
    private List<MessageModel> messageModels;
    private String friendAccount;

    public void initView(List<MessageModel> message,String friendAccount){
        this.friendAccount = friendAccount;
        messageModels = new ArrayList<>();
        List<MessageModel> unReadMessage = message;
        List<MessageModel> newMessage = getMessageFromNet(friendAccount);
        List<MessageModel> readMessage = getReadMessage(friendAccount);
        mergeMessage(readMessage,unReadMessage,newMessage);

        List<String> messageList = new ArrayList<>();
        messageList.add("接哦结果就哦我");
        for (int i=0; i<20; i++){
//            MessageModel model= new MessageModel();
//            model.setMessage(messageList);
//            model.setSenderImage("http://192.168.191.4:8080/file/girl.jpg");
//            if (i%3 == 0){
//                model.setType(MessageModel.TYPE_RECEIVE);
//            }else {
//                model.setType(MessageModel.TYPE_SEND);
//            }
//            messageModels.add(model);
        }

        adapter = new ChattingListAdapter(messageModels,this);
        LinearLayoutManager manager = new LinearLayoutManager(getView());
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.scrollToPosition(messageModels.size()-1);
    }

    private List<MessageModel> getMessageFromNet(String account){
        List<MessageModel> messages = new ArrayList<>();
        String content = "经费世界公司哦手机覅韩国就送大奖哦挤公交感觉颇为烦恼";
        for (int i=0; i<2; i++){
            MessageModel message = new MessageModel();
            message.setMessage(content+i);
            message.setSender(account);
            message.setSenderAccount("wonderful"+i);
            message.setSenderImage("http://192.168.31.32:8080/file/girl.jpg");
            message.setReceiver("机构鞥我");
            message.setReceiverAccount("thisismyse");
            message.setTime("2019-06-12 12:07");
            message.setType(MessageModel.TYPE_RECEIVE);
            messages.add(message);
        }

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
        Gson gson = new Gson();
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
        MessageModel model = new MessageModel();
        model.setType(MessageModel.TYPE_SEND);
        model.setMessage(message);
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
}
