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
import com.example.wonderfulchat.model.MessageEvent;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.utils.UnitChangeUtil;
import com.example.wonderfulchat.viewmodel.FriendListViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class FriendListFragment extends BaseFragment<FriendListViewModel> {

    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FriendListFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.friend_list_fragment_layout, container, false);
        binding.setWonderfulViewModel(getViewModel());
//        EventBus.getDefault().register(this);
        getViewModel().setLayoutBinding(binding);
        initView(binding);
        getViewModel().initView();
        return binding.getRoot();
    }

    private void initView(FriendListFragmentLayoutBinding binding){

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
                getViewModel().findAddFriend();
            }
        });
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(MessageEvent event) {
//        if ("message".equals(event.getType())){
//            List<MessageModel> messageList = event.getMessageList();
//            getViewModel().jumpToChatting(messageList);
//        }
//    }

    @Override
    public FriendListViewModel bindViewModel() {
        return new FriendListViewModel();
    }

    @Override
    public void dataLoad() {

    }

}
