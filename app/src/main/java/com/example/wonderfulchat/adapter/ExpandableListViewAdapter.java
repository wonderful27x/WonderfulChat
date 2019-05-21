package com.example.wonderfulchat.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ChildLayoutBinding;
import com.example.wonderfulchat.databinding.GroupLayoutBinding;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;

import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private List<GroupModel> groupModels;
    private FriendListViewModel viewModel;

    public ExpandableListViewAdapter(FriendListViewModel viewModel,List<GroupModel> groupModels){
        this.groupModels = groupModels;
        this.viewModel = viewModel;
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
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildLayoutBinding childLayoutBinding = null;
        if (view == null){
            childLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.child_layout,viewGroup,false);
            childLayoutBinding.setWonderfulViewModel(viewModel);
            view = childLayoutBinding.getRoot();
        }else {
            childLayoutBinding = DataBindingUtil.getBinding(view);
        }

        childLayoutBinding.headImage.setImageBitmap(groupModels.get(i).getChildModels().get(i1).getBitmap());
        childLayoutBinding.friendName.setText(groupModels.get(i).getChildModels().get(i1).getTitle());
        childLayoutBinding.myWord.setText(groupModels.get(i).getChildModels().get(i1).getContent());

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
