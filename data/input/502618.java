public class RotarySelector extends View {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final String LOG_TAG = "RotarySelector";
    private static final boolean DBG = false;
    private static final boolean VISUAL_DEBUG = false;
    private OnDialTriggerListener mOnDialTriggerListener;
    private float mDensity;
    private Bitmap mBackground;
    private Bitmap mDimple;
    private Bitmap mDimpleDim;
    private Bitmap mLeftHandleIcon;
    private Bitmap mRightHandleIcon;
    private Bitmap mArrowShortLeftAndRight;
    private Bitmap mArrowLongLeft;  
    private Bitmap mArrowLongRight;  
    private int mLeftHandleX;
    private int mRightHandleX;
    private int mRotaryOffsetX = 0;
    private boolean mAnimating = false;
    private long mAnimationStartTime;
    private long mAnimationDuration;
    private int mAnimatingDeltaXStart;   
    private int mAnimatingDeltaXEnd;
    private DecelerateInterpolator mInterpolator;
    private Paint mPaint = new Paint();
    final Matrix mBgMatrix = new Matrix();
    final Matrix mArrowMatrix = new Matrix();
    private int mGrabbedState = NOTHING_GRABBED;
    public static final int NOTHING_GRABBED = 0;
    public static final int LEFT_HANDLE_GRABBED = 1;
    public static final int RIGHT_HANDLE_GRABBED = 2;
    private boolean mTriggered = false;
    private Vibrator mVibrator;
    private static final long VIBRATE_SHORT = 20;  
    private static final long VIBRATE_LONG = 20;  
    private static final int ARROW_SCRUNCH_DIP = 6;
    private static final int EDGE_PADDING_DIP = 9;
    private static final int EDGE_TRIGGER_DIP = 100;
    static final int OUTER_ROTARY_RADIUS_DIP = 390;
    static final int ROTARY_STROKE_WIDTH_DIP = 83;
    static final int SNAP_BACK_ANIMATION_DURATION_MILLIS = 300;
    static final int SPIN_ANIMATION_DURATION_MILLIS = 800;
    private int mEdgeTriggerThresh;
    private int mDimpleWidth;
    private int mBackgroundWidth;
    private int mBackgroundHeight;
    private final int mOuterRadius;
    private final int mInnerRadius;
    private int mDimpleSpacing;
    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mDimplesOfFling = 0;
    private int mOrientation;
    public RotarySelector(Context context) {
        this(context, null);
    }
    public RotarySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(attrs, R.styleable.RotarySelector);
        mOrientation = a.getInt(R.styleable.RotarySelector_orientation, HORIZONTAL);
        a.recycle();
        Resources r = getResources();
        mDensity = r.getDisplayMetrics().density;
        if (DBG) log("- Density: " + mDensity);
        mBackground = getBitmapFor(R.drawable.jog_dial_bg);
        mDimple = getBitmapFor(R.drawable.jog_dial_dimple);
        mDimpleDim = getBitmapFor(R.drawable.jog_dial_dimple_dim);
        mArrowLongLeft = getBitmapFor(R.drawable.jog_dial_arrow_long_left_green);
        mArrowLongRight = getBitmapFor(R.drawable.jog_dial_arrow_long_right_red);
        mArrowShortLeftAndRight = getBitmapFor(R.drawable.jog_dial_arrow_short_left_and_right);
        mInterpolator = new DecelerateInterpolator(1f);
        mEdgeTriggerThresh = (int) (mDensity * EDGE_TRIGGER_DIP);
        mDimpleWidth = mDimple.getWidth();
        mBackgroundWidth = mBackground.getWidth();
        mBackgroundHeight = mBackground.getHeight();
        mOuterRadius = (int) (mDensity * OUTER_ROTARY_RADIUS_DIP);
        mInnerRadius = (int) ((OUTER_ROTARY_RADIUS_DIP - ROTARY_STROKE_WIDTH_DIP) * mDensity);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity() * 2;
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int edgePadding = (int) (EDGE_PADDING_DIP * mDensity);
        mLeftHandleX = edgePadding + mDimpleWidth / 2;
        final int length = isHoriz() ? w : h;
        mRightHandleX = length - edgePadding - mDimpleWidth / 2;
        mDimpleSpacing = (length / 2) - mLeftHandleX;
        mBgMatrix.setTranslate(0, 0);
        if (!isHoriz()) {
            final int left = w - mBackgroundHeight;
            mBgMatrix.preRotate(-90, 0, 0);
            mBgMatrix.postTranslate(left, h);
        } else {
            mBgMatrix.postTranslate(0, h - mBackgroundHeight);
        }
    }
    private boolean isHoriz() {
        return mOrientation == HORIZONTAL;
    }
    public void setLeftHandleResource(int resId) {
        if (resId != 0) {
            mLeftHandleIcon = getBitmapFor(resId);
        }
        invalidate();
    }
    public void setRightHandleResource(int resId) {
        if (resId != 0) {
            mRightHandleIcon = getBitmapFor(resId);
        }
        invalidate();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int length = isHoriz() ?
                MeasureSpec.getSize(widthMeasureSpec) :
                MeasureSpec.getSize(heightMeasureSpec);
        final int arrowScrunch = (int) (ARROW_SCRUNCH_DIP * mDensity);
        final int arrowH = mArrowShortLeftAndRight.getHeight();
        final int height = mBackgroundHeight + arrowH - arrowScrunch;
        if (isHoriz()) {
            setMeasuredDimension(length, height);
        } else {
            setMeasuredDimension(height, length);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        if (VISUAL_DEBUG) {
            mPaint.setColor(0xffff0000);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, width, getHeight(), mPaint);
        }
        final int height = getHeight();
        if (mAnimating) {
            updateAnimation();
        }
        canvas.drawBitmap(mBackground, mBgMatrix, mPaint);
        mArrowMatrix.reset();
        switch (mGrabbedState) {
            case NOTHING_GRABBED:
                break;
            case LEFT_HANDLE_GRABBED:
                mArrowMatrix.setTranslate(0, 0);
                if (!isHoriz()) {
                    mArrowMatrix.preRotate(-90, 0, 0);
                    mArrowMatrix.postTranslate(0, height);
                }
                canvas.drawBitmap(mArrowLongLeft, mArrowMatrix, mPaint);
                break;
            case RIGHT_HANDLE_GRABBED:
                mArrowMatrix.setTranslate(0, 0);
                if (!isHoriz()) {
                    mArrowMatrix.preRotate(-90, 0, 0);
                    mArrowMatrix.postTranslate(0, height + (mBackgroundWidth - height));
                }
                canvas.drawBitmap(mArrowLongRight, mArrowMatrix, mPaint);
                break;
            default:
                throw new IllegalStateException("invalid mGrabbedState: " + mGrabbedState);
        }
        final int bgHeight = mBackgroundHeight;
        final int bgTop = isHoriz() ?
                height - bgHeight:
                width - bgHeight;
        if (VISUAL_DEBUG) {
            float or = OUTER_ROTARY_RADIUS_DIP * mDensity;
            final int vOffset = mBackgroundWidth - height;
            final int midX = isHoriz() ? width / 2 : mBackgroundWidth / 2 - vOffset;
            if (isHoriz()) {
                canvas.drawCircle(midX, or + bgTop, or, mPaint);
            } else {
                canvas.drawCircle(or + bgTop, midX, or, mPaint);
            }
        }
        {
            final int xOffset = mLeftHandleX + mRotaryOffsetX;
            final int drawableY = getYOnArc(
                    mBackgroundWidth,
                    mInnerRadius,
                    mOuterRadius,
                    xOffset);
            final int x = isHoriz() ? xOffset : drawableY + bgTop;
            final int y = isHoriz() ? drawableY + bgTop : height - xOffset;
            if (mGrabbedState != RIGHT_HANDLE_GRABBED) {
                drawCentered(mDimple, canvas, x, y);
                drawCentered(mLeftHandleIcon, canvas, x, y);
            } else {
                drawCentered(mDimpleDim, canvas, x, y);
            }
        }
        {
            final int xOffset = isHoriz() ?
                    width / 2 + mRotaryOffsetX:
                    height / 2 + mRotaryOffsetX;
            final int drawableY = getYOnArc(
                    mBackgroundWidth,
                    mInnerRadius,
                    mOuterRadius,
                    xOffset);
            if (isHoriz()) {
                drawCentered(mDimpleDim, canvas, xOffset, drawableY + bgTop);
            } else {
                drawCentered(mDimpleDim, canvas, drawableY + bgTop, height - xOffset);
            }
        }
        {
            final int xOffset = mRightHandleX + mRotaryOffsetX;
            final int drawableY = getYOnArc(
                    mBackgroundWidth,
                    mInnerRadius,
                    mOuterRadius,
                    xOffset);
            final int x = isHoriz() ? xOffset : drawableY + bgTop;
            final int y = isHoriz() ? drawableY + bgTop : height - xOffset;
            if (mGrabbedState != LEFT_HANDLE_GRABBED) {
                drawCentered(mDimple, canvas, x, y);
                drawCentered(mRightHandleIcon, canvas, x, y);
            } else {
                drawCentered(mDimpleDim, canvas, x, y);
            }
        }
        int dimpleLeft = mRotaryOffsetX + mLeftHandleX - mDimpleSpacing;
        final int halfdimple = mDimpleWidth / 2;
        while (dimpleLeft > -halfdimple) {
            final int drawableY = getYOnArc(
                    mBackgroundWidth,
                    mInnerRadius,
                    mOuterRadius,
                    dimpleLeft);
            if (isHoriz()) {
                drawCentered(mDimpleDim, canvas, dimpleLeft, drawableY + bgTop);
            } else {
                drawCentered(mDimpleDim, canvas, drawableY + bgTop, height - dimpleLeft);
            }
            dimpleLeft -= mDimpleSpacing;
        }
        int dimpleRight = mRotaryOffsetX + mRightHandleX + mDimpleSpacing;
        final int rightThresh = mRight + halfdimple;
        while (dimpleRight < rightThresh) {
            final int drawableY = getYOnArc(
                    mBackgroundWidth,
                    mInnerRadius,
                    mOuterRadius,
                    dimpleRight);
            if (isHoriz()) {
                drawCentered(mDimpleDim, canvas, dimpleRight, drawableY + bgTop);
            } else {
                drawCentered(mDimpleDim, canvas, drawableY + bgTop, height - dimpleRight);
            }
            dimpleRight += mDimpleSpacing;
        }
    }
    private int getYOnArc(int backgroundWidth, int innerRadius, int outerRadius, int x) {
        final int halfWidth = (outerRadius - innerRadius) / 2;
        final int middleRadius = innerRadius + halfWidth;
        final int triangleBottom = (backgroundWidth / 2) - x;
        final int triangleY =
                (int) Math.sqrt(middleRadius * middleRadius - triangleBottom * triangleBottom);
        return middleRadius - triangleY + halfWidth;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAnimating) {
            return true;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int height = getHeight();
        final int eventX = isHoriz() ?
                (int) event.getX():
                height - ((int) event.getY());
        final int hitWindow = mDimpleWidth;
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (DBG) log("touch-down");
                mTriggered = false;
                if (mGrabbedState != NOTHING_GRABBED) {
                    reset();
                    invalidate();
                }
                if (eventX < mLeftHandleX + hitWindow) {
                    mRotaryOffsetX = eventX - mLeftHandleX;
                    setGrabbedState(LEFT_HANDLE_GRABBED);
                    invalidate();
                    vibrate(VIBRATE_SHORT);
                } else if (eventX > mRightHandleX - hitWindow) {
                    mRotaryOffsetX = eventX - mRightHandleX;
                    setGrabbedState(RIGHT_HANDLE_GRABBED);
                    invalidate();
                    vibrate(VIBRATE_SHORT);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (DBG) log("touch-move");
                if (mGrabbedState == LEFT_HANDLE_GRABBED) {
                    mRotaryOffsetX = eventX - mLeftHandleX;
                    invalidate();
                    final int rightThresh = isHoriz() ? getRight() : height;
                    if (eventX >= rightThresh - mEdgeTriggerThresh && !mTriggered) {
                        mTriggered = true;
                        dispatchTriggerEvent(OnDialTriggerListener.LEFT_HANDLE);
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        final int rawVelocity = isHoriz() ?
                                (int) velocityTracker.getXVelocity():
                                -(int) velocityTracker.getYVelocity();
                        final int velocity = Math.max(mMinimumVelocity, rawVelocity);
                        mDimplesOfFling = Math.max(
                                8,
                                Math.abs(velocity / mDimpleSpacing));
                        startAnimationWithVelocity(
                                eventX - mLeftHandleX,
                                mDimplesOfFling * mDimpleSpacing,
                                velocity);
                    }
                } else if (mGrabbedState == RIGHT_HANDLE_GRABBED) {
                    mRotaryOffsetX = eventX - mRightHandleX;
                    invalidate();
                    if (eventX <= mEdgeTriggerThresh && !mTriggered) {
                        mTriggered = true;
                        dispatchTriggerEvent(OnDialTriggerListener.RIGHT_HANDLE);
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        final int rawVelocity = isHoriz() ?
                                (int) velocityTracker.getXVelocity():
                                - (int) velocityTracker.getYVelocity();
                        final int velocity = Math.min(-mMinimumVelocity, rawVelocity);
                        mDimplesOfFling = Math.max(
                                8,
                                Math.abs(velocity / mDimpleSpacing));
                        startAnimationWithVelocity(
                                eventX - mRightHandleX,
                                -(mDimplesOfFling * mDimpleSpacing),
                                velocity);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (DBG) log("touch-up");
                if (mGrabbedState == LEFT_HANDLE_GRABBED
                        && Math.abs(eventX - mLeftHandleX) > 5) {
                    startAnimation(eventX - mLeftHandleX, 0, SNAP_BACK_ANIMATION_DURATION_MILLIS);
                } else if (mGrabbedState == RIGHT_HANDLE_GRABBED
                        && Math.abs(eventX - mRightHandleX) > 5) {
                    startAnimation(eventX - mRightHandleX, 0, SNAP_BACK_ANIMATION_DURATION_MILLIS);
                }
                mRotaryOffsetX = 0;
                setGrabbedState(NOTHING_GRABBED);
                invalidate();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle(); 
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (DBG) log("touch-cancel");
                reset();
                invalidate();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }
    private void startAnimation(int startX, int endX, int duration) {
        mAnimating = true;
        mAnimationStartTime = currentAnimationTimeMillis();
        mAnimationDuration = duration;
        mAnimatingDeltaXStart = startX;
        mAnimatingDeltaXEnd = endX;
        setGrabbedState(NOTHING_GRABBED);
        mDimplesOfFling = 0;
        invalidate();
    }
    private void startAnimationWithVelocity(int startX, int endX, int pixelsPerSecond) {
        mAnimating = true;
        mAnimationStartTime = currentAnimationTimeMillis();
        mAnimationDuration = 1000 * (endX - startX) / pixelsPerSecond;
        mAnimatingDeltaXStart = startX;
        mAnimatingDeltaXEnd = endX;
        setGrabbedState(NOTHING_GRABBED);
        invalidate();
    }
    private void updateAnimation() {
        final long millisSoFar = currentAnimationTimeMillis() - mAnimationStartTime;
        final long millisLeft = mAnimationDuration - millisSoFar;
        final int totalDeltaX = mAnimatingDeltaXStart - mAnimatingDeltaXEnd;
        final boolean goingRight = totalDeltaX < 0;
        if (DBG) log("millisleft for animating: " + millisLeft);
        if (millisLeft <= 0) {
            reset();
            return;
        }
        float interpolation =
                mInterpolator.getInterpolation((float) millisSoFar / mAnimationDuration);
        final int dx = (int) (totalDeltaX * (1 - interpolation));
        mRotaryOffsetX = mAnimatingDeltaXEnd + dx;
        if (mDimplesOfFling > 0) {
            if (!goingRight && mRotaryOffsetX < -3 * mDimpleSpacing) {
                mRotaryOffsetX += mDimplesOfFling * mDimpleSpacing;
            } else if (goingRight && mRotaryOffsetX > 3 * mDimpleSpacing) {
                mRotaryOffsetX -= mDimplesOfFling * mDimpleSpacing;
            }
        }
        invalidate();
    }
    private void reset() {
        mAnimating = false;
        mRotaryOffsetX = 0;
        mDimplesOfFling = 0;
        setGrabbedState(NOTHING_GRABBED);
        mTriggered = false;
    }
    private synchronized void vibrate(long duration) {
        if (mVibrator == null) {
            mVibrator = (android.os.Vibrator)
                    getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(duration);
    }
    private void drawCentered(Bitmap d, Canvas c, int x, int y) {
        int w = d.getWidth();
        int h = d.getHeight();
        c.drawBitmap(d, x - (w / 2), y - (h / 2), mPaint);
    }
    public void setOnDialTriggerListener(OnDialTriggerListener l) {
        mOnDialTriggerListener = l;
    }
    private void dispatchTriggerEvent(int whichHandle) {
        vibrate(VIBRATE_LONG);
        if (mOnDialTriggerListener != null) {
            mOnDialTriggerListener.onDialTrigger(this, whichHandle);
        }
    }
    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            mGrabbedState = newState;
            if (mOnDialTriggerListener != null) {
                mOnDialTriggerListener.onGrabbedStateChange(this, mGrabbedState);
            }
        }
    }
    public interface OnDialTriggerListener {
        public static final int LEFT_HANDLE = 1;
        public static final int RIGHT_HANDLE = 2;
        void onDialTrigger(View v, int whichHandle);
        void onGrabbedStateChange(View v, int grabbedState);
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
