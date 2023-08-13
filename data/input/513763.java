public class ExpandableListView extends ListView {
    public static final int PACKED_POSITION_TYPE_GROUP = 0;
    public static final int PACKED_POSITION_TYPE_CHILD = 1;
    public static final int PACKED_POSITION_TYPE_NULL = 2;
    public static final long PACKED_POSITION_VALUE_NULL = 0x00000000FFFFFFFFL;
    private static final long PACKED_POSITION_MASK_CHILD = 0x00000000FFFFFFFFL;
    private static final long PACKED_POSITION_MASK_GROUP = 0x7FFFFFFF00000000L;
    private static final long PACKED_POSITION_MASK_TYPE  = 0x8000000000000000L;
    private static final long PACKED_POSITION_SHIFT_GROUP = 32;
    private static final long PACKED_POSITION_SHIFT_TYPE  = 63;
    private static final long PACKED_POSITION_INT_MASK_CHILD = 0xFFFFFFFF;
    private static final long PACKED_POSITION_INT_MASK_GROUP = 0x7FFFFFFF;
    private ExpandableListConnector mConnector;
    private ExpandableListAdapter mAdapter;
    private int mIndicatorLeft;
    private int mIndicatorRight;
    private int mChildIndicatorLeft;
    private int mChildIndicatorRight;
    public static final int CHILD_INDICATOR_INHERIT = -1;
    private Drawable mGroupIndicator;
    private Drawable mChildIndicator;
    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {R.attr.state_expanded};
    private static final int[] GROUP_EMPTY_STATE_SET =
            {R.attr.state_empty};
    private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET =
            {R.attr.state_expanded, R.attr.state_empty};
    private static final int[][] GROUP_STATE_SETS = {
         EMPTY_STATE_SET, 
         GROUP_EXPANDED_STATE_SET, 
         GROUP_EMPTY_STATE_SET, 
         GROUP_EXPANDED_EMPTY_STATE_SET 
    };
    private static final int[] CHILD_LAST_STATE_SET =
            {R.attr.state_last};
    private Drawable mChildDivider;
    private boolean mClipChildDivider;
    private final Rect mIndicatorRect = new Rect();
    public ExpandableListView(Context context) {
        this(context, null);
    }
    public ExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.expandableListViewStyle);
    }
    public ExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ExpandableListView, defStyle,
                    0);
        mGroupIndicator = a
                .getDrawable(com.android.internal.R.styleable.ExpandableListView_groupIndicator);
        mChildIndicator = a
                .getDrawable(com.android.internal.R.styleable.ExpandableListView_childIndicator);
        mIndicatorLeft = a
                .getDimensionPixelSize(com.android.internal.R.styleable.ExpandableListView_indicatorLeft, 0);
        mIndicatorRight = a
                .getDimensionPixelSize(com.android.internal.R.styleable.ExpandableListView_indicatorRight, 0);
        mChildIndicatorLeft = a.getDimensionPixelSize(
                com.android.internal.R.styleable.ExpandableListView_childIndicatorLeft, CHILD_INDICATOR_INHERIT);
        mChildIndicatorRight = a.getDimensionPixelSize(
                com.android.internal.R.styleable.ExpandableListView_childIndicatorRight, CHILD_INDICATOR_INHERIT);
        mChildDivider = a.getDrawable(com.android.internal.R.styleable.ExpandableListView_childDivider);
        a.recycle();
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if ((mChildIndicator == null) && (mGroupIndicator == null)) {
            return;
        }
        int saveCount = 0;
        final boolean clipToPadding = (mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK;
        if (clipToPadding) {
            saveCount = canvas.save();
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;
            canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                    scrollX + mRight - mLeft - mPaddingRight,
                    scrollY + mBottom - mTop - mPaddingBottom);
        }
        final int headerViewsCount = getHeaderViewsCount();
        final int lastChildFlPos = mItemCount - getFooterViewsCount() - headerViewsCount - 1;
        final int myB = mBottom; 
        PositionMetadata pos;
        View item;
        Drawable indicator; 
        int t, b;
        int lastItemType = ~(ExpandableListPosition.CHILD | ExpandableListPosition.GROUP);
        final Rect indicatorRect = mIndicatorRect;
        final int childCount = getChildCount(); 
        for (int i = 0, childFlPos = mFirstPosition - headerViewsCount; i < childCount;
             i++, childFlPos++) {
            if (childFlPos < 0) {
                continue;
            } else if (childFlPos > lastChildFlPos) {
                break;
            }
            item = getChildAt(i);
            t = item.getTop();
            b = item.getBottom();
            if ((b < 0) || (t > myB)) continue;
            pos = mConnector.getUnflattenedPos(childFlPos);
            if (pos.position.type != lastItemType) {
                if (pos.position.type == ExpandableListPosition.CHILD) {
                    indicatorRect.left = (mChildIndicatorLeft == CHILD_INDICATOR_INHERIT) ?
                            mIndicatorLeft : mChildIndicatorLeft;
                    indicatorRect.right = (mChildIndicatorRight == CHILD_INDICATOR_INHERIT) ?
                            mIndicatorRight : mChildIndicatorRight;
                } else {
                    indicatorRect.left = mIndicatorLeft;
                    indicatorRect.right = mIndicatorRight;
                }
                lastItemType = pos.position.type; 
            }
            if (indicatorRect.left != indicatorRect.right) {
                if (mStackFromBottom) {
                    indicatorRect.top = t;
                    indicatorRect.bottom = b;
                } else {
                    indicatorRect.top = t;
                    indicatorRect.bottom = b;
                }
                indicator = getIndicator(pos);
                if (indicator != null) {
                    indicator.setBounds(indicatorRect);
                    indicator.draw(canvas);
                }
            }
            pos.recycle();
        }
        if (clipToPadding) {
            canvas.restoreToCount(saveCount);
        }
    }
    private Drawable getIndicator(PositionMetadata pos) {
        Drawable indicator;
        if (pos.position.type == ExpandableListPosition.GROUP) {
            indicator = mGroupIndicator;
            if (indicator != null && indicator.isStateful()) {
                boolean isEmpty = (pos.groupMetadata == null) ||
                        (pos.groupMetadata.lastChildFlPos == pos.groupMetadata.flPos);
                final int stateSetIndex =
                    (pos.isExpanded() ? 1 : 0) | 
                    (isEmpty ? 2 : 0); 
                indicator.setState(GROUP_STATE_SETS[stateSetIndex]);
            }
        } else {
            indicator = mChildIndicator;
            if (indicator != null && indicator.isStateful()) {
                final int stateSet[] = pos.position.flatListPos == pos.groupMetadata.lastChildFlPos
                        ? CHILD_LAST_STATE_SET
                        : EMPTY_STATE_SET;
                indicator.setState(stateSet);
            }
        }
        return indicator;
    }
    public void setChildDivider(Drawable childDivider) {
        mChildDivider = childDivider;
        mClipChildDivider = childDivider != null && childDivider instanceof ColorDrawable;
    }
    @Override
    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        int flatListPosition = childIndex + mFirstPosition;
        if (flatListPosition >= 0) {
            final int adjustedPosition = getFlatPositionForConnector(flatListPosition);
            PositionMetadata pos = mConnector.getUnflattenedPos(adjustedPosition);
            if ((pos.position.type == ExpandableListPosition.CHILD) || (pos.isExpanded() &&
                    pos.groupMetadata.lastChildFlPos != pos.groupMetadata.flPos)) {
                final Drawable divider = mChildDivider;
                final boolean clip = mClipChildDivider;
                if (!clip) {
                    divider.setBounds(bounds);
                } else {
                    canvas.save();
                    canvas.clipRect(bounds);
                }
                divider.draw(canvas);
                if (clip) {
                    canvas.restore();
                }
                pos.recycle();
                return;
            }
            pos.recycle();
        }
        super.drawDivider(canvas, bounds, flatListPosition);
    }
    @Override
    public void setAdapter(ListAdapter adapter) {
        throw new RuntimeException(
                "For ExpandableListView, use setAdapter(ExpandableListAdapter) instead of " +
                "setAdapter(ListAdapter)");
    }
    @Override
    public ListAdapter getAdapter() {
        return super.getAdapter();
    }
    @Override
    public void setOnItemClickListener(OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }
    public void setAdapter(ExpandableListAdapter adapter) {
        mAdapter = adapter;
        if (adapter != null) {
            mConnector = new ExpandableListConnector(adapter);
        } else {
            mConnector = null;
        }
        super.setAdapter(mConnector);
    }
    public ExpandableListAdapter getExpandableListAdapter() {
        return mAdapter;
    }
    private boolean isHeaderOrFooterPosition(int position) {
        final int footerViewsStart = mItemCount - getFooterViewsCount();
        return (position < getHeaderViewsCount() || position >= footerViewsStart);
    }
    private int getFlatPositionForConnector(int flatListPosition) {
        return flatListPosition - getHeaderViewsCount();
    }
    private int getAbsoluteFlatPosition(int flatListPosition) {
        return flatListPosition + getHeaderViewsCount();
    }
    @Override
    public boolean performItemClick(View v, int position, long id) {
        if (isHeaderOrFooterPosition(position)) {
            return super.performItemClick(v, position, id);
        }
        final int adjustedPosition = getFlatPositionForConnector(position);
        return handleItemClick(v, adjustedPosition, id);
    }
    boolean handleItemClick(View v, int position, long id) {
        final PositionMetadata posMetadata = mConnector.getUnflattenedPos(position);
        id = getChildOrGroupId(posMetadata.position);
        boolean returnValue;
        if (posMetadata.position.type == ExpandableListPosition.GROUP) {
            if (mOnGroupClickListener != null) {
                if (mOnGroupClickListener.onGroupClick(this, v,
                        posMetadata.position.groupPos, id)) {
                    posMetadata.recycle();
                    return true;
                }
            }
            if (posMetadata.isExpanded()) {
                mConnector.collapseGroup(posMetadata);
                playSoundEffect(SoundEffectConstants.CLICK);
                if (mOnGroupCollapseListener != null) {
                    mOnGroupCollapseListener.onGroupCollapse(posMetadata.position.groupPos);
                }
            } else {
                mConnector.expandGroup(posMetadata);
                playSoundEffect(SoundEffectConstants.CLICK);
                if (mOnGroupExpandListener != null) {
                    mOnGroupExpandListener.onGroupExpand(posMetadata.position.groupPos);
                }
                final int groupPos = posMetadata.position.groupPos;
                final int groupFlatPos = posMetadata.position.flatListPos;
                final int shiftedGroupPosition = groupFlatPos + getHeaderViewsCount(); 
                smoothScrollToPosition(shiftedGroupPosition + mAdapter.getChildrenCount(groupPos),
                        shiftedGroupPosition);
            }
            returnValue = true;
        } else {
            if (mOnChildClickListener != null) {
                playSoundEffect(SoundEffectConstants.CLICK);
                return mOnChildClickListener.onChildClick(this, v, posMetadata.position.groupPos,
                        posMetadata.position.childPos, id);
            }
            returnValue = false;
        }
        posMetadata.recycle();
        return returnValue;
    }
    public boolean expandGroup(int groupPos) {
        boolean retValue = mConnector.expandGroup(groupPos);
        if (mOnGroupExpandListener != null) {
            mOnGroupExpandListener.onGroupExpand(groupPos);
        }
        return retValue;
    }
    public boolean collapseGroup(int groupPos) {
        boolean retValue = mConnector.collapseGroup(groupPos);
        if (mOnGroupCollapseListener != null) {
            mOnGroupCollapseListener.onGroupCollapse(groupPos);
        }
        return retValue;
    }
    public interface OnGroupCollapseListener {
        void onGroupCollapse(int groupPosition);
    }
    private OnGroupCollapseListener mOnGroupCollapseListener;
    public void setOnGroupCollapseListener(
            OnGroupCollapseListener onGroupCollapseListener) {
        mOnGroupCollapseListener = onGroupCollapseListener;
    }
    public interface OnGroupExpandListener {
        void onGroupExpand(int groupPosition);
    }
    private OnGroupExpandListener mOnGroupExpandListener;
    public void setOnGroupExpandListener(
            OnGroupExpandListener onGroupExpandListener) {
        mOnGroupExpandListener = onGroupExpandListener;
    }
    public interface OnGroupClickListener {
        boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                long id);
    }
    private OnGroupClickListener mOnGroupClickListener;
    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        mOnGroupClickListener = onGroupClickListener;
    }
    public interface OnChildClickListener {
        boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id);
    }
    private OnChildClickListener mOnChildClickListener;
    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        mOnChildClickListener = onChildClickListener;
    }
    public long getExpandableListPosition(int flatListPosition) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return PACKED_POSITION_VALUE_NULL;
        }
        final int adjustedPosition = getFlatPositionForConnector(flatListPosition);
        PositionMetadata pm = mConnector.getUnflattenedPos(adjustedPosition);
        long packedPos = pm.position.getPackedPosition();
        pm.recycle();
        return packedPos;
    }
    public int getFlatListPosition(long packedPosition) {
        PositionMetadata pm = mConnector.getFlattenedPos(ExpandableListPosition
                .obtainPosition(packedPosition));
        final int flatListPosition = pm.position.flatListPos;
        pm.recycle();
        return getAbsoluteFlatPosition(flatListPosition);
    }
    public long getSelectedPosition() {
        final int selectedPos = getSelectedItemPosition();
        return getExpandableListPosition(selectedPos);
    }
    public long getSelectedId() {
        long packedPos = getSelectedPosition();
        if (packedPos == PACKED_POSITION_VALUE_NULL) return -1;
        int groupPos = getPackedPositionGroup(packedPos);
        if (getPackedPositionType(packedPos) == PACKED_POSITION_TYPE_GROUP) {
            return mAdapter.getGroupId(groupPos);
        } else {
            return mAdapter.getChildId(groupPos, getPackedPositionChild(packedPos));
        }
    }
    public void setSelectedGroup(int groupPosition) {
        ExpandableListPosition elGroupPos = ExpandableListPosition
                .obtainGroupPosition(groupPosition);
        PositionMetadata pm = mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        final int absoluteFlatPosition = getAbsoluteFlatPosition(pm.position.flatListPos);
        super.setSelection(absoluteFlatPosition);
        pm.recycle();
    }
    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        ExpandableListPosition elChildPos = ExpandableListPosition.obtainChildPosition(
                groupPosition, childPosition); 
        PositionMetadata flatChildPos = mConnector.getFlattenedPos(elChildPos);
        if (flatChildPos == null) {
            if (!shouldExpandGroup) return false; 
            expandGroup(groupPosition);
            flatChildPos = mConnector.getFlattenedPos(elChildPos);
            if (flatChildPos == null) {
                throw new IllegalStateException("Could not find child");
            }
        }
        int absoluteFlatPosition = getAbsoluteFlatPosition(flatChildPos.position.flatListPos);
        super.setSelection(absoluteFlatPosition);
        elChildPos.recycle();
        flatChildPos.recycle();
        return true;
    }
    public boolean isGroupExpanded(int groupPosition) {
        return mConnector.isGroupExpanded(groupPosition);
    }
    public static int getPackedPositionType(long packedPosition) {
        if (packedPosition == PACKED_POSITION_VALUE_NULL) {
            return PACKED_POSITION_TYPE_NULL;
        }
        return (packedPosition & PACKED_POSITION_MASK_TYPE) == PACKED_POSITION_MASK_TYPE
                ? PACKED_POSITION_TYPE_CHILD
                : PACKED_POSITION_TYPE_GROUP;
    }
    public static int getPackedPositionGroup(long packedPosition) {
        if (packedPosition == PACKED_POSITION_VALUE_NULL) return -1;
        return (int) ((packedPosition & PACKED_POSITION_MASK_GROUP) >> PACKED_POSITION_SHIFT_GROUP);
    }
    public static int getPackedPositionChild(long packedPosition) {
        if (packedPosition == PACKED_POSITION_VALUE_NULL) return -1;
        if ((packedPosition & PACKED_POSITION_MASK_TYPE) != PACKED_POSITION_MASK_TYPE) return -1;
        return (int) (packedPosition & PACKED_POSITION_MASK_CHILD);
    }
    public static long getPackedPositionForChild(int groupPosition, int childPosition) {
        return (((long)PACKED_POSITION_TYPE_CHILD) << PACKED_POSITION_SHIFT_TYPE)
                | ((((long)groupPosition) & PACKED_POSITION_INT_MASK_GROUP)
                        << PACKED_POSITION_SHIFT_GROUP)
                | (childPosition & PACKED_POSITION_INT_MASK_CHILD);  
    }
    public static long getPackedPositionForGroup(int groupPosition) {
        return ((((long)groupPosition) & PACKED_POSITION_INT_MASK_GROUP)
                        << PACKED_POSITION_SHIFT_GROUP); 
    }
    @Override
    ContextMenuInfo createContextMenuInfo(View view, int flatListPosition, long id) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return new AdapterContextMenuInfo(view, flatListPosition, id);
        }
        final int adjustedPosition = getFlatPositionForConnector(flatListPosition);
        PositionMetadata pm = mConnector.getUnflattenedPos(adjustedPosition);
        ExpandableListPosition pos = pm.position;
        pm.recycle();
        id = getChildOrGroupId(pos);
        long packedPosition = pos.getPackedPosition();
        pos.recycle();
        return new ExpandableListContextMenuInfo(view, packedPosition, id);
    }
    private long getChildOrGroupId(ExpandableListPosition position) {
        if (position.type == ExpandableListPosition.CHILD) {
            return mAdapter.getChildId(position.groupPos, position.childPos);
        } else {
            return mAdapter.getGroupId(position.groupPos);
        }
    }
    public void setChildIndicator(Drawable childIndicator) {
        mChildIndicator = childIndicator;
    }
    public void setChildIndicatorBounds(int left, int right) {
        mChildIndicatorLeft = left;
        mChildIndicatorRight = right;
    }
    public void setGroupIndicator(Drawable groupIndicator) {
        mGroupIndicator = groupIndicator;
    }
    public void setIndicatorBounds(int left, int right) {
        mIndicatorLeft = left;
        mIndicatorRight = right;
    }
    public static class ExpandableListContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public ExpandableListContextMenuInfo(View targetView, long packedPosition, long id) {
            this.targetView = targetView;
            this.packedPosition = packedPosition;
            this.id = id;
        }
        public View targetView;
        public long packedPosition;
        public long id;
    }
    static class SavedState extends BaseSavedState {
        ArrayList<ExpandableListConnector.GroupMetadata> expandedGroupMetadataList;
        SavedState(
                Parcelable superState,
                ArrayList<ExpandableListConnector.GroupMetadata> expandedGroupMetadataList) {
            super(superState);
            this.expandedGroupMetadataList = expandedGroupMetadataList;
        }
        private SavedState(Parcel in) {
            super(in);
            expandedGroupMetadataList = new ArrayList<ExpandableListConnector.GroupMetadata>();
            in.readList(expandedGroupMetadataList, ExpandableListConnector.class.getClassLoader());
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(expandedGroupMetadataList);
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
        return new SavedState(superState,
                mConnector != null ? mConnector.getExpandedGroupMetadataList() : null);
    }
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (mConnector != null && ss.expandedGroupMetadataList != null) {
            mConnector.setExpandedGroupMetadataList(ss.expandedGroupMetadataList);
        }
    }
}
