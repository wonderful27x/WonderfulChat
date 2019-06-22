package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.FriendListFragmentLayoutBinding;
import com.example.wonderfulchat.utils.UnitChangeUtil;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;

public class FriendListFragment extends BaseFragment<FriendListViewModel> {

    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FriendListFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.friend_list_fragment_layout, container, false);
        binding.setWonderfulViewModel(getViewModel());
        getViewModel().setLayoutBinding(binding);
        getViewModel().initView();
        initData(binding);
        return binding.getRoot();
    }

    private void initData(FriendListFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("我的朋友");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(20), UnitChangeUtil.dp2px(20));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rightImage.setLayoutParams(layoutParams);
        rightImage.setImageResource(R.mipmap.friend_add);
        leftImage.setVisibility(View.INVISIBLE);

        rightImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getViewModel().findAndaddFriend();
            }
        });
    }

    @Override
    public FriendListViewModel bindViewModel() {
        return new FriendListViewModel();
    }

    @Override
    public void dataLoad() {

    }

}
