public abstract class Animation implements Cloneable {
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    public static final int START_ON_FIRST_FRAME = -1;
    public static final int ABSOLUTE = 0;
    public static final int RELATIVE_TO_SELF = 1;
    public static final int RELATIVE_TO_PARENT = 2;
    public static final int ZORDER_NORMAL = 0;
    public static final int ZORDER_TOP = 1;
    public static final int ZORDER_BOTTOM = -1;
    boolean mEnded = false;
    boolean mStarted = false;
    boolean mCycleFlip = false;
    boolean mInitialized = false;
    boolean mFillBefore = true;
    boolean mFillAfter = false;
    boolean mFillEnabled = false;    
    long mStartTime = -1;
    long mStartOffset;
    long mDuration;
    int mRepeatCount = 0;
    int mRepeated = 0;
    int mRepeatMode = RESTART;
    Interpolator mInterpolator;
    AnimationListener mListener;
    private int mZAdjustment;
    private boolean mDetachWallpaper = false;
    private boolean mMore = true;
    private boolean mOneMoreTime = true;
    RectF mPreviousRegion = new RectF();
    RectF mRegion = new RectF();
    Transformation mTransformation = new Transformation();
    Transformation mPreviousTransformation = new Transformation();
    public Animation() {
        ensureInterpolator();
    }
    public Animation(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Animation);
        setDuration((long) a.getInt(com.android.internal.R.styleable.Animation_duration, 0));
        setStartOffset((long) a.getInt(com.android.internal.R.styleable.Animation_startOffset, 0));
        setFillEnabled(a.getBoolean(com.android.internal.R.styleable.Animation_fillEnabled, mFillEnabled));
        setFillBefore(a.getBoolean(com.android.internal.R.styleable.Animation_fillBefore, mFillBefore));
        setFillAfter(a.getBoolean(com.android.internal.R.styleable.Animation_fillAfter, mFillAfter));
        final int resID = a.getResourceId(com.android.internal.R.styleable.Animation_interpolator, 0);
        if (resID > 0) {
            setInterpolator(context, resID);
        }
        setRepeatCount(a.getInt(com.android.internal.R.styleable.Animation_repeatCount, mRepeatCount));
        setRepeatMode(a.getInt(com.android.internal.R.styleable.Animation_repeatMode, RESTART));
        setZAdjustment(a.getInt(com.android.internal.R.styleable.Animation_zAdjustment, ZORDER_NORMAL));
        setDetachWallpaper(a.getBoolean(com.android.internal.R.styleable.Animation_detachWallpaper, false));
        ensureInterpolator();
        a.recycle();
    }
    @Override
    protected Animation clone() throws CloneNotSupportedException {
        final Animation animation = (Animation) super.clone();
        animation.mPreviousRegion = new RectF();
        animation.mRegion = new RectF();
        animation.mTransformation = new Transformation();
        animation.mPreviousTransformation = new Transformation();
        return animation;
    }
    public void reset() {
        mPreviousRegion.setEmpty();
        mPreviousTransformation.clear();
        mInitialized = false;
        mCycleFlip = false;
        mRepeated = 0;
        mMore = true;
        mOneMoreTime = true;
    }
    public void cancel() {
        if (mStarted && !mEnded) {
            if (mListener != null) mListener.onAnimationEnd(this);
            mEnded = true;
        }
        mStartTime = Long.MIN_VALUE;
        mMore = mOneMoreTime = false;
    }
    public void detach() {
        if (mStarted && !mEnded) {
            mEnded = true;
            if (mListener != null) mListener.onAnimationEnd(this);
        }
    }
    public boolean isInitialized() {
        return mInitialized;
    }
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        reset();
        mInitialized = true;
    }
    public void setInterpolator(Context context, int resID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resID));
    }
    public void setInterpolator(Interpolator i) {
        mInterpolator = i;
    }
    public void setStartOffset(long startOffset) {
        mStartOffset = startOffset;
    }
    public void setDuration(long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        mDuration = durationMillis;
    }
    public void restrictDuration(long durationMillis) {
        if (mStartOffset > durationMillis) {
            mStartOffset = durationMillis;
            mDuration = 0;
            mRepeatCount = 0;
            return;
        }
        long dur = mDuration + mStartOffset;
        if (dur > durationMillis) {
            mDuration = durationMillis-mStartOffset;
            dur = durationMillis;
        }
        if (mDuration <= 0) {
            mDuration = 0;
            mRepeatCount = 0;
            return;
        }
        if (mRepeatCount < 0 || mRepeatCount > durationMillis
                || (dur*mRepeatCount) > durationMillis) {
            mRepeatCount = (int)(durationMillis/dur) - 1;
            if (mRepeatCount < 0) {
                mRepeatCount = 0;
            }
        }
    }
    public void scaleCurrentDuration(float scale) {
        mDuration = (long) (mDuration * scale);
    }
    public void setStartTime(long startTimeMillis) {
        mStartTime = startTimeMillis;
        mStarted = mEnded = false;
        mCycleFlip = false;
        mRepeated = 0;
        mMore = true;
    }
    public void start() {
        setStartTime(-1);
    }
    public void startNow() {
        setStartTime(AnimationUtils.currentAnimationTimeMillis());
    }
    public void setRepeatMode(int repeatMode) {
        mRepeatMode = repeatMode;
    }
    public void setRepeatCount(int repeatCount) {
        if (repeatCount < 0) {
            repeatCount = INFINITE;
        }
        mRepeatCount = repeatCount;
    }
    public boolean isFillEnabled() {
        return mFillEnabled;
    }
    public void setFillEnabled(boolean fillEnabled) {
        mFillEnabled = fillEnabled;
    }
    public void setFillBefore(boolean fillBefore) {
        mFillBefore = fillBefore;
    }
    public void setFillAfter(boolean fillAfter) {
        mFillAfter = fillAfter;
    }
    public void setZAdjustment(int zAdjustment) {
        mZAdjustment = zAdjustment;
    }
    public void setDetachWallpaper(boolean detachWallpaper) {
        mDetachWallpaper = detachWallpaper;
    }
    public Interpolator getInterpolator() {
        return mInterpolator;
    }
    public long getStartTime() {
        return mStartTime;
    }
    public long getDuration() {
        return mDuration;
    }
    public long getStartOffset() {
        return mStartOffset;
    }
    public int getRepeatMode() {
        return mRepeatMode;
    }
    public int getRepeatCount() {
        return mRepeatCount;
    }
    public boolean getFillBefore() {
        return mFillBefore;
    }
    public boolean getFillAfter() {
        return mFillAfter;
    }
    public int getZAdjustment() {
        return mZAdjustment;
    }
    public boolean getDetachWallpaper() {
        return mDetachWallpaper;
    }
    public boolean willChangeTransformationMatrix() {
        return true;
    }
    public boolean willChangeBounds() {
        return true;
    }
    public void setAnimationListener(AnimationListener listener) {
        mListener = listener;
    }
    protected void ensureInterpolator() {
        if (mInterpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        }
    }
    public long computeDurationHint() {
        return (getStartOffset() + getDuration()) * (getRepeatCount() + 1);
    }
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if (mStartTime == -1) {
            mStartTime = currentTime;
        }
        final long startOffset = getStartOffset();
        final long duration = mDuration;
        float normalizedTime;
        if (duration != 0) {
            normalizedTime = ((float) (currentTime - (mStartTime + startOffset))) /
                    (float) duration;
        } else {
            normalizedTime = currentTime < mStartTime ? 0.0f : 1.0f;
        }
        final boolean expired = normalizedTime >= 1.0f;
        mMore = !expired;
        if (!mFillEnabled) normalizedTime = Math.max(Math.min(normalizedTime, 1.0f), 0.0f);
        if ((normalizedTime >= 0.0f || mFillBefore) && (normalizedTime <= 1.0f || mFillAfter)) {
            if (!mStarted) {
                if (mListener != null) {
                    mListener.onAnimationStart(this);
                }
                mStarted = true;
            }
            if (mFillEnabled) normalizedTime = Math.max(Math.min(normalizedTime, 1.0f), 0.0f);
            if (mCycleFlip) {
                normalizedTime = 1.0f - normalizedTime;
            }
            final float interpolatedTime = mInterpolator.getInterpolation(normalizedTime);
            applyTransformation(interpolatedTime, outTransformation);
        }
        if (expired) {
            if (mRepeatCount == mRepeated) {
                if (!mEnded) {
                    mEnded = true;
                    if (mListener != null) {
                        mListener.onAnimationEnd(this);
                    }
                }
            } else {
                if (mRepeatCount > 0) {
                    mRepeated++;
                }
                if (mRepeatMode == REVERSE) {
                    mCycleFlip = !mCycleFlip;
                }
                mStartTime = -1;
                mMore = true;
                if (mListener != null) {
                    mListener.onAnimationRepeat(this);
                }
            }
        }
        if (!mMore && mOneMoreTime) {
            mOneMoreTime = false;
            return true;
        }
        return mMore;
    }
    public boolean hasStarted() {
        return mStarted;
    }
    public boolean hasEnded() {
        return mEnded;
    }
    protected void applyTransformation(float interpolatedTime, Transformation t) {
    }
    protected float resolveSize(int type, float value, int size, int parentSize) {
        switch (type) {
            case ABSOLUTE:
                return value;
            case RELATIVE_TO_SELF:
                return size * value;
            case RELATIVE_TO_PARENT:
                return parentSize * value;
            default:
                return value;
        }
    }
    public void getInvalidateRegion(int left, int top, int right, int bottom,
            RectF invalidate, Transformation transformation) {
        final RectF tempRegion = mRegion;
        final RectF previousRegion = mPreviousRegion;
        invalidate.set(left, top, right, bottom);
        transformation.getMatrix().mapRect(invalidate);
        invalidate.inset(-1.0f, -1.0f);
        tempRegion.set(invalidate);
        invalidate.union(previousRegion);
        previousRegion.set(tempRegion);
        final Transformation tempTransformation = mTransformation;
        final Transformation previousTransformation = mPreviousTransformation;
        tempTransformation.set(transformation);
        transformation.set(previousTransformation);
        previousTransformation.set(tempTransformation);
    }
    public void initializeInvalidateRegion(int left, int top, int right, int bottom) {
        final RectF region = mPreviousRegion;
        region.set(left, top, right, bottom);
        region.inset(-1.0f, -1.0f);
        if (mFillBefore) {
            final Transformation previousTransformation = mPreviousTransformation;
            applyTransformation(mInterpolator.getInterpolation(0.0f), previousTransformation);
        }
    }
    protected static class Description {
        public int type;
        public float value;
        static Description parseValue(TypedValue value) {
            Description d = new Description();
            if (value == null) {
                d.type = ABSOLUTE;
                d.value = 0;
            } else {
                if (value.type == TypedValue.TYPE_FRACTION) {
                    d.type = (value.data & TypedValue.COMPLEX_UNIT_MASK) ==
                            TypedValue.COMPLEX_UNIT_FRACTION_PARENT ?
                                    RELATIVE_TO_PARENT : RELATIVE_TO_SELF;
                    d.value = TypedValue.complexToFloat(value.data);
                    return d;
                } else if (value.type == TypedValue.TYPE_FLOAT) {
                    d.type = ABSOLUTE;
                    d.value = value.getFloat();
                    return d;
                } else if (value.type >= TypedValue.TYPE_FIRST_INT &&
                        value.type <= TypedValue.TYPE_LAST_INT) {
                    d.type = ABSOLUTE;
                    d.value = value.data;
                    return d;
                }
            }
            d.type = ABSOLUTE;
            d.value = 0.0f;
            return d;
        }
    }
    public static interface AnimationListener {
        void onAnimationStart(Animation animation);
        void onAnimationEnd(Animation animation);
        void onAnimationRepeat(Animation animation);
    }
}
