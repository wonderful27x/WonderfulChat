package com.example.wonderfulchat.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.wonderfulchat.utils.LogUtil;

/**
 * @Author wonderful
 * @Description 转盘内圆盘
 * @Date 2019-8-29
 */
public class CircleView extends View {

    private static final String TAG = "CircleView";

    /**中心坐标X**/
    private int circleX;
    /**中心坐标Y**/
    private int circleY;
    /**半径R**/
    private int circleRadius;
    /**圆盘颜色**/
    private int circleColor;
    /**字体颜色**/
    private int textColor;
    /**字体大小**/
    private int textSize;
    /**字体大小与半径比例**/
    private static final int P = 4;
    /**是否显示阴影**/
    private boolean showShadow;
    /**是否实心**/
    private boolean circleFill;
    /**是否显示图片**/
    private boolean showBitmap = false;
    /**显示图片**/
    private Bitmap circleBitmap;
    /**显示文字**/
    private String text;
    /**默认与边界有10个像素距离**/
    private static final int PADDING = 10;
    /**画笔**/
    private Paint paint;
    /**点击判断变量**/
    private boolean circleClick = false;
    /**长按判断变量**/
    private boolean circleLongClick = false;
    /**长按中判断变量**/
    private boolean circleLongClicking = false;
    /**点击事件监听器**/
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

    /**
     * @description 通过Builder构造实例
     * @param context,builder
     */
    public CircleView(Context context,Builder builder){
        super(context);
        this.circleRadius = builder.circleRadius;
        this.circleColor = builder.circleColor;
        this.textColor = builder.textColor;
        this.showShadow = builder.showShadow;
        this.circleFill = builder.circleFill;

        init();
    }


    /**
     * @description 初始化中心坐标，画笔及监听事件等
     */
    private void init(){
        textSize = circleRadius/P;

        circleX = circleRadius + PADDING;
        circleY = circleRadius + PADDING;

        paint = new Paint();
        paint.setAntiAlias(true);                                     //设置是否抗锯齿;
        setLayerType(LAYER_TYPE_SOFTWARE, null);                //关闭硬件加速
        paint.setStrokeWidth(2);                                       //画笔宽度

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

    /**
     * @description 画圆
     * @param canvas
     */
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

    /**
     * @description 画文字
     * @param canvas,text
     */
    private void drawCircleText(Canvas canvas,String text){
        int with;

        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.clearShadowLayer();
        with = (int)paint.measureText(text);
        canvas.drawText(text,circleX-with/2,circleY+textSize/2,paint);
    }

    /**
     * @description 画图片
     * @param canvas
     */
    private void drawCircleBitmap(Canvas canvas){
        Bitmap bitmap = null;
        bitmap = getCircleBitmap(circleBitmap);
        if (bitmap == null)return;
        canvas.drawBitmap(bitmap,circleX - circleRadius,circleY - circleRadius,paint);
    }

    /**
     * @description 得到圆形图片
     * @param bmp
     * @return Bitmap
     */
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

    /**
     * @description 裁剪压缩图片,得到宽高等于圆盘直径的正方形图片
     * @param source,width,height
     * @return Bitmap
     */
    private Bitmap zoom(Bitmap source,float width ,float height){
        if(source == null){
            return null;
        }
        int cutX;
        int cutY;
        int min;
        if (source.getWidth()<source.getHeight()){
            min = source.getWidth();
            cutX = 0;
            cutY = (source.getHeight() - source.getWidth())/2;
        }else {
            min = source.getHeight();
            cutX = (source.getWidth() - source.getHeight())/2;
            cutY = 0;
        }
        Bitmap bitmap = Bitmap.createBitmap(source, cutX, cutY, min, min);
        Matrix matrix = new Matrix();
        matrix.postScale(width/min,height/min);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, min, min, matrix, false);
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

    /**
     * @description 根据坐标判断点击事件
     * @param event
     * @return boolean
     */
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

    /**
     * @description 点击事件监听器
     */
    public interface CircleClickListener{
        public void onClick();
        public void onLongClick();
        public void onLongClickFinish();
    }

    /**
     * @description 设置监听
     * @param circleClickListener
     */
    public void setCircleClickListener(CircleClickListener circleClickListener){
        this.circleClickListener = circleClickListener;
    }

    /**
     * @description 设置图片并重绘
     * @param bitmap,showBitmap
     */
    public void showBitmap(Bitmap bitmap, boolean showBitmap) {
        circleBitmap = bitmap;
        this.showBitmap = showBitmap;
        invalidate();
    }

    /**
     * @description 设置文字并重绘
     * @param text
     */
    public void showText(String text) {
        this.text = text;
        invalidate();
    }

    /**
     * @description 设置是否显示阴影并重绘
     * @param isShow
     */
    public void showShadow(boolean isShow){
        this.showShadow = isShow;
        invalidate();
    }

    /**
     * @description 圆盘构造器
     */
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
}
