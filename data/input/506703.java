public final class IconMenuView extends ViewGroup implements ItemInvoker, MenuView, Runnable {
    private static final int ITEM_CAPTION_CYCLE_DELAY = 1000;
    private MenuBuilder mMenu;
    private int mRowHeight;
    private int mMaxRows;
    private int mMaxItems;
    private int mMaxItemsPerRow;
    private int mNumActualItemsShown;
    private Drawable mHorizontalDivider;
    private int mHorizontalDividerHeight;
    private ArrayList<Rect> mHorizontalDividerRects;
    private Drawable mVerticalDivider;
    private int mVerticalDividerWidth;
    private ArrayList<Rect> mVerticalDividerRects;
    private Drawable mMoreIcon;
    private IconMenuItemView mMoreItemView;
    private Drawable mItemBackground;
    private int mAnimations;
    private boolean mHasStaleChildren;
    private boolean mMenuBeingLongpressed = false;
    private boolean mLastChildrenCaptionMode;
    private int[] mLayout;
    private int mLayoutNumRows;
    public IconMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = 
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.IconMenuView, 0, 0);
        mRowHeight = a.getDimensionPixelSize(com.android.internal.R.styleable.IconMenuView_rowHeight, 64);
        mMaxRows = a.getInt(com.android.internal.R.styleable.IconMenuView_maxRows, 2);
        mMaxItems = a.getInt(com.android.internal.R.styleable.IconMenuView_maxItems, 6);
        mMaxItemsPerRow = a.getInt(com.android.internal.R.styleable.IconMenuView_maxItemsPerRow, 3);
        mMoreIcon = a.getDrawable(com.android.internal.R.styleable.IconMenuView_moreIcon);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.MenuView, 0, 0);
        mItemBackground = a.getDrawable(com.android.internal.R.styleable.MenuView_itemBackground);
        mHorizontalDivider = a.getDrawable(com.android.internal.R.styleable.MenuView_horizontalDivider);
        mHorizontalDividerRects = new ArrayList<Rect>();
        mVerticalDivider =  a.getDrawable(com.android.internal.R.styleable.MenuView_verticalDivider);
        mVerticalDividerRects = new ArrayList<Rect>();
        mAnimations = a.getResourceId(com.android.internal.R.styleable.MenuView_windowAnimationStyle, 0);
        a.recycle();
        if (mHorizontalDivider != null) {
            mHorizontalDividerHeight = mHorizontalDivider.getIntrinsicHeight();
            if (mHorizontalDividerHeight == -1) mHorizontalDividerHeight = 1;
        }
        if (mVerticalDivider != null) {
            mVerticalDividerWidth = mVerticalDivider.getIntrinsicWidth();
            if (mVerticalDividerWidth == -1) mVerticalDividerWidth = 1;
        }
        mLayout = new int[mMaxRows];
        setWillNotDraw(false);
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }
    private void layoutItems(int width) {
        int numItems = getChildCount();
        if (numItems == 0) {
            mLayoutNumRows = 0;
            return;
        }
        int curNumRows =
                Math.min((int) Math.ceil(numItems / (float) mMaxItemsPerRow), mMaxRows);
        for (; curNumRows <= mMaxRows; curNumRows++) {
            layoutItemsUsingGravity(curNumRows, numItems);
            if (curNumRows >= numItems) {
                break;
            }
            if (doItemsFit()) {
                break;
            }
        }
    }
    private void layoutItemsUsingGravity(int numRows, int numItems) {
        int numBaseItemsPerRow = numItems / numRows;
        int numLeftoverItems = numItems % numRows;
        int rowsThatGetALeftoverItem = numRows - numLeftoverItems;
        int[] layout = mLayout;
        for (int i = 0; i < numRows; i++) {
            layout[i] = numBaseItemsPerRow;
            if (i >= rowsThatGetALeftoverItem) {
                layout[i]++;
            }
        }
        mLayoutNumRows = numRows;
    }
    private boolean doItemsFit() {
        int itemPos = 0;
        int[] layout = mLayout;
        int numRows = mLayoutNumRows;
        for (int row = 0; row < numRows; row++) {
            int numItemsOnRow = layout[row];
            if (numItemsOnRow == 1) {
                itemPos++;
                continue;
            }
            for (int itemsOnRowCounter = numItemsOnRow; itemsOnRowCounter > 0;
                    itemsOnRowCounter--) {
                View child = getChildAt(itemPos++);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.maxNumItemsOnRow < numItemsOnRow) {
                    return false;
                }
            }
        }
        return true;
    }
    private void addItemView(IconMenuItemView itemView) {   
        itemView.setIconMenuView(this);
        itemView.setBackgroundDrawable(
                mItemBackground.getConstantState().newDrawable(
                        getContext().getResources()));
        itemView.setItemInvoker(this);
        addView(itemView, itemView.getTextAppropriateLayoutParams());
    }
    private IconMenuItemView createMoreItemView() {
        LayoutInflater inflater = mMenu.getMenuType(MenuBuilder.TYPE_ICON).getInflater();
        final IconMenuItemView itemView = (IconMenuItemView) inflater.inflate(
                com.android.internal.R.layout.icon_menu_item_layout, null);
        Resources r = getContext().getResources();
        itemView.initialize(r.getText(com.android.internal.R.string.more_item_label), mMoreIcon);
        itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MenuBuilder.Callback cb = mMenu.getCallback();
                if (cb != null) {
                    cb.onMenuModeChange(mMenu);
                }
            }
        });
        return itemView;
    }
    public void initialize(MenuBuilder menu, int menuType) {
        mMenu = menu;
        updateChildren(true);
    }
    public void updateChildren(boolean cleared) {
        removeAllViews();
        final ArrayList<MenuItemImpl> itemsToShow = mMenu.getVisibleItems();
        final int numItems = itemsToShow.size();
        final int numItemsThatCanFit = mMaxItems;
        final int minFitMinus1AndNumItems = Math.min(numItemsThatCanFit - 1, numItems);
        MenuItemImpl itemData;
        for (int i = 0; i < minFitMinus1AndNumItems; i++) {
            itemData = itemsToShow.get(i);
            addItemView((IconMenuItemView) itemData.getItemView(MenuBuilder.TYPE_ICON, this));
        }
        if (numItems > numItemsThatCanFit) {
            if (mMoreItemView == null) {
                mMoreItemView = createMoreItemView();
            }
            addItemView(mMoreItemView);
            mNumActualItemsShown = numItemsThatCanFit - 1;
        } else if (numItems == numItemsThatCanFit) {
            final MenuItemImpl lastItemData = itemsToShow.get(numItemsThatCanFit - 1);
            addItemView((IconMenuItemView) lastItemData.getItemView(MenuBuilder.TYPE_ICON, this));
            mNumActualItemsShown = numItemsThatCanFit;
        }
    }
    private void positionChildren(int menuWidth, int menuHeight) {
        if (mHorizontalDivider != null) mHorizontalDividerRects.clear();
        if (mVerticalDivider != null) mVerticalDividerRects.clear();
        final int numRows = mLayoutNumRows;
        final int numRowsMinus1 = numRows - 1;
        final int numItemsForRow[] = mLayout;
        int itemPos = 0;
        View child;
        IconMenuView.LayoutParams childLayoutParams = null; 
        float itemLeft;
        float itemTop = 0;
        float itemWidth;
        final float itemHeight = (menuHeight - mHorizontalDividerHeight * (numRows - 1))
                / (float)numRows;
        for (int row = 0; row < numRows; row++) {
            itemLeft = 0;
            itemWidth = (menuWidth - mVerticalDividerWidth * (numItemsForRow[row] - 1))
                    / (float)numItemsForRow[row];
            for (int itemPosOnRow = 0; itemPosOnRow < numItemsForRow[row]; itemPosOnRow++) {
                child = getChildAt(itemPos);
                child.measure(MeasureSpec.makeMeasureSpec((int) itemWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int) itemHeight, MeasureSpec.EXACTLY));
                childLayoutParams = (IconMenuView.LayoutParams) child.getLayoutParams();
                childLayoutParams.left = (int) itemLeft;
                childLayoutParams.right = (int) (itemLeft + itemWidth);
                childLayoutParams.top = (int) itemTop;
                childLayoutParams.bottom = (int) (itemTop + itemHeight); 
                itemLeft += itemWidth;
                itemPos++;
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
            if ((mHorizontalDivider != null) && (row < numRowsMinus1)) {
                mHorizontalDividerRects.add(new Rect(0, (int) itemTop, menuWidth,
                        (int) (itemTop + mHorizontalDividerHeight)));
                itemTop += mHorizontalDividerHeight;
            }
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHasStaleChildren) {
            mHasStaleChildren = false;
            updateChildren(false);
        }
        int measuredWidth = resolveSize(Integer.MAX_VALUE, widthMeasureSpec);
        calculateItemFittingMetadata(measuredWidth);
        layoutItems(measuredWidth);
        final int layoutNumRows = mLayoutNumRows;
        final int desiredHeight = (mRowHeight + mHorizontalDividerHeight) *
                layoutNumRows - mHorizontalDividerHeight;
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
        if (layoutNumRows > 0) {
            positionChildren(mMeasuredWidth, mMeasuredHeight);
        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child;
        IconMenuView.LayoutParams childLayoutParams;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            childLayoutParams = (IconMenuView.LayoutParams)child
                    .getLayoutParams();
            child.layout(childLayoutParams.left, childLayoutParams.top, childLayoutParams.right,
                    childLayoutParams.bottom);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = mHorizontalDivider;
        if (drawable != null) {
            final ArrayList<Rect> rects = mHorizontalDividerRects;
            for (int i = rects.size() - 1; i >= 0; i--) {
                drawable.setBounds(rects.get(i));
                drawable.draw(canvas);
            }
        }
        drawable = mVerticalDivider;
        if (drawable != null) {
            final ArrayList<Rect> rects = mVerticalDividerRects;
            for (int i = rects.size() - 1; i >= 0; i--) {
                drawable.setBounds(rects.get(i));
                drawable.draw(canvas);
            }
        }
    }
    public boolean invokeItem(MenuItemImpl item) {
        return mMenu.performItemAction(item, 0);
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new IconMenuView.LayoutParams(getContext(), attrs);
    }
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof IconMenuView.LayoutParams;
    }
    void markStaleChildren() {
        if (!mHasStaleChildren) {
            mHasStaleChildren = true;
            requestLayout();
        }
    }
    int getNumActualItemsShown() {
        return mNumActualItemsShown;
    }
    public int getWindowAnimations() {
        return mAnimations;
    }
    public int[] getLayout() {
        return mLayout;
    }
    public int getLayoutNumRows() {
        return mLayoutNumRows;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                removeCallbacks(this);
                postDelayed(this, ViewConfiguration.getLongPressTimeout());
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (mMenuBeingLongpressed) {
                    setCycleShortcutCaptionMode(false);
                    return true;
                } else {
                    removeCallbacks(this);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestFocus();
    }
    @Override
    protected void onDetachedFromWindow() {
        setCycleShortcutCaptionMode(false);
        super.onDetachedFromWindow();
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            setCycleShortcutCaptionMode(false);
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }
    private void setCycleShortcutCaptionMode(boolean cycleShortcutAndNormal) {
        if (!cycleShortcutAndNormal) {
            removeCallbacks(this);
            setChildrenCaptionMode(false);
            mMenuBeingLongpressed = false;
        } else {
            setChildrenCaptionMode(true);
        }
    }
    public void run() {
        if (mMenuBeingLongpressed) {
            setChildrenCaptionMode(!mLastChildrenCaptionMode);
        } else {
            mMenuBeingLongpressed = true;
            setCycleShortcutCaptionMode(true);
        }
        postDelayed(this, ITEM_CAPTION_CYCLE_DELAY);
    }
    private void setChildrenCaptionMode(boolean shortcut) {
        mLastChildrenCaptionMode = shortcut;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            ((IconMenuItemView) getChildAt(i)).setCaptionMode(shortcut);
        }
    }
    private void calculateItemFittingMetadata(int width) {
        int maxNumItemsPerRow = mMaxItemsPerRow;
        int numItems = getChildCount();
        for (int i = 0; i < numItems; i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            lp.maxNumItemsOnRow = 1;
            for (int curNumItemsPerRow = maxNumItemsPerRow; curNumItemsPerRow > 0;
                    curNumItemsPerRow--) {
                if (lp.desiredWidth < width / curNumItemsPerRow) {
                    lp.maxNumItemsOnRow = curNumItemsPerRow;
                    break;
                }
            }
        }
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        View focusedView = getFocusedChild();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) == focusedView) {
                return new SavedState(superState, i);
            }
        }
        return new SavedState(superState, -1);
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.focusedPosition >= getChildCount()) {
            return;
        }
        View v = getChildAt(ss.focusedPosition);
        if (v != null) {
            v.requestFocus();
        }
    }
    private static class SavedState extends BaseSavedState {
        int focusedPosition;
        public SavedState(Parcelable superState, int focusedPosition) {
            super(superState);
            this.focusedPosition = focusedPosition;
        }
        private SavedState(Parcel in) {
            super(in);
            focusedPosition = in.readInt();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(focusedPosition);
        }
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams
    {
        int left, top, right, bottom;
        int desiredWidth;
        int maxNumItemsOnRow;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
