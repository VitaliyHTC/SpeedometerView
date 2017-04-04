package com.vitaliyhtc.speedometerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by VitaliyHTC on 30.03.17.
 */

public class SpeedometerView extends ViewGroup {

    private static final float DEFAULT_ARROW_RADIUS = 96.0f;
    private static final float DEFAULT_INTERNAL_SECTOR_RADIUS = 64.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS = 48.0f;

    private static final int DEFAULT_SPEED_NOTCHING_INTERVAL = 10;
    // next 2 values must be multiples of DEFAULT_SPEED_NOTCHING_INTERVAL
    private static final int DEFAULT_BOTTOM_SPEEDOMETER_SPEED = 60;
    private static final int DEFAULT_MAXIMUM_SPEEDOMETER_SPEED = 120;
    private static final int DEFAULT_TOP_SPEEDOMETER_SPEED = 2000;

    private static final int STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER = 72;
    private static final int OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER = 2;
    private static final int NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER = 3;
    private static final int DIGITS_SIZE_FROM_VIEW_WIDTH_DIVIDER = 24;
    private static final int ARROW_CENTER_RADIUS_FROM_VIEW_WIDTH_DIVIDER = 24;
    private static final int ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER = 40;

    private static final float OIL_AND_LEVEL_VERTICAL_POSITION_TO_VIEW_HEIGHT_MULTIPLIER = (float) 1 / 2;
    private static final float OIL_AND_LEVEL_WIDTH_FROM_VIEW_WIDTH_MULTIPLIER = (float) 1 / 4;



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



    private DialSpeedometerView mDialSpeedometerView;
    private ArrowAndSectorsView mArrowAndSectorsView;
    private OilCanAndLevelView mOilCanAndLevelView;



    private float mSpeed;
    private float mEnergyLevel;

    private boolean isTrottlePedalPressed;
    private boolean isBrakePedalPressed;



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
            mBackgroundColor = a.getColor(R.styleable.SpeedometerView_sv_backgroundColor, 0xffffffff);
            mDigitsColor = a.getColor(R.styleable.SpeedometerView_sv_digitsColor, 0xff212121);
            mSectorBeforeArrowColor = a.getColor(R.styleable.SpeedometerView_sv_sectorBeforeArrowColor, 0xff00897b);
            mSectorAfterArrowColor = a.getColor(R.styleable.SpeedometerView_sv_sectorAfterArrowColor, 0xff01479b);

            float preArrowRadius = a.getDimension(R.styleable.SpeedometerView_sv_arrowRadius, convertDpToPixels(DEFAULT_ARROW_RADIUS, context));
            if (preArrowRadius > 0) {
                mArrowRadius = preArrowRadius;
            } else {
                throw new IllegalArgumentException("Arrow radius must be positive, found " + preArrowRadius);
            }

            mOuterCircleColor = a.getColor(R.styleable.SpeedometerView_sv_outerCircleColor, 0xff212121);
            mArrowColor = a.getColor(R.styleable.SpeedometerView_sv_arrowColor, 0xff212121);

            float preInternalSectorRadius = a.getDimension(R.styleable.SpeedometerView_sv_internalSectorRadius, convertDpToPixels(DEFAULT_INTERNAL_SECTOR_RADIUS, context));
            if(preInternalSectorRadius > 0){
                mInternalSectorRadius = preInternalSectorRadius;
            }else{
                throw new IllegalArgumentException("Internal sector radius must be positive, found " + preInternalSectorRadius);
            }

            float preExternalSectorRadius = a.getDimension(R.styleable.SpeedometerView_sv_externalSectorRadius, convertDpToPixels(DEFAULT_EXTERNAL_SECTOR_RADIUS, context));
            if (preExternalSectorRadius > mInternalSectorRadius) {
                mExternalSectorRadius = preExternalSectorRadius;
            } else {
                throw new IllegalArgumentException("External sector radius must be greater than internal sector radius, found " + preExternalSectorRadius);
            }

