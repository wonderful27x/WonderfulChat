package com.example.wonderfulchat.view;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.wonderfulchat.R;
import com.example.wonderfulchat.customview.DefuEditText;
import com.example.wonderfulchat.customview.TabGroupView;

public class MainActivity extends AppCompatActivity {

    private TabGroupView groupView;
    private ViewPager pager;
    private DefuEditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        groupView = (TabGroupView)findViewById(R.id.tab_group_view);
        pager = (ViewPager)findViewById(R.id.view_pager);
        editText = (DefuEditText)findViewById(R.id.edit);

        PagerAdapter pagerAdapter = new MyAdapter();
        pager.setAdapter(pagerAdapter);
        groupView.initChildren();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Log.d("onPageScrolled", ""+v);
                groupView.alphaChange(i,v);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        editText.setIconClickListener(new DefuEditText.IconClickListener() {
            @Override
            public void IconLeftOnClick() {
                Log.d("MainActivity", "IconLeftOnClick: ");
            }

            @Override
            public void IconRightOnClick() {
                Log.d("MainActivity", "IconRightOnClick: ");
            }
        });
    }

    class MyAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(MainActivity.this, R.layout.simple_layout,null);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

    }
}
