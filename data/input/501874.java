class InCallMenuView extends ViewGroup {
    private static final String LOG_TAG = "PHONE/InCallMenuView";
    private static final boolean DBG = false;
    private int mRowHeight;
    private Drawable mHorizontalDivider;
    private int mHorizontalDividerHeight;
    private ArrayList<Rect> mHorizontalDividerRects;
    private Drawable mVerticalDivider;
    private int mVerticalDividerWidth;
    private ArrayList<Rect> mVerticalDividerRects;
    private Drawable mItemBackground;
    private static final int NUM_ROWS = 3;
    private static final int MAX_ITEMS_PER_ROW = 10;
    private InCallMenuItemView[][] mItems = new InCallMenuItemView[NUM_ROWS][MAX_ITEMS_PER_ROW];
    private int mNumItemsForRow[] = new int[NUM_ROWS];
    private int mNumVisibleItemsForRow[] = new int[NUM_ROWS];
    private int mNumVisibleRows;
    private InCallScreen mInCallScreen;
    InCallMenuView(Context context, InCallScreen inCallScreen) {
        super(context);
        if (DBG) log("InCallMenuView constructor...");
        mInCallScreen = inCallScreen;
        TypedArray a =
                mContext.obtainStyledAttributes(com.android.internal.R.styleable.IconMenuView);
        if (DBG) log("- IconMenuView styled attrs: " + a);
        mRowHeight = a.getDimensionPixelSize(
                com.android.internal.R.styleable.IconMenuView_rowHeight, 64);
        if (DBG) log("  - mRowHeight: " + mRowHeight);
        a.recycle();
        a = mContext.obtainStyledAttributes(com.android.internal.R.styleable.MenuView);
        if (DBG) log("- MenuView styled attrs: " + a);
        mItemBackground = a.getDrawable(com.android.internal.R.styleable.MenuView_itemBackground);
        if (DBG) log("  - mItemBackground: " + mItemBackground);
        mHorizontalDivider = a.getDrawable(com.android.internal.R.styleable.MenuView_horizontalDivider);
        if (DBG) log("  - mHorizontalDivider: " + mHorizontalDivider);
        mHorizontalDividerRects = new ArrayList<Rect>();
        mVerticalDivider =  a.getDrawable(com.android.internal.R.styleable.MenuView_verticalDivider);
        if (DBG) log("  - mVerticalDivider: " + mVerticalDivider);
        mVerticalDividerRects = new ArrayList<Rect>();
        a.recycle();
        if (mHorizontalDivider != null) {
            mHorizontalDividerHeight = mHorizontalDivider.getIntrinsicHeight();
            if (mHorizontalDividerHeight == -1) mHorizontalDividerHeight = 1;
        }
        if (mVerticalDivider != null) {
            mVerticalDividerWidth = mVerticalDivider.getIntrinsicWidth();
            if (mVerticalDividerWidth == -1) mVerticalDividerWidth = 1;
        }
        setWillNotDraw(false);
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
    }
    void clearInCallScreenReference() {
        mInCallScreen = null;
    }
     void addItemView(InCallMenuItemView itemView, int row) {
        if (DBG) log("addItemView(" + itemView + ", row " + row + ")...");
        if (row >= NUM_ROWS) {
            throw new IllegalStateException("Row index " + row + " > NUM_ROWS");
        }
        int indexInRow = mNumItemsForRow[row];
        if (indexInRow >= MAX_ITEMS_PER_ROW) {
            throw new IllegalStateException("Too many items (" + indexInRow + ") in row " + row);
        }
        mNumItemsForRow[row]++;
        mItems[row][indexInRow] = itemView;
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }
        itemView.setBackgroundDrawable(mItemBackground.getConstantState().newDrawable());
        addView(itemView, lp);
    }
     void updateVisibility() {
        if (DBG) log("updateVisibility()...");
        mNumVisibleRows = 0;
        for (int row = 0; row < NUM_ROWS; row++) {
            InCallMenuItemView[] thisRow = mItems[row];
            int numItemsThisRow = mNumItemsForRow[row];
            int numVisibleThisRow = 0;
            for (int itemIndex = 0; itemIndex < numItemsThisRow; itemIndex++) {
                if  (mItems[row][itemIndex].isVisible()) numVisibleThisRow++;
            }
            if (DBG) log("==> Num visible for row " + row + ": " + numVisibleThisRow);
            mNumVisibleItemsForRow[row] = numVisibleThisRow;
            if (numVisibleThisRow > 0) mNumVisibleRows++;
        }
        if (DBG) log("==> Num visible rows: " + mNumVisibleRows);
    }
     void dumpState() {
        if (DBG) log("============ dumpState() ============");
        if (DBG) log("- mItems LENGTH: " + mItems.length);
        for (int row = 0; row < NUM_ROWS; row++) {
            if (DBG) log("-      Row " + row + ": length " + mItems[row].length
                         + ", num items " + mNumItemsForRow[row]
                         + ", num visible " + mNumVisibleItemsForRow[row]);
        }
    }
    private void positionChildren(int menuWidth, int menuHeight) {
        if (DBG) log("positionChildren(" + menuWidth + " x " + menuHeight + ")...");
        if (mHorizontalDivider != null) mHorizontalDividerRects.clear();
        if (mVerticalDivider != null) mVerticalDividerRects.clear();
        InCallMenuItemView child;
        InCallMenuView.LayoutParams childLayoutParams = null;
        float itemLeft;
        float itemTop = 0;
        float itemWidth;
        final float itemHeight = (menuHeight - mHorizontalDividerHeight * (mNumVisibleRows - 1))
                / (float) mNumVisibleRows;
        int numHorizDividersRemainingToDraw = mNumVisibleRows - 1;
        for (int row = 0; row < NUM_ROWS; row++) {
            int numItemsThisRow = mNumItemsForRow[row];
            int numVisibleThisRow = mNumVisibleItemsForRow[row];
            if (DBG) log("  - num visible for row " + row + ": " + numVisibleThisRow);
            if (numVisibleThisRow == 0) {
                continue;
            }
            InCallMenuItemView[] thisRow = mItems[row];
            itemLeft = 0;
            itemWidth = (menuWidth - mVerticalDividerWidth * (numVisibleThisRow - 1))
                    / (float) numVisibleThisRow;
            for (int itemIndex = 0; itemIndex < numItemsThisRow; itemIndex++) {
                child = mItems[row][itemIndex];
                if (!child.isVisible()) continue;
                if (DBG) log("==> child [" + row + "][" + itemIndex + "]: " + child);
                child.measure(MeasureSpec.makeMeasureSpec((int) itemWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec((int) itemHeight, MeasureSpec.EXACTLY));
                childLayoutParams = (InCallMenuView.LayoutParams) child.getLayoutParams();
                childLayoutParams.left = (int) itemLeft;
                childLayoutParams.right = (int) (itemLeft + itemWidth);
                childLayoutParams.top = (int) itemTop;
                childLayoutParams.bottom = (int) (itemTop + itemHeight);
                itemLeft += itemWidth;
                if (mVerticalDivider != null) {
                    mVerticalDividerRects.add(new Rect((int) itemLeft,
                            (int) itemTop, (int) (itemLeft + mVerticalDividerWidth),
                            (int) (itemTop + itemHeight)));
                }
                itemLeft += mVerticalDividerWidth;
            }
            if (childLayoutParams != null) {
                childLayoutParams.right = menuWidth;
            }
            itemTop += itemHeight;
            if ((mHorizontalDivider != null) && (numHorizDividersRemainingToDraw-- > 0)) {
                mHorizontalDividerRects.add(new Rect(0, (int) itemTop, menuWidth,
                                                     (int) (itemTop + mHorizontalDividerHeight)));
                itemTop += mHorizontalDividerHeight;
            }
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (DBG) log("onMeasure(" + widthMeasureSpec + " x " + heightMeasureSpec + ")...");
        final int desiredHeight = (mRowHeight + mHorizontalDividerHeight) * mNumVisibleRows
                - mHorizontalDividerHeight;
        setMeasuredDimension(resolveSize(Integer.MAX_VALUE, widthMeasureSpec),
                             resolveSize(desiredHeight, heightMeasureSpec));
        positionChildren(mMeasuredWidth, mMeasuredHeight);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (DBG) log("onLayout(changed " + changed
                     + ", l " + l + " t " + t + " r " + r + " b " + b + ")...");
        View child;
        InCallMenuView.LayoutParams childLayoutParams;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            childLayoutParams = (InCallMenuView.LayoutParams) child.getLayoutParams();
            child.layout(childLayoutParams.left, childLayoutParams.top,
                         childLayoutParams.right, childLayoutParams.bottom);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (DBG) log("onDraw()...");
        if (mHorizontalDivider != null) {
            for (int i = mHorizontalDividerRects.size() - 1; i >= 0; i--) {
                mHorizontalDivider.setBounds(mHorizontalDividerRects.get(i));
                mHorizontalDivider.draw(canvas);
            }
        }
        if (mVerticalDivider != null) {
            for (int i = mVerticalDividerRects.size() - 1; i >= 0; i--) {
                mVerticalDivider.setBounds(mVerticalDividerRects.get(i));
                mVerticalDivider.draw(canvas);
            }
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (DBG) log("dispatchKeyEvent(" + event + ")...");
        int keyCode = event.getKeyCode();
        if (event.isDown()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (DBG) log("==> BACK key!  handling it ourselves...");
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    break;
                default:
                    if (DBG) log("==> dispatchKeyEvent: forwarding event to the InCallScreen");
                    if (mInCallScreen != null) {
                        return mInCallScreen.onKeyDown(keyCode, event);
                    }
                    break;
            }
        } else if (mInCallScreen != null &&
                (keyCode == KeyEvent.KEYCODE_CALL ||
                        mInCallScreen.isKeyEventAcceptableDTMF(event))) {
            if (DBG) log("==> dispatchKeyEvent: forwarding key up event to the InCallScreen");
            return mInCallScreen.onKeyUp(keyCode, event);
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new InCallMenuView.LayoutParams(getContext(), attrs);
    }
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof InCallMenuView.LayoutParams;
    }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        int left, top, right, bottom;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
