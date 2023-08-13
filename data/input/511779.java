public class TableRow extends LinearLayout {
    private int mNumColumns = 0;
    private int[] mColumnWidths;
    private int[] mConstrainedColumnWidths;
    private SparseIntArray mColumnToChildIndex;
    private ChildrenTracker mChildrenTracker;
    public TableRow(Context context) {
        super(context);
        initTableRow();
    }
    public TableRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTableRow();
    }
    private void initTableRow() {
        OnHierarchyChangeListener oldListener = mOnHierarchyChangeListener;
        mChildrenTracker = new ChildrenTracker();
        if (oldListener != null) {
            mChildrenTracker.setOnHierarchyChangeListener(oldListener);
        }
        super.setOnHierarchyChangeListener(mChildrenTracker);
    }
    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mChildrenTracker.setOnHierarchyChangeListener(listener);
    }
    void setColumnCollapsed(int columnIndex, boolean collapsed) {
        View child = getVirtualChildAt(columnIndex);
        if (child != null) {
            child.setVisibility(collapsed ? GONE : VISIBLE);
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutHorizontal();
    }
    @Override
    public View getVirtualChildAt(int i) {
        if (mColumnToChildIndex == null) {
            mapIndexAndColumns();
        }
        final int deflectedIndex = mColumnToChildIndex.get(i, -1);
        if (deflectedIndex != -1) {
            return getChildAt(deflectedIndex);
        }
        return null;
    }
    @Override
    public int getVirtualChildCount() {
        if (mColumnToChildIndex == null) {
            mapIndexAndColumns();
        }
        return mNumColumns;
    }
    private void mapIndexAndColumns() {
        if (mColumnToChildIndex == null) {
            int virtualCount = 0;
            final int count = getChildCount();
            mColumnToChildIndex = new SparseIntArray();
            final SparseIntArray columnToChild = mColumnToChildIndex;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                if (layoutParams.column >= virtualCount) {
                    virtualCount = layoutParams.column;
                }
                for (int j = 0; j < layoutParams.span; j++) {
                    columnToChild.put(virtualCount++, i);
                }
            }
            mNumColumns = virtualCount;
        }
    }
    @Override
    int measureNullChild(int childIndex) {
        return mConstrainedColumnWidths[childIndex];
    }
    @Override
    void measureChildBeforeLayout(View child, int childIndex,
            int widthMeasureSpec, int totalWidth,
            int heightMeasureSpec, int totalHeight) {
        if (mConstrainedColumnWidths != null) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int measureMode = MeasureSpec.EXACTLY;
            int columnWidth = 0;
            final int span = lp.span;
            final int[] constrainedColumnWidths = mConstrainedColumnWidths;
            for (int i = 0; i < span; i++) {
                columnWidth += constrainedColumnWidths[childIndex + i];
            }
            final int gravity = lp.gravity;
            final boolean isHorizontalGravity = Gravity.isHorizontal(gravity);
            if (isHorizontalGravity) {
                measureMode = MeasureSpec.AT_MOST;
            }
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, columnWidth - lp.leftMargin - lp.rightMargin), measureMode
            );
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    mPaddingTop + mPaddingBottom + lp.topMargin +
                    lp .bottomMargin + totalHeight, lp.height);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (isHorizontalGravity) {
                final int childWidth = child.getMeasuredWidth();
                lp.mOffset[LayoutParams.LOCATION_NEXT] = columnWidth - childWidth;
                switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.LEFT:
                        break;
                    case Gravity.RIGHT:
                        lp.mOffset[LayoutParams.LOCATION] = lp.mOffset[LayoutParams.LOCATION_NEXT];
                        break;
                    case Gravity.CENTER_HORIZONTAL:
                        lp.mOffset[LayoutParams.LOCATION] = lp.mOffset[LayoutParams.LOCATION_NEXT] / 2;
                        break;
                }
            } else {
                lp.mOffset[LayoutParams.LOCATION] = lp.mOffset[LayoutParams.LOCATION_NEXT] = 0;
            }
        } else {
            super.measureChildBeforeLayout(child, childIndex, widthMeasureSpec,
                    totalWidth, heightMeasureSpec, totalHeight);
        }
    }
    @Override
    int getChildrenSkipCount(View child, int index) {
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        return layoutParams.span - 1;
    }
    @Override
    int getLocationOffset(View child) {
        return ((TableRow.LayoutParams) child.getLayoutParams()).mOffset[LayoutParams.LOCATION];
    }
    @Override
    int getNextLocationOffset(View child) {
        return ((TableRow.LayoutParams) child.getLayoutParams()).mOffset[LayoutParams.LOCATION_NEXT];
    }
    int[] getColumnsWidths(int widthMeasureSpec) {
        final int numColumns = getVirtualChildCount();
        if (mColumnWidths == null || numColumns != mColumnWidths.length) {
            mColumnWidths = new int[numColumns];
        }
        final int[] columnWidths = mColumnWidths;
        for (int i = 0; i < numColumns; i++) {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                if (layoutParams.span == 1) {
                    int spec;
                    switch (layoutParams.width) {
                        case LayoutParams.WRAP_CONTENT:
                            spec = getChildMeasureSpec(widthMeasureSpec, 0, LayoutParams.WRAP_CONTENT);
                            break;
                        case LayoutParams.MATCH_PARENT:
                            spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                            break;
                        default:
                            spec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
                    }
                    child.measure(spec, spec);
                    final int width = child.getMeasuredWidth() + layoutParams.leftMargin +
                            layoutParams.rightMargin;
                    columnWidths[i] = width;
                } else {
                    columnWidths[i] = 0;
                }
            } else {
                columnWidths[i] = 0;
            }
        }
        return columnWidths;
    }
    void setColumnsWidthConstraints(int[] columnWidths) {
        if (columnWidths == null || columnWidths.length < getVirtualChildCount()) {
            throw new IllegalArgumentException(
                    "columnWidths should be >= getVirtualChildCount()");
        }
        mConstrainedColumnWidths = columnWidths;
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new TableRow.LayoutParams(getContext(), attrs);
    }
    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof TableRow.LayoutParams;
    }
    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
    public static class LayoutParams extends LinearLayout.LayoutParams {
        @ViewDebug.ExportedProperty
        public int column;
        @ViewDebug.ExportedProperty
        public int span;
        private static final int LOCATION = 0;
        private static final int LOCATION_NEXT = 1;
        private int[] mOffset = new int[2];
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a =
                    c.obtainStyledAttributes(attrs,
                            com.android.internal.R.styleable.TableRow_Cell);
            column = a.getInt(com.android.internal.R.styleable.TableRow_Cell_layout_column, -1);
            span = a.getInt(com.android.internal.R.styleable.TableRow_Cell_layout_span, 1);
            if (span <= 1) {
                span = 1;
            }
            a.recycle();
        }
        public LayoutParams(int w, int h) {
            super(w, h);
            column = -1;
            span = 1;
        }
        public LayoutParams(int w, int h, float initWeight) {
            super(w, h, initWeight);
            column = -1;
            span = 1;
        }
        public LayoutParams() {
            super(MATCH_PARENT, WRAP_CONTENT);
            column = -1;
            span = 1;
        }
        public LayoutParams(int column) {
            this();
            this.column = column;
        }
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            if (a.hasValue(widthAttr)) {
                width = a.getLayoutDimension(widthAttr, "layout_width");
            } else {
                width = MATCH_PARENT;
            }
            if (a.hasValue(heightAttr)) {
                height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                height = WRAP_CONTENT;
            }
        }
    }
    private class ChildrenTracker implements OnHierarchyChangeListener {
        private OnHierarchyChangeListener listener;
        private void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
            this.listener = listener;
        }
        public void onChildViewAdded(View parent, View child) {
            mColumnToChildIndex = null;
            if (this.listener != null) {
                this.listener.onChildViewAdded(parent, child);
            }
        }
        public void onChildViewRemoved(View parent, View child) {
            mColumnToChildIndex = null;
            if (this.listener != null) {
                this.listener.onChildViewRemoved(parent, child);
            }
        }
    }
}