            int preMaximumSpeedometerSpeed = a.getInt(R.styleable.SpeedometerView_sv_maximumSpeedometerSpeed, DEFAULT_MAXIMUM_SPEEDOMETER_SPEED);
            int revalidatedInterval = getRevalidatedSpeedNotchingInterval(preMaximumSpeedometerSpeed);
            if(preMaximumSpeedometerSpeed > DEFAULT_BOTTOM_SPEEDOMETER_SPEED && preMaximumSpeedometerSpeed < DEFAULT_TOP_SPEEDOMETER_SPEED){
                mMaximumSpeedometerSpeed = ((preMaximumSpeedometerSpeed+revalidatedInterval-1)/revalidatedInterval)*revalidatedInterval;
            }else{
                throw new IllegalArgumentException("Maximum speedometer speed must be greater than "
                        +DEFAULT_BOTTOM_SPEEDOMETER_SPEED+", and less than "+DEFAULT_TOP_SPEEDOMETER_SPEED+
                        ", found: "+preMaximumSpeedometerSpeed+";");
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
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return mDigitsColor;
    }

    public void setDigitsColor(int digitsColor) {
        mDigitsColor = digitsColor;
        invalidate();
    }

    public int getSectorBeforeArrowColor() {
        return mSectorBeforeArrowColor;
    }

    public void setSectorBeforeArrowColor(int sectorBeforeArrowColor) {
        mSectorBeforeArrowColor = sectorBeforeArrowColor;
        invalidate();
    }

    public int getSectorAfterArrowColor() {
        return mSectorAfterArrowColor;
    }

    public void setSectorAfterArrowColor(int sectorAfterArrowColor) {
        mSectorAfterArrowColor = sectorAfterArrowColor;
        invalidate();
    }

    public float getArrowRadius() {
        return mArrowRadius;
    }

    public void setArrowRadius(float arrowRadius) {
        if (arrowRadius > 0) {
            mArrowRadius = arrowRadius;
        } else {
            throw new IllegalArgumentException("Arrow radius must be positive, found " + arrowRadius);
        }
        invalidate();
    }

    public int getOuterCircleColor() {
        return mOuterCircleColor;
    }

    public void setOuterCircleColor(int outerCircleColor) {
        mOuterCircleColor = outerCircleColor;
        invalidate();
    }

    public int getArrowColor() {
        return mArrowColor;
    }

    public void setArrowColor(int arrowColor) {
        mArrowColor = arrowColor;
        invalidate();
    }

    public float getInternalSectorRadius() {
        return mInternalSectorRadius;
    }

    public void setInternalSectorRadius(float internalSectorRadius) {
        if(internalSectorRadius > 0){
            mInternalSectorRadius = internalSectorRadius;
        }else{
            throw new IllegalArgumentException("Internal sector radius must be positive, found " + internalSectorRadius);
        }
        invalidate();
    }

    public float getExternalSectorRadius() {
        return mExternalSectorRadius;
    }

    public void setExternalSectorRadius(float externalSectorRadius) {
        if (externalSectorRadius > mInternalSectorRadius) {
            mExternalSectorRadius = externalSectorRadius;
        } else {
            throw new IllegalArgumentException("External sector radius must be greater than internal sector radius, found " + externalSectorRadius);
        }
        invalidate();
    }

    public int getMaximumSpeedometerSpeed() {
        return mMaximumSpeedometerSpeed;
    }

