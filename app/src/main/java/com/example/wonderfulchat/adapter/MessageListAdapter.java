package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.MessageItemBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.viewmodel.MessageViewModel;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{

    private List<MessageModel> message;
    private MessageViewModel messageViewModel;

    public MessageListAdapter(MessageViewModel messageViewModel, List<MessageModel> message){
        this.message = message;
        this.messageViewModel = messageViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MessageItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.message_item,viewGroup,false);
        binding.setWonderfulViewModel(messageViewModel);
        ViewHolder holder = new ViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.getBinding().message.setText(message.get(i).getMessage());
    }

    @Override
    public int getItemCount() {
        return message.size();
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
}
