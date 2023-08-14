public class AlphaAnimation extends Animation {
    private float mFromAlpha;
    private float mToAlpha;
    public AlphaAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.AlphaAnimation);
        mFromAlpha = a.getFloat(com.android.internal.R.styleable.AlphaAnimation_fromAlpha, 1.0f);
        mToAlpha = a.getFloat(com.android.internal.R.styleable.AlphaAnimation_toAlpha, 1.0f);
        a.recycle();
    }
    public AlphaAnimation(float fromAlpha, float toAlpha) {
        mFromAlpha = fromAlpha;
        mToAlpha = toAlpha;
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float alpha = mFromAlpha;
        t.setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
    }
    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }
    @Override
    public boolean willChangeBounds() {
        return false;
    }
}
