package com.example.wonderfulchat.viewmodel;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.example.wonderfulchat.adapter.ChattingListAdapter;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.MessageModel;
import java.util.ArrayList;
import java.util.List;

public class ChattingViewModel extends BaseViewModel<AppCompatActivity> {

    private ActivityChattingBinding binding;
    private ChattingListAdapter adapter;
    private List<MessageModel> messageModels;

    public void initView(List<MessageModel> message){
        messageModels = message;
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


    public void sendMessage(View view){
        String message = binding.messageContent.getText().toString();
        List<String> messageList = new ArrayList<>();
        messageList.add(message);
        if (message.isEmpty()){
            return;
        }
        MessageModel model = new MessageModel();
        model.setType(MessageModel.TYPE_SEND);
//        model.setMessage(messageList);
        messageModels.add(model);
        adapter.notifyItemInserted(messageModels.size());
        binding.recyclerView.scrollToPosition(messageModels.size()-1);
        binding.messageContent.setText("");
    }

    public void setBinding(ActivityChattingBinding binding){
        this.binding = binding;
    }
}
