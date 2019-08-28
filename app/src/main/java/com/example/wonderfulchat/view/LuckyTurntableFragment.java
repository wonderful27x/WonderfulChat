package com.example.wonderfulchat.view;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.Resource;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.customview.DefuTurntable;
import com.example.wonderfulchat.databinding.LuckyTurntableFragmentLayoutBinding;
import com.example.wonderfulchat.viewmodel.LuckyTurntableViewModel;

public class LuckyTurntableFragment extends BaseFragment<LuckyTurntableViewModel>{

    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;
    private View rootView;
    private DefuTurntable turntable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null){
            LuckyTurntableFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.lucky_turntable_fragment_layout,container,false);
            binding.setWonderfulViewModel(getViewModel());
            getViewModel().setBinding(binding);
            initView(binding);
            getViewModel().initView(turntable);
            rootView = binding.getRoot();
        }else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null){
                parent.removeView(rootView);
            }
        }
        return rootView;
    }

    private void resetTurntable(LuckyTurntableFragmentLayoutBinding binding){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int width = metrics.widthPixels;

        int circle = width/10;

        int color = ContextCompat.getColor(getActivity(), R.color.green);
        DefuTurntable.Builder builder = new DefuTurntable.Builder(getActivity());
        turntable = builder.setCircleRadius(circle)
                .setTurntableColor(color)
                .createTurntable();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        turntable.setLayoutParams(layoutParams);
        binding.turntableLayout.addView(turntable);
    }

    private void initView(LuckyTurntableFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("缘分也是一种恩赐");
        leftImage.setVisibility(View.GONE);
        rightImage.setVisibility(View.GONE);

        resetTurntable(binding);
    }

    @Override
    public LuckyTurntableViewModel bindViewModel() {
        return new LuckyTurntableViewModel();
    }

    @Override
    public void dataLoad() {
        getViewModel().refresh();
    }
}
