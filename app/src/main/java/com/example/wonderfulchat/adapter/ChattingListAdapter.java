package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ChattingItemBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.viewmodel.ChattingViewModel;
import java.util.List;

/**
 * @Author wonderful
 * @Description EChattingList适配器
 * @Date 2019-8-30
 */
public class ChattingListAdapter extends RecyclerView.Adapter<ChattingListAdapter.ViewHolder> {

    private List<MessageModel> messageModels;
    private ChattingViewModel chattingViewModel;
    private UserModel userModel;
    private UserModel friendModel;
    private RequestOptions options;

    public ChattingListAdapter(UserModel userModel,UserModel friendModel,List<MessageModel> messageModels,ChattingViewModel chattingViewModel){
        this.userModel = userModel;
        this.friendModel = friendModel;
        this.messageModels = messageModels;
        this.chattingViewModel = chattingViewModel;

        options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);
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
        if (messageModel.getType() == MessageType.MESSAGE_RECEIVE.getCode()){
            binding.leftLayout.setVisibility(View.VISIBLE);
            binding.rightLayout.setVisibility(View.GONE);
            binding.receiveMessage.setText(messageModel.getMessage());

            Glide.with(chattingViewModel.getView())
                    .load(friendModel.getImageUrl())
                    .apply(options)
                    .into(binding.friendImage);
        }else if(messageModel.getType() == MessageType.MESSAGE_SEND.getCode()){
            binding.leftLayout.setVisibility(View.GONE);
            binding.rightLayout.setVisibility(View.VISIBLE);
            binding.sendMessage.setText(messageModel.getMessage());

            Glide.with(chattingViewModel.getView())
                    .load(userModel.getImageUrl())
                    .apply(options)
                    .into(binding.myImage);
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
