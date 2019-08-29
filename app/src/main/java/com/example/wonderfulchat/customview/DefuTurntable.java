package com.example.wonderfulchat.customview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.style.BulletSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.model.UserModel;
import com.example.wonderfulchat.utils.LogUtil;
import com.example.wonderfulchat.utils.ToastUtil;
import com.example.wonderfulchat.view.MyApplication;

import java.sql.BatchUpdateException;
import java.util.List;
import java.util.Random;

/**
 * @Author wonderful
 * @Description 转盘,由内圆盘和外圆盘组成，可实现反方向旋转
 * @Date 2019-8-29
 */
public class DefuTurntable extends RelativeLayout {

    /**内圆盘**/
    private CircleView circleView;
    /**外圆盘**/
    private TurntableView turntableView;
    /**外圆盘旋转动画**/
    private ObjectAnimator turntableAnimator;
    /**内圆盘旋转动画**/
    private ObjectAnimator circleAnimator;
    /**内圆盘点击事件监听器**/
    private CircleClickListener circleClickListener;
    /**旋转角度**/
    private float rotateAngle;
    /**旋转结束标志**/
    private boolean rotateFinish = false;
    /**是否显示阴影**/
    private boolean showShadow = false;
    /**内圆盘半径**/
    private int circleRadius = 100;
    /**内圆盘颜色**/
    private int circleColor = Color.YELLOW;
    /**内圆盘字体颜色**/
    private int circleTextColor = Color.BLACK;
    /**内圆盘是否填充**/
    private boolean circleFill = true;
    /**外圆盘内圆缩放比例**/
    private Float innerCircleP = 1.3f;
    /**外圆盘外圆缩放比例**/
    private Float outerCircleP = 2.5f;
    /**外圆盘颜色**/
    private int turntableColor = Color.GREEN;
    /**橡皮颜色，用于擦出多余填充，应与背景颜色一致**/
    private int eraserColor = Color.parseColor("#FFFFFF");
    /**外圆盘字体颜色**/
    private int turntableTextColor = Color.BLACK;
    /**外圆盘是否填充**/
    private boolean turntableFill = false;
    /**坐标偏移量，用于控制扇形间隔**/
    private int offset = 5;
    /**圆盘与边界距离**/
    private static final int PADDING = 10;
    /**长按弹起标志**/
    private boolean longClickUp = false;
    /**记录上次坐标X**/
    private int lastX = 0;
    /**记录上次坐标Y**/
    private int lastY = 0;
    /**字体内容及图片资源来源**/
    private List<? extends UserModel> list;
    /**资源随机位置记录数组**/
    private int[] itemPosition;
    /**选中位置**/
    private int selectPosition;
    /**全局context**/
    private Context context;

    public DefuTurntable(Context context) {
        super(context);
        this.context = context;
    }

