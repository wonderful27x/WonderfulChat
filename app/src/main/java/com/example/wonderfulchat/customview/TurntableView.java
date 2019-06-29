package com.example.wonderfulchat.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TurntableView extends View {

    private int circleX;
    private int circleY;
    private int circleRadius;
    private int turntableColor;
    private int textColor;
    private boolean showShadow;
    private boolean turntableFill;
    private int offset;                     //坐标偏移量，用于控制扇形间隔
    private Float innerCircleP;             //内圆缩放比例
    private Float outerCircleP;             //外圆缩放比例
    private Paint paint;
    private static final int PADDING = 10;
    private TurntableClickListener clickListener;
    private boolean clickInRing = false;
    private boolean longClicking = false;
    private String[] text ;

    public TurntableView(Context context) {
        super(context);
        init();
    }

    public TurntableView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurntableView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TurntableView(Context context,Builder builder) {
        super(context);
        this.circleRadius = builder.circleRadius;
        this.turntableColor = builder.turntableColor;
        this.textColor = builder.textColor;
        this.showShadow = builder.showShadow;
        this.turntableFill = builder.turntableFill;
        this.offset = builder.offset;
        this.innerCircleP = builder.innerCircleP;
        this.outerCircleP = builder.outerCircleP;

        init();
    }

    private void init() {

        text = new String[]{"","","",""};

        circleX = (int) (circleRadius * outerCircleP + PADDING);
        circleY = (int) (circleRadius * outerCircleP + PADDING);

        paint = new Paint();
        paint.setAntiAlias(true);                                     //设置是否抗锯齿;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setStrokeWidth(2);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickInRing){
                    clickInRing = false;
                    if (clickListener != null){
                        clickListener.onClick();
                    }
                    Log.d("TurntableView", "onClick: ");
                }
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (clickInRing){
                    clickInRing = false;
                    longClicking = true;
                    if (clickListener != null){
                        clickListener.onLongClick();
                    }
                    Log.d("TurntableView", "onLongClick: ");
                }
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTurntable(canvas);
        drawTurntableText(canvas);
    }

    private void drawTurntable(Canvas canvas){
        RectF rectF;
        int angle;
        int x;
        int y;

        paint.setColor(turntableColor);
        if (turntableFill){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
        }else {
            paint.setStyle(Paint.Style.STROKE);
        }
        if (showShadow){
            paint.setShadowLayer(5,5,5,Color.GRAY);
        }else {
            paint.clearShadowLayer();
        }
        y = circleY - offset;

        x = getCoordinateX((int)(circleRadius*outerCircleP),y,1);
        rectF = CreateRectF((int)(circleRadius*outerCircleP));
        angle = getAngle(x-circleX,offset);
        drawArc(rectF,angle,canvas,turntableFill);

        if (turntableFill){
            paint.setColor(Color.WHITE);
            paint.clearShadowLayer();
        }

        x = getCoordinateX((int)(circleRadius*innerCircleP),y,1);
        rectF = CreateRectF((int)(circleRadius*innerCircleP));
        angle = getAngle(x-circleX,offset);
        drawArc(rectF,angle,canvas,turntableFill);

        makeSector(canvas);
        wipeEraser(canvas);

    }

    private int getCoordinateX(int r,int y,int LR){
        double variable;
        variable = Math.pow(r,2)-Math.pow((y-circleY),2);
        if (LR == 0){
            variable = circleX - Math.sqrt(variable);
        }else {
            variable = circleX + Math.sqrt(variable);
        }
        return (int)variable;
    }

    private int getCoordinateY(int r,int x,int TB){
        double variable;
        variable = Math.pow(r,2)-Math.pow((x-circleX),2);
        if (TB == 0){
            variable = circleY - Math.sqrt(variable);
        }else {
            variable = circleY + Math.sqrt(variable);
        }
        return (int)variable;
    }

    private int getAngle(int x,int y){
        double angle;
        angle = Math.atan(y*1.0/x)*180/Math.PI;
        return (int)angle;
    }

    private RectF CreateRectF(int transformationRatio){
        int left;
        int top;
        int right;
        int bottom;
        RectF rectF;

        left = circleX - transformationRatio;
        top = circleY - transformationRatio;
        right = circleX + transformationRatio;
        bottom = circleY + transformationRatio;
        rectF = new RectF(left,top,right,bottom);

        return rectF;
    }

    private void drawArc(RectF rectF,int gapAngle,Canvas canvas,boolean fill){
        int angle = 90 - gapAngle*2;
        canvas.drawArc(rectF,gapAngle,angle,fill,paint);
        canvas.drawArc(rectF,90 + gapAngle,angle,fill,paint);
        canvas.drawArc(rectF,180 + gapAngle,angle,fill,paint);
        canvas.drawArc(rectF,270 + gapAngle,angle,fill,paint);
    }

    private void makeSector(Canvas canvas){
        int common;
        int innerRadius;
        int outerRadius;

        paint.setColor(turntableColor);
        innerRadius = (int)(circleRadius*innerCircleP);
        outerRadius= (int)(circleRadius*outerCircleP);

        common = circleY - offset;
        drawHorizontalPath(canvas,common,innerRadius,outerRadius);

        common = circleY + offset;
        drawHorizontalPath(canvas,common,innerRadius,outerRadius);

        common = circleX - offset;
        drawVerticalPath(canvas,common,innerRadius,outerRadius);

        common = circleX + offset;
        drawVerticalPath(canvas,common,innerRadius,outerRadius);
    }

    //擦除多余的填充
    private void wipeEraser(Canvas canvas){
        if (!turntableFill)return;
        int a,b;
        int common;
        int outerRadius;
        Path path = new Path();

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.clearShadowLayer();
        outerRadius= (int)(circleRadius*outerCircleP);
        common = circleY - offset;

        a = getCoordinateX(outerRadius,common,1) + 10;
        b = getCoordinateX(outerRadius,common,0) - 10;

        path.moveTo(b,common);
        path.lineTo(b,common + offset*2);
        path.lineTo(a,common + offset*2);
        path.lineTo(a,common);
        path.lineTo(b,common);
        canvas.drawPath(path,paint);

        common = circleX - offset;
        a = getCoordinateY(outerRadius,common,1) + 10;
        b = getCoordinateY(outerRadius,common,0) - 10;

        path.moveTo(common,b);
        path.lineTo(common,a);
        path.lineTo(common + offset*2,a);
        path.lineTo(common + offset*2,b);
        path.lineTo(common,b);
        canvas.drawPath(path,paint);

    }

    private void drawHorizontalPath(Canvas canvas,int common,int innerRadius,int outerRadius){
        int a,b,c,d;
        Path path = new Path();

        a = getCoordinateX(innerRadius,common,1);
        b = getCoordinateX(outerRadius,common,1);
        c = getCoordinateX(innerRadius,common,0);
        d = getCoordinateX(outerRadius,common,0);

        path.moveTo(b,common);
        path.lineTo(a,common);
        path.moveTo(c,common);
        path.lineTo(d,common);

        canvas.drawPath(path,paint);
    }

    private void drawVerticalPath(Canvas canvas,int common,int innerRadius,int outerRadius){
        int a,b,c,d;
        Path path = new Path();

        a = getCoordinateY(innerRadius,common,1);
        b = getCoordinateY(outerRadius,common,1);
        c = getCoordinateY(innerRadius,common,0);
        d = getCoordinateY(outerRadius,common,0);

        path.moveTo(common,b);
        path.lineTo(common,a);
        path.moveTo(common,c);
        path.lineTo(common,d);

        canvas.drawPath(path,paint);
    }

    private void drawTurntableText(Canvas canvas){
        int angle;
        int x;
        int y;

        y = circleY - offset;
        x = getCoordinateX((int)(circleRadius*outerCircleP),y,1);
        angle = getAngle(x-circleX,offset);
        paint.setColor(textColor);
        paint.setTextSize(25);
        paint.clearShadowLayer();

        drawText(canvas,angle,angle,text[0]);

        drawText(canvas,90+angle,angle,text[1]);

        drawText(canvas,180+angle,angle,text[2]);

        drawText(canvas,270+angle,angle,text[3]);
    }

    private void drawText(Canvas canvas,int startAngle,int angle,String text){
        Path path;
        float textWidth;
        float hOffset;
        float vOffset;
        RectF rectF;

        path = new Path();
        rectF = CreateRectF((int)(circleRadius*outerCircleP));
        path.addArc(rectF,startAngle,90 - angle*2);
        textWidth = paint.measureText(text);
        vOffset = circleRadius*outerCircleP/4.0F;
        hOffset = ( float ) (Math.sin((45-angle)*Math.PI/180)) * (circleRadius*outerCircleP) - textWidth / 2;
        canvas.drawTextOnPath(text,path,hOffset,vOffset,paint);

    }

    public void showShadow(boolean isShow){
        this.showShadow = isShow;
        invalidate();
    }

    public boolean getShadowState(){
        return showShadow;
    }

    public String[] getText() {
        return text;
    }

    public void showText(String[] text) {
        this.text =  text;
        invalidate();
    }

    public boolean getLonClickState(){
        return longClicking;
    }

    public void setText(String[] text) {
        this.text = text;
    }

    public static class Builder{
        private int circleRadius;
        private int turntableColor;
        private int textColor;
        private boolean showShadow;
        private boolean turntableFill;
        private int offset;                 //坐标偏移量，用于控制扇形间隔
        private Float innerCircleP;         //内圆缩放比例
        private Float outerCircleP;         //外圆缩放比例

        public Builder setCircleRadius(int circleRadius) {
            this.circleRadius = circleRadius;
            return this;
        }

        public Builder setTurntableColor(int turntableColor) {
            this.turntableColor = turntableColor;
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

        public Builder setTurntableFill(boolean turntableFill) {
            this.turntableFill = turntableFill;
            return this;
        }

        public Builder setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder setInnerCircleP(Float innerCircleP) {
            this.innerCircleP = innerCircleP;
            return this;
        }

        public Builder setOuterCircleP(Float outerCircleP) {
            this.outerCircleP = outerCircleP;
            return this;
        }

        public TurntableView createTurntableView(Context context){
            return new TurntableView(context,this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int)((circleRadius*outerCircleP+PADDING)*2),(int)((circleRadius*outerCircleP+PADDING)*2));
    }

    private boolean clickInRing(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();
        int distance = (int)Math.sqrt(Math.pow((x-circleX),2) + Math.pow((y-circleY),2));
        if (distance >= circleRadius*innerCircleP && distance <= circleRadius*outerCircleP){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        clickInRing = clickInRing(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (longClicking){
                    if (clickListener != null){
                        clickListener.moving((int)event.getRawX(),(int)event.getRawY());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (longClicking){
                    if (clickListener != null){
                        clickListener.movingFinish((int)event.getRawX(),(int)event.getRawY());
                    }
                    longClicking = false;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface TurntableClickListener{
        public void onClick();
        public void onLongClick();
        public void moving(int x, int y);
        public void movingFinish(int x, int y);
    }

    public void setTurntableClickListener(TurntableClickListener clickListener){
        this.clickListener = clickListener;
    }
}
