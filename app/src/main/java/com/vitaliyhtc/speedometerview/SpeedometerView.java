package com.vitaliyhtc.speedometerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vitaliyhtc on 30.03.17.
 */

public class SpeedometerView extends ViewGroup {

    private int mBackgroundColor;
    private int mDigitsColor;
    private int mSectorBeforeArrowColor;
    private int mSectorAfterArrowColor;
    private float mArrowRadius;
    private int mOuterCircleColor;
    private int mArrowColor;
    private float mInternalSectorRadius;
    private float mExternalSectorRadius;
    private int mMaximumSpeedometerSpeed;

    private static final float DEFAULT_ARROW_RADIUS = 128.0f;
    private static final float DEFAULT_INTERNAL_SECTOR_RADIUS = 64.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS = 96.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS_OVER_INTERNAL = 10.0f;

    private static final int DEFAULT_SPEED_NOTCHING_INTERVAL = 10;
    // next 2 values must be multiples of DEFAULT_SPEED_NOTCHING_INTERVAL
    private static final int DEFAULT_BOTTOM_SPEEDOMETER_SPEED = 60;
    private static final int DEFAULT_MAXIMUM_SPEEDOMETER_SPEED = 120;


    private Paint mOuterCirclePaint;
    private RectF mOval;
    private OuterCircleView mOuterCircleView;

    private static final int STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER = 72;
    private static final int OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER = 2;
    private static final int NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER = 4;





