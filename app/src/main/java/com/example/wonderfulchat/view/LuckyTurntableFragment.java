package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.databinding.LuckyTurntableFragmentLayoutBinding;
import com.example.wonderfulchat.viewmodel.LuckyTurntableViewModel;

public class LuckyTurntableFragment extends BaseFragment<LuckyTurntableViewModel>{

    private ImageView leftImage;
    private ImageView rightImage;
    private TextView midText;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null){
            LuckyTurntableFragmentLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.lucky_turntable_fragment_layout,container,false);
            binding.setWonderfulViewModel(getViewModel());
            getViewModel().setBinding(binding);
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

    private void initView(LuckyTurntableFragmentLayoutBinding binding){

        midText = binding.head.findViewById(R.id.mid_text);
        leftImage = binding.head.findViewById(R.id.left_image);
        rightImage = binding.head.findViewById(R.id.right_image);

        midText.setText("缘分也是一种恩赐");
        leftImage.setVisibility(View.GONE);
        rightImage.setVisibility(View.GONE);

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
