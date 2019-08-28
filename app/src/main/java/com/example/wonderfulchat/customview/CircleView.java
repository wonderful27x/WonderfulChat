package com.example.wonderfulchat.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.wonderfulchat.utils.LogUtil;

public class CircleView extends View {

    private int circleX;
    private int circleY;
    private int circleRadius;
    private int circleColor;
    private int textColor;
    private int textSize;
    private boolean showShadow;
    private boolean circleFill;
    private boolean showBitmap = false;
    private boolean circleClick = false;
    private boolean circleLongClick = false;
    private boolean circleLongClicking = false;
    private Bitmap circleBitmap;
    private String text;
    private Paint paint;
    private static final int PADDING = 10;
    private static final int P = 4;       //字体大小与半径比例
    private CircleClickListener circleClickListener;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircleView(Context context,Builder builder){
        super(context);
        this.circleRadius = builder.circleRadius;
        this.circleColor = builder.circleColor;
        this.textColor = builder.textColor;
        this.showShadow = builder.showShadow;
        this.circleFill = builder.circleFill;

        init();
    }

    private void init(){
//        circleX = (int)(circleRadius * outerCircleP + PADDING);
//        circleY = (int)(circleRadius * outerCircleP  + PADDING);
        textSize = circleRadius/P;

        circleX = circleRadius + PADDING;
        circleY = circleRadius + PADDING;

        paint = new Paint();
        paint.setAntiAlias(true);                                     //设置是否抗锯齿;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setStrokeWidth(2);

        text = "";

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (circleLongClick){
                    circleLongClicking = true;
                    if (circleClickListener != null){
                        circleClickListener.onLongClick();
                    }
                    LogUtil.d("CircleView", "onLongClick: ");
                }
                return true;
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (circleClick){
                    circleClick = false;
                    if (circleClickListener != null){
                        circleClickListener.onClick();
                    }
                    LogUtil.d("CircleView", "onClick: ");
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas){
        paint.setColor(circleColor);
        if (circleFill){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
        }else {
            paint.setStyle(Paint.Style.STROKE);
        }
        if (showShadow){
            paint.setShadowLayer(5,5,5,Color.GRAY);
        }else {
            paint.clearShadowLayer();
        }
        if (showBitmap){
            drawCircleBitmap(canvas);
            drawCircleText(canvas,text);
        }else {
            canvas.drawCircle(circleX,circleY,circleRadius,paint);
            drawCircleText(canvas,text);
        }

    }

    private void drawCircleText(Canvas canvas,String text){
        int with;

        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.clearShadowLayer();
        with = (int)paint.measureText(text);
        canvas.drawText(text,circleX-with/2,circleY+textSize/2,paint);
    }

    private void drawCircleBitmap(Canvas canvas){
        Bitmap bitmap = null;
        bitmap = getCircleBitmap(circleBitmap);
        if (bitmap == null)return;
        canvas.drawBitmap(bitmap,circleX - circleRadius,circleY - circleRadius,paint);
    }

    //得到圆形图片
    private Bitmap getCircleBitmap(Bitmap bmp) {
        if (bmp == null){
            return null;
        }
        bmp = zoom(bmp,circleRadius*2.0f,circleRadius*2.0f);
        Bitmap newBitmap = Bitmap.createBitmap(circleRadius*2 ,circleRadius*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Path path = new Path();
        path.addCircle(circleRadius, circleRadius, circleRadius, Path.Direction.CW);
        canvas.clipPath(path);
        canvas.drawBitmap(bmp, 0, 0, paint);
        return newBitmap;
    }

    //图片压缩
    private Bitmap zoom(Bitmap source,float width ,float height){
        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postScale(width / source.getWidth(),height / source.getHeight());
        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(clickInCircle(event)){
                    circleLongClick = true;
                }else {
                    circleLongClick = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                circleLongClick = false;
                if (clickInCircle(event)){
                    circleClick = true;
                }
                if (circleLongClicking){
                    circleLongClicking = false;
                    circleClickListener.onLongClickFinish();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean clickInCircle(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();
        int distance = (int)Math.sqrt(Math.pow((x-circleX),2) + Math.pow((y-circleY),2));
        if (distance <= circleRadius){
            return true;
        }else {
            return false;
        }
    }

    public interface CircleClickListener{
        public void onClick();
        public void onLongClick();
        public void onLongClickFinish();
    }

    public void setCircleClickListener(CircleClickListener circleClickListener){
        this.circleClickListener = circleClickListener;
    }

    public void showBitmap(Bitmap bitmap, boolean showBitmap) {
        circleBitmap = bitmap;
        this.showBitmap = showBitmap;
        invalidate();
    }

    public void showText(String text) {
        this.text = text;
        invalidate();
    }

    public void showShadow(boolean isShow){
        this.showShadow = isShow;
        invalidate();
    }

    public boolean getShadowState(){
        return showShadow;
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public static class Builder{

        private int circleRadius;
        private int circleColor;
        private int textColor;
        private boolean showShadow;
        private boolean circleFill;

        public Builder setCircleRadius(int circleRadius) {
            this.circleRadius = circleRadius;
            return this;
        }

        public Builder setCircleColor(int circleColor) {
            this.circleColor = circleColor;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setShowShadow(boolean showShadow) {
            this.showShadow = showShadow;
            return this;
        }

        public Builder setCircleFill(boolean circleFill) {
            this.circleFill = circleFill;
            return this;
        }

        public CircleView createCircleView(Context context){
            return new CircleView(context,this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((circleRadius+PADDING)*2,(circleRadius+PADDING)*2);
    }
}