    /**
     * Class constructor taking only context. Use this constructor to create
     * {@link SpeedometerView} objects from your own code.
     *
     * @param context
     */
    public SpeedometerView(Context context) {
        super(context);
        setWillNotDraw(false);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link SpeedometerView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link com.vitaliyhtc.speedometerview.R.styleable} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpeedometerView, 0, 0);
        try{
            mBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, 0xffffffff);
            mDigitsColor = a.getColor(R.styleable.SpeedometerView_digitsColor, 0xff212121);
            mSectorBeforeArrowColor = a.getColor(R.styleable.SpeedometerView_sectorBeforeArrowColor, 0xff00897b);
            mSectorAfterArrowColor = a.getColor(R.styleable.SpeedometerView_sectorAfterArrowColor, 0xff01479b);

            float preArrowRadius = a.getDimension(R.styleable.SpeedometerView_arrowRadius, DEFAULT_ARROW_RADIUS);
            if(preArrowRadius > 0){
                mArrowRadius = preArrowRadius;
            }else{
                mArrowRadius = DEFAULT_ARROW_RADIUS;
            }

            mOuterCircleColor = a.getColor(R.styleable.SpeedometerView_outerCircleColor, 0xff212121);
            mArrowColor = a.getColor(R.styleable.SpeedometerView_arrowColor, 0xff212121);


            float preInternalSectorRadius = a.getDimension(R.styleable.SpeedometerView_internalSectorRadius, DEFAULT_INTERNAL_SECTOR_RADIUS);
            if(preInternalSectorRadius > 0){
                mInternalSectorRadius = preInternalSectorRadius;
            }else{
                mInternalSectorRadius = DEFAULT_INTERNAL_SECTOR_RADIUS;
            }

            float preExternalSectorRadius = a.getDimension(R.styleable.SpeedometerView_externalSectorRadius, DEFAULT_EXTERNAL_SECTOR_RADIUS);
            if(preExternalSectorRadius > mInternalSectorRadius){
                mExternalSectorRadius = preExternalSectorRadius;
            }else{
                if(mInternalSectorRadius < DEFAULT_EXTERNAL_SECTOR_RADIUS){
                    mExternalSectorRadius = DEFAULT_EXTERNAL_SECTOR_RADIUS;
                }else{
                    mExternalSectorRadius = mInternalSectorRadius+DEFAULT_EXTERNAL_SECTOR_RADIUS_OVER_INTERNAL;
                }
            }

            int preMaximumSpeedometerSpeed = a.getInt(R.styleable.SpeedometerView_maximumSpeedometerSpeed, DEFAULT_MAXIMUM_SPEEDOMETER_SPEED);
            if(preMaximumSpeedometerSpeed > DEFAULT_BOTTOM_SPEEDOMETER_SPEED){
                mMaximumSpeedometerSpeed = ((preMaximumSpeedometerSpeed+DEFAULT_SPEED_NOTCHING_INTERVAL-1)/DEFAULT_SPEED_NOTCHING_INTERVAL)*DEFAULT_SPEED_NOTCHING_INTERVAL;
            }else{
                mMaximumSpeedometerSpeed = DEFAULT_MAXIMUM_SPEEDOMETER_SPEED;
            }
        } finally {
            a.recycle();
        }

        init();
    }




    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return mDigitsColor;
    }

    public void setDigitsColor(int digitsColor) {
        this.mDigitsColor = digitsColor;
        invalidate();
    }

    public int getSectorBeforeArrowColor() {
        return mSectorBeforeArrowColor;
    }

    public void setSectorBeforeArrowColor(int sectorBeforeArrowColor) {
        this.mSectorBeforeArrowColor = sectorBeforeArrowColor;
        invalidate();
    }

    public int getSectorAfterArrowColor() {
        return mSectorAfterArrowColor;
    }

    public void setSectorAfterArrowColor(int sectorAfterArrowColor) {
        this.mSectorAfterArrowColor = sectorAfterArrowColor;
        invalidate();
    }

    public float getArrowRadius() {
        return mArrowRadius;
    }

    public void setArrowRadius(float arrowRadius) {
        if(arrowRadius>0){
            this.mArrowRadius = arrowRadius;
        }else{
            this.mArrowRadius = DEFAULT_ARROW_RADIUS;
        }
        invalidate();
    }

    public int getOuterCircleColor() {
        return mOuterCircleColor;
    }

    public void setOuterCircleColor(int outerCircleColor) {
        this.mOuterCircleColor = outerCircleColor;
        invalidate();
    }

    public int getArrowColor() {
        return mArrowColor;
    }

    public void setArrowColor(int arrowColor) {
        this.mArrowColor = arrowColor;
        invalidate();
    }

    public float getInternalSectorRadius() {
        return mInternalSectorRadius;
    }

    public void setInternalSectorRadius(float internalSectorRadius) {
        if(internalSectorRadius > 0){
            mInternalSectorRadius = internalSectorRadius;
        }else{
            mInternalSectorRadius = DEFAULT_INTERNAL_SECTOR_RADIUS;
        }
        invalidate();
    }

    public float getExternalSectorRadius() {
        return mExternalSectorRadius;
    }

    public void setExternalSectorRadius(float externalSectorRadius) {
        if(externalSectorRadius > mInternalSectorRadius){
            mExternalSectorRadius = externalSectorRadius;
        }else{
            if(mInternalSectorRadius < DEFAULT_EXTERNAL_SECTOR_RADIUS){
                mExternalSectorRadius = DEFAULT_EXTERNAL_SECTOR_RADIUS;
            }else{
                mExternalSectorRadius = mInternalSectorRadius+DEFAULT_EXTERNAL_SECTOR_RADIUS_OVER_INTERNAL;
            }
        }
        invalidate();
    }

    public int getMaximumSpeedometerSpeed() {
        return mMaximumSpeedometerSpeed;
    }

    public void setMaximumSpeedometerSpeed(int maximumSpeedometerSpeed) {
        if(maximumSpeedometerSpeed > DEFAULT_BOTTOM_SPEEDOMETER_SPEED){
            //mMaximumSpeedometerSpeed = ((maximumSpeedometerSpeed+9)/10)*10;
            mMaximumSpeedometerSpeed = ((maximumSpeedometerSpeed+DEFAULT_SPEED_NOTCHING_INTERVAL-1)/DEFAULT_SPEED_NOTCHING_INTERVAL)*DEFAULT_SPEED_NOTCHING_INTERVAL;
        }else{
            mMaximumSpeedometerSpeed = DEFAULT_MAXIMUM_SPEEDOMETER_SPEED;
        }
        invalidate();
    }


















    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // next 2 lines not correct.
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. SpeedometerView lays out its children in onSizeChanged().

        // l, t, r, b - absolute numbers. To get relative - you must subtract t and l coordinate from r and b.
        // 0, 0, r-l, b-t, - same as 0, 0, getWidth(), getHeight()
        //Log.e("onLayout:: ", "l="+l+"; t="+t+"; r="+r+"; b="+b+";");
        //Log.e("onLayout:: ", "rr="+(r-l)+"; rb="+(b-t)+"; width="+getWidth()+"; height="+getHeight()+";");

        int width = getWidth();
        int height = getHeight();



        mOuterCircleView.layout(0, 0, width, width/2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        Log.e("SpeedometerView", "W="+width+"; H="+height+";");

        setMeasuredDimension(width, height);
    }

    private void init(){
        setLayerToSW(this);



        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOval = new RectF();
        mOuterCircleView = new OuterCircleView(getContext());
        addView(mOuterCircleView);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawARGB(0xff, 0xf0, 0xf0, 0xf0);
        canvas.drawColor(mBackgroundColor);

        Log.e("SpeedometerView", "ViewGroup onDraw()");
    }

















    private void setLayerToSW(View v) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void setLayerToHW(View v) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }










































    private class OuterCircleView extends View {

        public OuterCircleView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            Log.e("SpeedometerView", "OuterCircleView onDraw()");



            int width = getWidth();
            int height = getHeight();

            int mStrokeWidth = width / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER;
            int radius = width/2 - OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            int notchingLenght = NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            int centerX = width/2;
            int centerY = height;

            Log.e("OuterCircleView", "onDraw(): "+width+", "+height+", "+radius+", "+centerX+", "+centerY+";");



            // OuterCircle draw
            mOuterCirclePaint.setColor(mOuterCircleColor);
            mOuterCirclePaint.setStrokeWidth(mStrokeWidth);

            mOuterCirclePaint.setStyle(Paint.Style.STROKE);
            mOval.set(centerX-radius, centerY - radius, centerX+radius, centerY+radius);
            canvas.drawArc(mOval, 180, 180, false, mOuterCirclePaint);

            // Notches draw
            int notchingsCount = mMaximumSpeedometerSpeed/DEFAULT_SPEED_NOTCHING_INTERVAL; //you need add 1 for angle calculation
            double anglePart = Math.PI/(notchingsCount+1);
            double alpha;
            int internalNotchingRadius = radius - notchingLenght;
            int startX;
            int startY;
            int stopX;
            int stopY;
            for (int i = 1; i <= notchingsCount; i++) {
                alpha = anglePart * i;
                startX = (int)((-1)*radius*Math.cos(alpha)) + centerX;
                startY = (int)((-1)*radius*Math.sin(alpha)) + centerY;
                stopX = (int)((-1)*internalNotchingRadius*Math.cos(alpha)) + centerX;
                stopY = (int)((-1)*internalNotchingRadius*Math.sin(alpha)) + centerY;
                canvas.drawLine(startX, startY, stopX, stopY, mOuterCirclePaint);
            }
        }
    }












}
