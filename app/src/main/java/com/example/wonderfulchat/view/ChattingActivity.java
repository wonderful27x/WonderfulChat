package com.example.wonderfulchat.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.viewmodel.ChattingViewModel;

import java.util.List;

public class ChattingActivity extends BaseActivity<ChattingViewModel> {

    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityChattingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_chatting);
        binding.setWonderfulViewModel(getViewModel());
        getViewModel().setBinding(binding);

        Intent intent = getIntent();
        String friend = intent.getStringExtra("friendName");
        List<MessageModel> messageModel = (List<MessageModel>) intent.getSerializableExtra("message");

        getViewModel().initView(messageModel);

        initData(binding,friend);
    }

    private void initData(ActivityChattingBinding binding,String friend){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText(friend);
        leftImage.setImageResource(R.mipmap.com_back_blue);
        rightImage.setVisibility(View.GONE);

        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public ChattingViewModel bindViewModel() {
        return new ChattingViewModel();
    }
}
