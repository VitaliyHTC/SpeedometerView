package com.vitaliyhtc.speedometerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
    private static final float DEFAULT_INTERNAL_SECTOR_RADIUS = 72.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS = 96.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS_OVER_INTERNAL = 10.0f;

    private static final int DEFAULT_SPEED_NOTCHING_INTERVAL = 10;
    // next 2 values must be multiples of DEFAULT_SPEED_NOTCHING_INTERVAL
    private static final int DEFAULT_BOTTOM_SPEEDOMETER_SPEED = 60;
    private static final int DEFAULT_MAXIMUM_SPEEDOMETER_SPEED = 120;

    private Paint mOuterCirclePaint;
    private Paint mDigitsPaint;
    private Rect mDigitsBoundRect;
    private RectF mOuterCircleOval;
    private DialSpeedometerView mDialSpeedometerView;

    private Paint mArrowCenterPaint;
    private Paint mArrowPaint;
    private Paint mSectorBeforeArrowPaint;
    private Paint mSectorAfterArrowPaint;
    private RectF mSectorBeforeOval;
    private RectF mSectorAfterOval;
    private ArrowAndSectorsView  mArrowAndSectorsView;

    private static final int STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER = 72;
    private static final int OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER = 2;
    private static final int NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER = 3;
    private static final int DIGITS_SIZE_FROM_VIEW_WIDTH_DIVIDER = 24;
    private static final int ARROW_CENTER_RADIUS_FROM_VIEW_WIDTH_DIVIDER = 24;
    private static final int ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER = 40;





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
            int revalidatedInterval = getRevalidatedSpeedNotchingInterval(preMaximumSpeedometerSpeed);
            if(preMaximumSpeedometerSpeed > DEFAULT_BOTTOM_SPEEDOMETER_SPEED){
                mMaximumSpeedometerSpeed = ((preMaximumSpeedometerSpeed+revalidatedInterval-1)/revalidatedInterval)*revalidatedInterval;
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



        mDialSpeedometerView.layout(0, 0, width, width/2);
        mArrowAndSectorsView.layout(0, 0, width, width*5/9);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(height < width*5/9){
            height = width*5/9;
        }

        Log.e("SpeedometerView", "W="+width+"; H="+height+";");

        setMeasuredDimension(width, height);
    }

    private void init(){
        setLayerToSW(this);



        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDigitsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDigitsBoundRect = new Rect();
        mOuterCircleOval = new RectF();
        mDialSpeedometerView = new DialSpeedometerView(getContext());
        addView(mDialSpeedometerView);



        mArrowCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorBeforeArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorAfterArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorBeforeOval = new RectF();
        mSectorAfterOval = new RectF();
        mArrowAndSectorsView = new ArrowAndSectorsView(getContext());
        addView(mArrowAndSectorsView);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);

        Log.e("SpeedometerView", "ViewGroup onDraw()");
    }

















    private void setLayerToSW(View v) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void setLayerToHW(View v) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }










































    private class DialSpeedometerView extends View {

        public DialSpeedometerView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            //Log.e("SpeedometerView", "OuterCircleView onDraw()");

            int width = getWidth();
            int height = getHeight();

            int centerX = width/2;
            int centerY = centerX;

            int mStrokeWidth = width / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER;
            int radius = width/2 - OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            int notchingLength = NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            //Log.e("OuterCircleView", "onDraw(): "+width+", "+height+", "+radius+", "+centerX+", "+centerY+";");

            // OuterCircle draw
            mOuterCirclePaint.setColor(mOuterCircleColor);
            mOuterCirclePaint.setStrokeWidth(mStrokeWidth);
            mOuterCirclePaint.setStyle(Paint.Style.STROKE);

            mDigitsPaint.setColor(mDigitsColor);
            //mDigitsPaint.setStrokeWidth(mStrokeWidth);
            mDigitsPaint.setStyle(Paint.Style.FILL);

            mOuterCircleOval.set(centerX-radius, centerY - radius, centerX+radius, centerY+radius);
            canvas.drawArc(mOuterCircleOval, 180, 180, false, mOuterCirclePaint);

            // Notches and digits draw
            int revalidatedSpeedNotchingInterval = getRevalidatedSpeedNotchingInterval(mMaximumSpeedometerSpeed);
            int notchingsCount = mMaximumSpeedometerSpeed/revalidatedSpeedNotchingInterval; //you need add 1 for angle calculation
            double anglePart = Math.PI/(notchingsCount+1);
            double alpha;
            int internalNotchingRadius = radius - notchingLength;
            float digitsTextSize = width * 1f / DIGITS_SIZE_FROM_VIEW_WIDTH_DIVIDER;
            int internalDigitsRadius = internalNotchingRadius - (int)digitsTextSize;
            int startX;
            int startY;
            int stopX;
            int stopY;
            int digitX;
            int digitY;
            int digits;
            String digitsString;
            float digitsWidth;
            float digitsHeight;
            mDigitsPaint.setTextSize(digitsTextSize);
            mDigitsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            for (int i = 1; i <= notchingsCount; i++) {
                alpha = anglePart * i;
                startX = (int)((-1)*radius*Math.cos(alpha)) + centerX;
                startY = (int)((-1)*radius*Math.sin(alpha)) + centerY;
                stopX = (int)((-1)*internalNotchingRadius*Math.cos(alpha)) + centerX;
                stopY = (int)((-1)*internalNotchingRadius*Math.sin(alpha)) + centerY;
                digitX = (int)((-1)*internalDigitsRadius*Math.cos(alpha)) + centerX;
                digitY = (int)((-1)*internalDigitsRadius*Math.sin(alpha)) + centerY;
                canvas.drawLine(startX, startY, stopX, stopY, mOuterCirclePaint);

                digits = revalidatedSpeedNotchingInterval * i;
                digitsString = digits +"";
                mDigitsPaint.getTextBounds(digitsString, 0, digitsString.length(), mDigitsBoundRect);
                digitsWidth = mDigitsPaint.measureText(digitsString);
                digitsHeight = mDigitsBoundRect.height();
                canvas.drawText(digitsString, digitX - (digitsWidth/2f), digitY + (digitsHeight/2f), mDigitsPaint);
            }
        }
    }



    private class ArrowAndSectorsView extends View {

        public ArrowAndSectorsView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            int centerX = width/2;
            int centerY = centerX;

            int arrowCenterRadius = width / ARROW_CENTER_RADIUS_FROM_VIEW_WIDTH_DIVIDER;

            mArrowCenterPaint.setColor(mArrowColor);
            mArrowCenterPaint.setStyle(Paint.Style.FILL);
            mArrowPaint.setColor(mArrowColor);
            mArrowPaint.setStyle(Paint.Style.STROKE);
            mArrowPaint.setStrokeWidth(width/ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER);



            double startAngle = 1;
            mSectorBeforeArrowPaint.setStyle(Paint.Style.STROKE);
            mSectorAfterArrowPaint.setStyle(Paint.Style.STROKE);
            mSectorBeforeArrowPaint.setColor(mSectorBeforeArrowColor);
            mSectorAfterArrowPaint.setColor(mSectorAfterArrowColor);
            float strokeWidth = mExternalSectorRadius - mInternalSectorRadius;
            mSectorBeforeArrowPaint.setStrokeWidth(strokeWidth);
            mSectorAfterArrowPaint.setStrokeWidth(strokeWidth);



            float radius = mExternalSectorRadius;
            mSectorBeforeOval.set(centerX-radius+strokeWidth/2, centerY - radius+strokeWidth/2, centerX+radius-strokeWidth/2, centerY+radius-strokeWidth/2);
            mSectorAfterOval.set(centerX-radius+strokeWidth/2, centerY - radius+strokeWidth/2, centerX+radius-strokeWidth/2, centerY+radius-strokeWidth/2);
            canvas.drawArc(mSectorBeforeOval, 180, (float)radiansToDegrees(startAngle), false, mSectorBeforeArrowPaint);
            canvas.drawArc(mSectorAfterOval, 180+(float)radiansToDegrees(startAngle), 180-(float)radiansToDegrees(startAngle), false, mSectorAfterArrowPaint);



            int startX = (int)((-1)*mArrowRadius*Math.cos(startAngle)) + centerX;
            int startY = (int)((-1)*mArrowRadius*Math.sin(startAngle)) + centerY;
            canvas.drawCircle(centerX, centerY, arrowCenterRadius, mArrowCenterPaint);
            canvas.drawLine(startX, startY, centerX, centerY, mArrowPaint);
        }
    }



    // Numbers bigger than 2000 no have sense, better idea to add x10 x100 x1000 multiplier mark.
    private int getRevalidatedSpeedNotchingInterval(int maximumSpeed){
        int revalidatedSpeedNotchingInterval;
        if (maximumSpeed <= 160) {
            revalidatedSpeedNotchingInterval = DEFAULT_SPEED_NOTCHING_INTERVAL;
        } else if (maximumSpeed <= 300) {
            revalidatedSpeedNotchingInterval = DEFAULT_SPEED_NOTCHING_INTERVAL * 2;
        } else if (maximumSpeed <= 600) {
            revalidatedSpeedNotchingInterval = DEFAULT_SPEED_NOTCHING_INTERVAL * 4;
        } else if (maximumSpeed <= 1200) {
            revalidatedSpeedNotchingInterval = DEFAULT_SPEED_NOTCHING_INTERVAL * 10;
        } else {
            revalidatedSpeedNotchingInterval = DEFAULT_SPEED_NOTCHING_INTERVAL * 20;
        }
        return revalidatedSpeedNotchingInterval;
    }

    private double radiansToDegrees(double radians){
        return radians*(180/Math.PI);
    }

    private double degreesToRadians(double degrees){
        return degrees*(Math.PI/180);
    }

}
