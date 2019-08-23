package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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
    private boolean firstLoad = true;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null){
            FriendListFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.friend_list_fragment_layout, container, false);
            binding.setWonderfulViewModel(getViewModel());
//        EventBus.getDefault().register(this);
            getViewModel().setLayoutBinding(binding);
            initView(binding);
            getViewModel().initView();
            rootView = binding.getRoot();
        }else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null){
                parent.removeView(rootView);
            }
        }

        return rootView;
    }

    private void initView(FriendListFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("我的朋友");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(20), UnitChangeUtil.dp2px(20));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.setMargins(UnitChangeUtil.dp2px(5),0,0,0);
        rightImage.setLayoutParams(layoutParams);
        setImageSelector(rightImage);
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

    //设置状态选择器
    private void setImageSelector(ImageView imageView){
        imageView.setClickable(true);
        StateListDrawable drawable = new StateListDrawable();
        Drawable drawableSelect = getResources().getDrawable(R.mipmap.friend_add_blue);
        Drawable drawableNormal = getResources().getDrawable(R.mipmap.friend_add);
        //选中
        drawable.addState(new int[]{android.R.attr.state_pressed},drawableSelect);
        //未选中
        drawable.addState(new int[]{},drawableNormal);
        imageView.setBackgroundDrawable(drawable);
    }

    @Override
    public FriendListViewModel bindViewModel() {
        return new FriendListViewModel();
    }

    @Override
    public void dataLoad() {
        if (firstLoad){
            firstLoad = false;
        }else {
            getViewModel().refreshUserModel();
        }
//        getViewModel().refreshUserModel();
    }

}
