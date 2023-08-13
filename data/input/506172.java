public class Scroller  {
    private int mMode;
    private int mStartX;
    private int mStartY;
    private int mFinalX;
    private int mFinalY;
    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;
    private int mCurrX;
    private int mCurrY;
    private long mStartTime;
    private int mDuration;
    private float mDurationReciprocal;
    private float mDeltaX;
    private float mDeltaY;
    private float mViscousFluidScale;
    private float mViscousFluidNormalize;
    private boolean mFinished;
    private Interpolator mInterpolator;
    private float mCoeffX = 0.0f;
    private float mCoeffY = 1.0f;
    private float mVelocity;
    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;
    private final float mDeceleration;
    public Scroller(Context context) {
        this(context, null);
    }
    public Scroller(Context context, Interpolator interpolator) {
        mFinished = true;
        mInterpolator = interpolator;
        float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = SensorManager.GRAVITY_EARTH   
                      * 39.37f                        
                      * ppi                           
                      * ViewConfiguration.getScrollFriction();
    }
    public final boolean isFinished() {
        return mFinished;
    }
    public final void forceFinished(boolean finished) {
        mFinished = finished;
    }
    public final int getDuration() {
        return mDuration;
    }
    public final int getCurrX() {
        return mCurrX;
    }
    public final int getCurrY() {
        return mCurrY;
    }
    public float getCurrVelocity() {
        return mVelocity - mDeceleration * timePassed() / 2000.0f;
    }
    public final int getStartX() {
        return mStartX;
    }
    public final int getStartY() {
        return mStartY;
    }
    public final int getFinalX() {
        return mFinalX;
    }
    public final int getFinalY() {
        return mFinalY;
    }
    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }
        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                float x = (float)timePassed * mDurationReciprocal;
                if (mInterpolator == null)
                    x = viscousFluid(x); 
                else
                    x = mInterpolator.getInterpolation(x);
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                float timePassedSeconds = timePassed / 1000.0f;
                float distance = (mVelocity * timePassedSeconds)
                        - (mDeceleration * timePassedSeconds * timePassedSeconds / 2.0f);
                mCurrX = mStartX + Math.round(distance * mCoeffX);
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                mCurrY = mStartY + Math.round(distance * mCoeffY);
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);
                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
        mViscousFluidScale = 8.0f;
        mViscousFluidNormalize = 1.0f;
        mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;
        mFinished = false;
        float velocity = (float)Math.hypot(velocityX, velocityY);
        mVelocity = velocity;
        mDuration = (int) (1000 * velocity / mDeceleration); 
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mCoeffX = velocity == 0 ? 1.0f : velocityX / velocity; 
        mCoeffY = velocity == 0 ? 1.0f : velocityY / velocity;
        int totalDistance = (int) ((velocity * velocity) / (2 * mDeceleration));
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;
        mFinalX = startX + Math.round(totalDistance * mCoeffX);
        mFinalX = Math.min(mFinalX, mMaxX);
        mFinalX = Math.max(mFinalX, mMinX);
        mFinalY = startY + Math.round(totalDistance * mCoeffY);
        mFinalY = Math.min(mFinalY, mMaxY);
        mFinalY = Math.max(mFinalY, mMinY);
    }
    private float viscousFluid(float x)
    {
        x *= mViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float)Math.exp(-x));
        } else {
            float start = 0.36787944117f;   
            x = 1.0f - (float)Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= mViscousFluidNormalize;
        return x;
    }
    public void abortAnimation() {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }
    public void extendDuration(int extend) {
        int passed = timePassed();
        mDuration = passed + extend;
        mDurationReciprocal = 1.0f / (float)mDuration;
        mFinished = false;
    }
    public int timePassed() {
        return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }
    public void setFinalX(int newX) {
        mFinalX = newX;
        mDeltaX = mFinalX - mStartX;
        mFinished = false;
    }
    public void setFinalY(int newY) {
        mFinalY = newY;
        mDeltaY = mFinalY - mStartY;
        mFinished = false;
    }
}
