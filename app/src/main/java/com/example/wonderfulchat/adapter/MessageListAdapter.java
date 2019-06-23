package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.MessageItemBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{

    private List<List<MessageModel>> unReadMessageList;
    private List<List<MessageModel>> readedMessageList;
    private List<List<MessageModel>> messageList;
    private MessageViewModel messageViewModel;
    private ItemClickListener itemClickListener;
    private int notePosition;

    public MessageListAdapter(MessageViewModel messageViewModel, List<List<MessageModel>> unReadMessageList,List<List<MessageModel>> readedMessageList){
        this.unReadMessageList = unReadMessageList;
        this.readedMessageList = readedMessageList;
        this.messageViewModel = messageViewModel;
        messageList = new ArrayList<>();
        messageList.addAll(unReadMessageList);
        messageList.addAll(messageList.size(),readedMessageList);
        notePosition = unReadMessageList.size();
    }

    public void notifyData(){
        messageList.clear();
        messageList.addAll(unReadMessageList);
        messageList.addAll(messageList.size(),readedMessageList);
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
            viewHolder.getBinding().messageNum.setText(messageModels.size()+"");
            viewHolder.getBinding().messageNum.setVisibility(View.VISIBLE);
        }
        viewHolder.getBinding().userName.setText(messageModel.getSender());
        viewHolder.getBinding().lastTime.setText(messageModel.getTime());
        Glide.with(messageViewModel.getView()).load(messageModel.getSenderImage()).into(viewHolder.getBinding().headImage);
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
}
