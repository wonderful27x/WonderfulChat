package com.example.wonderfulchat.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * @Author wonderful
 * @Description 自定义ExpandableListView，为解决嵌套问题
 * @Date 2019-8-30
 */
public class WrapExpandableListView extends ExpandableListView {
    public WrapExpandableListView(Context context) {
        super(context);
    }

    public WrapExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
