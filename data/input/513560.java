public abstract class AbstractCursor implements CrossProcessCursor {
    private static final String TAG = "Cursor";
    DataSetObservable mDataSetObservable = new DataSetObservable();
    ContentObservable mContentObservable = new ContentObservable();
    abstract public int getCount();
    abstract public String[] getColumnNames();
    abstract public String getString(int column);
    abstract public short getShort(int column);
    abstract public int getInt(int column);
    abstract public long getLong(int column);
    abstract public float getFloat(int column);
    abstract public double getDouble(int column);
    abstract public boolean isNull(int column);
    public byte[] getBlob(int column) {
        throw new UnsupportedOperationException("getBlob is not supported");
    }
    public CursorWindow getWindow() {
        return null;
    }
    public int getColumnCount() {
        return getColumnNames().length;
    }
    public void deactivate() {
        deactivateInternal();
    }
    public void deactivateInternal() {
        if (mSelfObserver != null) {
            mContentResolver.unregisterContentObserver(mSelfObserver);
            mSelfObserverRegistered = false;
        }
        mDataSetObservable.notifyInvalidated();
    }
    public boolean requery() {
        if (mSelfObserver != null && mSelfObserverRegistered == false) {
            mContentResolver.registerContentObserver(mNotifyUri, true, mSelfObserver);
            mSelfObserverRegistered = true;
        }
        mDataSetObservable.notifyChanged();
        return true;
    }
    public boolean isClosed() {
        return mClosed;
    }
    public void close() {
        mClosed = true;
        mContentObservable.unregisterAll();
        deactivateInternal();
    }
    public boolean commitUpdates(Map<? extends Long,? extends Map<String,Object>> values) {
        return false;
    }
    public boolean deleteRow() {
        return false;
    }
    public boolean onMove(int oldPosition, int newPosition) {
        return true;
    }
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        String result = getString(columnIndex);
        if (result != null) {
            char[] data = buffer.data;
            if (data == null || data.length < result.length()) {
                buffer.data = result.toCharArray();
            } else {
                result.getChars(0, result.length(), data, 0);
            }
            buffer.sizeCopied = result.length();
        }
    }
    public AbstractCursor() {
        mPos = -1;
        mRowIdColumnIndex = -1;
        mCurrentRowID = null;
        mUpdatedRows = new HashMap<Long, Map<String, Object>>();
    }
    public final int getPosition() {
        return mPos;
    }
    public final boolean moveToPosition(int position) {
        final int count = getCount();
        if (position >= count) {
            mPos = count;
            return false;
        }
        if (position < 0) {
            mPos = -1;
            return false;
        }
        if (position == mPos) {
            return true;
        }
        boolean result = onMove(mPos, position);
        if (result == false) {
            mPos = -1;
        } else {
            mPos = position;
            if (mRowIdColumnIndex != -1) {
                mCurrentRowID = Long.valueOf(getLong(mRowIdColumnIndex));
            }
        }
        return result;
    }
    public void fillWindow(int position, CursorWindow window) {
        if (position < 0 || position > getCount()) {
            return;
        }
        window.acquireReference();
        try {
            int oldpos = mPos;
            mPos = position - 1;
            window.clear();
            window.setStartPosition(position);
            int columnNum = getColumnCount();
            window.setNumColumns(columnNum);
            while (moveToNext() && window.allocRow()) {            
                for (int i = 0; i < columnNum; i++) {
                    String field = getString(i);
                    if (field != null) {
                        if (!window.putString(field, mPos, i)) {
                            window.freeLastRow();
                            break;
                        }
                    } else {
                        if (!window.putNull(mPos, i)) {
                            window.freeLastRow();
                            break;
                        }
                    }
                }
            }
            mPos = oldpos;
        } catch (IllegalStateException e){
        } finally {
            window.releaseReference();
        }
    }
    public final boolean move(int offset) {
        return moveToPosition(mPos + offset);
    }
    public final boolean moveToFirst() {
        return moveToPosition(0);
    }
    public final boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }
    public final boolean moveToNext() {
        return moveToPosition(mPos + 1);
    }
    public final boolean moveToPrevious() {
        return moveToPosition(mPos - 1);
    }
    public final boolean isFirst() {
        return mPos == 0 && getCount() != 0;
    }
    public final boolean isLast() {
        int cnt = getCount();
        return mPos == (cnt - 1) && cnt != 0;
    }
    public final boolean isBeforeFirst() {
        if (getCount() == 0) {
            return true;
        }
        return mPos == -1;
    }
    public final boolean isAfterLast() {
        if (getCount() == 0) {
            return true;
        }
        return mPos == getCount();
    }
    public int getColumnIndex(String columnName) {
        final int periodIndex = columnName.lastIndexOf('.');
        if (periodIndex != -1) {
            Exception e = new Exception();
            Log.e(TAG, "requesting column name with table name -- " + columnName, e);
            columnName = columnName.substring(periodIndex + 1);
        }
        String columnNames[] = getColumnNames();
        int length = columnNames.length;
        for (int i = 0; i < length; i++) {
            if (columnNames[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        if (Config.LOGV) {
            if (getCount() > 0) {
                Log.w("AbstractCursor", "Unknown column " + columnName);
            }
        }
        return -1;
    }
    public int getColumnIndexOrThrow(String columnName) {
        final int index = getColumnIndex(columnName);
        if (index < 0) {
            throw new IllegalArgumentException("column '" + columnName + "' does not exist");
        }
        return index;
    }
    public String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }
    public boolean updateBlob(int columnIndex, byte[] value) {
        return update(columnIndex, value);
    }
    public boolean updateString(int columnIndex, String value) {
        return update(columnIndex, value);
    }
    public boolean updateShort(int columnIndex, short value) {
        return update(columnIndex, Short.valueOf(value));
    }
    public boolean updateInt(int columnIndex, int value) {
        return update(columnIndex, Integer.valueOf(value));
    }
    public boolean updateLong(int columnIndex, long value) {
        return update(columnIndex, Long.valueOf(value));
    }
    public boolean updateFloat(int columnIndex, float value) {
        return update(columnIndex, Float.valueOf(value));
    }
    public boolean updateDouble(int columnIndex, double value) {
        return update(columnIndex, Double.valueOf(value));
    }
    public boolean updateToNull(int columnIndex) {
        return update(columnIndex, null);
    }
    public boolean update(int columnIndex, Object obj) {
        if (!supportsUpdates()) {
            return false;
        }
        Long rowid = new Long(getLong(mRowIdColumnIndex));
        if (rowid == null) {
            throw new IllegalStateException("null rowid. mRowIdColumnIndex = " + mRowIdColumnIndex);
        }
        synchronized(mUpdatedRows) {
            Map<String, Object> row = mUpdatedRows.get(rowid);
            if (row == null) {
                row = new HashMap<String, Object>();
                mUpdatedRows.put(rowid, row);
            }
            row.put(getColumnNames()[columnIndex], obj);
        }
        return true;
    }
    public boolean hasUpdates() {
        synchronized(mUpdatedRows) {
            return mUpdatedRows.size() > 0;
        }
    }
    public void abortUpdates() {
        synchronized(mUpdatedRows) {
            mUpdatedRows.clear();
        }
    }
    public boolean commitUpdates() {
        return commitUpdates(null);
    }
    public boolean supportsUpdates() {
        return mRowIdColumnIndex != -1;
    }
    public void registerContentObserver(ContentObserver observer) {
        mContentObservable.registerObserver(observer);
    }
    public void unregisterContentObserver(ContentObserver observer) {
        if (!mClosed) {
            mContentObservable.unregisterObserver(observer);
        }
    }
    protected void notifyDataSetChange() {
        mDataSetObservable.notifyChanged();
    }
    protected DataSetObservable getDataSetObservable() {
        return mDataSetObservable;
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    protected void onChange(boolean selfChange) {
        synchronized (mSelfObserverLock) {
            mContentObservable.dispatchChange(selfChange);
            if (mNotifyUri != null && selfChange) {
                mContentResolver.notifyChange(mNotifyUri, mSelfObserver);
            }
        }
    }
    public void setNotificationUri(ContentResolver cr, Uri notifyUri) {
        synchronized (mSelfObserverLock) {
            mNotifyUri = notifyUri;
            mContentResolver = cr;
            if (mSelfObserver != null) {
                mContentResolver.unregisterContentObserver(mSelfObserver);
            }
            mSelfObserver = new SelfContentObserver(this);
            mContentResolver.registerContentObserver(mNotifyUri, true, mSelfObserver);
            mSelfObserverRegistered = true;
        }
    }
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }
    public Bundle getExtras() {
        return Bundle.EMPTY;
    }
    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }
    protected boolean isFieldUpdated(int columnIndex) {
        if (mRowIdColumnIndex != -1 && mUpdatedRows.size() > 0) {
            Map<String, Object> updates = mUpdatedRows.get(mCurrentRowID);
            if (updates != null && updates.containsKey(getColumnNames()[columnIndex])) {
                return true;
            }
        }
        return false;
    }
    protected Object getUpdatedField(int columnIndex) {
        Map<String, Object> updates = mUpdatedRows.get(mCurrentRowID);
        return updates.get(getColumnNames()[columnIndex]);
    }
    protected void checkPosition() {
        if (-1 == mPos || getCount() == mPos) {
            throw new CursorIndexOutOfBoundsException(mPos, getCount());
        }
    }
    @Override
    protected void finalize() {
        if (mSelfObserver != null && mSelfObserverRegistered == true) {
            mContentResolver.unregisterContentObserver(mSelfObserver);
        }
    }
    protected static class SelfContentObserver extends ContentObserver {
        WeakReference<AbstractCursor> mCursor;
        public SelfContentObserver(AbstractCursor cursor) {
            super(null);
            mCursor = new WeakReference<AbstractCursor>(cursor);
        }
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }
        @Override
        public void onChange(boolean selfChange) {
            AbstractCursor cursor = mCursor.get();
            if (cursor != null) {
                cursor.onChange(false);
            }
        }
    }
    protected HashMap<Long, Map<String, Object>> mUpdatedRows;
    protected int mRowIdColumnIndex;
    protected int mPos;
    protected Long mCurrentRowID;
    protected ContentResolver mContentResolver;
    protected boolean mClosed = false;
    private Uri mNotifyUri;
    private ContentObserver mSelfObserver;
    final private Object mSelfObserverLock = new Object();
    private boolean mSelfObserverRegistered;
}
