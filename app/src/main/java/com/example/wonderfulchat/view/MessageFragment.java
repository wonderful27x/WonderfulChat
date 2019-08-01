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
import com.example.wonderfulchat.databinding.MessageFragmentLayoutBinding;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.UnitChangeUtil;
import com.example.wonderfulchat.viewmodel.MessageViewModel;

public class MessageFragment extends BaseFragment<MessageViewModel> {

    private static final String TAG = "MessageFragment";
    private LeftImageClickListener listener;
    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;
    private boolean firstLoad = true;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
         MessageFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.message_fragment_layout, container, false);
         binding.setWonderfulViewModel(getViewModel());
         getViewModel().setLayoutBinding(binding);

         initView(binding);
         //getViewModel().initView();

         return binding.getRoot();
    }

    private void initView(MessageFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("消息");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(20), UnitChangeUtil.dp2px(20));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        leftImage.setLayoutParams(layoutParams);
        leftImage.setImageResource(R.mipmap.little_hand);
        rightImage.setVisibility(View.INVISIBLE);

        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.leftImageClick();
            }
        });
    }

    @Override
    public MessageViewModel bindViewModel() {
        return new MessageViewModel();
    }

    public interface LeftImageClickListener{
        public void leftImageClick();
    }

    public void setLeftImageClickListener(LeftImageClickListener listener){
        this.listener = listener;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        LogUtil.d(TAG,"fragment onResume");
//    }
//
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser){
//            LogUtil.d(TAG,"fragment visible");
//        }
//    }

    @Override
    public void dataLoad() {
        if (firstLoad){
            getViewModel().initView();
        }else {
            getViewModel().refresh();
        }
        LogUtil.d(TAG,"dataLoad");
    }
}
