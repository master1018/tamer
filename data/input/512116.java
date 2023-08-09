public class ScrollingTabWidget extends RelativeLayout
        implements OnClickListener, ViewTreeObserver.OnGlobalFocusChangeListener,
        OnFocusChangeListener {
    private static final String TAG = "ScrollingTabWidget";
    private OnTabSelectionChangedListener mSelectionChangedListener;
    private int mSelectedTab = 0;
    private ImageView mLeftArrowView;
    private ImageView mRightArrowView;
    private HorizontalScrollView mTabsScrollWrapper;
    private TabStripView mTabsView;
    private LayoutInflater mInflater;
    private int mLeftMostVisibleTabIndex = 0;
    public ScrollingTabWidget(Context context) {
        this(context, null);
    }
    public ScrollingTabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ScrollingTabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        setFocusable(true);
        setOnFocusChangeListener(this);
        if (!hasFocus()) {
            setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        }
        mLeftArrowView = (ImageView) mInflater.inflate(R.layout.tab_left_arrow, this, false);
        mLeftArrowView.setOnClickListener(this);
        mRightArrowView = (ImageView) mInflater.inflate(R.layout.tab_right_arrow, this, false);
        mRightArrowView.setOnClickListener(this);
        mTabsScrollWrapper = (HorizontalScrollView) mInflater.inflate(
                R.layout.tab_layout, this, false);
        mTabsView = (TabStripView) mTabsScrollWrapper.findViewById(android.R.id.tabs);
        View accountNameView = mInflater.inflate(R.layout.tab_account_name, this, false);
        mLeftArrowView.setVisibility(View.INVISIBLE);
        mRightArrowView.setVisibility(View.INVISIBLE);
        addView(mTabsScrollWrapper);
        addView(mLeftArrowView);
        addView(mRightArrowView);
        addView(accountNameView);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.addOnGlobalFocusChangeListener(this);
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.removeOnGlobalFocusChangeListener(this);
        }
    }
    protected void updateArrowVisibility() {
        int scrollViewLeftEdge = mTabsScrollWrapper.getScrollX();
        int tabsViewLeftEdge = mTabsView.getLeft();
        int scrollViewRightEdge = scrollViewLeftEdge + mTabsScrollWrapper.getWidth();
        int tabsViewRightEdge = mTabsView.getRight();
        int rightArrowCurrentVisibility = mRightArrowView.getVisibility();
        if (scrollViewRightEdge == tabsViewRightEdge
                && rightArrowCurrentVisibility == View.VISIBLE) {
            mRightArrowView.setVisibility(View.INVISIBLE);
        } else if (scrollViewRightEdge < tabsViewRightEdge
                && rightArrowCurrentVisibility != View.VISIBLE) {
            mRightArrowView.setVisibility(View.VISIBLE);
        }
        int leftArrowCurrentVisibility = mLeftArrowView.getVisibility();
        if (scrollViewLeftEdge == tabsViewLeftEdge
                && leftArrowCurrentVisibility == View.VISIBLE) {
            mLeftArrowView.setVisibility(View.INVISIBLE);
        } else if (scrollViewLeftEdge > tabsViewLeftEdge
                && leftArrowCurrentVisibility != View.VISIBLE) {
            mLeftArrowView.setVisibility(View.VISIBLE);
        }
    }
    public View getChildTabViewAt(int index) {
        return mTabsView.getChildAt(index);
    }
    public int getTabCount() {
        return mTabsView.getChildCount();
    }
    public ViewGroup getTabParent() {
        return mTabsView;
    }
    public void removeAllTabs() {
        mTabsView.removeAllViews();
    }
    @Override
    public void dispatchDraw(Canvas canvas) {
        updateArrowVisibility();
        super.dispatchDraw(canvas);
    }
    public void setCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }
        if (mSelectedTab < getTabCount()) {
            mTabsView.setSelected(mSelectedTab, false);
        }
        mSelectedTab = index;
        mTabsView.setSelected(mSelectedTab, true);
    }
    public int getCurrentTab() {
        return mSelectedTab;
    }
    public void focusCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }
        setCurrentTab(index);
        getChildTabViewAt(index).requestFocus();
    }
    public void addTab(int layoutResId) {
        addTab(mInflater.inflate(layoutResId, mTabsView, false));
    }
    public void addTab(View child) {
        if (child == null) {
            return;
        }
        if (child.getLayoutParams() == null) {
            final LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            child.setLayoutParams(lp);
        }
        child.setFocusable(true);
        child.setClickable(true);
        child.setOnClickListener(new TabClickListener());
        child.setOnFocusChangeListener(this);
        mTabsView.addView(child);
    }
    public void setTabSelectionListener(OnTabSelectionChangedListener listener) {
        mSelectionChangedListener = listener;
    }
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (isTab(oldFocus) && !isTab(newFocus)) {
            onLoseFocus();
        }
    }
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus) {
            onObtainFocus();
            return;
        }
        if (hasFocus) {
            for (int i = 0; i < getTabCount(); i++) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, false);
                    break;
                }
            }
        }
    }
    protected void onObtainFocus() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        focusCurrentTab(mSelectedTab);
        mSelectionChangedListener.onTabSelectionChanged(mSelectedTab, false);
    }
    protected void onLoseFocus() {
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }
    public boolean isTab(View v) {
        for (int i = 0; i < getTabCount(); i++) {
            if (getChildTabViewAt(i) == v) {
                return true;
            }
        }
        return false;
    }
    private class TabClickListener implements OnClickListener {
        public void onClick(View v) {
            for (int i = 0; i < getTabCount(); i++) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, true);
                    break;
                }
            }
        }
    }
    public interface OnTabSelectionChangedListener {
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
    public void onClick(View v) {
        updateLeftMostVisible();
        if (v == mRightArrowView && (mLeftMostVisibleTabIndex + 1 < getTabCount())) {
            tabScroll(true );
        } else if (v == mLeftArrowView && mLeftMostVisibleTabIndex > 0) {
            tabScroll(false );
        }
    }
    protected void updateLeftMostVisible() {
        int viewableLeftEdge = mTabsScrollWrapper.getScrollX();
        if (mLeftArrowView.getVisibility() == View.VISIBLE) {
            viewableLeftEdge += mLeftArrowView.getWidth();
        }
        for (int i = 0; i < getTabCount(); i++) {
            View tab = getChildTabViewAt(i);
            int tabLeftEdge = tab.getLeft();
            if (tabLeftEdge >= viewableLeftEdge) {
                mLeftMostVisibleTabIndex = i;
                break;
            }
        }
    }
    protected void tabScroll(boolean directionRight) {
        int scrollWidth = 0;
        View newLeftMostVisibleTab = null;
        if (directionRight) {
            newLeftMostVisibleTab = getChildTabViewAt(++mLeftMostVisibleTabIndex);
        } else {
            newLeftMostVisibleTab = getChildTabViewAt(--mLeftMostVisibleTabIndex);
        }
        scrollWidth = newLeftMostVisibleTab.getLeft() - mTabsScrollWrapper.getScrollX();
        if (mLeftMostVisibleTabIndex > 0) {
            scrollWidth -= mLeftArrowView.getWidth();
        }
        mTabsScrollWrapper.smoothScrollBy(scrollWidth, 0);
    }
}
