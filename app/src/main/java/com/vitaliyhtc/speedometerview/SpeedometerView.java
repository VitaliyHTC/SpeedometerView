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

import java.util.ArrayList;
import java.util.List;

public class SpeedometerView extends ViewGroup {

    private static final int DEFAULT_BACKGROUND_COLOR = 0xffffffff;
    private static final int DEFAULT_DIGITS_COLOR = 0xff212121;
    private static final int DEFAULT_SECTOR_BEFORE_ARROW_COLOR = 0xff00897b;
    private static final int DEFAULT_SECTOR_AFTER_ARROW_COLOR = 0xff01479b;
    private static final int DEFAULT_OUTER_CIRCLE_COLOR = 0xff212121;
    private static final int DEFAULT_ARROW_COLOR = 0xff212121;

    private static final float DEFAULT_ARROW_RADIUS = 96.0f;
    private static final float DEFAULT_INTERNAL_SECTOR_RADIUS = 48.0f;
    private static final float DEFAULT_EXTERNAL_SECTOR_RADIUS = 64.0f;

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

    private static final float ENERGY_LEVEL_CAN_EMPTY = 9.0f;
    private static final float ENERGY_LEVEL_BLINK = 30.0f;
    private static final float ENERGY_LEVEL_BLINK_ALPHA_STEP = 0.05f;



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
    private float mArrowAccelerationSpeed;
    private float mArrowAttenuationSpeed;
    private float mEnergyLevelChangeSpeed;

    private volatile boolean isTrottlePedalPressed;
    private volatile boolean isBrakePedalPressed;
    private volatile boolean isSwitchedOn;

    private int mNotchesCount;

    private List<SpeedChangeListener> mSpeedChangeListenerList;