    public void setMaximumSpeedometerSpeed(int maximumSpeedometerSpeed) {
        int revalidatedInterval = getRevalidatedSpeedNotchingInterval(maximumSpeedometerSpeed);
        if(maximumSpeedometerSpeed > DEFAULT_BOTTOM_SPEEDOMETER_SPEED && maximumSpeedometerSpeed < DEFAULT_TOP_SPEEDOMETER_SPEED){
            mMaximumSpeedometerSpeed = ((maximumSpeedometerSpeed+revalidatedInterval-1)/revalidatedInterval)*revalidatedInterval;
        }else{
            throw new IllegalArgumentException("Maximum speedometer speed must be greater than "
                    +DEFAULT_BOTTOM_SPEEDOMETER_SPEED+", and less than "+DEFAULT_TOP_SPEEDOMETER_SPEED+
                    ", found: "+maximumSpeedometerSpeed+";");
        }
        invalidate();
    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // l, t, r, b - absolute numbers. To get relative - you must subtract t and l coordinate from r and b.
        // 0, 0, r-l, b-t, - same as 0, 0, getWidth(), getHeight()
        //Log.e("onLayout:: ", "l="+l+"; t="+t+"; r="+r+"; b="+b+";");
        //Log.e("onLayout:: ", "rr="+(r-l)+"; rb="+(b-t)+"; width="+getWidth()+"; height="+getHeight()+";");

        int width = r-l;
        int height = b-t;

        int viewWidth;
        int viewHeight;
        int leftPosition;
        int topPosition;

        if (height >= width / 2) {
            viewWidth = width;
            viewHeight = width / 2;
            leftPosition = 0;
            topPosition = 0;
        } else {
            viewWidth = height * 2;
            viewHeight = height;
            leftPosition = (width - viewWidth) / 2;
            topPosition = 0;
        }

        mDialSpeedometerView.layout(leftPosition, topPosition, leftPosition+viewWidth, topPosition+viewHeight);
        mOilCanAndLevelView.layout(leftPosition, topPosition, leftPosition+viewWidth, topPosition+viewHeight);
        mArrowAndSectorsView.layout(leftPosition, topPosition, leftPosition+viewWidth, topPosition+viewHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = convertDpToPixels(256, getContext());
        int desiredHeight = convertDpToPixels(128, getContext());

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            height = heightSize;
        } else {
            height = desiredHeight;
        }

        //Log.e("SpeedometerView", "W="+width+"; H="+height+";");

        setMeasuredDimension(width, height);
    }

