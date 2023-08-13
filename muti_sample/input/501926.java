public abstract class CursorTreeAdapter extends BaseExpandableListAdapter implements Filterable,
        CursorFilter.CursorFilterClient {
    private Context mContext;
    private Handler mHandler;
    private boolean mAutoRequery;
    MyCursorHelper mGroupCursorHelper;
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;
    CursorFilter mCursorFilter;
    FilterQueryProvider mFilterQueryProvider;
    public CursorTreeAdapter(Cursor cursor, Context context) {
        init(cursor, context, true);
    }
    public CursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
        init(cursor, context, autoRequery);
    }
    private void init(Cursor cursor, Context context, boolean autoRequery) {
        mContext = context;
        mHandler = new Handler();
        mAutoRequery = autoRequery;
        mGroupCursorHelper = new MyCursorHelper(cursor);
        mChildrenCursorHelpers = new SparseArray<MyCursorHelper>();
    }
    synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper cursorHelper = mChildrenCursorHelpers.get(groupPosition);
        if (cursorHelper == null) {
            if (mGroupCursorHelper.moveTo(groupPosition) == null) return null;
            final Cursor cursor = getChildrenCursor(mGroupCursorHelper.getCursor());
            cursorHelper = new MyCursorHelper(cursor);
            mChildrenCursorHelpers.put(groupPosition, cursorHelper);
        }
        return cursorHelper;
    }
    abstract protected Cursor getChildrenCursor(Cursor groupCursor);
    public void setGroupCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, false);
    }
    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        MyCursorHelper childrenCursorHelper = getChildrenCursorHelper(groupPosition, false);
        childrenCursorHelper.changeCursor(childrenCursor, false);
    }
    public Cursor getChild(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
    }
    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).getId(childPosition);
    }
    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        return (mGroupCursorHelper.isValid() && helper != null) ? helper.getCount() : 0;
    }
    public Cursor getGroup(int groupPosition) {
        return mGroupCursorHelper.moveTo(groupPosition);
    }
    public int getGroupCount() {
        return mGroupCursorHelper.getCount();
    }
    public long getGroupId(int groupPosition) {
        return mGroupCursorHelper.getId(groupPosition);
    }
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        Cursor cursor = mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        View v;
        if (convertView == null) {
            v = newGroupView(mContext, cursor, isExpanded, parent);
        } else {
            v = convertView;
        }
        bindGroupView(v, mContext, cursor, isExpanded);
        return v;
    }
    protected abstract View newGroupView(Context context, Cursor cursor, boolean isExpanded,
            ViewGroup parent);
    protected abstract void bindGroupView(View view, Context context, Cursor cursor,
            boolean isExpanded);
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        Cursor cursor = cursorHelper.moveTo(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        View v;
        if (convertView == null) {
            v = newChildView(mContext, cursor, isLastChild, parent);
        } else {
            v = convertView;
        }
        bindChildView(v, mContext, cursor, isLastChild);
        return v;
    }
    protected abstract View newChildView(Context context, Cursor cursor, boolean isLastChild,
            ViewGroup parent);
    protected abstract void bindChildView(View view, Context context, Cursor cursor,
            boolean isLastChild);
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public boolean hasStableIds() {
        return true;
    }
    private synchronized void releaseCursorHelpers() {
        for (int pos = mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            mChildrenCursorHelpers.valueAt(pos).deactivate();
        }
        mChildrenCursorHelpers.clear();
    }
    @Override
    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }
    public void notifyDataSetChanged(boolean releaseCursors) {
        if (releaseCursors) {
            releaseCursorHelpers();
        }
        super.notifyDataSetChanged();
    }
    @Override
    public void notifyDataSetInvalidated() {
        releaseCursorHelpers();
        super.notifyDataSetInvalidated();
    }
    @Override
    public void onGroupCollapsed(int groupPosition) {
        deactivateChildrenCursorHelper(groupPosition);
    }
    synchronized void deactivateChildrenCursorHelper(int groupPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        mChildrenCursorHelpers.remove(groupPosition);
        cursorHelper.deactivate();
    }
    public String convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }
        return mGroupCursorHelper.getCursor();
    }
    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }
    public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }
    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }
    public void changeCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, true);
    }
    public Cursor getCursor() {
        return mGroupCursorHelper.getCursor();
    }
    class MyCursorHelper {
        private Cursor mCursor;
        private boolean mDataValid;
        private int mRowIDColumn;
        private MyContentObserver mContentObserver;
        private MyDataSetObserver mDataSetObserver;
        MyCursorHelper(Cursor cursor) {
            final boolean cursorPresent = cursor != null;
            mCursor = cursor;
            mDataValid = cursorPresent;
            mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            mContentObserver = new MyContentObserver();
            mDataSetObserver = new MyDataSetObserver();
            if (cursorPresent) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
            }
        }
        Cursor getCursor() {
            return mCursor;
        }
        int getCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }
        long getId(int position) {
            if (mDataValid && mCursor != null) {
                if (mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
        Cursor moveTo(int position) {
            if (mDataValid && (mCursor != null) && mCursor.moveToPosition(position)) {
                return mCursor;
            } else {
                return null;
            }
        }
        void changeCursor(Cursor cursor, boolean releaseCursors) {
            if (cursor == mCursor) return;
            deactivate();
            mCursor = cursor;
            if (cursor != null) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
                mRowIDColumn = cursor.getColumnIndex("_id");
                mDataValid = true;
                notifyDataSetChanged(releaseCursors);
            } else {
                mRowIDColumn = -1;
                mDataValid = false;
                notifyDataSetInvalidated();
            }
        }
        void deactivate() {
            if (mCursor == null) {
                return;
            }
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.deactivate();
            mCursor = null;
        }
        boolean isValid() {
            return mDataValid && mCursor != null;
        }
        private class MyContentObserver extends ContentObserver {
            public MyContentObserver() {
                super(mHandler);
            }
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
            @Override
            public void onChange(boolean selfChange) {
                if (mAutoRequery && mCursor != null) {
                    if (Config.LOGV) Log.v("Cursor", "Auto requerying " + mCursor +
                            " due to update");
                    mDataValid = mCursor.requery();
                }
            }
        }
        private class MyDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                mDataValid = true;
                notifyDataSetChanged();
            }
            @Override
            public void onInvalidated() {
                mDataValid = false;
                notifyDataSetInvalidated();
            }
        }
    }
}