    public DefuTurntable(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public DefuTurntable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    /**
     * @description 圆盘初始化，初始化内外圆盘并组装成转盘，添加监听事件等
     * @param attrs ：如果不为null为xml方式注册，否则为动态创建
     */
    private void init(AttributeSet attrs){

        if (attrs != null){

            /**
             * 获取自定义属性
             */
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.defuTurntableStyle);

            showShadow = typedArray.getBoolean(R.styleable.defuTurntableStyle_showShadow,false);

            circleRadius = (int)typedArray.getDimension(R.styleable.defuTurntableStyle_circleRadius, 100);
            circleColor = typedArray.getColor(R.styleable.defuTurntableStyle_circleColor,Color.YELLOW);
            circleTextColor = typedArray.getColor(R.styleable.defuTurntableStyle_circleTextColor,Color.BLACK);
            circleFill = typedArray.getBoolean(R.styleable.defuTurntableStyle_circleFill,true);

            innerCircleP = typedArray.getFloat(R.styleable.defuTurntableStyle_innerCircleP,1.3f);
            outerCircleP = typedArray.getFloat(R.styleable.defuTurntableStyle_outerCircleP,2.5f);
            turntableColor = typedArray.getColor(R.styleable.defuTurntableStyle_turntableColor,Color.GREEN);
            turntableTextColor = typedArray.getColor(R.styleable.defuTurntableStyle_turntableTextColor,Color.BLACK);
            turntableFill = typedArray.getBoolean(R.styleable.defuTurntableStyle_turntableFill,false);
            offset = (int)typedArray.getDimension(R.styleable.defuTurntableStyle_offset,5);

            typedArray.recycle();
        }

        selectPosition = -1;

        /**
         * 构造内圆盘
         */
        CircleView.Builder circleBuilder= new CircleView.Builder();
        circleView = circleBuilder.setCircleRadius(circleRadius)
            .setCircleColor(circleColor)
            .setTextColor(circleTextColor)
            .setCircleFill(circleFill)
            .setShowShadow(showShadow)
            .createCircleView(context);

        /**
         * 构造外圆盘
         */
        TurntableView.Builder turntableBuilder = new TurntableView.Builder();
        turntableView = turntableBuilder.setCircleRadius(circleRadius)
            .setInnerCircleP(innerCircleP)
            .setOuterCircleP(outerCircleP)
            .setTurntableColor(turntableColor)
            .setEraserColor(eraserColor)
            .setTextColor(turntableTextColor)
            .setTurntableFill(turntableFill)
            .setShowShadow(showShadow)
            .setOffset(offset)
            .createTurntableView(context);

        /**
         * 组装成转盘
         */
        LayoutParams turntableParams = new LayoutParams((int)((circleRadius*outerCircleP+PADDING)*2),(int)((circleRadius*outerCircleP+PADDING)*2));
        LayoutParams circleParams = new LayoutParams(circleRadius*2,circleRadius*2);
        turntableParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        circleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        turntableView.setLayoutParams(turntableParams);
        circleView.setLayoutParams(circleParams);
        this.addView(turntableView);
        this.addView(circleView);

        circleAnimator = new ObjectAnimator();
        turntableAnimator = new ObjectAnimator();
        rotateAngle = 36000f;

        turntableAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (longClickUp){
                    circleView.showShadow(showShadow);
                    turntableView.showShadow(showShadow);
                }
                if (rotateFinish) {
                    setSelectContent();
                    circleView.showShadow(showShadow);
                    turntableView.showShadow(showShadow);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSelectContent();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        /**
         * 重要的旋转更新，产生随机数，不断改变显示字体内容
         */
        turntableAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int time  = (int)valueAnimator.getCurrentPlayTime();
                LogUtil.d("DefuTurnTable", "currentPlayTime: "+time);
                if (time >= 20000){
                    rotateFinish = true;
                }
                if (longClickUp)return;
                time = time % 500;
                LogUtil.d("DefuTurnTable", "onAnimationUpdate: "+time);
                if(time>0 && time<10){
                    LogUtil.d("DefuTurnTable", "onAnimationUpdate: ");
                    itemPosition = getItemPosition(list);
                    String [] text = new String[4];
                    for (int i=0; i<4; i++){
                        if (itemPosition[i] != -1){
                            String remark = list.get(itemPosition[i]).getRemark();
                            String nickname = list.get(itemPosition[i]).getNickname();
                            String account = list.get(itemPosition[i]).getAccount();
                            if (remark != null && !remark.isEmpty()){
                                text[i] = remark;
                            }else if (nickname != null && !nickname.isEmpty()){
                                text[i] = nickname;
                            }else {
                                text[i] = account;
                            }
                        }else {
                            text[i] = "";
                        }
                    }
                    turntableView.showText(text);
                }
            }
        });

        /**
         * 内圆盘重要的点击事件监听，点击跳转，长按旋转，长按弹起时内圆盘反方向旋转
         */
        circleView.setCircleClickListener(new CircleView.CircleClickListener() {
            @Override
            public void onClick() {
                LogUtil.d("DefuTurnTable", "onClick: ");
                if(list.size() <= 0){
                    ToastUtil.showToast("您还未添加任何好友！");
                    return;
                }
                if (selectPosition != -1){
                    if (circleClickListener != null){
                        circleClickListener.circleClick(selectPosition);
                    }
                }else {
                    ToastUtil.showToast("请长按旋转转盘！");
                }
            }

            @Override
            public void onLongClick() {
                LogUtil.d("DefuTurnTable", "onLongClick: ");
                rotateFinish = false;
                longClickUp = false;
                circleView.showBitmap(null,false);
                circleView.showShadow(false);
                circleView.showText("");
                turntableView.showShadow(false);

                turntableAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                turntableAnimator.setTarget(turntableView);
                turntableAnimator.setPropertyName("rotation");
                turntableAnimator.setFloatValues(0f,rotateAngle);
                turntableAnimator.setDuration(20000);
                turntableAnimator.start();

                requestDisallowInterceptTouchEvent(true);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onLongClickFinish() {
                LogUtil.d("DefuTurnTable", "onLongClickFinish: ");
                if (rotateFinish)return;
                turntableAnimator.cancel();
                turntableAnimator.setInterpolator(new DecelerateInterpolator());
                turntableAnimator.setFloatValues(0f,getRandomNumber(360)+360*5);
                turntableAnimator.setDuration(3000);
                longClickUp = true;

                circleAnimator.setInterpolator(new DecelerateInterpolator());
                circleAnimator.setTarget(circleView);
                circleAnimator.setPropertyName("rotation");
                circleAnimator.setFloatValues(360*2,getRandomNumber(360));
                circleAnimator.setDuration(3000);

                turntableAnimator.start();
                circleAnimator.start();

            }
        });

        /**
         * 外圆盘重要的点击事件监听，可移动转盘
         */
        turntableView.setTurntableClickListener(new TurntableView.TurntableClickListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onLongClick() {
                circleView.showShadow(!showShadow);
                turntableView.showShadow(!showShadow);
                requestDisallowInterceptTouchEvent(true);
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void moving(int x, int y) {

                if (lastX == 0 && lastY == 0){
                    lastX = x;
                    lastY = y;
                    return;
                }

                int translationX = (int)(getTranslationX() + x - lastX);
                int translationY = (int)(getTranslationY() + y - lastY);

                setTranslationX(translationX);
                setTranslationY(translationY);

                lastX = x;
                lastY = y;
            }

            @Override
            public void movingFinish(int x, int y) {
                lastX = 0;
                lastY = 0;
                circleView.showShadow(showShadow);
                turntableView.showShadow(showShadow);
            }
        });

    }

    /**
     * @description 产生随机数0-number的随机数
     * @param number
     * @return int
     */
    private int getRandomNumber(int number){
        return (int) (Math.random()*(number+1));
    }

    /**
     * @description 去除重复随机数
     * @param numbers,size
     * @return int
     */
    private int getRandomNumber(int[] numbers,int size){
        boolean theSame;
        int number;
        do {
            theSame = false;
            number = (int) (Math.random()*(size + 1));
            for (int a:numbers) {
                if (number == a) {
                    theSame = true;
                    break;
                }
            }
        }while (theSame);
        return number;
    }

    private String generateString(int length) {
        Random random = new Random();
        String characters = "ksogijogijiosjogghoisj0geghjnjbnmoz";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    /**
     * @description 重置资源列表
     * @param list
     */
    public void setList(List<? extends UserModel> list){
        this.list = list;
    }

    /**
     * @description 获取四个随机数据，分别对应四个扇形区域显示的内容
     * @param list
     * @return int[]
     */
    private int[] getItemPosition(List<? extends UserModel> list){
        int[] itemPosition = new int[]{-1,-1,-1,-1};
        if (list == null || list.size() == 0){
            return itemPosition;
        }else if (list.size() == 1){
            itemPosition[0] = 0;
        }else if (list.size() == 2){
            itemPosition[0] = 0;
            itemPosition[1] = 1;
        }else if (list.size() == 3){
            itemPosition[0] = 0;
            itemPosition[1] = 1;
            itemPosition[2] = 2;
        }else if (list.size() == 4){
            itemPosition[0] = 0;
            itemPosition[1] = 1;
            itemPosition[2] = 2;
            itemPosition[3] = 3;
        }else {
            itemPosition[0] = getRandomNumber(itemPosition,list.size()-1);
            itemPosition[1] = getRandomNumber(itemPosition,list.size()-1);
            itemPosition[2] = getRandomNumber(itemPosition,list.size()-1);
            itemPosition[3] = getRandomNumber(itemPosition,list.size()-1);
        }
        return itemPosition;
    }

    /**
     * @description 长按弹起时，内圆盘随机获取外圆盘的一个内容，表示幸运者
     */
    private void setSelectContent(){
        if (itemPosition == null || list.size()<=0){
            selectPosition = -1;
            return;
        }
        int number;
        if(list.size() <= 4){
            number = getRandomNumber(list.size()-1);
        }else {
            number = getRandomNumber(3);
        }
        selectPosition = itemPosition[number];
        if (selectPosition <0)return;
        String remark = list.get(selectPosition).getRemark();
        String nickname = list.get(selectPosition).getNickname();
        String account = list.get(selectPosition).getAccount();
        String text;
        if (remark != null && !remark.isEmpty()){
            text = remark;
        }else if (nickname != null && !nickname.isEmpty()){
            text = nickname;
        }else {
            text = account;
        }
        circleView.showText(text);

        Glide.with(context).load(list.get(selectPosition).getImageUrl()).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                circleView.showBitmap(drawableToBitmap(resource), true);
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int)((circleRadius*outerCircleP+PADDING)*2),(int)((circleRadius*outerCircleP+PADDING)*2));
    }

    public interface CircleClickListener{
        public void circleClick(int position);
    }

    public void setCircleClickListener(CircleClickListener circleClickListener){
        this.circleClickListener = circleClickListener;
    }

    /**
     * @description 资源文件转换成bitmap
     * @param drawable
     * @return Bitmap
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * @description 重置资源
     * @param list
     */
    public void reset(List<? extends UserModel> list){
        this.list = list;
        selectPosition = -1;
    }

    public float getOuterCircleP(){
        return outerCircleP;
    }

    public void setShowShadow(boolean showShadow) {
        this.showShadow = showShadow;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }

    public void setCircleTextColor(int circleTextColor) {
        this.circleTextColor = circleTextColor;
    }

    public void setCircleFill(boolean circleFill) {
        this.circleFill = circleFill;
    }

    public void setInnerCircleP(Float innerCircleP) {
        this.innerCircleP = innerCircleP;
    }

    public void setOuterCircleP(Float outerCircleP) {
        this.outerCircleP = outerCircleP;
    }

    public void setTurntableColor(int turntableColor) {
        this.turntableColor = turntableColor;
    }

    public void setEraserColor(int eraserColor) {
        this.eraserColor = eraserColor;
    }

    public void setTurntableTextColor(int turntableTextColor) {
        this.turntableTextColor = turntableTextColor;
    }

    public void setTurntableFill(boolean turntableFill) {
        this.turntableFill = turntableFill;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @description 重要的构造器，用于构造转盘，在动态创建转盘时尤其重要
     */
    public static class Builder{

        private DefuTurntable turntable;

        public Builder(Context context){
            turntable = new DefuTurntable(context);
        }

        public Builder setShowShadow(boolean showShadow){
            turntable.setShowShadow(showShadow);
            return this;
        }

        public Builder setCircleRadius(int circleRadius){
            turntable.setCircleRadius(circleRadius);
            return this;
        }

        public Builder setCircleColor(int circleColor){
            turntable.setCircleColor(circleColor);
            return this;
        }

        public Builder setCircleTextColor(int circleTextColor){
            turntable.setCircleTextColor(circleTextColor);
            return this;
        }

        public Builder setCircleFill(boolean circleFill){
            turntable.setCircleFill(circleFill);
            return this;
        }

        public Builder setInnerCircleP(float innerCircleP){
            turntable.setInnerCircleP(innerCircleP);
            return this;
        }

        public Builder setOuterCircleP(float outerCircleP){
            turntable.setOuterCircleP(outerCircleP);
            return this;
        }

        public Builder setTurntableColor(int turntableColor){
            turntable.setTurntableColor(turntableColor);
            return this;
        }

        public Builder setEraserColor(int eraserColor){
            turntable.setEraserColor(eraserColor);
            return this;
        }

        public Builder setTurntableTextColor(int turntableTextColor){
            turntable.setTurntableTextColor(turntableTextColor);
            return this;
        }

        public Builder setTurntableFill(boolean turntableFill){
            turntable.setTurntableFill(turntableFill);
            return this;
        }

        public Builder setOffset(int offset){
            turntable.setOffset(offset);
            return this;
        }

        public DefuTurntable createTurntable(){
            try{
                turntable.init(null);
                return turntable;
            }finally {
                turntable = null;
            }

        }

        public void clear(){
            turntable = null;
        }

    }

}
