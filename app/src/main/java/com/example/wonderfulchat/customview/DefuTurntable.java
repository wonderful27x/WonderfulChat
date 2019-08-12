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
import com.example.wonderfulchat.view.MyApplication;

import java.util.List;
import java.util.Random;

public class DefuTurntable extends RelativeLayout {

    private CircleView circleView;
    private TurntableView turntableView;
    private ObjectAnimator turntableAnimator;
    private ObjectAnimator circleAnimator;
    private CircleClickListener circleClickListener;
    private float rotateAngle;
    private boolean rotateFinish = false;

    private boolean showShadow;

    private int circleRadius;
    private int circleColor;
    private int circleTextColor;
    private boolean circleFill;

    private Float innerCircleP;          //内圆缩放比例
    private Float outerCircleP;          //外圆缩放比例
    private int turntableColor;
    private int turntableTextColor;
    private boolean turntableFill;
    private int offset;                  //坐标偏移量，用于控制扇形间隔
    private static final int PADDING = 10;
    private boolean longClickUp = false;

    private int lastX = 0;
    private int lastY = 0;

    private List<? extends UserModel> list;
    private int[] itemPosition;
    private int selectPosition;
    private Context context;

    public DefuTurntable(Context context) {
        super(context);
        init(context,null);
    }

    public DefuTurntable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public DefuTurntable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(final Context context, AttributeSet attrs){
        this.context = context;
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

        selectPosition = -1;

        CircleView.Builder circleBuilder= new CircleView.Builder();
        circleView = circleBuilder.setCircleRadius(circleRadius)
            .setCircleColor(circleColor)
            .setTextColor(circleTextColor)
            .setCircleFill(circleFill)
            .setShowShadow(showShadow)
            .createCircleView(context);

        TurntableView.Builder turntableBuilder = new TurntableView.Builder();
        turntableView = turntableBuilder.setCircleRadius(circleRadius)
            .setInnerCircleP(innerCircleP)
            .setOuterCircleP(outerCircleP)
            .setTurntableColor(turntableColor)
            .setTextColor(turntableTextColor)
            .setTurntableFill(turntableFill)
            .setShowShadow(showShadow)
            .setOffset(offset)
            .createTurntableView(context);

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
        turntableAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int time  = (int)valueAnimator.getCurrentPlayTime();
                Log.d("DefuTurnTable", "currentPlayTime: "+time);
                if (time >= 20000){
                    rotateFinish = true;
                }
                if (longClickUp)return;
                time = time % 500;
                Log.d("DefuTurnTable", "onAnimationUpdate: "+time);
                if(time>0 && time<10){
                    Log.d("DefuTurnTable", "onAnimationUpdate: ");
                    itemPosition = getItemPosition(list);
                    String [] text = new String[4];
                    for (int i=0; i<4; i++){
                        if (itemPosition[i] != -1){
                            String remark = list.get(itemPosition[i]).getRemark();
                            String nickname = list.get(itemPosition[i]).getNickname();
                            String account = list.get(itemPosition[i]).getAccount();
                            if (remark != null){
                                text[i] = remark;
                            }else if (nickname != null){
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

        circleView.setCircleClickListener(new CircleView.CircleClickListener() {
            @Override
            public void onClick() {
                Log.d("DefuTurnTable", "onClick: ");
                if (selectPosition != -1){
                    if (circleClickListener != null){
                        circleClickListener.circleClick(selectPosition);
                    }
                }
            }

            @Override
            public void onLongClick() {
                Log.d("DefuTurnTable", "onLongClick: ");
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
                Log.d("DefuTurnTable", "onLongClickFinish: ");
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

    private int getRandomNumber(int number){
        return (int) (Math.random()*(number+1));
    }

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

    public void setList(List<? extends UserModel> list){
        this.list = list;
    }

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

    private void setSelectContent(){
        if (itemPosition == null || list.size()<=0)return;
        int number = getRandomNumber(list.size()-1);
        selectPosition = itemPosition[number];
        if (selectPosition <0)return;
        String remark = list.get(selectPosition).getRemark();
        String nickname = list.get(selectPosition).getNickname();
        String account = list.get(selectPosition).getAccount();
        String text;
        if (remark != null){
            text = remark;
        }else if (nickname != null){
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

}
