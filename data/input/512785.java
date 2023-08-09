public class TranslateAnimation extends Animation {
    private int mFromXType = ABSOLUTE;
    private int mToXType = ABSOLUTE;
    private int mFromYType = ABSOLUTE;
    private int mToYType = ABSOLUTE;
    private float mFromXValue = 0.0f;
    private float mToXValue = 0.0f;
    private float mFromYValue = 0.0f;
    private float mToYValue = 0.0f;
    private float mFromXDelta;
    private float mToXDelta;
    private float mFromYDelta;
    private float mToYDelta;
    public TranslateAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.TranslateAnimation);
        Description d = Description.parseValue(a.peekValue(
            com.android.internal.R.styleable.TranslateAnimation_fromXDelta));
        mFromXType = d.type;
        mFromXValue = d.value;
        d = Description.parseValue(a.peekValue(
                com.android.internal.R.styleable.TranslateAnimation_toXDelta));
        mToXType = d.type;
        mToXValue = d.value;
        d = Description.parseValue(a.peekValue(
            com.android.internal.R.styleable.TranslateAnimation_fromYDelta));
        mFromYType = d.type;
        mFromYValue = d.value;
        d = Description.parseValue(a.peekValue(
            com.android.internal.R.styleable.TranslateAnimation_toYDelta));
        mToYType = d.type;
        mToYValue = d.value;
        a.recycle();
    }
    public TranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        mFromXValue = fromXDelta;
        mToXValue = toXDelta;
        mFromYValue = fromYDelta;
        mToYValue = toYDelta;
        mFromXType = ABSOLUTE;
        mToXType = ABSOLUTE;
        mFromYType = ABSOLUTE;
        mToYType = ABSOLUTE;
    }
    public TranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
            int fromYType, float fromYValue, int toYType, float toYValue) {
        mFromXValue = fromXValue;
        mToXValue = toXValue;
        mFromYValue = fromYValue;
        mToYValue = toYValue;
        mFromXType = fromXType;
        mToXType = toXType;
        mFromYType = fromYType;
        mToYType = toYType;
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float dx = mFromXDelta;
        float dy = mFromYDelta;
        if (mFromXDelta != mToXDelta) {
            dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
        }
        if (mFromYDelta != mToYDelta) {
            dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
        }
        t.getMatrix().setTranslate(dx, dy);
    }
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth);
        mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
        mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight);
        mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);
    }
}
