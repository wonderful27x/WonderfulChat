package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Window;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ChattingListAdapter;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.viewmodel.ChattingViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChattingActivity extends BaseActivity<ChattingViewModel> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityChattingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_chatting);
        binding.setWonderfulViewModel(getViewModel());
        init(binding);
    }

    private void init(ActivityChattingBinding binding){
        List<MessageModel> messageModels = new ArrayList<>();
        for (int i=0; i<20; i++){
            MessageModel messageModel= new MessageModel();
            messageModel.setMessage("接哦结果就哦我");
            if (i%3 == 0){
                messageModel.setType(MessageModel.TYPE_RECEIVE);
            }else {
                messageModel.setType(MessageModel.TYPE_SEND);
            }
            messageModels.add(messageModel);
        }

        ChattingListAdapter adapter = new ChattingListAdapter(messageModels,binding.getWonderfulViewModel());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public ChattingViewModel bindViewModel() {
        return new ChattingViewModel();
    }
}
