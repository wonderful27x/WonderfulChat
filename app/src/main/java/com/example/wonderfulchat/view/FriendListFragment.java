package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

/**
 * @Author wonderful
 * @Description 好友列表，这里还集成了重要的好友申请功能，
 * 好友的添加请求会展示在好友列表之上
 * @Date 2019-8-30
 */
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

    /**
     * @description 动态设置状态选择器
     * @param imageView
     */
    private void setImageSelector(ImageView imageView){
        imageView.setClickable(true);
        StateListDrawable drawable = new StateListDrawable();
        Drawable drawableSelect = ContextCompat.getDrawable(getActivity(),R.drawable.friend_add_black);
        Drawable drawableNormal = ContextCompat.getDrawable(getActivity(),R.drawable.friend_add_white);
        //选中
        drawable.addState(new int[]{android.R.attr.state_pressed},drawableSelect);
        //未选中
        drawable.addState(new int[]{},drawableNormal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setBackground(drawable);
        }else {
            imageView.setBackgroundDrawable(drawable);
        }
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
            getViewModel().getFriendRequest();
        }
    }

}
