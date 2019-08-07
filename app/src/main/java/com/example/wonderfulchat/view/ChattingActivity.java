package com.example.wonderfulchat.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.utils.UnitChangeUtil;
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
        String friendName = intent.getStringExtra("friendName");
        String friendAccount = intent.getStringExtra("friendAccount");
        List<MessageModel> messageModel = (List<MessageModel>) intent.getSerializableExtra("message");

        initView(binding,friendName);
        getViewModel().initView(messageModel,friendAccount);

    }

    private void initView(ActivityChattingBinding binding,String friendName){

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(35), UnitChangeUtil.dp2px(35));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText(friendName);
        leftImage.setLayoutParams(layoutParams);
        leftImage.setBackgroundResource(R.mipmap.com_back_blue);
        rightImage.setVisibility(View.GONE);

        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getViewModel().exit();
                getViewModel().messageSave();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        getViewModel().exit();
        getViewModel().messageSave();
//        getViewModel().clearUnreadMessage();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public ChattingViewModel bindViewModel() {
        return new ChattingViewModel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
