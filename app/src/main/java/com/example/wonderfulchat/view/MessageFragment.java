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
import com.example.wonderfulchat.databinding.MessageFragmentLayoutBinding;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.UnitChangeUtil;
import com.example.wonderfulchat.viewmodel.MessageViewModel;

/**
 * @Author wonderful
 * @Description 消息展示Fragment,为了获得最新消息，使用懒加载
 * @Date 2019-8-30
 */
public class MessageFragment extends BaseFragment<MessageViewModel> {

    private static final String TAG = "MessageFragment";
    private LeftImageClickListener listener;
    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;
    private boolean firstLoad = true;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        if (rootView == null){
            MessageFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.message_fragment_layout, container, false);
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

    private void initView(MessageFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("消息");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(UnitChangeUtil.dp2px(25), UnitChangeUtil.dp2px(25));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        leftImage.setLayoutParams(layoutParams);
        setImageSelector(leftImage);
        rightImage.setVisibility(View.INVISIBLE);

        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null){
                    listener.leftImageClick();
                }
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
        Drawable drawableSelect = ContextCompat.getDrawable(getActivity(),R.drawable.elephant);
        Drawable drawableNormal = ContextCompat.getDrawable(getActivity(),R.drawable.elephant_white);
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
    public MessageViewModel bindViewModel() {
        return new MessageViewModel();
    }

    public interface LeftImageClickListener{
        public void leftImageClick();
    }

    public void setLeftImageClickListener(LeftImageClickListener listener){
        this.listener = listener;
    }

    @Override
    public void dataLoad() {
        getViewModel().refresh();
        if (firstLoad){
            firstLoad = false;
        }else {
            getViewModel().refreshUserModel();
        }
        LogUtil.d(TAG,"dataLoad");
    }
}