    /**
     * Class constructor taking only context. Use this constructor to create
     * {@link SpeedometerView} objects from your own code.
     *
     * @param context Context
     */
    public SpeedometerView(Context context) {
        super(context);
        setWillNotDraw(false);
        setDefaults();
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link SpeedometerView} from a set of
     * XML attributes.
     *
     * @param context Context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link com.vitaliyhtc.speedometerview.R.styleable} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpeedometerView, 0, 0);
        try{
            mBackgroundColor = a.getColor(R.styleable.SpeedometerView_sv_backgroundColor, DEFAULT_BACKGROUND_COLOR);
            mDigitsColor = a.getColor(R.styleable.SpeedometerView_sv_digitsColor, DEFAULT_DIGITS_COLOR);
            mSectorBeforeArrowColor = a.getColor(R.styleable.SpeedometerView_sv_sectorBeforeArrowColor, DEFAULT_SECTOR_BEFORE_ARROW_COLOR);
            mSectorAfterArrowColor = a.getColor(R.styleable.SpeedometerView_sv_sectorAfterArrowColor, DEFAULT_SECTOR_AFTER_ARROW_COLOR);

            float preArrowRadius = a.getDimension(R.styleable.SpeedometerView_sv_arrowRadius, convertDpToPixels(DEFAULT_ARROW_RADIUS, context));
            if (preArrowRadius > 0) {
                mArrowRadius = preArrowRadius;
            } else {
                throw new IllegalArgumentException("Arrow radius must be positive, found " + preArrowRadius);
            }

            mOuterCircleColor = a.getColor(R.styleable.SpeedometerView_sv_outerCircleColor, DEFAULT_OUTER_CIRCLE_COLOR);
            mArrowColor = a.getColor(R.styleable.SpeedometerView_sv_arrowColor, DEFAULT_ARROW_COLOR);

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



    /*********************************************************************************************
     * getters and setters *
     ********************************************************************************************/

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



    /*********************************************************************************************
     * init(), onMeasure(), onLayout(), onDraw(), ... *
     ********************************************************************************************/

    private void setDefaults(){
        mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
        mDigitsColor = DEFAULT_DIGITS_COLOR;
        mSectorBeforeArrowColor = DEFAULT_SECTOR_BEFORE_ARROW_COLOR;
        mSectorAfterArrowColor = DEFAULT_SECTOR_AFTER_ARROW_COLOR;
        mArrowRadius = convertDpToPixels(DEFAULT_ARROW_RADIUS, getContext());
        mOuterCircleColor = DEFAULT_OUTER_CIRCLE_COLOR;
        mArrowColor = DEFAULT_ARROW_COLOR;
        mInternalSectorRadius = convertDpToPixels(DEFAULT_INTERNAL_SECTOR_RADIUS, getContext());
        mExternalSectorRadius = convertDpToPixels(DEFAULT_EXTERNAL_SECTOR_RADIUS, getContext());
        mMaximumSpeedometerSpeed = DEFAULT_MAXIMUM_SPEEDOMETER_SPEED;
    }

    private void init(){
        setLayerToSW(this);

        mSpeedChangeListenerList = new ArrayList<>();

        mSpeed = 0;
        mNotchesCount = mMaximumSpeedometerSpeed/getRevalidatedSpeedNotchingInterval(mMaximumSpeedometerSpeed);

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
            if(height > width/2){
                height = width/2;
            }
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // l, t, r, b - absolute numbers. To get relative - you must subtract t and l coordinate from r and b.
        // 0, 0, r-l, b-t, - same as 0, 0, getWidth(), getHeight()

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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);
    }



    /*********************************************************************************************
     * Views *
     ********************************************************************************************/

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

        private int mWidth;
        private int mHeight;
        private int mCenterX;
        private int mCenterY;

        private int mStrokeWidth;
        private int mRadius;
        private int mNotchingLength;
        private int mRevalidatedSpeedNotchingInterval;
        private int mNotchingsCount;
        private double mAnglePart;

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

            mOuterCirclePaint.setStyle(Paint.Style.STROKE);
            mNotchesPaint.setStyle(Paint.Style.FILL);
            mDigitsPaint.setStyle(Paint.Style.FILL);
            mDigitsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mWidth = right - left;
            mHeight = bottom - top;
            mCenterX = mWidth/2;
            mCenterY = mWidth/2;

            mStrokeWidth = mWidth / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER;
            mOuterCirclePaint.setStrokeWidth(mStrokeWidth);
            mDigitsPaint.setTextSize(getWidth() * 1f / DIGITS_SIZE_FROM_VIEW_WIDTH_DIVIDER);

            mRadius = mWidth/2 - OUTER_CIRCLE_MARGIN_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;
            mNotchingLength = NOTCHING_LENGTH_TO_STROKE_WIDTH_MULTIPLIER * mStrokeWidth;

            mRevalidatedSpeedNotchingInterval = getRevalidatedSpeedNotchingInterval(mMaximumSpeedometerSpeed);
            mNotchingsCount = mMaximumSpeedometerSpeed/mRevalidatedSpeedNotchingInterval; //you need add 1 for angle calculation
            mAnglePart = Math.PI/(mNotchingsCount+1);

            super.onLayout(changed, left, top, right, bottom);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            mOuterCirclePaint.setColor(mOuterCircleColor);
            mNotchesPaint.setColor(mOuterCircleColor);
            mDigitsPaint.setColor(mDigitsColor);

            mOuterCircleOval.set(mCenterX-mRadius, mCenterY - mRadius, mCenterX+mRadius, mCenterY+mRadius);
            canvas.drawArc(mOuterCircleOval, 180, 180, false, mOuterCirclePaint);

            double alpha;
            int digits;
            String digitsString;
            float digitsWidth;
            float digitsHeight;

            mNotchesPath.reset();
            mNotchesPath.addRect(
                    mCenterX - mRadius,
                    mCenterY - mStrokeWidth/2,
                    mCenterX - mRadius + mNotchingLength,
                    mCenterY + mStrokeWidth/2,
                    Path.Direction.CW);
            mNotchesMatrix.reset();
            mNotchesMatrix.setRotate((float)radiansToDegrees(mAnglePart), mCenterX, mCenterY);

            for (int i = 1; i <= mNotchingsCount; i++) {
                alpha = mAnglePart * i;

                mNotchesPath.transform(mNotchesMatrix);
                canvas.drawPath(mNotchesPath, mNotchesPaint);

                digits = mRevalidatedSpeedNotchingInterval * i;
                digitsString = digits +"";
                mDigitsPaint.getTextBounds(digitsString, 0, digitsString.length(), mDigitsBoundRect);
                digitsWidth = mDigitsPaint.measureText(digitsString);
                digitsHeight = mDigitsBoundRect.height();

                int digitsPositionShift = mCenterX - mRadius + mNotchingLength + mStrokeWidth;
                mDigitsPath.reset();
                mDigitsPath.moveTo(digitsPositionShift, mCenterY);
                mDigitsPath.lineTo(digitsPositionShift + digitsWidth, mCenterY);
                mDigitsMatrix.reset();
                mDigitsMatrix.setRotate((-1)*(float)radiansToDegrees(alpha), digitsPositionShift + digitsWidth/2, mCenterY);
                mDigitsPath.transform(mDigitsMatrix);
                mDigitsMatrix.reset();
                mDigitsMatrix.setRotate((float)radiansToDegrees(alpha), mCenterX, mCenterY);
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

        private int mWidth;
        private int mHeight;
        private int mCenterX;
        private int mCenterY;

        private double mStartAngle;

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

            mArrowCenterPaint.setStyle(Paint.Style.FILL);
            mArrowPaint.setStyle(Paint.Style.FILL);
            mSectorBeforeArrowPaint.setStyle(Paint.Style.STROKE);
            mSectorAfterArrowPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mWidth = right - left;
            mHeight = bottom - top;
            mCenterX = mWidth/2;
            mCenterY = mWidth/2;

            float strokeWidth = mExternalSectorRadius - mInternalSectorRadius;
            float radius = mExternalSectorRadius;
            mSectorBeforeOval.set(mCenterX-radius+strokeWidth/2, mCenterY - radius+strokeWidth/2, mCenterX+radius-strokeWidth/2, mCenterY+radius-strokeWidth/2);
            mSectorAfterOval.set(mCenterX-radius+strokeWidth/2, mCenterY - radius+strokeWidth/2, mCenterX+radius-strokeWidth/2, mCenterY+radius-strokeWidth/2);

            super.onLayout(changed, left, top, right, bottom);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            mArrowCenterPaint.setColor(mArrowColor);
            mArrowPaint.setColor(mArrowColor);
            mSectorBeforeArrowPaint.setColor(mSectorBeforeArrowColor);
            mSectorAfterArrowPaint.setColor(mSectorAfterArrowColor);
            mSectorBeforeArrowPaint.setStrokeWidth(mExternalSectorRadius - mInternalSectorRadius);
            mSectorAfterArrowPaint.setStrokeWidth(mExternalSectorRadius - mInternalSectorRadius);

            mStartAngle = Math.PI * (mSpeed / mMaximumSpeedometerSpeed) * ((float) mNotchesCount /((float) mNotchesCount +1));

            canvas.drawArc(mSectorBeforeOval, 180, (float)radiansToDegrees(mStartAngle), false, mSectorBeforeArrowPaint);
            canvas.drawArc(mSectorAfterOval, 180+(float)radiansToDegrees(mStartAngle), 180-(float)radiansToDegrees(mStartAngle), false, mSectorAfterArrowPaint);
            canvas.drawCircle(mCenterX, mCenterY, mWidth / ARROW_CENTER_RADIUS_FROM_VIEW_WIDTH_DIVIDER, mArrowCenterPaint);

            mArrowPath.reset();
            mArrowPath.addRect(
                    mCenterX-mArrowRadius,
                    mCenterY-mWidth/(ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER*2),
                    mCenterX,
                    mCenterY+mWidth/(ARROW_WIDTH_FROM_VIEW_WIDTH_DIVIDER*2),
                    Path.Direction.CW);

            mArrowMatrix.reset();
            mArrowMatrix.setRotate((float)radiansToDegrees(mStartAngle), mCenterX, mCenterY);
            mArrowPath.transform(mArrowMatrix);
            canvas.drawPath(mArrowPath, mArrowPaint);
        }
    }



    private class OilCanAndLevelView extends View {

        private Paint mOilCanPaint;
        private Bitmap mOilCanBitmap;
        private Rect mOilCanRect;
        private Paint mLevelPaint;

        // RGBATxRGBA
        float[] cmDataGreen = new float[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0 };
        float[] cmDataRed = new float[]{
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0 };

        ColorMatrix mColorGreenMatrix;
        ColorMatrix mColorRedMatrix;
        ColorFilter mColorGreenFilter;
        ColorFilter mColorRedFilter;

        private int mWidth;
        private int mHeight;
        private int mCenterX;
        private int mCenterY;
        private int mOilCanAndLevelViewWidth;
        private int mOilCanAndLevelViewHeight;
        private int mEnergyLevelXOffset;

        private float mAlphaLevel;
        private boolean isAlphaIncreasing;

        public OilCanAndLevelView(Context context) {
            super(context);
        }

        public void init(){
            mAlphaLevel = 1.0f;
            isAlphaIncreasing = false;

            mOilCanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOilCanBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_oil);
            mOilCanRect = new Rect();
            mLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            mColorGreenMatrix = new ColorMatrix(cmDataGreen);
            mColorRedMatrix = new ColorMatrix(cmDataRed);
            mColorGreenFilter = new ColorMatrixColorFilter(mColorGreenMatrix);
            mColorRedFilter = new ColorMatrixColorFilter(mColorRedMatrix);

            mLevelPaint.setColor(0xff000000);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mWidth = right - left;
            mHeight = bottom - top;
            mCenterX = mWidth/2;
            mCenterY = (int) (OIL_AND_LEVEL_VERTICAL_POSITION_TO_VIEW_HEIGHT_MULTIPLIER * mCenterX);

            mOilCanAndLevelViewWidth = (int) (mWidth * OIL_AND_LEVEL_WIDTH_FROM_VIEW_WIDTH_MULTIPLIER);
            mOilCanAndLevelViewHeight = mOilCanAndLevelViewWidth / 2;

            super.onLayout(changed, left, top, right, bottom);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (mEnergyLevel > ENERGY_LEVEL_CAN_EMPTY) {
                mOilCanPaint.setColorFilter(mColorGreenFilter);
                mLevelPaint.setColorFilter(mColorGreenFilter);
            } else {
                mOilCanPaint.setColorFilter(mColorRedFilter);
                mLevelPaint.setColorFilter(mColorRedFilter);
            }

            if(mEnergyLevel < ENERGY_LEVEL_BLINK){
                if(isAlphaIncreasing){
                    mAlphaLevel+=ENERGY_LEVEL_BLINK_ALPHA_STEP;
                    if(mAlphaLevel>1){
                        mAlphaLevel = 1;
                        isAlphaIncreasing = false;
                    }
                }
                if(!isAlphaIncreasing){
                    mAlphaLevel-=ENERGY_LEVEL_BLINK_ALPHA_STEP;
                    if(mAlphaLevel<0){
                        mAlphaLevel = 0;
                        isAlphaIncreasing = true;
                    }
                }
                mOilCanPaint.setAlpha((int)(mAlphaLevel*255));
                mLevelPaint.setAlpha((int)(mAlphaLevel*255));
            } else {
                mOilCanPaint.setAlpha(255);
                mLevelPaint.setAlpha(255);
            }

            mOilCanRect.set(
                    mCenterX - mOilCanAndLevelViewWidth / 2,
                    mCenterY - mOilCanAndLevelViewHeight / 3,
                    mCenterX - mOilCanAndLevelViewWidth / 2 + mOilCanAndLevelViewHeight * 2 / 3,
                    mCenterY + mOilCanAndLevelViewHeight / 3);
            canvas.drawBitmap(mOilCanBitmap, null, mOilCanRect, mOilCanPaint);

            mLevelPaint.setStrokeWidth(mWidth / STROKE_WIDTH_FROM_VIEW_WIDTH_DIVIDER);

            mEnergyLevelXOffset = mOilCanAndLevelViewWidth / 2 - mOilCanAndLevelViewHeight * 15 / 24;
            canvas.drawLine(
                    mCenterX - mEnergyLevelXOffset,
                    mCenterY,
                    mCenterX - mEnergyLevelXOffset + mEnergyLevel * (mEnergyLevelXOffset+mOilCanAndLevelViewWidth / 2) / 100,
                    mCenterY,
                    mLevelPaint);
        }
    }



    /*********************************************************************************************
     * Utils *
     ********************************************************************************************/

    private void setLayerToSW(View v) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void setLayerToHW(View v) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
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
     * Animator *
     ********************************************************************************************/

    private Runnable animator = new Runnable() {
        @Override
        public void run() {

            if (isTrottlePedalPressed && mSpeed < mMaximumSpeedometerSpeed) {
                mSpeed+=mArrowAccelerationSpeed;
                mEnergyLevel-=mEnergyLevelChangeSpeed;
            }
            if (isBrakePedalPressed && mSpeed > 0) {
                mSpeed-=2*mArrowAccelerationSpeed;
            }
            if (!isBrakePedalPressed && !isTrottlePedalPressed && mSpeed > 0) {
                mSpeed-=mArrowAttenuationSpeed;
            }
            if(mSpeed > mMaximumSpeedometerSpeed){
                mSpeed = mMaximumSpeedometerSpeed;
            }
            if(mSpeed<0){
                mSpeed = 0;
            }
            if(mEnergyLevel<0){
                mEnergyLevel = 0;
            }

            for (SpeedChangeListener speedChangeListener :
                    mSpeedChangeListenerList) {
                speedChangeListener.onSpeedChanged((int)mSpeed);
            }

            if(isSwitchedOn){
                postDelayed(this, 30);
            }

            invalidate();
        }
    };



    /*********************************************************************************************
     * other public methods *
     ********************************************************************************************/

    public void setArrowAccelerationSpeed(float accelerationSpeed){
        mArrowAccelerationSpeed = accelerationSpeed;
    }

    public void setArrowAttenuationSpeed(float attenuationSpeed){
        mArrowAttenuationSpeed = attenuationSpeed;
    }

    public void setEnergyLevel(float energyLevel){
        mEnergyLevel = energyLevel;
    }

    public float getEnergyLevel(){
        return mEnergyLevel;
    }

    public void setEnergyLevelChangeSpeed(float energyLevelChangeSpeedPerSecond){
        mEnergyLevelChangeSpeed = energyLevelChangeSpeedPerSecond;
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

    public void switchOn(){
        isSwitchedOn = true;
        post(animator);
    }

    public void switchOff(){
        isSwitchedOn = false;
    }

    public void setOnSpeedChangeListener(SpeedChangeListener speedChangeListener){
        mSpeedChangeListenerList.add(speedChangeListener);
    }

    public interface SpeedChangeListener {
        void onSpeedChanged(int value);
    }
}