    private void init(){
        setLayerToSW(this);

        mDialSpeedometerView = new DialSpeedometerView(getContext());
        mDialSpeedometerView.init();
        addView(mDialSpeedometerView);

        mOilCanAndLevelView = new OilCanAndLevelView(getContext());
        mOilCanAndLevelView.init();
        addView(mOilCanAndLevelView);

        mArrowAndSectorsView = new ArrowAndSectorsView(getContext());
        mArrowAndSectorsView.init();
        addView(mArrowAndSectorsView);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);
    }



    private void setLayerToSW(View v) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void setLayerToHW(View v) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }



    private class DialSpeedometerView extends View {

        private Paint mOuterCirclePaint;
        private Paint mNotchesPaint;
        private Path mNotchesPath;
        private Matrix mNotchesMatrix;
        private Paint mDigitsPaint;
        private Rect mDigitsBoundRect;
        private Path mDigitsPath;
        private Matrix mDigitsMatrix;
        private RectF mOuterCircleOval;

        public DialSpeedometerView(Context context) {
            super(context);
        }

        public void init(){
            mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNotchesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNotchesPath = new Path();
            mNotchesMatrix = new Matrix();
            mDigitsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDigitsBoundRect = new Rect();
            mDigitsPath = new Path();
            mDigitsMatrix = new Matrix();
            mOuterCircleOval = new RectF();

            mOuterCirclePaint.setColor(mOuterCircleColor);
            mOuterCirclePaint.setStyle(Paint.Style.STROKE);
            mNotchesPaint.setColor(mOuterCircleColor);
            mNotchesPaint.setStyle(Paint.Style.FILL);
            mDigitsPaint.setColor(mDigitsColor);
            mDigitsPaint.setStyle(Paint.Style.FILL);
            mDigitsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            int width = getWidth();
            int centerX = width/2;
            int centerY = width/2;

            int mStrokeWidth = width / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER;
            int radius = width/2 - OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;
            int notchingLength = NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            mOuterCirclePaint.setStrokeWidth(mStrokeWidth);
            mDigitsPaint.setTextSize(getWidth() * 1f / DIGITS_SIZE_FROM_VIEW_WIDTH_DIVIDER);

            mOuterCircleOval.set(centerX-radius, centerY - radius, centerX+radius, centerY+radius);
            canvas.drawArc(mOuterCircleOval, 180, 180, false, mOuterCirclePaint);

            // Notches and digits draw
            int revalidatedSpeedNotchingInterval = getRevalidatedSpeedNotchingInterval(mMaximumSpeedometerSpeed);
            int notchingsCount = mMaximumSpeedometerSpeed/revalidatedSpeedNotchingInterval; //you need add 1 for angle calculation
            double anglePart = Math.PI/(notchingsCount+1);
            double alpha;

            int digits;
            String digitsString;
            float digitsWidth;
            float digitsHeight;

            mNotchesPath.reset();
            mNotchesPath.addRect(
                    centerX - radius,
                    centerY - mStrokeWidth/2,
                    centerX - radius + notchingLength,
                    centerY + mStrokeWidth/2,
                    Path.Direction.CW);
            mNotchesMatrix.reset();
            mNotchesMatrix.setRotate((float)radiansToDegrees(anglePart), centerX, centerY);

            for (int i = 1; i <= notchingsCount; i++) {
                alpha = anglePart * i;

                mNotchesPath.transform(mNotchesMatrix);
                canvas.drawPath(mNotchesPath, mNotchesPaint);

                digits = revalidatedSpeedNotchingInterval * i;
                digitsString = digits +"";
                mDigitsPaint.getTextBounds(digitsString, 0, digitsString.length(), mDigitsBoundRect);
                digitsWidth = mDigitsPaint.measureText(digitsString);
                digitsHeight = mDigitsBoundRect.height();

                int digitsPositionShift = centerX - radius + notchingLength + mStrokeWidth;
                mDigitsPath.reset();
                mDigitsPath.moveTo(digitsPositionShift, centerY);
                mDigitsPath.lineTo(digitsPositionShift + digitsWidth, centerY);
                mDigitsMatrix.reset();
                mDigitsMatrix.setRotate((-1)*(float)radiansToDegrees(alpha), digitsPositionShift + digitsWidth/2, centerY);
                mDigitsPath.transform(mDigitsMatrix);
                mDigitsMatrix.reset();
                mDigitsMatrix.setRotate((float)radiansToDegrees(alpha), centerX, centerY);
                mDigitsPath.transform(mDigitsMatrix);
                canvas.drawTextOnPath(digitsString, mDigitsPath, 0, digitsHeight/2, mDigitsPaint);
            }
        }
    }



    private class ArrowAndSectorsView extends View {

        private Paint mArrowCenterPaint;
        private Paint mArrowPaint;
        private Matrix mArrowMatrix;
        private Path mArrowPath;
        private Paint mSectorBeforeArrowPaint;
        private Paint mSectorAfterArrowPaint;
        private RectF mSectorBeforeOval;
        private RectF mSectorAfterOval;

        public ArrowAndSectorsView(Context context) {
            super(context);
        }

        public void init(){
            mArrowCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mArrowMatrix = new Matrix();
            mArrowPath = new Path();
            mSectorBeforeArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSectorAfterArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSectorBeforeOval = new RectF();
            mSectorAfterOval = new RectF();

            mArrowCenterPaint.setColor(mArrowColor);
            mArrowCenterPaint.setStyle(Paint.Style.FILL);
            mArrowPaint.setColor(mArrowColor);
            mArrowPaint.setStyle(Paint.Style.FILL);

            mSectorBeforeArrowPaint.setStyle(Paint.Style.STROKE);
            mSectorAfterArrowPaint.setStyle(Paint.Style.STROKE);
            mSectorBeforeArrowPaint.setColor(mSectorBeforeArrowColor);
            mSectorAfterArrowPaint.setColor(mSectorAfterArrowColor);
            mSectorBeforeArrowPaint.setStrokeWidth(mExternalSectorRadius - mInternalSectorRadius);
            mSectorAfterArrowPaint.setStrokeWidth(mExternalSectorRadius - mInternalSectorRadius);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int centerX = width/2;
            int centerY = width/2;

            double startAngle = 1;

            float strokeWidth = mExternalSectorRadius - mInternalSectorRadius;
            float radius = mExternalSectorRadius;
            mSectorBeforeOval.set(centerX-radius+strokeWidth/2, centerY - radius+strokeWidth/2, centerX+radius-strokeWidth/2, centerY+radius-strokeWidth/2);
            mSectorAfterOval.set(centerX-radius+strokeWidth/2, centerY - radius+strokeWidth/2, centerX+radius-strokeWidth/2, centerY+radius-strokeWidth/2);

            mArrowPath.reset();
            mArrowPath.addRect(
                    centerX-mArrowRadius,
                    centerY-width/(ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER*2),
                    centerX,
                    centerY+width/(ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER*2),
                    Path.Direction.CW);

            canvas.drawArc(mSectorBeforeOval, 180, (float)radiansToDegrees(startAngle), false, mSectorBeforeArrowPaint);
            canvas.drawArc(mSectorAfterOval, 180+(float)radiansToDegrees(startAngle), 180-(float)radiansToDegrees(startAngle), false, mSectorAfterArrowPaint);
            canvas.drawCircle(centerX, centerY, width / ARROW_CENTER_RADIUS_FROM_VIEW_WIDTH_DIVIDER, mArrowCenterPaint);

            mArrowMatrix.reset();
            mArrowMatrix.setRotate((float)radiansToDegrees(startAngle), centerX, centerY);
            mArrowPath.transform(mArrowMatrix);
            canvas.drawPath(mArrowPath, mArrowPaint);
        }
    }



    private class OilCanAndLevelView extends View {

        private Paint mOilCanPaint;
        private Bitmap mOilCanBitmap;
        private Rect mOilCanRect;
        private Paint mLevelPaint;

        float[] cmData = new float[]{
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0 };
        float[] cmDataGreen = new float[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0 };

        ColorMatrix mColorMatrix;
        ColorFilter mColorFilter;

        public OilCanAndLevelView(Context context) {
            super(context);
        }

        public void init(){
            mOilCanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOilCanBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_oil);
            mOilCanRect = new Rect();
            mLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mColorMatrix = new ColorMatrix(cmDataGreen);
            mColorFilter = new ColorMatrixColorFilter(mColorMatrix);

            mOilCanPaint.setColorFilter(mColorFilter);
            mLevelPaint.setColorFilter(mColorFilter);
            mLevelPaint.setColor(0xff000000);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int centerX = width / 2;
            int centerY = (int) (OIL_AND_LEVEL_VERTICAL_POSITION_TO_VIEW_HEIGHT_MULTIPLIER * centerX);
            int oilCanAndLevelViewWidth = (int) (width * OIL_AND_LEVEL_WIDTH_FROM_VIEW_WIDTH_MULTIPLIER);
            int oilCanAndLevelViewHeight = oilCanAndLevelViewWidth / 2;

            /*
            Log.e("OilCanAndLevelView", "width: "+width+"; height: "+height+"; centerX: "+centerX+"; centerY:"+centerY+
                    "; oilW: "+oilCanAndLevelViewWidth+"; oilH: "+oilCanAndLevelViewHeight+";");
            */

            mOilCanRect.set(
                    centerX - oilCanAndLevelViewWidth / 2,
                    centerY - oilCanAndLevelViewHeight / 3,
                    centerX - oilCanAndLevelViewWidth / 2 + oilCanAndLevelViewHeight * 2 / 3,
                    centerY + oilCanAndLevelViewHeight / 3);

            canvas.drawBitmap(mOilCanBitmap, null, mOilCanRect, mOilCanPaint);

            mLevelPaint.setStrokeWidth(width / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER);
            canvas.drawLine(
                    centerX - oilCanAndLevelViewWidth / 2 + oilCanAndLevelViewHeight * 15 / 24,
                    centerY,
                    centerX + oilCanAndLevelViewWidth / 2,
                    centerY,
                    mLevelPaint);
        }
    }



    // Numbers bigger than 2000 no have sense, better idea to add x10 x100 x1000 multiplier mark.
    // 03/04/17 you can set maximum speed restriction and no need to calculate these values.
    // Added 2000 top limit. Can be easy changed later.
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

    private int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }



    /*********************************************************************************************
     * other public methods *
     ********************************************************************************************/

    public void setArrowAccelerationSpeed(float accelerationSpeed){

    }

    public void setArrowAttenuationSpeed(float attenuationSpeed){

    }

    public void pressTrottlePedal(){
        isTrottlePedalPressed = true;
    }

    public void releaseTrottlePedal(){
        isTrottlePedalPressed = false;
    }

    public void pressBrakePedal(){
        isBrakePedalPressed = true;
    }

    public void releaseBrakePedal(){
        isBrakePedalPressed= false;
    }

    public void setEnergyLevel(float energyLevel){

    }

    public void setEnergyLevelChangeSpeed(float energyLevelChangeSpeedPerSecond){

    }




    public interface SpeedChange{
        void onSpeedChanged(int value);
    }
}
