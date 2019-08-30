package com.example.wonderfulchat.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.ActivityChattingBinding;
import com.example.wonderfulchat.model.MessageModel;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.UnitChangeUtil;
import com.example.wonderfulchat.viewmodel.ChattingViewModel;
import java.util.List;

/**
 * @Author wonderful
 * @Description 聊天界面，核心功能
 * 采用socket与服务器建立连接，通过服务器中转进行消息交换
 * 只有点击启动Activity才建立连接，退出释放连接
 * @Date 2019-8-30
 */
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
        UserModel friendModel = (UserModel) intent.getSerializableExtra("friendModel");
        if (friendModel == null){
            friendModel = new UserModel();
        }
        List<MessageModel> messageModel = (List<MessageModel>) intent.getSerializableExtra("message");

        initView(binding,friendModel);
        getViewModel().initView(messageModel,friendModel);

    }

    private void initView(ActivityChattingBinding binding,UserModel friendModel){

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(30), UnitChangeUtil.dp2px(30));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        String name;
        if (friendModel.getRemark() != null && !friendModel.getRemark().isEmpty()){
            name = friendModel.getRemark();
        }else if (friendModel.getNickname() != null && !friendModel.getNickname().isEmpty()){
            name = friendModel.getNickname();
        }else {
            name = friendModel.getAccount();
        }
        midText.setText(name);
        leftImage.setLayoutParams(layoutParams);
        setImageSelector(leftImage);
        rightImage.setVisibility(View.GONE);

        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getViewModel().exit();
                getViewModel().messageSave();
                getViewModel().saveMessageAccounts();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        });
    }

    /**
     * @description 设置状态选择器
     * @param imageView
     */
    private void setImageSelector(ImageView imageView){
        imageView.setClickable(true);
        StateListDrawable drawable = new StateListDrawable();
        Drawable drawableSelect = ContextCompat.getDrawable(this,R.drawable.com_back_gray);
        Drawable drawableNormal = ContextCompat.getDrawable(this,R.drawable.com_back_white);
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


    /**
     * @description 退出存储消息并释放连接
     */
    @Override
    public void onBackPressed() {
        getViewModel().exit();
        getViewModel().messageSave();
        getViewModel().saveMessageAccounts();
        try {
            Thread.sleep(200);
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
