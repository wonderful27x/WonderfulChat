package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.MessageListAdapter;
import com.example.wonderfulchat.databinding.MessageFragmentLayoutBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends BaseFragment<MessageViewModel> {

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
         MessageFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.message_fragment_layout, container, false);
         binding.setWonderfulViewModel(getViewModel());
         init(binding);
         return binding.getRoot();
    }

    private void init(MessageFragmentLayoutBinding binding){
        List<MessageModel> messages = new ArrayList<>();
        for (int i=0; i<10; i++){
            MessageModel message = new MessageModel();
            message.setMessage("经费世界公司哦手机覅韩国就送大奖哦挤公交感觉颇为烦恼");
            messages.add(message);
        }
        MessageListAdapter adapter = new MessageListAdapter(binding.getWonderfulViewModel(),messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public MessageViewModel bindViewModel() {
        return new MessageViewModel();
    }
}
