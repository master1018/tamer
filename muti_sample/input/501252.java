public class FixedGridLayout extends ViewGroup {
    int mCellWidth;
    int mCellHeight;
    public FixedGridLayout(Context context) {
        super(context);
    }
    public FixedGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FixedGridLayout);
        mCellWidth = a.getDimensionPixelSize(
                R.styleable.FixedGridLayout_cellWidth, -1);
        mCellHeight = a.getDimensionPixelSize(
                R.styleable.FixedGridLayout_cellHeight, -1);
        a.recycle();
    }
    public void setCellWidth(int px) {
        mCellWidth = px;
        requestLayout();
    }
    public void setCellHeight(int px) {
        mCellHeight = px;
        requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth,
                MeasureSpec.AT_MOST);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
                MeasureSpec.AT_MOST);
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            child.measure(cellWidthSpec, cellHeightSpec);
        }
        setMeasuredDimension(resolveSize(mCellWidth*count, widthMeasureSpec),
                resolveSize(mCellHeight*count, heightMeasureSpec));
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cellWidth = mCellWidth;
        int cellHeight = mCellHeight;
        int columns = (r - l) / cellWidth;
        if (columns < 0) {
            columns = 1;
        }
        int x = 0;
        int y = 0;
        int i = 0;
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();
            int left = x + ((cellWidth-w)/2);
            int top = y + ((cellHeight-h)/2);
            child.layout(left, top, left+w, top+h);
            if (i >= (columns-1)) {
                i = 0;
                x = 0;
                y += cellHeight;
            } else {
                i++;
                x += cellWidth;
            }
        }
    }
}
