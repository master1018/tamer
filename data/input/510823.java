public class DecelerateInterpolator implements Interpolator {
    public DecelerateInterpolator() {
    }
    public DecelerateInterpolator(float factor) {
        mFactor = factor;
    }
    public DecelerateInterpolator(Context context, AttributeSet attrs) {
        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.DecelerateInterpolator);
        mFactor = a.getFloat(com.android.internal.R.styleable.DecelerateInterpolator_factor, 1.0f);
        a.recycle();
    }
    public float getInterpolation(float input) {
        if (mFactor == 1.0f) {
            return (float)(1.0f - (1.0f - input) * (1.0f - input));
        } else {
            return (float)(1.0f - Math.pow((1.0f - input), 2 * mFactor));
        }
    }
    private float mFactor = 1.0f;
}
