public abstract class ExpandableListScenario extends ListScenario {
    protected ExpandableListAdapter mAdapter; 
    protected List<MyGroup> mGroups;
    @Override
    protected ListView createListView() {
        return new ExpandableListView(this);
    }
    @Override
    protected Params createParams() {
        return new ExpandableParams();
    }
    @Override
    protected void setAdapter(ListView listView) {
        ((ExpandableListView) listView).setAdapter(mAdapter = createAdapter());
    }
    protected ExpandableListAdapter createAdapter() {
        return new MyAdapter();
    }
    @Override
    protected void readAndValidateParams(Params params) {
        ExpandableParams expandableParams = (ExpandableParams) params;
        int[] numChildren = expandableParams.mNumChildren;
        mGroups = new ArrayList<MyGroup>(numChildren.length);
        for (int i = 0; i < numChildren.length; i++) {
            mGroups.add(new MyGroup(numChildren[i]));
        }
        expandableParams.superSetNumItems();
        super.readAndValidateParams(params);
    }
    public ExpandableListView getExpandableListView() {
        return (ExpandableListView) super.getListView();
    }
    public static class ExpandableParams extends Params {
        private int[] mNumChildren;
        public ExpandableParams setNumChildren(int[] numChildren) {
            mNumChildren = numChildren;
            return this;
        }
        private ExpandableParams superSetNumItems() {
            int numItems = 0;
            if (mNumChildren != null) {
                for (int i = mNumChildren.length - 1; i >= 0; i--) {
                    numItems += mNumChildren[i];
                }
            }
            super.setNumItems(numItems);
            return this;
        }
        @Override
        public Params setNumItems(int numItems) {
            throw new IllegalStateException("Use setNumGroups and setNumChildren instead.");
        }
        @Override
        public ExpandableParams setFadingEdgeScreenSizeFactor(double fadingEdgeScreenSizeFactor) {
            return (ExpandableParams) super.setFadingEdgeScreenSizeFactor(fadingEdgeScreenSizeFactor);
        }
        @Override
        public ExpandableParams setItemScreenSizeFactor(double itemScreenSizeFactor) {
            return (ExpandableParams) super.setItemScreenSizeFactor(itemScreenSizeFactor);
        }
        @Override
        public ExpandableParams setItemsFocusable(boolean itemsFocusable) {
            return (ExpandableParams) super.setItemsFocusable(itemsFocusable);
        }
        @Override
        public ExpandableParams setMustFillScreen(boolean fillScreen) {
            return (ExpandableParams) super.setMustFillScreen(fillScreen);
        }
        @Override
        public ExpandableParams setPositionScreenSizeFactorOverride(int position, double itemScreenSizeFactor) {
            return (ExpandableParams) super.setPositionScreenSizeFactorOverride(position, itemScreenSizeFactor);
        }
        @Override
        public ExpandableParams setPositionUnselectable(int position) {
            return (ExpandableParams) super.setPositionUnselectable(position);
        }
        @Override
        public ExpandableParams setStackFromBottom(boolean stackFromBottom) {
            return (ExpandableParams) super.setStackFromBottom(stackFromBottom);
        }
        @Override
        public ExpandableParams setStartingSelectionPosition(int startingSelectionPosition) {
            return (ExpandableParams) super.setStartingSelectionPosition(startingSelectionPosition);
        }
        @Override
        public ExpandableParams setConnectAdapter(boolean connectAdapter) {
            return (ExpandableParams) super.setConnectAdapter(connectAdapter);
        }
    }
    public final String getValueAtPosition(long packedPosition) {
        final int type = ExpandableListView.getPackedPositionType(packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            return mGroups.get(ExpandableListView.getPackedPositionGroup(packedPosition))
                    .children.get(ExpandableListView.getPackedPositionChild(packedPosition))
                    .name;
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            return mGroups.get(ExpandableListView.getPackedPositionGroup(packedPosition))
                    .name;
        } else {
            throw new IllegalStateException("packedPosition is not a valid position.");
        }
    }
    private boolean isOutOfBounds(long packedPosition) {
        final int type = ExpandableListView.getPackedPositionType(packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
            throw new IllegalStateException("packedPosition is not a valid position.");
        }
        final int group = ExpandableListView.getPackedPositionGroup(packedPosition); 
        if (group >= mGroups.size() || group < 0) {
            return true;
        }
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            final int child = ExpandableListView.getPackedPositionChild(packedPosition); 
            if (child >= mGroups.get(group).children.size() || child < 0) {
                return true;
            }
        }
        return false;
    }
    private View getView(long packedPosition, View convertView, ViewGroup parent) {
        if (isOutOfBounds(packedPosition)) {
            throw new IllegalStateException("position out of range for adapter!");
        }
        final ExpandableListView elv = getExpandableListView();
        final int flPos = elv.getFlatListPosition(packedPosition); 
        if (convertView != null) {
            ((TextView) convertView).setText(getValueAtPosition(packedPosition));
            convertView.setId(flPos);
            return convertView;
        }
        int desiredHeight = getHeightForPosition(flPos);
        return createView(packedPosition, flPos, parent, desiredHeight);
    }
    protected View createView(long packedPosition, int flPos, ViewGroup parent, int desiredHeight) {
        TextView result = new TextView(parent.getContext());
        result.setHeight(desiredHeight);
        result.setText(getValueAtPosition(packedPosition));
        final ViewGroup.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lp);
        result.setGravity(Gravity.CENTER_VERTICAL);
        result.setPadding(36, 0, 0, 0);
        result.setId(flPos);
        return result;
    }
    public int findGroupWithNumChildren(int numChildren, boolean atLeastOneChild) {
        final ExpandableListAdapter adapter = mAdapter;
        for (int i = adapter.getGroupCount() - 1; i >= 0; i--) {
            final int curNumChildren = adapter.getChildrenCount(i);
            if (numChildren == curNumChildren || atLeastOneChild && curNumChildren > 0) {
                return i;
            }
        }
        return -1;
    }
    public List<MyGroup> getGroups() {
        return mGroups;
    }
    public ExpandableListAdapter getAdapter() {
        return mAdapter;
    }
    protected class MyAdapter extends BaseExpandableListAdapter {
        public Object getChild(int groupPosition, int childPosition) {
            return getValueAtPosition(ExpandableListView.getPackedPositionForChild(groupPosition,
                    childPosition));
        }
        public long getChildId(int groupPosition, int childPosition) {
            return mGroups.get(groupPosition).children.get(childPosition).id;
        }
        public int getChildrenCount(int groupPosition) {
            return mGroups.get(groupPosition).children.size();
        }
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            return getView(ExpandableListView.getPackedPositionForChild(groupPosition,
                    childPosition), convertView, parent);
        }
        public Object getGroup(int groupPosition) {
            return getValueAtPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
        }
        public int getGroupCount() {
            return mGroups.size();
        }
        public long getGroupId(int groupPosition) {
            return mGroups.get(groupPosition).id;
        }
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            return getView(ExpandableListView.getPackedPositionForGroup(groupPosition),
                    convertView, parent);
        }
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
        public boolean hasStableIds() {
            return true;
        }
    }
    public static class MyGroup {
        private static long mNextId = 1000;
        String name;
        long id = mNextId++;
        List<MyChild> children;
        public MyGroup(int numChildren) {
            name = "Group " + id;
            children = new ArrayList<MyChild>(numChildren);
            for (int i = 0; i < numChildren; i++) {
                children.add(new MyChild());
            }
        }
    }
    public static class MyChild {
        private static long mNextId = 2000;
        String name;
        long id = mNextId++;
        public MyChild() {
            name = "Child " + id;
        }
    }
    @Override
    protected final void init(Params params) {
        init((ExpandableParams) params);
    }
    protected abstract void init(ExpandableParams params);
}
