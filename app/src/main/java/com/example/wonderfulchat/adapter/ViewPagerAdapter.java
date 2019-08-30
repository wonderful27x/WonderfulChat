package com.example.wonderfulchat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

/**
 * @Author wonderful
 * @Description ViewPager适配器
 * @Date 2019-8-30
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;

    public ViewPagerAdapter(FragmentManager manager, List<Fragment> fragments){
        super(manager);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
