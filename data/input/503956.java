public class Gallery extends AbsSpinner implements GestureDetector.OnGestureListener {
    private static final String TAG = "Gallery";
    private static final boolean localLOGV = false;
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;
    private int mSpacing = 0;
    private int mAnimationDuration = 400;
    private float mUnselectedAlpha;
    private int mLeftMost;
    private int mRightMost;
    private int mGravity;
    private GestureDetector mGestureDetector;
    private int mDownTouchPosition;
    private View mDownTouchView;
    private FlingRunnable mFlingRunnable = new FlingRunnable();
    private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
        public void run() {
            mSuppressSelectionChanged = false;
            selectionChanged();
        }
    };
    private boolean mShouldStopFling;
    private View mSelectedChild;
    private boolean mShouldCallbackDuringFling = true;
    private boolean mShouldCallbackOnUnselectedItemClick = true;
    private boolean mSuppressSelectionChanged;
    private boolean mReceivedInvokeKeyDown;
    private AdapterContextMenuInfo mContextMenuInfo;
    private boolean mIsFirstScroll;
    public Gallery(Context context) {
        this(context, null);
    }
    public Gallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.galleryStyle);
    }
    public Gallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(true);
        TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Gallery, defStyle, 0);
        int index = a.getInt(com.android.internal.R.styleable.Gallery_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }
        int animationDuration =
                a.getInt(com.android.internal.R.styleable.Gallery_animationDuration, -1);
        if (animationDuration > 0) {
            setAnimationDuration(animationDuration);
        }
        int spacing =
                a.getDimensionPixelOffset(com.android.internal.R.styleable.Gallery_spacing, 0);
        setSpacing(spacing);
        float unselectedAlpha = a.getFloat(
                com.android.internal.R.styleable.Gallery_unselectedAlpha, 0.5f);
        setUnselectedAlpha(unselectedAlpha);
        a.recycle();
        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
        mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
    }
    public void setCallbackDuringFling(boolean shouldCallback) {
        mShouldCallbackDuringFling = shouldCallback;
    }
    public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
        mShouldCallbackOnUnselectedItemClick = shouldCallback;
    }
    public void setAnimationDuration(int animationDurationMillis) {
        mAnimationDuration = animationDurationMillis;
    }
    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }
    public void setUnselectedAlpha(float unselectedAlpha) {
        mUnselectedAlpha = unselectedAlpha;
    }
    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);
        return true;
    }
    @Override
    protected int computeHorizontalScrollExtent() {
        return 1;
    }
    @Override
    protected int computeHorizontalScrollOffset() {
        return mSelectedPosition;
    }
    @Override
    protected int computeHorizontalScrollRange() {
        return mItemCount;
    }
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mInLayout = true;
        layout(0, false);
        mInLayout = false;
    }
    @Override
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }
    void trackMotionScroll(int deltaX) {
        if (getChildCount() == 0) {
            return;
        }
        boolean toLeft = deltaX < 0; 
        int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
        if (limitedDeltaX != deltaX) {
            mFlingRunnable.endFling(false);
            onFinishedMovement();
        }
        offsetChildrenLeftAndRight(limitedDeltaX);
        detachOffScreenChildren(toLeft);
        if (toLeft) {
            fillToGalleryRight();
        } else {
            fillToGalleryLeft();
        }
        mRecycler.clear();
        setSelectionToCenterChild();
        invalidate();
    }
    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);
        if (extremeChild == null) {
            return deltaX;
        }
        int extremeChildCenter = getCenterOfView(extremeChild);
        int galleryCenter = getCenterOfGallery();
        if (motionToLeft) {
            if (extremeChildCenter <= galleryCenter) {
                return 0;
            }
        } else {
            if (extremeChildCenter >= galleryCenter) {
                return 0;
            }
        }
        int centerDifference = galleryCenter - extremeChildCenter;
        return motionToLeft
                ? Math.max(centerDifference, deltaX)
                : Math.min(centerDifference, deltaX); 
    }
    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }
    private int getCenterOfGallery() {
        return (getWidth() - mPaddingLeft - mPaddingRight) / 2 + mPaddingLeft;
    }
    private static int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }
    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;
        if (toLeft) {
            final int galleryLeft = mPaddingLeft;
            for (int i = 0; i < numChildren; i++) {
                final View child = getChildAt(i);
                if (child.getRight() >= galleryLeft) {
                    break;
                } else {
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }
        } else {
            final int galleryRight = getWidth() - mPaddingRight;
            for (int i = numChildren - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getLeft() <= galleryRight) {
                    break;
                } else {
                    start = i;
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }
        }
        detachViewsFromParent(start, count);
        if (toLeft) {
            mFirstPosition += count;
        }
    }
    private void scrollIntoSlots() {
        if (getChildCount() == 0 || mSelectedChild == null) return;
        int selectedCenter = getCenterOfView(mSelectedChild);
        int targetCenter = getCenterOfGallery();
        int scrollAmount = targetCenter - selectedCenter;
        if (scrollAmount != 0) {
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            onFinishedMovement();
        }
    }
    private void onFinishedMovement() {
        if (mSuppressSelectionChanged) {
            mSuppressSelectionChanged = false;
            super.selectionChanged();
        }
        invalidate();
    }
    @Override
    void selectionChanged() {
        if (!mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }
    private void setSelectionToCenterChild() {
        View selView = mSelectedChild;
        if (mSelectedChild == null) return;
        int galleryCenter = getCenterOfGallery();
        if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
            return;
        }
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getLeft() <= galleryCenter && child.getRight() >=  galleryCenter) {
                newSelectedChildIndex = i;
                break;
            }
            int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
                    Math.abs(child.getRight() - galleryCenter));
            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }
        int newPos = mFirstPosition + newSelectedChildIndex;
        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }
    @Override
    void layout(int delta, boolean animate) {
        int childrenLeft = mSpinnerPadding.left;
        int childrenWidth = mRight - mLeft - mSpinnerPadding.left - mSpinnerPadding.right;
        if (mDataChanged) {
            handleDataChanged();
        }
        if (mItemCount == 0) {
            resetList();
            return;
        }
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }
        recycleAllViews();
        detachAllViewsFromParent();
        mRightMost = 0;
        mLeftMost = 0;
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddView(mSelectedPosition, 0, 0, true);
        int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        sel.offsetLeftAndRight(selectedOffset);
        fillToGalleryRight();
        fillToGalleryLeft();
        mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);
        updateSelectedItemMetadata();
    }
    private void fillToGalleryLeft() {
        int itemSpacing = mSpacing;
        int galleryLeft = mPaddingLeft;
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;
        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            curPosition = 0; 
            curRightEdge = mRight - mLeft - mPaddingRight;
            mShouldStopFling = true;
        }
        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curRightEdge, false);
            mFirstPosition = curPosition;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }
    private void fillToGalleryRight() {
        int itemSpacing = mSpacing;
        int galleryRight = mRight - mLeft - mPaddingRight;
        int numChildren = getChildCount();
        int numItems = mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;
        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = mPaddingLeft;
            mShouldStopFling = true;
        }
        while (curLeftEdge < galleryRight && curPosition < numItems) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curLeftEdge, true);
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
    }
    private View makeAndAddView(int position, int offset, int x,
            boolean fromLeft) {
        View child;
        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {
                int childLeft = child.getLeft();
                mRightMost = Math.max(mRightMost, childLeft 
                        + child.getMeasuredWidth());
                mLeftMost = Math.min(mLeftMost, childLeft);
                setUpChild(child, offset, x, fromLeft);
                return child;
            }
        }
        child = mAdapter.getView(position, null, this);
        setUpChild(child, offset, x, fromLeft);
        return child;
    }
    private void setUpChild(View child, int offset, int x, boolean fromLeft) {
        Gallery.LayoutParams lp = (Gallery.LayoutParams) 
            child.getLayoutParams();
        if (lp == null) {
            lp = (Gallery.LayoutParams) generateDefaultLayoutParams();
        }
        addViewInLayout(child, fromLeft ? -1 : 0, lp);
        child.setSelected(offset == 0);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mSpinnerPadding.left + mSpinnerPadding.right, lp.width);
        child.measure(childWidthSpec, childHeightSpec);
        int childLeft;
        int childRight;
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();
        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }
        child.layout(childLeft, childTop, childRight, childBottom);
    }
    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? mMeasuredHeight : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight(); 
        int childTop = 0;
        switch (mGravity) {
        case Gravity.TOP:
            childTop = mSpinnerPadding.top;
            break;
        case Gravity.CENTER_VERTICAL:
            int availableSpace = myHeight - mSpinnerPadding.bottom
                    - mSpinnerPadding.top - childHeight;
            childTop = mSpinnerPadding.top + (availableSpace / 2);
            break;
        case Gravity.BOTTOM:
            childTop = myHeight - mSpinnerPadding.bottom - childHeight;
            break;
        }
        return childTop;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            onUp();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onCancel();
        }
        return retValue;
    }
    public boolean onSingleTapUp(MotionEvent e) {
        if (mDownTouchPosition >= 0) {
            scrollToChild(mDownTouchPosition - mFirstPosition);
            if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
                performItemClick(mDownTouchView, mDownTouchPosition, mAdapter
                        .getItemId(mDownTouchPosition));
            }
            return true;
        }
        return false;
    }
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mShouldCallbackDuringFling) {
            removeCallbacks(mDisableSuppressSelectionChangedRunnable);
            if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
        }
        mFlingRunnable.startUsingVelocity((int) -velocityX);
        return true;
    }
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (localLOGV) Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));
        mParent.requestDisallowInterceptTouchEvent(true);
        if (!mShouldCallbackDuringFling) {
            if (mIsFirstScroll) {
                if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
                postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
            }
        } else {
            if (mSuppressSelectionChanged) mSuppressSelectionChanged = false;
        }
        trackMotionScroll(-1 * (int) distanceX);
        mIsFirstScroll = false;
        return true;
    }
    public boolean onDown(MotionEvent e) {
        mFlingRunnable.stop(false);
        mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
            mDownTouchView.setPressed(true);
        }
        mIsFirstScroll = true;
        return true;
    }
    void onUp() {
        if (mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }
        dispatchUnpress();
    }
    void onCancel() {
        onUp();
    }
    public void onLongPress(MotionEvent e) {
        if (mDownTouchPosition < 0) {
            return;
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        long id = getItemIdAtPosition(mDownTouchPosition);
        dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
    }
    public void onShowPress(MotionEvent e) {
    }
    private void dispatchPress(View child) {
        if (child != null) {
            child.setPressed(true);
        }
        setPressed(true);
    }
    private void dispatchUnpress() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }
        setPressed(false);
    }
    @Override
    public void dispatchSetSelected(boolean selected) {
    }
    @Override
    protected void dispatchSetPressed(boolean pressed) {
        if (mSelectedChild != null) {
            mSelectedChild.setPressed(pressed);
        }
    }
    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }
    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        final long longPressId = mAdapter.getItemId(longPressPosition);
        return dispatchLongPress(originalView, longPressPosition, longPressId);
    }
    @Override
    public boolean showContextMenu() {
        if (isPressed() && mSelectedPosition >= 0) {
            int index = mSelectedPosition - mFirstPosition;
            View v = getChildAt(index);
            return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
        }        
        return false;
    }
    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;
        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView,
                    mDownTouchPosition, id);
        }
        if (!handled) {
            mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return event.dispatch(this);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (movePrevious()) {
                playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
            }
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (moveNext()) {
                playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
            }
            return true;
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            mReceivedInvokeKeyDown = true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER: {
            if (mReceivedInvokeKeyDown) {
                if (mItemCount > 0) {
                    dispatchPress(mSelectedChild);
                    postDelayed(new Runnable() {
                        public void run() {
                            dispatchUnpress();
                        }
                    }, ViewConfiguration.getPressedStateDuration());
                    int selectedIndex = mSelectedPosition - mFirstPosition;
                    performItemClick(getChildAt(selectedIndex), mSelectedPosition, mAdapter
                            .getItemId(mSelectedPosition));
                }
            }
            mReceivedInvokeKeyDown = false;
            return true;
        }
        }
        return super.onKeyUp(keyCode, event);
    }
    boolean movePrevious() {
        if (mItemCount > 0 && mSelectedPosition > 0) {
            scrollToChild(mSelectedPosition - mFirstPosition - 1);
            return true;
        } else {
            return false;
        }
    }
    boolean moveNext() {
        if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
            scrollToChild(mSelectedPosition - mFirstPosition + 1);
            return true;
        } else {
            return false;
        }
    }
    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);
        if (child != null) {
            int distance = getCenterOfGallery() - getCenterOfView(child);
            mFlingRunnable.startUsingDistance(distance);
            return true;
        }
        return false;
    }
    @Override
    void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);
        updateSelectedItemMetadata();
    }
    private void updateSelectedItemMetadata() {
        View oldSelectedChild = mSelectedChild;
        View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
        if (child == null) {
            return;
        }
        child.setSelected(true);
        child.setFocusable(true);
        if (hasFocus()) {
            child.requestFocus();
        }
        if (oldSelectedChild != null) {
            oldSelectedChild.setSelected(false);
            oldSelectedChild.setFocusable(false);
        }
    }
    public void setGravity(int gravity)
    {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = mSelectedPosition - mFirstPosition;
        if (selectedIndex < 0) return i;
        if (i == childCount - 1) {
            return selectedIndex;
        } else if (i >= selectedIndex) {
            return i + 1;
        } else {
            return i;
        }
    }
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && mSelectedChild != null) {
            mSelectedChild.requestFocus(direction);
        }
    }
    private class FlingRunnable implements Runnable {
        private Scroller mScroller;
        private int mLastFlingX;
        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }
        private void startCommon() {
            removeCallbacks(this);
        }
        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0) return;
            startCommon();
            int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingX = initialX;
            mScroller.fling(initialX, 0, initialVelocity, 0,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            post(this);
        }
        public void startUsingDistance(int distance) {
            if (distance == 0) return;
            startCommon();
            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            post(this);
        }
        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }
        private void endFling(boolean scrollIntoSlots) {
            mScroller.forceFinished(true);
            if (scrollIntoSlots) scrollIntoSlots();
        }
        public void run() {
            if (mItemCount == 0) {
                endFling(true);
                return;
            }
            mShouldStopFling = false;
            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();
            int delta = mLastFlingX - x;
            if (delta > 0) {
                mDownTouchPosition = mFirstPosition;
                delta = Math.min(getWidth() - mPaddingLeft - mPaddingRight - 1, delta);
            } else {
                int offsetToLast = getChildCount() - 1;
                mDownTouchPosition = mFirstPosition + offsetToLast;
                delta = Math.max(-(getWidth() - mPaddingRight - mPaddingLeft - 1), delta);
            }
            trackMotionScroll(delta);
            if (more && !mShouldStopFling) {
                mLastFlingX = x;
                post(this);
            } else {
               endFling(true);
            }
        }
    }
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int w, int h) {
            super(w, h);
        }
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
