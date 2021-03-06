package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ChildLayoutBinding;
import com.example.wonderfulchat.databinding.GroupLayoutBinding;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;
import java.util.List;

/**
 * @Author wonderful
 * @Description ExpandableListView适配器
 * @Date 2019-8-30
 */
public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private List<GroupModel> groupModels;
    private FriendListViewModel viewModel;
    private RequestOptions options;

    public ExpandableListViewAdapter(FriendListViewModel viewModel,List<GroupModel> groupModels){
        this.groupModels = groupModels;
        this.viewModel = viewModel;

        options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);
    }

    @Override
    public int getGroupCount() {
        return groupModels.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return groupModels.get(i).getChildModels().size();
    }

    @Override
    public Object getGroup(int i) {
        return groupModels.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return groupModels.get(i).getChildModels().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupLayoutBinding groupLayoutBinding = null;
        if (view == null){
            groupLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.group_layout,viewGroup,false);
            groupLayoutBinding.setWonderfulViewModel(viewModel);
            view = groupLayoutBinding.getRoot();
        }else {
            groupLayoutBinding = DataBindingUtil.getBinding(view);
        }

        groupLayoutBinding.groupName.setText(groupModels.get(i).getTitle());
        groupLayoutBinding.friendsNum.setText(String.valueOf(groupModels.get(i).getNumber()));

        return view;
    }

    @Override
    public View getChildView(final int i, final int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildLayoutBinding childLayoutBinding = null;
        if (view == null){
            childLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.child_layout,viewGroup,false);
            childLayoutBinding.setWonderfulViewModel(viewModel);
            view = childLayoutBinding.getRoot();
        }else {
            childLayoutBinding = DataBindingUtil.getBinding(view);
        }

        UserModel userModel = groupModels.get(i).getChildModels().get(i1);

        Glide.with(viewModel.getView())
                .load(userModel.getImageUrl())
                .apply(options)
                .into(childLayoutBinding.headImage);

        String content;
        if((userModel.getRemark() == null || userModel.getRemark().isEmpty()) && (userModel.getNickname() == null || userModel.getNickname().isEmpty())){
            content = userModel.getAccount();
        }else if(userModel.getRemark() != null && !userModel.getRemark().isEmpty() && (userModel.getNickname() == null || userModel.getNickname().isEmpty())){
            content = userModel.getRemark();
        }else if((userModel.getRemark() == null || userModel.getRemark().isEmpty()) && userModel.getNickname() != null && !userModel.getNickname().isEmpty()){
            content = userModel.getNickname();
        }else {
            content = userModel.getRemark() + "～" + userModel.getNickname();
        }
        childLayoutBinding.friendName.setText(content);
        childLayoutBinding.myWord.setText(userModel.getLifeMotto());

        childLayoutBinding.childLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.jumpToChatting(i,i1);
            }
        });

        childLayoutBinding.headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.showFriendMessage(i,i1);
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
