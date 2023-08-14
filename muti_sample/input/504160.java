public class WeightedLinearLayout extends LinearLayout {
    private float mMajorWeight;
    private float mMinorWeight;
    public WeightedLinearLayout(Context context) {
        super(context);
    }
    public WeightedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = 
            context.obtainStyledAttributes(attrs, styleable.WeightedLinearLayout);
        mMajorWeight = a.getFloat(styleable.WeightedLinearLayout_majorWeight, 0.0f);
        mMinorWeight = a.getFloat(styleable.WeightedLinearLayout_minorWeight, 0.0f);
        a.recycle();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final boolean isPortrait = screenWidth < metrics.heightPixels;
        final int widthMode = getMode(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        boolean measure = false;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, EXACTLY);
        final float widthWeight = isPortrait ? mMinorWeight : mMajorWeight;
        if (widthMode == AT_MOST && widthWeight > 0.0f) {
            if (width < (screenWidth * widthWeight)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (screenWidth * widthWeight),
                        EXACTLY);
                measure = true;
            }
        }
        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);            
        }
    }
}
