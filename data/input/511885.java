public abstract class GroupingListAdapter extends BaseAdapter {
    private static final int GROUP_METADATA_ARRAY_INITIAL_SIZE = 16;
    private static final int GROUP_METADATA_ARRAY_INCREMENT = 128;
    private static final long GROUP_OFFSET_MASK    = 0x00000000FFFFFFFFL;
    private static final long GROUP_SIZE_MASK     = 0x7FFFFFFF00000000L;
    private static final long EXPANDED_GROUP_MASK = 0x8000000000000000L;
    public static final int ITEM_TYPE_STANDALONE = 0;
    public static final int ITEM_TYPE_GROUP_HEADER = 1;
    public static final int ITEM_TYPE_IN_GROUP = 2;
    protected static class PositionMetadata {
        int itemType;
        boolean isExpanded;
        int cursorPosition;
        int childCount;
        private int groupPosition;
        private int listPosition = -1;
    }
    private Context mContext;
    private Cursor mCursor;
    private int mCount;
    private int mRowIdColumnIndex;
    private int mGroupCount;
    private long[] mGroupMetadata;
    private SparseIntArray mPositionCache = new SparseIntArray();
    private int mLastCachedListPosition;
    private int mLastCachedCursorPosition;
    private int mLastCachedGroup;
    private PositionMetadata mPositionMetadata = new PositionMetadata();
    protected ContentObserver mChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    };
    protected DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    };
    public GroupingListAdapter(Context context) {
        mContext = context;
        resetCache();
    }
    protected abstract void addGroups(Cursor cursor);
    protected abstract View newStandAloneView(Context context, ViewGroup parent);
    protected abstract void bindStandAloneView(View view, Context context, Cursor cursor);
    protected abstract View newGroupView(Context context, ViewGroup parent);
    protected abstract void bindGroupView(View view, Context context, Cursor cursor, int groupSize,
            boolean expanded);
    protected abstract View newChildView(Context context, ViewGroup parent);
    protected abstract void bindChildView(View view, Context context, Cursor cursor);
    private void resetCache() {
        mCount = -1;
        mLastCachedListPosition = -1;
        mLastCachedCursorPosition = -1;
        mLastCachedGroup = -1;
        mPositionMetadata.listPosition = -1;
        mPositionCache.clear();
    }
    protected void onContentChanged() {
    }
    public void changeCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mChangeObserver);
            mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.close();
        }
        mCursor = cursor;
        resetCache();
        findGroups();
        if (cursor != null) {
            cursor.registerContentObserver(mChangeObserver);
            cursor.registerDataSetObserver(mDataSetObserver);
            mRowIdColumnIndex = cursor.getColumnIndexOrThrow("_id");
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }
    public Cursor getCursor() {
        return mCursor;
    }
    private void findGroups() {
        mGroupCount = 0;
        mGroupMetadata = new long[GROUP_METADATA_ARRAY_INITIAL_SIZE];
        if (mCursor == null) {
            return;
        }
        addGroups(mCursor);
    }
    protected void addGroup(int cursorPosition, int size, boolean expanded) {
        if (mGroupCount >= mGroupMetadata.length) {
            int newSize = ArrayUtils.idealLongArraySize(
                    mGroupMetadata.length + GROUP_METADATA_ARRAY_INCREMENT);
            long[] array = new long[newSize];
            System.arraycopy(mGroupMetadata, 0, array, 0, mGroupCount);
            mGroupMetadata = array;
        }
        long metadata = ((long)size << 32) | cursorPosition;
        if (expanded) {
            metadata |= EXPANDED_GROUP_MASK;
        }
        mGroupMetadata[mGroupCount++] = metadata;
    }
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }
        if (mCount != -1) {
            return mCount;
        }
        int cursorPosition = 0;
        int count = 0;
        for (int i = 0; i < mGroupCount; i++) {
            long metadata = mGroupMetadata[i];
            int offset = (int)(metadata & GROUP_OFFSET_MASK);
            boolean expanded = (metadata & EXPANDED_GROUP_MASK) != 0;
            int size = (int)((metadata & GROUP_SIZE_MASK) >> 32);
            count += (offset - cursorPosition);
            if (expanded) {
                count += size + 1;
            } else {
                count++;
            }
            cursorPosition = offset + size;
        }
        mCount = count + mCursor.getCount() - cursorPosition;
        return mCount;
    }
    public void obtainPositionMetadata(PositionMetadata metadata, int position) {
        if (metadata.listPosition == position) {
            return;
        }
        int listPosition = 0;
        int cursorPosition = 0;
        int firstGroupToCheck = 0;
        if (mLastCachedListPosition != -1) {
            if (position <= mLastCachedListPosition) {
                int index = mPositionCache.indexOfKey(position);
                if (index < 0) {
                    index = ~index - 1;
                    if (index >= mPositionCache.size()) {
                        index--;
                    }
                }
                if (index >= 0) {
                    listPosition = mPositionCache.keyAt(index);
                    firstGroupToCheck = mPositionCache.valueAt(index);
                    long descriptor = mGroupMetadata[firstGroupToCheck];
                    cursorPosition = (int)(descriptor & GROUP_OFFSET_MASK);
                }
            } else {
                firstGroupToCheck = mLastCachedGroup;
                listPosition = mLastCachedListPosition;
                cursorPosition = mLastCachedCursorPosition;
            }
        }
        for (int i = firstGroupToCheck; i < mGroupCount; i++) {
            long group = mGroupMetadata[i];
            int offset = (int)(group & GROUP_OFFSET_MASK);
            listPosition += (offset - cursorPosition);
            cursorPosition = offset;
            if (i > mLastCachedGroup) {
                mPositionCache.append(listPosition, i);
                mLastCachedListPosition = listPosition;
                mLastCachedCursorPosition = cursorPosition;
                mLastCachedGroup = i;
            }
            if (position < listPosition) {
                metadata.itemType = ITEM_TYPE_STANDALONE;
                metadata.cursorPosition = cursorPosition - (listPosition - position);
                return;
            }
            boolean expanded = (group & EXPANDED_GROUP_MASK) != 0;
            int size = (int) ((group & GROUP_SIZE_MASK) >> 32);
            if (position == listPosition) {
                metadata.itemType = ITEM_TYPE_GROUP_HEADER;
                metadata.groupPosition = i;
                metadata.isExpanded = expanded;
                metadata.childCount = size;
                metadata.cursorPosition = offset;
                return;
            }
            if (expanded) {
                if (position < listPosition + size + 1) {
                    metadata.itemType = ITEM_TYPE_IN_GROUP;
                    metadata.cursorPosition = cursorPosition + (position - listPosition) - 1;
                    return;
                }
                listPosition += size + 1;
            } else {
                listPosition++;
            }
            cursorPosition += size;
        }
        metadata.itemType = ITEM_TYPE_STANDALONE;
        metadata.cursorPosition = cursorPosition + (position - listPosition);
    }
    public boolean isGroupHeader(int position) {
        obtainPositionMetadata(mPositionMetadata, position);
        return mPositionMetadata.itemType == ITEM_TYPE_GROUP_HEADER;
    }
    public int getGroupSize(int position) {
        obtainPositionMetadata(mPositionMetadata, position);
        return mPositionMetadata.childCount;
    }
    public void toggleGroup(int position) {
        obtainPositionMetadata(mPositionMetadata, position);
        if (mPositionMetadata.itemType != ITEM_TYPE_GROUP_HEADER) {
            throw new IllegalArgumentException("Not a group at position " + position);
        }
        if (mPositionMetadata.isExpanded) {
            mGroupMetadata[mPositionMetadata.groupPosition] &= ~EXPANDED_GROUP_MASK;
        } else {
            mGroupMetadata[mPositionMetadata.groupPosition] |= EXPANDED_GROUP_MASK;
        }
        resetCache();
        notifyDataSetChanged();
    }
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    @Override
    public int getItemViewType(int position) {
        obtainPositionMetadata(mPositionMetadata, position);
        return mPositionMetadata.itemType;
    }
    public Object getItem(int position) {
        if (mCursor == null) {
            return null;
        }
        obtainPositionMetadata(mPositionMetadata, position);
        if (mCursor.moveToPosition(mPositionMetadata.cursorPosition)) {
            return mCursor;
        } else {
            return null;
        }
    }
    public long getItemId(int position) {
        Object item = getItem(position);
        if (item != null) {
            return mCursor.getLong(mRowIdColumnIndex);
        } else {
            return -1;
        }
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        obtainPositionMetadata(mPositionMetadata, position);
        View view = convertView;
        if (view == null) {
            switch (mPositionMetadata.itemType) {
                case ITEM_TYPE_STANDALONE:
                    view = newStandAloneView(mContext, parent);
                    break;
                case ITEM_TYPE_GROUP_HEADER:
                    view = newGroupView(mContext, parent);
                    break;
                case ITEM_TYPE_IN_GROUP:
                    view = newChildView(mContext, parent);
                    break;
            }
        }
        mCursor.moveToPosition(mPositionMetadata.cursorPosition);
        switch (mPositionMetadata.itemType) {
            case ITEM_TYPE_STANDALONE:
                bindStandAloneView(view, mContext, mCursor);
                break;
            case ITEM_TYPE_GROUP_HEADER:
                bindGroupView(view, mContext, mCursor, mPositionMetadata.childCount,
                        mPositionMetadata.isExpanded);
                break;
            case ITEM_TYPE_IN_GROUP:
                bindChildView(view, mContext, mCursor);
                break;
        }
        return view;
    }
}
