public class RatingBar extends AbsSeekBar {
    public interface OnRatingBarChangeListener {
        void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser);
    }
    private int mNumStars = 5;
    private int mProgressOnStartTracking;
    private OnRatingBarChangeListener mOnRatingBarChangeListener;
    public RatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBar,
                defStyle, 0);
        final int numStars = a.getInt(R.styleable.RatingBar_numStars, mNumStars);
        setIsIndicator(a.getBoolean(R.styleable.RatingBar_isIndicator, !mIsUserSeekable));
        final float rating = a.getFloat(R.styleable.RatingBar_rating, -1);
        final float stepSize = a.getFloat(R.styleable.RatingBar_stepSize, -1);
        a.recycle();
        if (numStars > 0 && numStars != mNumStars) {
            setNumStars(numStars);            
        }
        if (stepSize >= 0) {
            setStepSize(stepSize);
        } else {
            setStepSize(0.5f);
        }
        if (rating >= 0) {
            setRating(rating);
        }
        mTouchProgressOffset = 1.1f;
    }
    public RatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.ratingBarStyle);
    }
    public RatingBar(Context context) {
        this(context, null);
    }
    public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
        mOnRatingBarChangeListener = listener;
    }
    public OnRatingBarChangeListener getOnRatingBarChangeListener() {
        return mOnRatingBarChangeListener;
    }
    public void setIsIndicator(boolean isIndicator) {
        mIsUserSeekable = !isIndicator;
        setFocusable(!isIndicator);
    }
    public boolean isIndicator() {
        return !mIsUserSeekable;
    }
    public void setNumStars(final int numStars) {
        if (numStars <= 0) {
            return;
        }
        mNumStars = numStars;
        requestLayout();
    }
    public int getNumStars() {
        return mNumStars;
    }
    public void setRating(float rating) {
        setProgress(Math.round(rating * getProgressPerStar()));
    }
    public float getRating() {
        return getProgress() / getProgressPerStar();        
    }
    public void setStepSize(float stepSize) {
        if (stepSize <= 0) {
            return;
        }
        final float newMax = mNumStars / stepSize;
        final int newProgress = (int) (newMax / getMax() * getProgress());
        setMax((int) newMax);
        setProgress(newProgress);
    }
    public float getStepSize() {
        return (float) getNumStars() / getMax();
    }
    private float getProgressPerStar() {
        if (mNumStars > 0) {
            return 1f * getMax() / mNumStars;
        } else {
            return 1;
        }
    }
    @Override
    Shape getDrawableShape() {
        return new RectShape();
    }
    @Override
    void onProgressRefresh(float scale, boolean fromUser) {
        super.onProgressRefresh(scale, fromUser);
        updateSecondaryProgress(getProgress());
        if (!fromUser) {
            dispatchRatingChange(false);
        }
    }
    private void updateSecondaryProgress(int progress) {
        final float ratio = getProgressPerStar();
        if (ratio > 0) {
            final float progressInStars = progress / ratio;
            final int secondaryProgress = (int) (Math.ceil(progressInStars) * ratio);
            setSecondaryProgress(secondaryProgress);
        }
    }
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mSampleTile != null) {
            final int width = mSampleTile.getWidth() * mNumStars;
            setMeasuredDimension(resolveSize(width, widthMeasureSpec), mMeasuredHeight);
        }
    }
    @Override
    void onStartTrackingTouch() {
        mProgressOnStartTracking = getProgress();
        super.onStartTrackingTouch();
    }
    @Override
    void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        if (getProgress() != mProgressOnStartTracking) {
            dispatchRatingChange(true);
        }
    }
    @Override
    void onKeyChange() {
        super.onKeyChange();
        dispatchRatingChange(true);
    }
    void dispatchRatingChange(boolean fromUser) {
        if (mOnRatingBarChangeListener != null) {
            mOnRatingBarChangeListener.onRatingChanged(this, getRating(),
                    fromUser);
        }
    }
    @Override
    public synchronized void setMax(int max) {
        if (max <= 0) {
            return;
        }
        super.setMax(max);
    }
}
