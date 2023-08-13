public class OvershootInterpolator implements Interpolator {
    private final float mTension;
    public OvershootInterpolator() {
        mTension = 2.0f;
    }
    public OvershootInterpolator(float tension) {
        mTension = tension;
    }
    public OvershootInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.OvershootInterpolator);
        mTension =
                a.getFloat(com.android.internal.R.styleable.OvershootInterpolator_tension, 2.0f);
        a.recycle();
    }
    public float getInterpolation(float t) {
        t -= 1.0f;
        return t * t * ((mTension + 1) * t + mTension) + 1.0f;
    }
}
