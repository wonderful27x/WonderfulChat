package com.example.wonderfulchat.view;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ExpandableListViewAdapter;
import com.example.wonderfulchat.databinding.FriendListFragmentLayoutBinding;
import com.example.wonderfulchat.model.ChildModel;
import com.example.wonderfulchat.model.GroupModel;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends BaseFragment<FriendListViewModel> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FriendListFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.friend_list_fragment_layout, container, false);
        binding.setWonderfulViewModel(getViewModel());
        init(binding);
        return binding.getRoot();
    }

    @Override
    public FriendListViewModel bindViewModel() {
        return new FriendListViewModel();
    }

    private void init(FriendListFragmentLayoutBinding binding){
        List<GroupModel> groupModels = new ArrayList<>();
        List<ChildModel> childModels = new ArrayList<>();

        ChildModel childModel = new ChildModel();
        Resources resources = getActivity().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_file_music);
        childModel.setTitle("黄世豪");
        childModel.setContent("为国家繁荣富强而努力奋斗");
        childModel.setBitmap(bitmap);

        for (int i=0; i<3; i++){
            childModels.add(childModel);
        }

        GroupModel groupModel = new GroupModel();
        groupModel.setTitle("我的亲密好友");
        groupModel.setNumber(27);
        groupModel.setChildModels(childModels);

        for (int i=0; i<5; i++){
            groupModels.add(groupModel);
        }

        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(binding.getWonderfulViewModel(),groupModels);
        binding.friendList.setAdapter(adapter);
    }
}
