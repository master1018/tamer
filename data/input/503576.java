public class ContentQueryMap extends Observable {
    private Cursor mCursor;
    private String[] mColumnNames;
    private int mKeyColumn;
    private Handler mHandlerForUpdateNotifications = null;
    private boolean mKeepUpdated = false;
    private Map<String, ContentValues> mValues = null;
    private ContentObserver mContentObserver;
    private boolean mDirty = false;
    public ContentQueryMap(Cursor cursor, String columnNameOfKey, boolean keepUpdated,
            Handler handlerForUpdateNotifications) {
        mCursor = cursor;
        mColumnNames = mCursor.getColumnNames();
        mKeyColumn = mCursor.getColumnIndexOrThrow(columnNameOfKey);
        mHandlerForUpdateNotifications = handlerForUpdateNotifications;
        setKeepUpdated(keepUpdated);
        if (!keepUpdated) {
            readCursorIntoCache();
        }
    }
    public void setKeepUpdated(boolean keepUpdated) {
        if (keepUpdated == mKeepUpdated) return;
        mKeepUpdated = keepUpdated;
        if (!mKeepUpdated) {
            mCursor.unregisterContentObserver(mContentObserver);
            mContentObserver = null;
        } else {
            if (mHandlerForUpdateNotifications == null) {
                mHandlerForUpdateNotifications = new Handler();
            }
            if (mContentObserver == null) {
                mContentObserver = new ContentObserver(mHandlerForUpdateNotifications) {
                    @Override
                    public void onChange(boolean selfChange) {
                        if (countObservers() != 0) {
                            requery();
                        } else {
                            mDirty = true;
                        }
                    }
                };
            }
            mCursor.registerContentObserver(mContentObserver);
            mDirty = true;
        }
    }
    public synchronized ContentValues getValues(String rowName) {
        if (mDirty) requery();
        return mValues.get(rowName);
    }
    public void requery() {
        mDirty = false;
        mCursor.requery();
        readCursorIntoCache();
        setChanged();
        notifyObservers();
    }
    private synchronized void readCursorIntoCache() {
        int capacity = mValues != null ? mValues.size() : 0;
        mValues = new HashMap<String, ContentValues>(capacity);
        while (mCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < mColumnNames.length; i++) {
                if (i != mKeyColumn) {
                    values.put(mColumnNames[i], mCursor.getString(i));
                }
            }
            mValues.put(mCursor.getString(mKeyColumn), values);
        }
    }
    public synchronized Map<String, ContentValues> getRows() {
        if (mDirty) requery();
        return mValues;
    }
    public synchronized void close() {
        if (mContentObserver != null) {
            mCursor.unregisterContentObserver(mContentObserver);
            mContentObserver = null;
        }
        mCursor.close();
        mCursor = null;
    }
    @Override
    protected void finalize() throws Throwable {
        if (mCursor != null) close();
        super.finalize();
    }
}
