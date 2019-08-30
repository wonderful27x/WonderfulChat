package com.example.wonderfulchat.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.utils.UnitChangeUtil;

/**
 * @Author wonderful
 * @Description ViewPager 底部页签控件内部View
 * @Date 2019-8-30
 */
public class TabView extends RelativeLayout {

    private Context context;

    private boolean isChecked;

    /**底部标题**/
    private TextView bottomTextView;
    private int bottomTitleSize;
    private int bottomTitleColor;
    private String bottomTitle;
    private static final int bottomTextViewId = 10000;

    /**顶部标题**/
    private TextView topTextView;
    private int topTitleSize;
    private int topTitleColor;
    private String topTitle;
    private static final int topTextViewId = 10001;

    /**底部图标**/
    private ImageView bottomImage;
    private Drawable bottomDrawable;
    private static final int bottomImageId = 10010;

    /**顶部图标**/
    private ImageView topImage;
    private Drawable topDrawable;
    private static final int topImageId = 10011;

    public TabView(Context context) {
        super(context);
        init(context,null);
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        initAttrs(context,attrs);
        initView();
        initData();
    }

    private void initAttrs(Context context,AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulChatTabStyle);

        isChecked = typedArray.getBoolean(R.styleable.wonderfulChatTabStyle_isChecked,false);

        bottomTitleSize = (int) typedArray.getDimension(R.styleable.wonderfulChatTabStyle_bottomTitleSize, UnitChangeUtil.sp2px(5));
        bottomTitleColor = typedArray.getColor(R.styleable.wonderfulChatTabStyle_bottomTitleColor,Color.GRAY);
        bottomTitle = typedArray.getString(R.styleable.wonderfulChatTabStyle_bottomTitle);

        topTitleSize = (int) typedArray.getDimension(R.styleable.wonderfulChatTabStyle_topTitleSize, UnitChangeUtil.sp2px(5));
        topTitleColor = typedArray.getColor(R.styleable.wonderfulChatTabStyle_topTitleColor,Color.BLACK);
        topTitle = typedArray.getString(R.styleable.wonderfulChatTabStyle_topTitle);

        bottomDrawable = typedArray.getDrawable(R.styleable.wonderfulChatTabStyle_bottomDrawable);
        topDrawable = typedArray.getDrawable(R.styleable.wonderfulChatTabStyle_topDrawable);

        typedArray.recycle();
    }

    private void initView(){
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);

        bottomTextView = new TextView(context);
        bottomTextView.setId(bottomTextViewId);
        bottomTextView.setLayoutParams(params);
        this.addView(bottomTextView);

        topTextView = new TextView(context);
        topTextView.setId(topTextViewId);
        topTextView.setLayoutParams(params);
        this.addView(topTextView);

        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE,bottomTextViewId);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);

        bottomImage = new ImageView(context);
        bottomImage.setId(bottomImageId);
        bottomImage.setLayoutParams(params);
        this.addView(bottomImage);

        topImage = new ImageView(context);
        topImage.setId(topImageId);
        topImage.setLayoutParams(params);
        this.addView(topImage);
    }

    private void initData(){
        bottomTextView.setTextColor(bottomTitleColor);
        bottomTextView.setTextSize(bottomTitleSize);
        bottomTextView.setText(bottomTitle);

        topTextView.setTextColor(topTitleColor);
        topTextView.setTextSize(topTitleSize);
        topTextView.setText(topTitle);

        bottomImage.setImageDrawable(bottomDrawable);

        topImage.setImageDrawable(topDrawable);

        if(isChecked){
            alphaChange(1.0f);
        }else {
            alphaChange(0.0f);
        }
    }

    public void alphaChange(float alpha){
        topTextView.setAlpha(alpha);
        topImage.setAlpha(alpha);
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
        if(isChecked){
            alphaChange(1.0f);
        }else {
            alphaChange(0.0f);
        }
    }

}
