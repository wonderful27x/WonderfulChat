package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ChattingItemBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.viewmodel.ChattingViewModel;
import com.google.gson.Gson;

import java.util.List;

public class ChattingListAdapter extends RecyclerView.Adapter<ChattingListAdapter.ViewHolder> {

    private List<MessageModel> messageModels;
    private ChattingViewModel chattingViewModel;
    private String userImage;

    public ChattingListAdapter(List<MessageModel> messageModels,ChattingViewModel chattingViewModel){
        this.messageModels = messageModels;
        this.chattingViewModel = chattingViewModel;

        String userModel = MemoryUtil.sharedPreferencesGetString("UserModel");
        Gson gson = new Gson();
        UserModel model = gson.fromJson(userModel, UserModel.class);
        userImage = model.getImageUrl();
    }
    @NonNull
    @Override
    public ChattingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ChattingItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.chatting_item,viewGroup,false);
        binding.setWonderfulViewModel(chattingViewModel);
        ViewHolder viewHolder = new ViewHolder(binding.getRoot());
        viewHolder.setBinding(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingListAdapter.ViewHolder viewHolder, int i) {
        MessageModel messageModel = messageModels.get(i);
        ChattingItemBinding binding = viewHolder.getBinding();
        if (messageModels.get(i).getType() == MessageType.MESSAGE_RECEIVE.getCode()){
            binding.leftLayout.setVisibility(View.VISIBLE);
            binding.rightLayout.setVisibility(View.GONE);
            binding.receiveMessage.setText(messageModel.getMessage());
            Glide.with(chattingViewModel.getView()).load(messageModel.getSenderImage()).into(binding.friendImage);
        }else if(messageModels.get(i).getType() == MessageType.MESSAGE_SEND.getCode()){
            binding.leftLayout.setVisibility(View.GONE);
            binding.rightLayout.setVisibility(View.VISIBLE);
            binding.sendMessage.setText(messageModel.getMessage());
            Glide.with(chattingViewModel.getView()).load(userImage).into(binding.myImage);
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private ChattingItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setBinding(ChattingItemBinding binding){
            this.binding = binding;
        }

        public ChattingItemBinding getBinding(){
            return this.binding;
        }
    }

    public List<MessageModel> getMessageModels(){
        return messageModels;
    }

}
