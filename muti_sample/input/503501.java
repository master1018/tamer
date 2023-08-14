public class MultiStateButton extends Button {
    private int mState;
    private int mMaxStates;
    private int mButtonResource;
    private int[] mButtonResources;
    private Drawable mButtonDrawable;
    private String TAG = "MSB";
    public MultiStateButton(Context context) {
        this(context, null);
    }
    public MultiStateButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MultiStateButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMaxStates = 1;
        mState = 0;
        mButtonResources = new int[] { R.drawable.widget_show };
        setButtonDrawable(mButtonResources[mState]);
    }
    @Override
    public boolean performClick() {
        transitionState();
        return super.performClick();
    }
    public void transitionState() {
        mState = (mState + 1) % mMaxStates;
        setButtonDrawable(mButtonResources[mState]);
    }
    public void setButtonResources(int[] resources) throws IllegalArgumentException {
        if(resources == null) {
            throw new IllegalArgumentException("Button resources cannot be null");
        }
        mMaxStates = resources.length;
        if(mState >= mMaxStates) {
            mState = mMaxStates - 1;
        }
        mButtonResources = resources;
    }
    public boolean setState(int state){
        if(state >= mMaxStates || state < 0) {
            Log.w("Cal", "MultiStateButton state set to value greater than maxState or < 0");
            return false;
        }
        mState = state;
        setButtonDrawable(mButtonResources[mState]);
        return true;
    }
    public int getState() {
        return mState;
    }
    public void setButtonDrawable(int resid) {
        if (resid != 0 && resid == mButtonResource) {
            return;
        }
        mButtonResource = resid;
        Drawable d = null;
        if (mButtonResource != 0) {
            d = getResources().getDrawable(mButtonResource);
        }
        setButtonDrawable(d);
    }
    public void setButtonDrawable(Drawable d) {
        if (d != null) {
            if (mButtonDrawable != null) {
                mButtonDrawable.setCallback(null);
                unscheduleDrawable(mButtonDrawable);
            }
            d.setCallback(this);
            d.setState(getDrawableState());
            d.setVisible(getVisibility() == VISIBLE, false);
            mButtonDrawable = d;
            mButtonDrawable.setState(null);
            setMinHeight(mButtonDrawable.getIntrinsicHeight());
            setWidth(mButtonDrawable.getIntrinsicWidth());
        }
        refreshDrawableState();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mButtonDrawable != null) {
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int horizontalGravity = getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int height = mButtonDrawable.getIntrinsicHeight();
            final int width = mButtonDrawable.getIntrinsicWidth();
            int y = 0;
            int x = 0;
            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }
            switch (horizontalGravity) {
                case Gravity.RIGHT:
                    x = getWidth() - width;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    x = (getWidth() - width) / 2;
                    break;
            }
            mButtonDrawable.setBounds(x, y, x + width, y + height);
            mButtonDrawable.draw(canvas);
        }
    }
}
