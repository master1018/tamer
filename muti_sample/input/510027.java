public class TableLayout extends LinearLayout {
    private int[] mMaxWidths;
    private SparseBooleanArray mStretchableColumns;
    private SparseBooleanArray mShrinkableColumns;
    private SparseBooleanArray mCollapsedColumns;
    private boolean mShrinkAllColumns;
    private boolean mStretchAllColumns;
    private TableLayout.PassThroughHierarchyChangeListener mPassThroughListener;
    private boolean mInitialized;
    public TableLayout(Context context) {
        super(context);
        initTableLayout();
    }
    public TableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TableLayout);
        String stretchedColumns = a.getString(R.styleable.TableLayout_stretchColumns);
        if (stretchedColumns != null) {
            if (stretchedColumns.charAt(0) == '*') {
                mStretchAllColumns = true;
            } else {
                mStretchableColumns = parseColumns(stretchedColumns);
            }
        }
        String shrinkedColumns = a.getString(R.styleable.TableLayout_shrinkColumns);
        if (shrinkedColumns != null) {
            if (shrinkedColumns.charAt(0) == '*') {
                mShrinkAllColumns = true;
            } else {
                mShrinkableColumns = parseColumns(shrinkedColumns);
            }
        }
        String collapsedColumns = a.getString(R.styleable.TableLayout_collapseColumns);
        if (collapsedColumns != null) {
            mCollapsedColumns = parseColumns(collapsedColumns);
        }
        a.recycle();
        initTableLayout();
    }
    private static SparseBooleanArray parseColumns(String sequence) {
        SparseBooleanArray columns = new SparseBooleanArray();
        Pattern pattern = Pattern.compile("\\s*,\\s*");
        String[] columnDefs = pattern.split(sequence);
        for (String columnIdentifier : columnDefs) {
            try {
                int columnIndex = Integer.parseInt(columnIdentifier);
                if (columnIndex >= 0) {
                    columns.put(columnIndex, true);
                }
            } catch (NumberFormatException e) {
            }
        }
        return columns;
    }
    private void initTableLayout() {
        if (mCollapsedColumns == null) {
            mCollapsedColumns = new SparseBooleanArray();
        }
        if (mStretchableColumns == null) {
            mStretchableColumns = new SparseBooleanArray();
        }
        if (mShrinkableColumns == null) {
            mShrinkableColumns = new SparseBooleanArray();
        }
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
        mInitialized = true;
    }
    @Override
    public void setOnHierarchyChangeListener(
            OnHierarchyChangeListener listener) {
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }
    private void requestRowsLayout() {
        if (mInitialized) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).requestLayout();
            }
        }
    }
    @Override
    public void requestLayout() {
        if (mInitialized) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).forceLayout();
            }
        }
        super.requestLayout();
    }
    public boolean isShrinkAllColumns() {
        return mShrinkAllColumns;
    }
    public void setShrinkAllColumns(boolean shrinkAllColumns) {
        mShrinkAllColumns = shrinkAllColumns;
    }
    public boolean isStretchAllColumns() {
        return mStretchAllColumns;
    }
    public void setStretchAllColumns(boolean stretchAllColumns) {
        mStretchAllColumns = stretchAllColumns;
    }
    public void setColumnCollapsed(int columnIndex, boolean isCollapsed) {
        mCollapsedColumns.put(columnIndex, isCollapsed);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            if (view instanceof TableRow) {
                ((TableRow) view).setColumnCollapsed(columnIndex, isCollapsed);
            }
        }
        requestRowsLayout();
    }
    public boolean isColumnCollapsed(int columnIndex) {
        return mCollapsedColumns.get(columnIndex);
    }
    public void setColumnStretchable(int columnIndex, boolean isStretchable) {
        mStretchableColumns.put(columnIndex, isStretchable);
        requestRowsLayout();
    }
    public boolean isColumnStretchable(int columnIndex) {
        return mStretchAllColumns || mStretchableColumns.get(columnIndex);
    }
    public void setColumnShrinkable(int columnIndex, boolean isShrinkable) {
        mShrinkableColumns.put(columnIndex, isShrinkable);
        requestRowsLayout();
    }
    public boolean isColumnShrinkable(int columnIndex) {
        return mShrinkAllColumns || mShrinkableColumns.get(columnIndex);
    }
    private void trackCollapsedColumns(View child) {
        if (child instanceof TableRow) {
            final TableRow row = (TableRow) child;
            final SparseBooleanArray collapsedColumns = mCollapsedColumns;
            final int count = collapsedColumns.size();
            for (int i = 0; i < count; i++) {
                int columnIndex = collapsedColumns.keyAt(i);
                boolean isCollapsed = collapsedColumns.valueAt(i);
                if (isCollapsed) {
                    row.setColumnCollapsed(columnIndex, isCollapsed);
                }
            }
        }
    }
    @Override
    public void addView(View child) {
        super.addView(child);
        requestRowsLayout();
    }
    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        requestRowsLayout();
    }
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        requestRowsLayout();
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        requestRowsLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutVertical();
    }
    @Override
    void measureChildBeforeLayout(View child, int childIndex,
            int widthMeasureSpec, int totalWidth,
            int heightMeasureSpec, int totalHeight) {
        if (child instanceof TableRow) {
            ((TableRow) child).setColumnsWidthConstraints(mMaxWidths);
        }
        super.measureChildBeforeLayout(child, childIndex,
                widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }
    @Override
    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        findLargestCells(widthMeasureSpec);
        shrinkAndStretchColumns(widthMeasureSpec);
        super.measureVertical(widthMeasureSpec, heightMeasureSpec);
    }
    private void findLargestCells(int widthMeasureSpec) {
        boolean firstRow = true;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (child instanceof TableRow) {
                final TableRow row = (TableRow) child;
                final ViewGroup.LayoutParams layoutParams = row.getLayoutParams();
                layoutParams.height = LayoutParams.WRAP_CONTENT;
                final int[] widths = row.getColumnsWidths(widthMeasureSpec);
                final int newLength = widths.length;
                if (firstRow) {
                    if (mMaxWidths == null || mMaxWidths.length != newLength) {
                        mMaxWidths = new int[newLength];
                    }
                    System.arraycopy(widths, 0, mMaxWidths, 0, newLength);
                    firstRow = false;
                } else {
                    int length = mMaxWidths.length;
                    final int difference = newLength - length;
                    if (difference > 0) {
                        final int[] oldMaxWidths = mMaxWidths;
                        mMaxWidths = new int[newLength];
                        System.arraycopy(oldMaxWidths, 0, mMaxWidths, 0,
                                oldMaxWidths.length);
                        System.arraycopy(widths, oldMaxWidths.length,
                                mMaxWidths, oldMaxWidths.length, difference);
                    }
                    final int[] maxWidths = mMaxWidths;
                    length = Math.min(length, newLength);
                    for (int j = 0; j < length; j++) {
                        maxWidths[j] = Math.max(maxWidths[j], widths[j]);
                    }
                }
            }
        }
    }
    private void shrinkAndStretchColumns(int widthMeasureSpec) {
        if (mMaxWidths == null) {
            return;
        }
        int totalWidth = 0;
        for (int width : mMaxWidths) {
            totalWidth += width;
        }
        int size = MeasureSpec.getSize(widthMeasureSpec) - mPaddingLeft - mPaddingRight;
        if ((totalWidth > size) && (mShrinkAllColumns || mShrinkableColumns.size() > 0)) {
            mutateColumnsWidth(mShrinkableColumns, mShrinkAllColumns, size, totalWidth);
        } else if ((totalWidth < size) && (mStretchAllColumns || mStretchableColumns.size() > 0)) {
            mutateColumnsWidth(mStretchableColumns, mStretchAllColumns, size, totalWidth);
        }
    }
    private void mutateColumnsWidth(SparseBooleanArray columns,
            boolean allColumns, int size, int totalWidth) {
        int skipped = 0;
        final int[] maxWidths = mMaxWidths;
        final int length = maxWidths.length;
        final int count = allColumns ? length : columns.size();
        final int totalExtraSpace = size - totalWidth;
        int extraSpace = totalExtraSpace / count;
        final int nbChildren = getChildCount();
        for (int i = 0; i < nbChildren; i++) {
            View child = getChildAt(i);
            if (child instanceof TableRow) {
                child.forceLayout();
            }
        }
        if (!allColumns) {
            for (int i = 0; i < count; i++) {
                int column = columns.keyAt(i);
                if (columns.valueAt(i)) {
                    if (column < length) {
                        maxWidths[column] += extraSpace;
                    } else {
                        skipped++;
                    }
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                maxWidths[i] += extraSpace;
            }
            return;
        }
        if (skipped > 0 && skipped < count) {
            extraSpace = skipped * extraSpace / (count - skipped);
            for (int i = 0; i < count; i++) {
                int column = columns.keyAt(i);
                if (columns.valueAt(i) && column < length) {
                    if (extraSpace > maxWidths[column]) {
                        maxWidths[column] = 0;
                    } else {
                        maxWidths[column] += extraSpace;
                    }
                }
            }
        }
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new TableLayout.LayoutParams(getContext(), attrs);
    }
    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof TableLayout.LayoutParams;
    }
    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
    @SuppressWarnings({"UnusedDeclaration"})
    public static class LayoutParams extends LinearLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int w, int h) {
            super(MATCH_PARENT, h);
        }
        public LayoutParams(int w, int h, float initWeight) {
            super(MATCH_PARENT, h, initWeight);
        }
        public LayoutParams() {
            super(MATCH_PARENT, WRAP_CONTENT);
        }
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
        @Override
        protected void setBaseAttributes(TypedArray a,
                int widthAttr, int heightAttr) {
            this.width = MATCH_PARENT;
            if (a.hasValue(heightAttr)) {
                this.height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                this.height = WRAP_CONTENT;
            }
        }
    }
    private class PassThroughHierarchyChangeListener implements
            OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;
        public void onChildViewAdded(View parent, View child) {
            trackCollapsedColumns(child);
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }
        public void onChildViewRemoved(View parent, View child) {
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
