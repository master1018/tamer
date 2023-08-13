public class CheckedTextView extends TextView implements Checkable {
    private boolean mChecked;
    private int mCheckMarkResource;
    private Drawable mCheckMarkDrawable;
    private int mBasePaddingRight;
    private int mCheckMarkWidth;
    private static final int[] CHECKED_STATE_SET = {
        R.attr.state_checked
    };
    public CheckedTextView(Context context) {
        this(context, null);
    }
    public CheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CheckedTextView, defStyle, 0);
        Drawable d = a.getDrawable(R.styleable.CheckedTextView_checkMark);
        if (d != null) {
            setCheckMarkDrawable(d);
        }
        boolean checked = a.getBoolean(R.styleable.CheckedTextView_checked, false);
        setChecked(checked);
        a.recycle();
    }
    public void toggle() {
        setChecked(!mChecked);
    }
    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return mChecked;
    }
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }
    public void setCheckMarkDrawable(int resid) {
        if (resid != 0 && resid == mCheckMarkResource) {
            return;
        }
        mCheckMarkResource = resid;
        Drawable d = null;
        if (mCheckMarkResource != 0) {
            d = getResources().getDrawable(mCheckMarkResource);
        }
        setCheckMarkDrawable(d);
    }
    public void setCheckMarkDrawable(Drawable d) {
        if (mCheckMarkDrawable != null) {
            mCheckMarkDrawable.setCallback(null);
            unscheduleDrawable(mCheckMarkDrawable);
        }
        if (d != null) {
            d.setCallback(this);
            d.setVisible(getVisibility() == VISIBLE, false);
            d.setState(CHECKED_STATE_SET);
            setMinHeight(d.getIntrinsicHeight());
            mCheckMarkWidth = d.getIntrinsicWidth();
            mPaddingRight = mCheckMarkWidth + mBasePaddingRight;
            d.setState(getDrawableState());
        } else {
            mPaddingRight = mBasePaddingRight;
        }
        mCheckMarkDrawable = d;
        requestLayout();
    }
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        mBasePaddingRight = mPaddingRight;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable checkMarkDrawable = mCheckMarkDrawable;
        if (checkMarkDrawable != null) {
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int height = checkMarkDrawable.getIntrinsicHeight();
            int y = 0;
            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }
            int right = getWidth();
            checkMarkDrawable.setBounds(
                    right - mCheckMarkWidth - mBasePaddingRight, 
                    y, 
                    right - mBasePaddingRight, 
                    y + height);
            checkMarkDrawable.draw(canvas);
        }
    }
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mCheckMarkDrawable != null) {
            int[] myDrawableState = getDrawableState();
            mCheckMarkDrawable.setState(myDrawableState);
            invalidate();
        }
    }
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean populated = super.dispatchPopulateAccessibilityEvent(event);
        if (!populated) {
            event.setChecked(mChecked);
        }
        return populated;
    }
}
