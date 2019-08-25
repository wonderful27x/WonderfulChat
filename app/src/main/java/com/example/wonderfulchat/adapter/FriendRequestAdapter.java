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
import com.example.wonderfulchat.databinding.FriendRequestItemBinding;
import com.example.wonderfulchat.model.FriendRequestModel;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>{

    private FriendListViewModel friendListViewModel;
    private List<FriendRequestModel> friendsRequest;
    private RequestOptions options;

    public FriendRequestAdapter(FriendListViewModel friendListViewModel,List<FriendRequestModel> friendsRequest){
        this.friendListViewModel = friendListViewModel;
        this.friendsRequest = friendsRequest;
        options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        FriendRequestItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.friend_request_item,viewGroup,false);
        binding.setWonderfulViewModel(friendListViewModel);
        ViewHolder holder = new ViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        FriendRequestModel friend  = friendsRequest.get(i);
        Glide.with(friendListViewModel.getView())
                .load(friend.getImageUrl())
                .apply(options)
                .into(viewHolder.getBinding().headImage);
        String friendName;
        if (friend.getNickname() != null && !friend.getNickname().isEmpty()){
            friendName = friend.getNickname();
        }else {
            friendName = friend.getAccount();
        }
        friendName += "向您发起了好友申请";
        viewHolder.getBinding().requestFriend.setText(friendName);
        viewHolder.getBinding().requestTime.setText(friend.getRequestTime());

        viewHolder.getBinding().friendRequestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendListViewModel.showFriendRequestMessage(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsRequest.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private FriendRequestItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setBinding(FriendRequestItemBinding binding){
            this.binding = binding;
        }

        public FriendRequestItemBinding getBinding(){
            return this.binding;
        }
    }
}
