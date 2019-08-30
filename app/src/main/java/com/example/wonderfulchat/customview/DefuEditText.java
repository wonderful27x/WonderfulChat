package com.example.wonderfulchat.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.wonderfulchat.R;

/**
 * @Author wonderful
 * @Description 自定义EditTextView,可实现左右图片的大小控制和点击事件监听，可实现清除功能
 * @Date 2019-8-29
 */
public class DefuEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener{

    private boolean rightClear;
    private Drawable rightDrawable;
    private Drawable leftDrawable;
    private int drawableLeftWith;
    private int drawableLeftHeight;
    private int drawableRightWith;
    private int drawableRightHeight;
    private IconClickListener iconClickListener;
    private DefuTextWatcher defuTextWatcher;

    public DefuEditText(Context context) {
        this(context,null);
    }

    public DefuEditText(Context context, AttributeSet attrs) {
        this(context, attrs,android.R.attr.editTextStyle);
    }

    public DefuEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        int leftWith = 0;
        int leftHeight = 0;
        int rightWith = 0;
        int rightHeight = 0;

        leftDrawable = getCompoundDrawables()[0];
        rightDrawable = getCompoundDrawables()[2];

        if(leftDrawable != null){
            leftWith = leftDrawable.getIntrinsicWidth();
            leftHeight = leftDrawable.getIntrinsicHeight();
        }
        if(rightDrawable != null){
            rightWith = rightDrawable.getIntrinsicWidth();
            rightHeight = rightDrawable.getIntrinsicHeight();
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulChatEditStyle);
        rightClear = typedArray.getBoolean(R.styleable.wonderfulChatEditStyle_rightClear,false);
        drawableLeftWith = typedArray.getDimensionPixelSize(R.styleable.wonderfulChatEditStyle_drawableLeftWith,leftWith);
        drawableLeftHeight = typedArray.getDimensionPixelSize(R.styleable.wonderfulChatEditStyle_drawableLeftHeight,leftHeight);
        drawableRightWith = typedArray.getDimensionPixelSize(R.styleable.wonderfulChatEditStyle_drawableRightWith,rightWith);
        drawableRightHeight = typedArray.getDimensionPixelSize(R.styleable.wonderfulChatEditStyle_drawableRightHeight,rightHeight);
        typedArray.recycle();

        if(leftDrawable != null){
            if(drawableLeftHeight<leftHeight){
                if(drawableLeftWith<leftWith){
                    leftDrawable.setBounds(leftWith-drawableLeftWith, (leftHeight-drawableLeftHeight)/2, leftWith, drawableLeftHeight+(leftHeight-drawableLeftHeight)/2);
                }else {
                    leftDrawable.setBounds(0, (leftHeight-drawableLeftHeight)/2, drawableLeftWith, drawableLeftHeight+(leftHeight-drawableLeftHeight)/2);
                }
            }else {
                if(drawableLeftWith<leftWith){
                    leftDrawable.setBounds(leftWith-drawableLeftWith, 0, leftWith, drawableLeftHeight);
                }else {
                    leftDrawable.setBounds(0, 0, drawableLeftWith, drawableLeftHeight);
                }
            }
        }

        if(rightDrawable != null){
            if(drawableRightHeight<rightHeight){
                if(drawableRightWith<rightWith){
                    rightDrawable.setBounds(rightWith - drawableRightWith, (rightHeight-drawableRightHeight)/2, rightWith, drawableRightHeight+(rightHeight-drawableRightHeight)/2);
                }else {
                    rightDrawable.setBounds(0, (rightHeight-drawableRightHeight)/2, drawableRightWith, drawableRightHeight+(rightHeight-drawableRightHeight)/2);
                }
            }else {
                if(drawableRightWith<rightWith){
                    rightDrawable.setBounds(rightWith - drawableRightWith, 0, rightWith, drawableRightHeight);
                }else {
                    rightDrawable.setBounds(0, 0, drawableRightWith, drawableRightHeight);
                }
            }
        }

        setClearIconVisible(true);                      //必须加否则大小设置出问题
        if(rightClear){
            setClearIconVisible(false);
            setOnFocusChangeListener(this);
            addTextChangedListener(new DfTextWatcher());//必须这样写，否则执行顺序会出错
        }
    }

    /**
     * @description 根据点击坐标位置判断是否有点击事件发生
     * @param event
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(getCompoundDrawables()[2] != null){
                boolean touchableRight =
                        (event.getX() > (getWidth()-drawableRightWith-getPaddingRight()))
                        && (event.getX() < ((getWidth() - getPaddingRight())))
                        && (event.getY()>((getHeight()-drawableRightHeight)/2))
                        && (event.getY()<((getHeight()-drawableRightHeight)/2)+drawableRightHeight);
                if (touchableRight && rightClear) {
                    this.setText("");
                }else if(touchableRight && !rightClear && iconClickListener != null){
                    iconClickListener.IconRightOnClick();
                }
            }

            if(getCompoundDrawables()[0] != null){
                boolean touchableLeft =
                        (event.getX() >getPaddingLeft())
                        && (event.getX() < (getPaddingLeft() + drawableLeftWith))
                        && (event.getY()>((getHeight()-drawableLeftHeight)/2))
                        && (event.getY()<((getHeight()-drawableLeftHeight)/2)+drawableLeftHeight);
                if (touchableLeft && iconClickListener != null) {
                    iconClickListener.IconLeftOnClick();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * @description  当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     *@param v,hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * @description 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? rightDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    private class DfTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setClearIconVisible(charSequence.length() > 0);
            if(defuTextWatcher == null)return;
            defuTextWatcher.onTextChanged(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    public interface IconClickListener{
        public void IconLeftOnClick();
        public void IconRightOnClick();
    }

    public interface DefuTextWatcher{
        public void onTextChanged(String text);
    }

    public void setIconClickListener(IconClickListener iconClickListener){
        this.iconClickListener = iconClickListener;
    }

    public void setDefuTextWatcher(DefuTextWatcher defuTextWatcher){
        this.defuTextWatcher = defuTextWatcher;
    }

}
