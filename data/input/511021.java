public class DateSortedExpandableListAdapter implements ExpandableListAdapter {
    private int mItemMap[];
    private int mNumberOfBins;
    private Vector<DataSetObserver> mObservers;
    private Cursor mCursor;
    private DateSorter mDateSorter;
    private int mDateIndex;
    private int mIdIndex;
    private Context mContext;
    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            refreshData();
        }
    }
    public DateSortedExpandableListAdapter(Context context, Cursor cursor,
            int dateIndex) {
        mContext = context;
        mDateSorter = new DateSorter(context);
        mObservers = new Vector<DataSetObserver>();
        mCursor = cursor;
        mIdIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        cursor.registerContentObserver(new ChangeObserver());
        mDateIndex = dateIndex;
        buildMap();
    }
    private void buildMap() {
        int array[] = new int[DateSorter.DAY_COUNT];
        for (int j = 0; j < DateSorter.DAY_COUNT; j++) {
            array[j] = 0;
        }
        mNumberOfBins = 0;
        int dateIndex = -1;
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            while (!mCursor.isAfterLast()) {
                long date = getLong(mDateIndex);
                int index = mDateSorter.getIndex(date);
                if (index > dateIndex) {
                    mNumberOfBins++;
                    if (index == DateSorter.DAY_COUNT - 1) {
                        array[index] = mCursor.getCount()
                                - mCursor.getPosition();
                        break;
                    }
                    dateIndex = index;
                }
                array[dateIndex]++;
                mCursor.moveToNext();
            }
        }
        mItemMap = array;
    }
     byte[] getBlob(int cursorIndex) {
        return mCursor.getBlob(cursorIndex);
    }
     Context getContext() {
        return mContext;
    }
     int getInt(int cursorIndex) {
        return mCursor.getInt(cursorIndex);
    }
     long getLong(int cursorIndex) {
        return mCursor.getLong(cursorIndex);
    }
     String getString(int cursorIndex) {
        return mCursor.getString(cursorIndex);
    }
     int groupFromChildId(long childId) {
        int group = -1;
        for (mCursor.moveToFirst(); !mCursor.isAfterLast();
                mCursor.moveToNext()) {
            if (getLong(mIdIndex) == childId) {
                int bin = mDateSorter.getIndex(getLong(mDateIndex));
                if (mDateSorter.DAY_COUNT == mNumberOfBins) return bin;
                group = 0;
                for (int i = 0; i < bin; i++) {
                    if (mItemMap[i] != 0) group++;
                }
                break;
            }
        }
        return group;
    }
    private int groupPositionToBin(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= DateSorter.DAY_COUNT) {
            throw new AssertionError("group position out of range");
        }
        if (DateSorter.DAY_COUNT == mNumberOfBins || 0 == mNumberOfBins) {
            return groupPosition;
        }
        int arrayPosition = -1;
        while (groupPosition > -1) {
            arrayPosition++;
            if (mItemMap[arrayPosition] != 0) {
                groupPosition--;
            }
        }
        return arrayPosition;
    }
    boolean moveCursorToPackedChildPosition(long packedPosition) {
        if (ExpandableListView.getPackedPositionType(packedPosition) !=
                ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            return false;
        }
        int groupPosition = ExpandableListView.getPackedPositionGroup(
                packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(
                packedPosition);
        return moveCursorToChildPosition(groupPosition, childPosition);
    }
     boolean moveCursorToChildPosition(int groupPosition,
            int childPosition) {
        if (mCursor.isClosed()) return false;
        groupPosition = groupPositionToBin(groupPosition);
        int index = childPosition;
        for (int i = 0; i < groupPosition; i++) {
            index += mItemMap[i];
        }
        return mCursor.moveToPosition(index);
    }
     void refreshData() {
        if (mCursor.isClosed()) {
            return;
        }
        mCursor.requery();
        buildMap();
        for (DataSetObserver o : mObservers) {
            o.onChanged();
        }
    }
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        TextView item;
        if (null == convertView || !(convertView instanceof TextView)) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            item = (TextView) factory.inflate(R.layout.history_header, null);
        } else {
            item = (TextView) convertView;
        }
        String label = mDateSorter.getLabel(groupPositionToBin(groupPosition));
        item.setText(label);
        return item;
    }
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }
    public boolean areAllItemsEnabled() {
        return true;
    }
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public int getGroupCount() {
        return mNumberOfBins;
    }
    public int getChildrenCount(int groupPosition) {
        return mItemMap[groupPositionToBin(groupPosition)];
    }
    public Object getGroup(int groupPosition) {
        return null;
    }
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    public long getChildId(int groupPosition, int childPosition) {
        if (moveCursorToChildPosition(groupPosition, childPosition)) {
            return getLong(mIdIndex);
        }
        return 0;
    }
    public boolean hasStableIds() {
        return true;
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mObservers.add(observer);
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservers.remove(observer);
    }
    public void onGroupExpanded(int groupPosition) {
    }
    public void onGroupCollapsed(int groupPosition) {
    }
    public long getCombinedChildId(long groupId, long childId) {
        return childId;
    }
    public long getCombinedGroupId(long groupId) {
        return groupId;
    }
    public boolean isEmpty() {
        return mCursor.isClosed() || mCursor.getCount() == 0;
    }
}
