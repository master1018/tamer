public abstract class CursorAdapter extends BaseAdapter implements Filterable,
        CursorFilter.CursorFilterClient {
    protected boolean mDataValid;
    protected boolean mAutoRequery;
    protected Cursor mCursor;
    protected Context mContext;
    protected int mRowIDColumn;
    protected ChangeObserver mChangeObserver;
    protected DataSetObserver mDataSetObserver = new MyDataSetObserver();
    protected CursorFilter mCursorFilter;
    protected FilterQueryProvider mFilterQueryProvider;
    public CursorAdapter(Context context, Cursor c) {
        init(context, c, true);
    }
    public CursorAdapter(Context context, Cursor c, boolean autoRequery) {
        init(context, c, autoRequery);
    }
    protected void init(Context context, Cursor c, boolean autoRequery) {
        boolean cursorPresent = c != null;
        mAutoRequery = autoRequery;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        mChangeObserver = new ChangeObserver();
        if (cursorPresent) {
            c.registerContentObserver(mChangeObserver);
            c.registerDataSetObserver(mDataSetObserver);
        }
    }
    public Cursor getCursor() {
        return mCursor;
    }
    public int getCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
    public Object getItem(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }
    public long getItemId(int position) {
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
    @Override
    public boolean hasStableIds() {
        return true;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (mDataValid) {
            mCursor.moveToPosition(position);
            View v;
            if (convertView == null) {
                v = newDropDownView(mContext, mCursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, mContext, mCursor);
            return v;
        } else {
            return null;
        }
    }
    public abstract View newView(Context context, Cursor cursor, ViewGroup parent);
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return newView(context, cursor, parent);
    }
    public abstract void bindView(View view, Context context, Cursor cursor);
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
        if (cursor != null) {
            cursor.registerContentObserver(mChangeObserver);
            cursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = cursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }
        return mCursor;
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
    protected void onContentChanged() {
        if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
            if (Config.LOGV) Log.v("Cursor", "Auto requerying " + mCursor + " due to update");
            mDataValid = mCursor.requery();
        }
    }
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
            onContentChanged();
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
