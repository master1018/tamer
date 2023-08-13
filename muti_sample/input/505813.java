public class LayoutAnimationController {
    public static final int ORDER_NORMAL  = 0;
    public static final int ORDER_REVERSE = 1;
    public static final int ORDER_RANDOM  = 2;
    protected Animation mAnimation;
    protected Random mRandomizer;
    protected Interpolator mInterpolator;
    private float mDelay;
    private int mOrder;
    private long mDuration;
    private long mMaxDelay;    
    public LayoutAnimationController(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.LayoutAnimation);
        Animation.Description d = Animation.Description.parseValue(
                a.peekValue(com.android.internal.R.styleable.LayoutAnimation_delay));
        mDelay = d.value;
        mOrder = a.getInt(com.android.internal.R.styleable.LayoutAnimation_animationOrder, ORDER_NORMAL);
        int resource = a.getResourceId(com.android.internal.R.styleable.LayoutAnimation_animation, 0);
        if (resource > 0) {
            setAnimation(context, resource);
        }
        resource = a.getResourceId(com.android.internal.R.styleable.LayoutAnimation_interpolator, 0);
        if (resource > 0) {
            setInterpolator(context, resource);
        }
        a.recycle();
    }
    public LayoutAnimationController(Animation animation) {
        this(animation, 0.5f);
    }
    public LayoutAnimationController(Animation animation, float delay) {
        mDelay = delay;
        setAnimation(animation);
    }
    public int getOrder() {
        return mOrder;
    }
    public void setOrder(int order) {
        mOrder = order;
    }
    public void setAnimation(Context context, int resourceID) {
        setAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }
    public void setAnimation(Animation animation) {
        mAnimation = animation;
        mAnimation.setFillBefore(true);
    }
    public Animation getAnimation() {
        return mAnimation;
    }
    public void setInterpolator(Context context, int resourceID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resourceID));
    }
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }
    public Interpolator getInterpolator() {
        return mInterpolator;
    }
    public float getDelay() {
        return mDelay;
    }
    public void setDelay(float delay) {
        mDelay = delay;
    }
    public boolean willOverlap() {
        return mDelay < 1.0f;
    }
    public void start() {
        mDuration = mAnimation.getDuration();
        mMaxDelay = Long.MIN_VALUE;
        mAnimation.setStartTime(-1);
    }
    public final Animation getAnimationForView(View view) {
        final long delay = getDelayForView(view) + mAnimation.getStartOffset();
        mMaxDelay = Math.max(mMaxDelay, delay);
        try {
            final Animation animation = mAnimation.clone();
            animation.setStartOffset(delay);
            return animation;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public boolean isDone() {
        return AnimationUtils.currentAnimationTimeMillis() >
                mAnimation.getStartTime() + mMaxDelay + mDuration;
    }
    protected long getDelayForView(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        AnimationParameters params = lp.layoutAnimationParameters;
        if (params == null) {
            return 0;
        }
        final float delay = mDelay * mAnimation.getDuration();
        final long viewDelay = (long) (getTransformedIndex(params) * delay);
        final float totalDelay = delay * params.count;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
        float normalizedDelay = viewDelay / totalDelay;
        normalizedDelay = mInterpolator.getInterpolation(normalizedDelay);
        return (long) (normalizedDelay * totalDelay);
    }
    protected int getTransformedIndex(AnimationParameters params) {
        switch (getOrder()) {
            case ORDER_REVERSE:
                return params.count - 1 - params.index;
            case ORDER_RANDOM:
                if (mRandomizer == null) {
                    mRandomizer = new Random();
                }
                return (int) (params.count * mRandomizer.nextFloat());
            case ORDER_NORMAL:
            default:
                return params.index;
        }
    }
    public static class AnimationParameters {
        public int count;
        public int index;
    }
}
