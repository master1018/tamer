public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {
    SpinnerAdapter mAdapter;
    int mHeightMeasureSpec;
    int mWidthMeasureSpec;
    boolean mBlockLayoutRequests;
    int mSelectionLeftPadding = 0;
    int mSelectionTopPadding = 0;
    int mSelectionRightPadding = 0;
    int mSelectionBottomPadding = 0;
    final Rect mSpinnerPadding = new Rect();
    final RecycleBin mRecycler = new RecycleBin();
    private DataSetObserver mDataSetObserver;
    private Rect mTouchFrame;
    public AbsSpinner(Context context) {
        super(context);
        initAbsSpinner();
    }
    public AbsSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AbsSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAbsSpinner();
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.AbsSpinner, defStyle, 0);
        CharSequence[] entries = a.getTextArray(R.styleable.AbsSpinner_entries);
        if (entries != null) {
            ArrayAdapter<CharSequence> adapter =
                    new ArrayAdapter<CharSequence>(context,
                            R.layout.simple_spinner_item, entries);
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            setAdapter(adapter);
        }
        a.recycle();
    }
    private void initAbsSpinner() {
        setFocusable(true);
        setWillNotDraw(false);
    }
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (null != mAdapter) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            resetList();
        }
        mAdapter = adapter;
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        if (mAdapter != null) {
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
            checkFocus();
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
            int position = mItemCount > 0 ? 0 : INVALID_POSITION;
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            if (mItemCount == 0) {
                checkSelectionChanged();
            }
        } else {
            checkFocus();            
            resetList();
            checkSelectionChanged();
        }
        requestLayout();
    }
    void resetList() {
        mDataChanged = false;
        mNeedSync = false;
        removeAllViewsInLayout();
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        setSelectedPositionInt(INVALID_POSITION);
        setNextSelectedPositionInt(INVALID_POSITION);
        invalidate();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize;
        int heightSize;
        mSpinnerPadding.left = mPaddingLeft > mSelectionLeftPadding ? mPaddingLeft
                : mSelectionLeftPadding;
        mSpinnerPadding.top = mPaddingTop > mSelectionTopPadding ? mPaddingTop
                : mSelectionTopPadding;
        mSpinnerPadding.right = mPaddingRight > mSelectionRightPadding ? mPaddingRight
                : mSelectionRightPadding;
        mSpinnerPadding.bottom = mPaddingBottom > mSelectionBottomPadding ? mPaddingBottom
                : mSelectionBottomPadding;
        if (mDataChanged) {
            handleDataChanged();
        }
        int preferredHeight = 0;
        int preferredWidth = 0;
        boolean needsMeasuring = true;
        int selectedPosition = getSelectedItemPosition();
        if (selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount()) {
            View view = mRecycler.get(selectedPosition);
            if (view == null) {
                view = mAdapter.getView(selectedPosition, null, this);
            }
            if (view != null) {
                mRecycler.put(selectedPosition, view);
            }
            if (view != null) {
                if (view.getLayoutParams() == null) {
                    mBlockLayoutRequests = true;
                    view.setLayoutParams(generateDefaultLayoutParams());
                    mBlockLayoutRequests = false;
                }
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
                preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;
                needsMeasuring = false;
            }
        }
        if (needsMeasuring) {
            preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
            }
        }
        preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
        preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());
        heightSize = resolveSize(preferredHeight, heightMeasureSpec);
        widthSize = resolveSize(preferredWidth, widthMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
        mHeightMeasureSpec = heightMeasureSpec;
        mWidthMeasureSpec = widthMeasureSpec;
    }
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }
    int getChildWidth(View child) {
        return child.getMeasuredWidth();
    }
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    void recycleAllViews() {
        final int childCount = getChildCount();
        final AbsSpinner.RecycleBin recycleBin = mRecycler;
        final int position = mFirstPosition;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            int index = position + i;
            recycleBin.put(index, v);
        }  
    }
    public void setSelection(int position, boolean animate) {
        boolean shouldAnimate = animate && mFirstPosition <= position &&
                position <= mFirstPosition + getChildCount() - 1;
        setSelectionInt(position, shouldAnimate);
    }
    @Override
    public void setSelection(int position) {
        setNextSelectedPositionInt(position);
        requestLayout();
        invalidate();
    }
    void setSelectionInt(int position, boolean animate) {
        if (position != mOldSelectedPosition) {
            mBlockLayoutRequests = true;
            int delta  = position - mSelectedPosition;
            setNextSelectedPositionInt(position);
            layout(delta, animate);
            mBlockLayoutRequests = false;
        }
    }
    abstract void layout(int delta, boolean animate);
    @Override
    public View getSelectedView() {
        if (mItemCount > 0 && mSelectedPosition >= 0) {
            return getChildAt(mSelectedPosition - mFirstPosition);
        } else {
            return null;
        }
    }
    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }
    @Override
    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }
    @Override
    public int getCount() {
        return mItemCount;
    }
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        } 
        return INVALID_POSITION;
    }
    static class SavedState extends BaseSavedState {
        long selectedId;
        int position;
        SavedState(Parcelable superState) {
            super(superState);
        }
        private SavedState(Parcel in) {
            super(in);
            selectedId = in.readLong();
            position = in.readInt();
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedId);
            out.writeInt(position);
        }
        @Override
        public String toString() {
            return "AbsSpinner.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " selectedId=" + selectedId
                    + " position=" + position + "}";
        }
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedId = getSelectedItemId();
        if (ss.selectedId >= 0) {
            ss.position = getSelectedItemPosition();
        } else {
            ss.position = INVALID_POSITION;
        }
        return ss;
    }
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.selectedId >= 0) {
            mDataChanged = true;
            mNeedSync = true;
            mSyncRowId = ss.selectedId;
            mSyncPosition = ss.position;
            mSyncMode = SYNC_SELECTED_POSITION;
            requestLayout();
        }
    }
    class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<View>();
        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }
        View get(int position) {
            View result = mScrapHeap.get(position);
            if (result != null) {
                mScrapHeap.delete(position);
            } else {
            }
            return result;
        }
        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
    }
}
