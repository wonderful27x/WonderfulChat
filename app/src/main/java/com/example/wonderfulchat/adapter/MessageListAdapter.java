package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.MessageItemBinding;
import com.example.wonderfulchat.model.FriendModel;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.MemoryUtil;
import com.example.wonderfulchat.viewmodel.MessageViewModel;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{

    private List<List<MessageModel>> unReadMessageList;
    private List<List<MessageModel>> ReadMessageList;
    private List<List<MessageModel>> messageList;
    private MessageViewModel messageViewModel;
//    private UserModel userModel;
    private ItemClickListener itemClickListener;
    private int notePosition;
    private RequestOptions options;

    public MessageListAdapter(MessageViewModel messageViewModel, List<List<MessageModel>> unReadMessageList,List<List<MessageModel>> ReadMessageList){
        this.unReadMessageList = unReadMessageList;
        this.ReadMessageList = ReadMessageList;
        this.messageViewModel = messageViewModel;

//        String modelString = MemoryUtil.sharedPreferencesGetString("UserModel");
//        Gson gson = new Gson();
//        userModel = gson.fromJson(modelString, UserModel.class);

        messageList = new ArrayList<>();
        messageList.addAll(unReadMessageList);
        messageList.addAll(messageList.size(),ReadMessageList);
        notePosition = unReadMessageList.size();
        options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);
    }

    public void notifyData(){
        messageList.clear();
        messageList.addAll(unReadMessageList);
        messageList.addAll(messageList.size(),ReadMessageList);
        notePosition = unReadMessageList.size();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,int i) {
        MessageItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.message_item,viewGroup,false);
        binding.setWonderfulViewModel(messageViewModel);
        ViewHolder holder = new ViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        List<MessageModel> messageModels = messageList.get(i);
        MessageModel messageModel = messageModels.get(messageModels.size()-1);
        viewHolder.getBinding().message.setText(messageModel.getMessage());
        if (i>=notePosition){
            viewHolder.getBinding().messageNum.setVisibility(View.GONE);
        }else {
            viewHolder.getBinding().messageNum.setText(String.valueOf(messageModels.size()));
            viewHolder.getBinding().messageNum.setVisibility(View.VISIBLE);
        }

        UserModel userModel = null;
        String name = "";
        String image = "";
        if (messageModel.getType() == MessageType.MESSAGE_RECEIVE.getCode()){
            userModel = messageViewModel.getUserModelFromDatabase(messageModel.getSenderAccount());
        }else if (messageModel.getType() == MessageType.MESSAGE_SEND.getCode()){
            userModel = messageViewModel.getUserModelFromDatabase(messageModel.getReceiverAccount());
        }
        if (userModel != null){
            name = userModel.getNickname() == null ? "" : userModel.getNickname();
            image = userModel.getImageUrl() == null ? "" : userModel.getImageUrl();
        }

//        //这里逻辑不好，应该从数据库取出friend的信息
//        String name = "";
//        String image = "";
//        if (messageModel.getType() == MessageType.MESSAGE_RECEIVE.getCode()){
//            name = messageModel.getSender();
//            image = messageModel.getSenderImage();
//        }else if (messageModel.getType() == MessageType.MESSAGE_SEND.getCode()){
//            name = messageModel.getReceiver();
//            image = "";
//        }

        viewHolder.getBinding().userName.setText(name);
        viewHolder.getBinding().lastTime.setText(messageModel.getTime());
        Glide.with(messageViewModel.getView())
                .load(image)
                .apply(options)
                .into(viewHolder.getBinding().headImage);
        viewHolder.binding.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null){
                    itemClickListener.itemClick(i);
                }
            }
        });
        viewHolder.binding.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickListener != null){
                    itemClickListener.itemLongClick(i);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private MessageItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setBinding(MessageItemBinding binding){
            this.binding = binding;
        }

        public MessageItemBinding getBinding(){
            return this.binding;
        }
    }

    public interface ItemClickListener{
        public void itemClick(int position);
        public void itemLongClick(int position);
    }
    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

//    private UserModel getUserModelFromDatabase(String account){
//        List userModel;
//        if (getHostState()){
//            userModel = LitePal.where("account=?",account).find(UserModel.class);
//        }else {
//            userModel = LitePal.where("account=?",account).find(FriendModel.class);
//        }
//        if (userModel == null || userModel.size()<=0)return null;
//        return (UserModel) userModel.get(0);
//    }
}
