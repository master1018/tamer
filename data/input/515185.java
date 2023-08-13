public class TabStripView extends LinearLayout {
    private Drawable mBottomLeftStrip;
    private Drawable mBottomRightStrip;
    private int mSelectedTabIndex;
    public TabStripView(Context context) {
        this(context, null);
    }
    public TabStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
        mBottomLeftStrip = mContext.getResources().getDrawable(
                R.drawable.tab_bottom);
        mBottomRightStrip = mContext.getResources().getDrawable(
                R.drawable.tab_bottom);
    }
    public void setSelected(int index, boolean selected) {
        mSelectedTabIndex = index;
        getChildAt(index).setSelected(selected);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewParent parent = getParent();
        if (parent instanceof HorizontalScrollView) {
            setMinimumWidth(((HorizontalScrollView) getParent()).getMeasuredWidth());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == childCount - 1) {
            return mSelectedTabIndex;
        } else if (i >= mSelectedTabIndex) {
            return i + 1;
        } else {
            return i;
        }
    }
    @Override
    public void childDrawableStateChanged(View child) {
        if (child == getChildAt(mSelectedTabIndex)) {
            invalidate();
        }
        super.childDrawableStateChanged(child);
    }
    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        View selectedChild = getChildAt(mSelectedTabIndex);
        mBottomRightStrip.setState(selectedChild.getDrawableState());
        mBottomLeftStrip.setState(selectedChild.getDrawableState());
        Rect selBounds = new Rect(); 
        selBounds.left = selectedChild.getLeft() - getScrollX();
        selBounds.right = selectedChild.getRight() - getScrollX();
        final int myHeight = getHeight();
        mBottomLeftStrip.setBounds(
                Math.min(0, selBounds.left
                             - mBottomLeftStrip.getIntrinsicWidth()),
                myHeight - mBottomLeftStrip.getIntrinsicHeight(),
                selBounds.left,
                myHeight);
        mBottomRightStrip.setBounds(
                selBounds.right,
                myHeight - mBottomRightStrip.getIntrinsicHeight(),
                Math.max(getWidth(),
                        selBounds.right + mBottomRightStrip.getIntrinsicWidth()),
                myHeight);
        mBottomLeftStrip.draw(canvas);
        mBottomRightStrip.draw(canvas);
    }
}
