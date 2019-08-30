package com.example.wonderfulchat.customview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wonderful
 * @Description ViewPager 底部页签，可时间颜色渐变
 * @Date 2019-8-30
 */
public class TabGroupView extends LinearLayout {

    private TabSelectedListener tabSelectedListener;
    private List<TabView> tabViews;
    public TabGroupView(Context context) {
        super(context);
        init();
    }

    public TabGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        this.setOrientation(HORIZONTAL);
        tabViews = new ArrayList<>();
    }

    public void initChildren(){
        int count = getChildCount();
        for(int i=0; i<count; i++){
            final TabView tabView = (TabView) getChildAt(i);
            final int position = i;
            tabViews.add(tabView);
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearCheckedAll();
                    tabView.setChecked(true);
                    tabSelectedListener.onSelect(position);
                }
            });
        }
    }

    private void clearCheckedAll(){
        for(int i=0; i<tabViews.size(); i++){
            tabViews.get(i).setChecked(false);
        }
    }

    public void alphaChange(int position,float alpha){
        tabViews.get(position).alphaChange(1-alpha);
        if(position+1<tabViews.size()){
            tabViews.get(position+1).alphaChange(alpha);
        }
    }

    public interface TabSelectedListener{
        public void onSelect(int position);
    }

    public void setTabSelectedListener(TabSelectedListener tabSelectedListener){
        this.tabSelectedListener = tabSelectedListener;
    }
}
