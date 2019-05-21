package com.example.wonderfulchat.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.adapter.ViewPagerAdapter;
import com.example.wonderfulchat.customview.TabGroupView;
import com.example.wonderfulchat.databinding.ActivityWonderfulChatBinding;
import com.example.wonderfulchat.viewmodel.WonderfulChatViewModel;
import java.util.ArrayList;
import java.util.List;

public class WonderfulChatActivity extends BaseActivity <WonderfulChatViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWonderfulChatBinding chatBinding = DataBindingUtil.setContentView(this,R.layout.activity_wonderful_chat);
        chatBinding.setWonderfulViewModel(getViewModel());
        init(chatBinding);
    }

    private void init(ActivityWonderfulChatBinding chatBinding){
        final ViewPager viewPager = chatBinding.wonderfulChat.viewPager;
        final TabGroupView tabGroupView = chatBinding.wonderfulChat.tabGroupView;

        List<Fragment> fragments = new ArrayList<>();
        for (int i=0;i<2; i++){
            fragments.add(new MessageFragment());
        }
        fragments.add(new FriendListFragment());

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);
        tabGroupView.initChildren();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                tabGroupView.alphaChange(i,v);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        tabGroupView.setTabSelectedListener(new TabGroupView.TabSelectedListener() {
            @Override
            public void onSelect(int position) {
                viewPager.setCurrentItem(position);
            }
        });
    }

    @Override
    public WonderfulChatViewModel bindViewModel() {
        return new WonderfulChatViewModel();
    }
}
