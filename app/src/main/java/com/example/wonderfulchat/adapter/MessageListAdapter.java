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
import com.example.wonderfulchat.databinding.MessageItemBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.MessageType;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.viewmodel.MessageViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description 消息列表适配器，使用了DataBinding的写法，并且让其持有了一个ViewModel的引用，
 * 和与其关联的Fragment共用一个ViewModel,加剧了耦合性
 * @Date 2019-8-30
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{

    private List<List<MessageModel>> unReadMessageList;
    private List<List<MessageModel>> ReadMessageList;
    private List<List<MessageModel>> messageList;
    private MessageViewModel messageViewModel;
    private ItemClickListener itemClickListener;
    private int notePosition;
    private RequestOptions options;

    public MessageListAdapter(MessageViewModel messageViewModel, List<List<MessageModel>> unReadMessageList,List<List<MessageModel>> ReadMessageList){
        this.unReadMessageList = unReadMessageList;
        this.ReadMessageList = ReadMessageList;
        this.messageViewModel = messageViewModel;

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
            image = userModel.getImageUrl() == null ? "" : userModel.getImageUrl();
            if (userModel.getRemark() != null && !userModel.getRemark().isEmpty()){
                name = userModel.getRemark();
            }else if (userModel.getNickname() != null && !userModel.getNickname().isEmpty()){
                name = userModel.getNickname();
            }else {
                name = userModel.getAccount();
            }
        }

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

}
