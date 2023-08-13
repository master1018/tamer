public class ViewAnimator extends FrameLayout {
    int mWhichChild = 0;
    boolean mFirstTime = true;
    boolean mAnimateFirstTime = true;
    Animation mInAnimation;
    Animation mOutAnimation;
    public ViewAnimator(Context context) {
        super(context);
        initViewAnimator(context, null);
    }
    public ViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ViewAnimator);
        int resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_inAnimation, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        }
        resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_outAnimation, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        }
        a.recycle();
        initViewAnimator(context, attrs);
    }
    private void initViewAnimator(Context context, AttributeSet attrs) {
        if (attrs == null) {
            mMeasureAllChildren = true;
            return;
        }
        final TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.FrameLayout);
        final boolean measureAllChildren = a.getBoolean(
                com.android.internal.R.styleable.FrameLayout_measureAllChildren, true);
        setMeasureAllChildren(measureAllChildren);
        a.recycle();
    }
    public void setDisplayedChild(int whichChild) {
        mWhichChild = whichChild;
        if (whichChild >= getChildCount()) {
            mWhichChild = 0;
        } else if (whichChild < 0) {
            mWhichChild = getChildCount() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        showOnly(mWhichChild);
        if (hasFocus) {
            requestFocus(FOCUS_FORWARD);
        }
    }
    public int getDisplayedChild() {
        return mWhichChild;
    }
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }
    void showOnly(int childIndex) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final boolean checkForFirst = (!mFirstTime || mAnimateFirstTime);
            if (i == childIndex) {
                if (checkForFirst && mInAnimation != null) {
                    child.startAnimation(mInAnimation);
                }
                child.setVisibility(View.VISIBLE);
                mFirstTime = false;
            } else {
                if (checkForFirst && mOutAnimation != null && child.getVisibility() == View.VISIBLE) {
                    child.startAnimation(mOutAnimation);
                } else if (child.getAnimation() == mInAnimation)
                    child.clearAnimation();
                child.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() == 1) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
    }
    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mWhichChild = 0;
        mFirstTime = true;
    }
    @Override
    public void removeView(View view) {
        final int index = indexOfChild(view);
        if (index >= 0) {
            removeViewAt(index);
        }
    }
    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        final int childCount = getChildCount();
        if (childCount == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= childCount) {
            setDisplayedChild(childCount - 1);
        } else if (mWhichChild == index) {
            setDisplayedChild(mWhichChild);
        }
    }
    public void removeViewInLayout(View view) {
        removeView(view);
    }
    public void removeViews(int start, int count) {
        super.removeViews(start, count);
        if (getChildCount() == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= start && mWhichChild < start + count) {
            setDisplayedChild(mWhichChild);
        }
    }
    public void removeViewsInLayout(int start, int count) {
        removeViews(start, count);
    }
    public View getCurrentView() {
        return getChildAt(mWhichChild);
    }
    public Animation getInAnimation() {
        return mInAnimation;
    }
    public void setInAnimation(Animation inAnimation) {
        mInAnimation = inAnimation;
    }
    public Animation getOutAnimation() {
        return mOutAnimation;
    }
    public void setOutAnimation(Animation outAnimation) {
        mOutAnimation = outAnimation;
    }
    public void setInAnimation(Context context, int resourceID) {
        setInAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }
    public void setOutAnimation(Context context, int resourceID) {
        setOutAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }
    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }
    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }
}
