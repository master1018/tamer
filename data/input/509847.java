class ExpandableListConnector extends BaseAdapter implements Filterable {
    private ExpandableListAdapter mExpandableListAdapter;
    private ArrayList<GroupMetadata> mExpGroupMetadataList;
    private int mTotalExpChildrenCount;
    private int mMaxExpGroupCount = Integer.MAX_VALUE;
    private final DataSetObserver mDataSetObserver = new MyDataSetObserver();
    public ExpandableListConnector(ExpandableListAdapter expandableListAdapter) {
        mExpGroupMetadataList = new ArrayList<GroupMetadata>();
        setExpandableListAdapter(expandableListAdapter);
    }
    public void setExpandableListAdapter(ExpandableListAdapter expandableListAdapter) {
        if (mExpandableListAdapter != null) {
            mExpandableListAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mExpandableListAdapter = expandableListAdapter;
        expandableListAdapter.registerDataSetObserver(mDataSetObserver);
    }
    PositionMetadata getUnflattenedPos(final int flPos) {
        final ArrayList<GroupMetadata> egml = mExpGroupMetadataList;
        final int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        GroupMetadata midExpGm; 
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(flPos, ExpandableListPosition.GROUP, flPos,
                    -1, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex =
                    (rightExpGroupIndex - leftExpGroupIndex) / 2
                            + leftExpGroupIndex;
            midExpGm = egml.get(midExpGroupIndex);
            if (flPos > midExpGm.lastChildFlPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (flPos < midExpGm.flPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (flPos == midExpGm.flPos) {
                return PositionMetadata.obtain(flPos, ExpandableListPosition.GROUP,
                        midExpGm.gPos, -1, midExpGm, midExpGroupIndex);
            } else if (flPos <= midExpGm.lastChildFlPos
                    ) {
                final int childPos = flPos - (midExpGm.flPos + 1);
                return PositionMetadata.obtain(flPos, ExpandableListPosition.CHILD,
                        midExpGm.gPos, childPos, midExpGm, midExpGroupIndex);
            } 
        }
        int insertPosition = 0;
        int groupPos = 0;
        if (leftExpGroupIndex > midExpGroupIndex) {
            final GroupMetadata leftExpGm = egml.get(leftExpGroupIndex-1);            
            insertPosition = leftExpGroupIndex;
            groupPos =
                (flPos - leftExpGm.lastChildFlPos) + leftExpGm.gPos;            
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            final GroupMetadata rightExpGm = egml.get(++rightExpGroupIndex);            
            insertPosition = rightExpGroupIndex;
            groupPos = rightExpGm.gPos - (rightExpGm.flPos - flPos);
        } else {
            throw new RuntimeException("Unknown state");
        }
        return PositionMetadata.obtain(flPos, ExpandableListPosition.GROUP, groupPos, -1,
                null, insertPosition);
    }
    PositionMetadata getFlattenedPos(final ExpandableListPosition pos) {
        final ArrayList<GroupMetadata> egml = mExpGroupMetadataList;
        final int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        GroupMetadata midExpGm; 
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(pos.groupPos, pos.type,
                    pos.groupPos, pos.childPos, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = (rightExpGroupIndex - leftExpGroupIndex)/2 + leftExpGroupIndex;
            midExpGm = egml.get(midExpGroupIndex);
            if (pos.groupPos > midExpGm.gPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (pos.groupPos < midExpGm.gPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (pos.groupPos == midExpGm.gPos) {
                if (pos.type == ExpandableListPosition.GROUP) {
                    return PositionMetadata.obtain(midExpGm.flPos, pos.type,
                            pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                } else if (pos.type == ExpandableListPosition.CHILD) {
                    return PositionMetadata.obtain(midExpGm.flPos + pos.childPos
                            + 1, pos.type, pos.groupPos, pos.childPos,
                            midExpGm, midExpGroupIndex);
                } else {
                    return null;
                }
            } 
        }
        if (pos.type != ExpandableListPosition.GROUP) {
            return null;
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            final GroupMetadata leftExpGm = egml.get(leftExpGroupIndex-1);            
            final int flPos =
                    leftExpGm.lastChildFlPos
                            + (pos.groupPos - leftExpGm.gPos);
            return PositionMetadata.obtain(flPos, pos.type, pos.groupPos,
                    pos.childPos, null, leftExpGroupIndex);
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            final GroupMetadata rightExpGm = egml.get(++rightExpGroupIndex);            
            final int flPos =
                    rightExpGm.flPos
                            - (rightExpGm.gPos - pos.groupPos);
            return PositionMetadata.obtain(flPos, pos.type, pos.groupPos,
                    pos.childPos, null, rightExpGroupIndex);
        } else {
            return null;
        }
    }
    @Override
    public boolean areAllItemsEnabled() {
        return mExpandableListAdapter.areAllItemsEnabled();
    }
    @Override
    public boolean isEnabled(int flatListPos) {
        final ExpandableListPosition pos = getUnflattenedPos(flatListPos).position;
        boolean retValue;
        if (pos.type == ExpandableListPosition.CHILD) {
            retValue = mExpandableListAdapter.isChildSelectable(pos.groupPos, pos.childPos);
        } else {
            retValue = true;
        }
        pos.recycle();
        return retValue;
    }
    public int getCount() {
        return mExpandableListAdapter.getGroupCount() + mTotalExpChildrenCount;
    }
    public Object getItem(int flatListPos) {
        final PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        Object retValue;
        if (posMetadata.position.type == ExpandableListPosition.GROUP) {
            retValue = mExpandableListAdapter
                    .getGroup(posMetadata.position.groupPos);
        } else if (posMetadata.position.type == ExpandableListPosition.CHILD) {
            retValue = mExpandableListAdapter.getChild(posMetadata.position.groupPos,
                    posMetadata.position.childPos);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }
    public long getItemId(int flatListPos) {
        final PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        final long groupId = mExpandableListAdapter.getGroupId(posMetadata.position.groupPos);
        long retValue;
        if (posMetadata.position.type == ExpandableListPosition.GROUP) {
            retValue = mExpandableListAdapter.getCombinedGroupId(groupId);
        } else if (posMetadata.position.type == ExpandableListPosition.CHILD) {
            final long childId = mExpandableListAdapter.getChildId(posMetadata.position.groupPos,
                    posMetadata.position.childPos);
            retValue = mExpandableListAdapter.getCombinedChildId(groupId, childId);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }
    public View getView(int flatListPos, View convertView, ViewGroup parent) {
        final PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        View retValue;
        if (posMetadata.position.type == ExpandableListPosition.GROUP) {
            retValue = mExpandableListAdapter.getGroupView(posMetadata.position.groupPos,
                    posMetadata.isExpanded(), convertView, parent);
        } else if (posMetadata.position.type == ExpandableListPosition.CHILD) {
            final boolean isLastChild = posMetadata.groupMetadata.lastChildFlPos == flatListPos;
            retValue = mExpandableListAdapter.getChildView(posMetadata.position.groupPos,
                    posMetadata.position.childPos, isLastChild, convertView, parent);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }
    @Override
    public int getItemViewType(int flatListPos) {
        final ExpandableListPosition pos = getUnflattenedPos(flatListPos).position;
        int retValue;
        if (mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter =
                    (HeterogeneousExpandableList) mExpandableListAdapter;
            if (pos.type == ExpandableListPosition.GROUP) {
                retValue = adapter.getGroupType(pos.groupPos);
            } else {
                final int childType = adapter.getChildType(pos.groupPos, pos.childPos);
                retValue = adapter.getGroupTypeCount() + childType;
            }
        } else {
            if (pos.type == ExpandableListPosition.GROUP) {
                retValue = 0;
            } else {
                retValue = 1;
            }
        }
        pos.recycle();
        return retValue;
    }
    @Override
    public int getViewTypeCount() {
        if (mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter =
                    (HeterogeneousExpandableList) mExpandableListAdapter;
            return adapter.getGroupTypeCount() + adapter.getChildTypeCount();
        } else {
            return 2;
        }
    }
    @Override
    public boolean hasStableIds() {
        return mExpandableListAdapter.hasStableIds();
    }
    @SuppressWarnings("unchecked")
    private void refreshExpGroupMetadataList(boolean forceChildrenCountRefresh,
            boolean syncGroupPositions) {
        final ArrayList<GroupMetadata> egml = mExpGroupMetadataList;
        int egmlSize = egml.size();
        int curFlPos = 0;
        mTotalExpChildrenCount = 0;
        if (syncGroupPositions) {
            boolean positionsChanged = false;
            for (int i = egmlSize - 1; i >= 0; i--) {
                GroupMetadata curGm = egml.get(i);
                int newGPos = findGroupPosition(curGm.gId, curGm.gPos);
                if (newGPos != curGm.gPos) {
                    if (newGPos == AdapterView.INVALID_POSITION) {
                        egml.remove(i);
                        egmlSize--;
                    }
                    curGm.gPos = newGPos;
                    if (!positionsChanged) positionsChanged = true;
                }
            }
            if (positionsChanged) {
                Collections.sort(egml);
            }
        }
        int gChildrenCount;
        int lastGPos = 0;
        for (int i = 0; i < egmlSize; i++) {
            GroupMetadata curGm = egml.get(i);
            if ((curGm.lastChildFlPos == GroupMetadata.REFRESH) || forceChildrenCountRefresh) {
                gChildrenCount = mExpandableListAdapter.getChildrenCount(curGm.gPos);
            } else {
                gChildrenCount = curGm.lastChildFlPos - curGm.flPos;
            }
            mTotalExpChildrenCount += gChildrenCount;
            curFlPos += (curGm.gPos - lastGPos);
            lastGPos = curGm.gPos;
            curGm.flPos = curFlPos;
            curFlPos += gChildrenCount; 
            curGm.lastChildFlPos = curFlPos; 
        }
    }
    boolean collapseGroup(int groupPos) {
        PositionMetadata pm = getFlattenedPos(ExpandableListPosition.obtain(
                ExpandableListPosition.GROUP, groupPos, -1, -1)); 
        if (pm == null) return false;
        boolean retValue = collapseGroup(pm);
        pm.recycle();
        return retValue;
    }
    boolean collapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null) return false;
        mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
        return true;
    }
    boolean expandGroup(int groupPos) {
        PositionMetadata pm = getFlattenedPos(ExpandableListPosition.obtain(
                ExpandableListPosition.GROUP, groupPos, -1, -1));
        boolean retValue = expandGroup(pm);
        pm.recycle();
        return retValue;
    }
    boolean expandGroup(PositionMetadata posMetadata) {
        if (posMetadata.position.groupPos < 0) {
            throw new RuntimeException("Need group");
        }
        if (mMaxExpGroupCount == 0) return false;
        if (posMetadata.groupMetadata != null) return false;
        if (mExpGroupMetadataList.size() >= mMaxExpGroupCount) {
            GroupMetadata collapsedGm = mExpGroupMetadataList.get(0);
            int collapsedIndex = mExpGroupMetadataList.indexOf(collapsedGm);
            collapseGroup(collapsedGm.gPos);
            if (posMetadata.groupInsertIndex > collapsedIndex) {
                posMetadata.groupInsertIndex--;
            }
        }
        GroupMetadata expandedGm = GroupMetadata.obtain(
                GroupMetadata.REFRESH,
                GroupMetadata.REFRESH,
                posMetadata.position.groupPos,
                mExpandableListAdapter.getGroupId(posMetadata.position.groupPos));
        mExpGroupMetadataList.add(posMetadata.groupInsertIndex, expandedGm);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        mExpandableListAdapter.onGroupExpanded(expandedGm.gPos);
        return true;
    }
    public boolean isGroupExpanded(int groupPosition) {
        GroupMetadata groupMetadata;
        for (int i = mExpGroupMetadataList.size() - 1; i >= 0; i--) {
            groupMetadata = mExpGroupMetadataList.get(i);
            if (groupMetadata.gPos == groupPosition) {
                return true;
            }
        }
        return false;
    }
    public void setMaxExpGroupCount(int maxExpGroupCount) {
        mMaxExpGroupCount = maxExpGroupCount;
    }    
    ExpandableListAdapter getAdapter() {
        return mExpandableListAdapter;
    }
    public Filter getFilter() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter instanceof Filterable) {
            return ((Filterable) adapter).getFilter();
        } else {
            return null;
        }
    }
    ArrayList<GroupMetadata> getExpandedGroupMetadataList() {
        return mExpGroupMetadataList;
    }
    void setExpandedGroupMetadataList(ArrayList<GroupMetadata> expandedGroupMetadataList) {
        if ((expandedGroupMetadataList == null) || (mExpandableListAdapter == null)) {
            return;
        }
        int numGroups = mExpandableListAdapter.getGroupCount();
        for (int i = expandedGroupMetadataList.size() - 1; i >= 0; i--) {
            if (expandedGroupMetadataList.get(i).gPos >= numGroups) {
                return;
            }
        }
        mExpGroupMetadataList = expandedGroupMetadataList;
        refreshExpGroupMetadataList(true, false);
    }
    @Override
    public boolean isEmpty() {
        ExpandableListAdapter adapter = getAdapter();
        return adapter != null ? adapter.isEmpty() : true;
    }
    int findGroupPosition(long groupIdToMatch, int seedGroupPosition) {
        int count = mExpandableListAdapter.getGroupCount();
        if (count == 0) {
            return AdapterView.INVALID_POSITION;
        }
        if (groupIdToMatch == AdapterView.INVALID_ROW_ID) {
            return AdapterView.INVALID_POSITION;
        }
        seedGroupPosition = Math.max(0, seedGroupPosition);
        seedGroupPosition = Math.min(count - 1, seedGroupPosition);
        long endTime = SystemClock.uptimeMillis() + AdapterView.SYNC_MAX_DURATION_MILLIS;
        long rowId;
        int first = seedGroupPosition;
        int last = seedGroupPosition;
        boolean next = false;
        boolean hitFirst;
        boolean hitLast;
        ExpandableListAdapter adapter = getAdapter();
        if (adapter == null) {
            return AdapterView.INVALID_POSITION;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            rowId = adapter.getGroupId(seedGroupPosition);
            if (rowId == groupIdToMatch) {
                return seedGroupPosition;
            }
            hitLast = last == count - 1;
            hitFirst = first == 0;
            if (hitLast && hitFirst) {
                break;
            }
            if (hitFirst || (next && !hitLast)) {
                last++;
                seedGroupPosition = last;
                next = false;
            } else if (hitLast || (!next && !hitFirst)) {
                first--;
                seedGroupPosition = first;
                next = true;
            }
        }
        return AdapterView.INVALID_POSITION;
    }
    protected class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            refreshExpGroupMetadataList(true, true);
            notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            refreshExpGroupMetadataList(true, true);
            notifyDataSetInvalidated();
        }
    }
    static class GroupMetadata implements Parcelable, Comparable<GroupMetadata> {
        final static int REFRESH = -1;
        int flPos;
        int lastChildFlPos;
        int gPos;
        long gId;
        private GroupMetadata() {
        }
        static GroupMetadata obtain(int flPos, int lastChildFlPos, int gPos, long gId) {
            GroupMetadata gm = new GroupMetadata();
            gm.flPos = flPos;
            gm.lastChildFlPos = lastChildFlPos;
            gm.gPos = gPos;
            gm.gId = gId;
            return gm;
        }
        public int compareTo(GroupMetadata another) {
            if (another == null) {
                throw new IllegalArgumentException();
            }
            return gPos - another.gPos;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(flPos);
            dest.writeInt(lastChildFlPos);
            dest.writeInt(gPos);
            dest.writeLong(gId);
        }
        public static final Parcelable.Creator<GroupMetadata> CREATOR =
                new Parcelable.Creator<GroupMetadata>() {
            public GroupMetadata createFromParcel(Parcel in) {
                GroupMetadata gm = GroupMetadata.obtain(
                        in.readInt(),
                        in.readInt(),
                        in.readInt(),
                        in.readLong());
                return gm;
            }
            public GroupMetadata[] newArray(int size) {
                return new GroupMetadata[size];
            }
        };
    }
    static public class PositionMetadata {
        private static final int MAX_POOL_SIZE = 5;
        private static ArrayList<PositionMetadata> sPool =
                new ArrayList<PositionMetadata>(MAX_POOL_SIZE);
        public ExpandableListPosition position;
        public GroupMetadata groupMetadata;
        public int groupInsertIndex;
        private void resetState() {
            position = null;
            groupMetadata = null;
            groupInsertIndex = 0;
        }
        private PositionMetadata() {
        }
        static PositionMetadata obtain(int flatListPos, int type, int groupPos,
                int childPos, GroupMetadata groupMetadata, int groupInsertIndex) {
            PositionMetadata pm = getRecycledOrCreate();
            pm.position = ExpandableListPosition.obtain(type, groupPos, childPos, flatListPos);
            pm.groupMetadata = groupMetadata;
            pm.groupInsertIndex = groupInsertIndex;
            return pm;
        }
        private static PositionMetadata getRecycledOrCreate() {
            PositionMetadata pm;
            synchronized (sPool) {
                if (sPool.size() > 0) {
                    pm = sPool.remove(0);
                } else {
                    return new PositionMetadata();
                }
            }
            pm.resetState();
            return pm;
        }
        public void recycle() {
            synchronized (sPool) {
                if (sPool.size() < MAX_POOL_SIZE) {
                    sPool.add(this);
                }
            }
        }
        public boolean isExpanded() {
            return groupMetadata != null;
        }
    }
}
