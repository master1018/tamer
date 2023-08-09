public class AnticipateInterpolator implements Interpolator {
    private final float mTension;
    public AnticipateInterpolator() {
        mTension = 2.0f;
    }
    public AnticipateInterpolator(float tension) {
        mTension = tension;
    }
    public AnticipateInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.AnticipateInterpolator);
        mTension =
                a.getFloat(com.android.internal.R.styleable.AnticipateInterpolator_tension, 2.0f);
        a.recycle();
    }
    public float getInterpolation(float t) {
        return t * t * ((mTension + 1) * t - mTension);
    }
}
