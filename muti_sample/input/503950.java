class InCallMenuItemView extends TextView {
    private static final String LOG_TAG = "PHONE/InCallMenuItemView";
    private static final boolean DBG = false;
    private boolean mIndicatorVisible;
    private boolean mIndicatorState;
    private Drawable mIndicatorDrawable;
    private Drawable mIcon;
    public InCallMenuItemView(Context context) {
        super(context);
        if (DBG) log("InCallMenuView constructor...");
        setGravity(Gravity.CENTER);
        TypedArray a =
                context.obtainStyledAttributes(
                        com.android.internal.R.styleable.MenuView);
        int textAppearance = a.getResourceId(com.android.internal.R.styleable.
                                             MenuView_itemTextAppearance, -1);
        a.recycle();
        setClickable(true);
        setFocusable(true);
        setTextAppearance(context, textAppearance);
        setPadding(3, getPaddingTop(), 3, getPaddingBottom());
    }
    public void setVisible(boolean isVisible) {
        setVisibility(isVisible ? VISIBLE : GONE);
    }
    public boolean isVisible() {
        return (getVisibility() == VISIBLE);
    }
    public void setIndicatorVisible(boolean isVisible) {
        if (DBG) log("setIndicatorVisible(" + isVisible + ")...");
        mIndicatorVisible = isVisible;
        updateIndicator();
        updateCompoundDrawables();
    }
    public void setIndicatorState(boolean onoff) {
        if (DBG) log("setIndicatorState(" + onoff + ")...");
        mIndicatorState = onoff;
        updateIndicator();
        updateCompoundDrawables();
    }
    public void setIcon(Drawable icon) {
        if (DBG) log("setIcon(" + icon + ")...");
        mIcon = icon;
        updateCompoundDrawables();
        if (icon != null) setSingleLineMarquee();
    }
    public void setIconResource(int resId) {
        if (DBG) log("setIconResource(" + resId + ")...");
         Drawable iconDrawable = getResources().getDrawable(resId);
         setIcon(iconDrawable);
    }
    private void updateIndicator() {
        if (mIndicatorVisible) {
            int resId = mIndicatorState ? android.R.drawable.button_onoff_indicator_on
                    : android.R.drawable.button_onoff_indicator_off;
            mIndicatorDrawable = getResources().getDrawable(resId);
        } else {
            mIndicatorDrawable = null;
        }
    }
    private void updateCompoundDrawables() {
        if (mIcon != null) {
            setCompoundDrawablePadding(-10);
        }
        int topPadding = (mIcon != null) ? 5 : 0;
        int bottomPadding = (mIndicatorDrawable != null) ? 5 : 0;
        setPadding(0, topPadding, 0, bottomPadding);
        setCompoundDrawablesWithIntrinsicBounds(null, mIcon, null, mIndicatorDrawable);
    }
    private void setSingleLineMarquee() {
        setEllipsize(TruncateAt.MARQUEE);
        setHorizontalFadingEdgeEnabled(true);
        setSingleLine(true);
    }
    @Override
    public String toString() {
        return "'" + getText() + "' (" + super.toString() + ")";
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
