public class TabWidget extends LinearLayout implements OnFocusChangeListener {
    private OnTabSelectionChanged mSelectionChangedListener;
    private int mSelectedTab = 0;
    private Drawable mLeftStrip;
    private Drawable mRightStrip;
    private boolean mDrawBottomStrips = true;
    private boolean mStripMoved;
    private Drawable mDividerDrawable;
    private final Rect mBounds = new Rect();
    public TabWidget(Context context) {
        this(context, null);
    }
    public TabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.tabWidgetStyle);
    }
    public TabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TabWidget,
                    defStyle, 0);
        mDrawBottomStrips = a.getBoolean(R.styleable.TabWidget_tabStripEnabled, true);
        mDividerDrawable = a.getDrawable(R.styleable.TabWidget_divider);
        mLeftStrip = a.getDrawable(R.styleable.TabWidget_tabStripLeft);
        mRightStrip = a.getDrawable(R.styleable.TabWidget_tabStripRight);
        a.recycle();
        initTabWidget();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mStripMoved = true;
        super.onSizeChanged(w, h, oldw, oldh);
    }
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == childCount - 1) {
            return mSelectedTab;
        } else if (i >= mSelectedTab) {
            return i + 1;
        } else {
            return i;
        }
    }
    private void initTabWidget() {
        setOrientation(LinearLayout.HORIZONTAL);
        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
        final Context context = mContext;
        final Resources resources = context.getResources();
        if (context.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.DONUT) {
            if (mLeftStrip == null) {
                mLeftStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_left_v4);
            }
            if (mRightStrip == null) {
                mRightStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_right_v4);
            }
        } else {
            if (mLeftStrip == null) {
                mLeftStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_left);
            }
            if (mRightStrip == null) {
                mRightStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_right);
            }
        }
        setFocusable(true);
        setOnFocusChangeListener(this);
    }
    public View getChildTabViewAt(int index) {
        if (mDividerDrawable != null) {
            index *= 2;
        }
        return getChildAt(index);
    }
    public int getTabCount() {
        int children = getChildCount();
        if (mDividerDrawable != null) {
            children = (children + 1) / 2;
        }
        return children;
    }
    public void setDividerDrawable(Drawable drawable) {
        mDividerDrawable = drawable;
        requestLayout();
        invalidate();
    }
    public void setDividerDrawable(int resId) {
        mDividerDrawable = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }
    public void setLeftStripDrawable(Drawable drawable) {
        mLeftStrip = drawable;
        requestLayout();
        invalidate();
    }
    public void setLeftStripDrawable(int resId) {
        mLeftStrip = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }
    public void setRightStripDrawable(Drawable drawable) {
        mRightStrip = drawable;
        requestLayout();
        invalidate();    }
    public void setRightStripDrawable(int resId) {
        mRightStrip = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }
    public void setStripEnabled(boolean stripEnabled) {
        mDrawBottomStrips = stripEnabled;
        invalidate();
    }
    public boolean isStripEnabled() {
        return mDrawBottomStrips;
    }
    @Override
    public void childDrawableStateChanged(View child) {
        if (getTabCount() > 0 && child == getChildTabViewAt(mSelectedTab)) {
            invalidate();
        }
        super.childDrawableStateChanged(child);
    }
    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getTabCount() == 0) return;
        if (!mDrawBottomStrips) {
            return;
        }
        final View selectedChild = getChildTabViewAt(mSelectedTab);
        final Drawable leftStrip = mLeftStrip;
        final Drawable rightStrip = mRightStrip;
        leftStrip.setState(selectedChild.getDrawableState());
        rightStrip.setState(selectedChild.getDrawableState());
        if (mStripMoved) {
            final Rect bounds = mBounds;
            bounds.left = selectedChild.getLeft();
            bounds.right = selectedChild.getRight();
            final int myHeight = getHeight();
            leftStrip.setBounds(Math.min(0, bounds.left - leftStrip.getIntrinsicWidth()),
                    myHeight - leftStrip.getIntrinsicHeight(), bounds.left, myHeight);
            rightStrip.setBounds(bounds.right, myHeight - rightStrip.getIntrinsicHeight(),
                    Math.max(getWidth(), bounds.right + rightStrip.getIntrinsicWidth()), myHeight);
            mStripMoved = false;
        }
        leftStrip.draw(canvas);
        rightStrip.draw(canvas);
    }
    public void setCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }
        getChildTabViewAt(mSelectedTab).setSelected(false);
        mSelectedTab = index;
        getChildTabViewAt(mSelectedTab).setSelected(true);
        mStripMoved = true;
    }
    public void focusCurrentTab(int index) {
        final int oldTab = mSelectedTab;
        setCurrentTab(index);
        if (oldTab != index) {
            getChildTabViewAt(index).requestFocus();
        }
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            View child = getChildTabViewAt(i);
            child.setEnabled(enabled);
        }
    }
    @Override
    public void addView(View child) {
        if (child.getLayoutParams() == null) {
            final LinearLayout.LayoutParams lp = new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            lp.setMargins(0, 0, 0, 0);
            child.setLayoutParams(lp);
        }
        child.setFocusable(true);
        child.setClickable(true);
        if (mDividerDrawable != null && getTabCount() > 0) {
            ImageView divider = new ImageView(mContext);
            final LinearLayout.LayoutParams lp = new LayoutParams(
                    mDividerDrawable.getIntrinsicWidth(),
                    LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 0);
            divider.setLayoutParams(lp);
            divider.setBackgroundDrawable(mDividerDrawable);
            super.addView(divider);
        }
        super.addView(child);
        child.setOnClickListener(new TabClickListener(getTabCount() - 1));
        child.setOnFocusChangeListener(this);
    }
    void setTabSelectionListener(OnTabSelectionChanged listener) {
        mSelectionChangedListener = listener;
    }
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus && getTabCount() > 0) {
            getChildTabViewAt(mSelectedTab).requestFocus();
            return;
        }
        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, false);
                    break;
                }
                i++;
            }
        }
    }
    private class TabClickListener implements OnClickListener {
        private final int mTabIndex;
        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }
        public void onClick(View v) {
            mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
        }
    }
    static interface OnTabSelectionChanged {
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
}
