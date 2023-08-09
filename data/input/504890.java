public class SQLiteCursor extends AbstractWindowedCursor {
    static final String TAG = "Cursor";
    static final int NO_COUNT = -1;
    private String mEditTable;
    private String[] mColumns;
    private SQLiteQuery mQuery;
    private SQLiteDatabase mDatabase;
    private SQLiteCursorDriver mDriver;
    private int mCount = NO_COUNT;
    private Map<String, Integer> mColumnNameMap;
    private Throwable mStackTrace;
    private int mMaxRead = Integer.MAX_VALUE;
    private int mInitialRead = Integer.MAX_VALUE;
    private int mCursorState = 0;
    private ReentrantLock mLock = null;
    private boolean mPendingData = false;
    public void setLoadStyle(int initialRead, int maxRead) {
        mMaxRead = maxRead;
        mInitialRead = initialRead;
        mLock = new ReentrantLock(true);
    }
    private void queryThreadLock() {
        if (mLock != null) {
            mLock.lock();            
        }
    }
    private void queryThreadUnlock() {
        if (mLock != null) {
            mLock.unlock();            
        }
    }
    final private class QueryThread implements Runnable {
        private final int mThreadState;
        QueryThread(int version) {
            mThreadState = version;
        }
        private void sendMessage() {
            if (mNotificationHandler != null) {
                mNotificationHandler.sendEmptyMessage(1);
                mPendingData = false;
            } else {
                mPendingData = true;
            }
        }
        public void run() {
            CursorWindow cw = mWindow;
            Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                mLock.lock();
                if (mCursorState != mThreadState) {
                    mLock.unlock();
                    break;
                }
                try {
                    int count = mQuery.fillWindow(cw, mMaxRead, mCount);
                    if (count != 0) {
                        if (count == NO_COUNT){
                            mCount += mMaxRead;
                            sendMessage();
                        } else {                                
                            mCount = count;
                            sendMessage();
                            break;
                        }
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    break;
                } finally {
                    mLock.unlock();
                }
            }
        }        
    }
    protected class MainThreadNotificationHandler extends Handler {
        public void handleMessage(Message msg) {
            notifyDataSetChange();
        }
    }
    protected MainThreadNotificationHandler mNotificationHandler;    
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if ((Integer.MAX_VALUE != mMaxRead || Integer.MAX_VALUE != mInitialRead) && 
                mNotificationHandler == null) {
            queryThreadLock();
            try {
                mNotificationHandler = new MainThreadNotificationHandler();
                if (mPendingData) {
                    notifyDataSetChange();
                    mPendingData = false;
                }
            } finally {
                queryThreadUnlock();
            }
        }
    }
    public SQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
            String editTable, SQLiteQuery query) {
        super();
        mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();
        mDatabase = db;
        mDriver = driver;
        mEditTable = editTable;
        mColumnNameMap = null;
        mQuery = query;
        try {
            db.lock();
            int columnCount = mQuery.columnCountLocked();
            mColumns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String columnName = mQuery.columnNameLocked(i);
                mColumns[i] = columnName;
                if (Config.LOGV) {
                    Log.v("DatabaseWindow", "mColumns[" + i + "] is "
                            + mColumns[i]);
                }
                if ("_id".equals(columnName)) {
                    mRowIdColumnIndex = i;
                }
            }
        } finally {
            db.unlock();
        }
    }
    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }
    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (mWindow == null || newPosition < mWindow.getStartPosition() ||
                newPosition >= (mWindow.getStartPosition() + mWindow.getNumRows())) {
            fillWindow(newPosition);
        }
        return true;
    }
    @Override
    public int getCount() {
        if (mCount == NO_COUNT) {
            fillWindow(0);
        }
        return mCount;
    }
    private void fillWindow (int startPos) {
        if (mWindow == null) {
            mWindow = new CursorWindow(true );
        } else {
            mCursorState++;
                queryThreadLock();
                try {
                    mWindow.clear();
                } finally {
                    queryThreadUnlock();
                }
        }
        mWindow.setStartPosition(startPos);
        mCount = mQuery.fillWindow(mWindow, mInitialRead, 0);
        if (mCount == NO_COUNT){
            mCount = startPos + mInitialRead;
            Thread t = new Thread(new QueryThread(mCursorState), "query thread");
            t.start();
        } 
    }
    @Override
    public int getColumnIndex(String columnName) {
        if (mColumnNameMap == null) {
            String[] columns = mColumns;
            int columnCount = columns.length;
            HashMap<String, Integer> map = new HashMap<String, Integer>(columnCount, 1);
            for (int i = 0; i < columnCount; i++) {
                map.put(columns[i], i);
            }
            mColumnNameMap = map;
        }
        final int periodIndex = columnName.lastIndexOf('.');
        if (periodIndex != -1) {
            Exception e = new Exception();
            Log.e(TAG, "requesting column name with table name -- " + columnName, e);
            columnName = columnName.substring(periodIndex + 1);
        }
        Integer i = mColumnNameMap.get(columnName);
        if (i != null) {
            return i.intValue();
        } else {
            return -1;
        }
    }
    @Override
    public boolean deleteRow() {
        checkPosition();
        if (mRowIdColumnIndex == -1 || mCurrentRowID == null) {
            Log.e(TAG,
                    "Could not delete row because either the row ID column is not available or it" +
                    "has not been read.");
            return false;
        }
        boolean success;
        mDatabase.lock();
        try {
            try {
                mDatabase.delete(mEditTable, mColumns[mRowIdColumnIndex] + "=?",
                        new String[] {mCurrentRowID.toString()});
                success = true;
            } catch (SQLException e) {
                success = false;
            }
            int pos = mPos;
            requery();
            moveToPosition(pos);
        } finally {
            mDatabase.unlock();
        }
        if (success) {
            onChange(true);
            return true;
        } else {
            return false;
        }
    }
    @Override
    public String[] getColumnNames() {
        return mColumns;
    }
    @Override
    public boolean supportsUpdates() {
        return super.supportsUpdates() && !TextUtils.isEmpty(mEditTable);
    }
    @Override
    public boolean commitUpdates(Map<? extends Long,
            ? extends Map<String, Object>> additionalValues) {
        if (!supportsUpdates()) {
            Log.e(TAG, "commitUpdates not supported on this cursor, did you "
                    + "include the _id column?");
            return false;
        }
        synchronized (mUpdatedRows) {
            if (additionalValues != null) {
                mUpdatedRows.putAll(additionalValues);
            }
            if (mUpdatedRows.size() == 0) {
                return true;
            }
            mDatabase.beginTransaction();
            try {
                StringBuilder sql = new StringBuilder(128);
                for (Map.Entry<Long, Map<String, Object>> rowEntry :
                        mUpdatedRows.entrySet()) {
                    Map<String, Object> values = rowEntry.getValue();
                    Long rowIdObj = rowEntry.getKey();
                    if (rowIdObj == null || values == null) {
                        throw new IllegalStateException("null rowId or values found! rowId = "
                                + rowIdObj + ", values = " + values);
                    }
                    if (values.size() == 0) {
                        continue;
                    }
                    long rowId = rowIdObj.longValue();
                    Iterator<Map.Entry<String, Object>> valuesIter =
                            values.entrySet().iterator();
                    sql.setLength(0);
                    sql.append("UPDATE " + mEditTable + " SET ");
                    Object[] bindings = new Object[values.size()];
                    int i = 0;
                    while (valuesIter.hasNext()) {
                        Map.Entry<String, Object> entry = valuesIter.next();
                        sql.append(entry.getKey());
                        sql.append("=?");
                        bindings[i] = entry.getValue();
                        if (valuesIter.hasNext()) {
                            sql.append(", ");
                        }
                        i++;
                    }
                    sql.append(" WHERE " + mColumns[mRowIdColumnIndex]
                            + '=' + rowId);
                    sql.append(';');
                    mDatabase.execSQL(sql.toString(), bindings);
                    mDatabase.rowUpdated(mEditTable, rowId);
                }
                mDatabase.setTransactionSuccessful();
            } finally {
                mDatabase.endTransaction();
            }
            mUpdatedRows.clear();
        }
        onChange(true);
        return true;
    }
    private void deactivateCommon() {
        if (Config.LOGV) Log.v(TAG, "<<< Releasing cursor " + this);
        mCursorState = 0;
        if (mWindow != null) {
            mWindow.close();
            mWindow = null;
        }
        if (Config.LOGV) Log.v("DatabaseWindow", "closing window in release()");
    }
    @Override
    public void deactivate() {
        super.deactivate();
        deactivateCommon();
        mDriver.cursorDeactivated();
    }
    @Override
    public void close() {
        super.close();
        deactivateCommon();
        mQuery.close();
        mDriver.cursorClosed();
    }
    @Override
    public boolean requery() {
        if (isClosed()) {
            return false;
        }
        long timeStart = 0;
        if (Config.LOGV) {
            timeStart = System.currentTimeMillis();
        }
        mDatabase.lock();
        try {
            if (mWindow != null) {
                mWindow.clear();
            }
            mPos = -1;
            mDriver.cursorRequeried(this);
            mCount = NO_COUNT;
            mCursorState++;
            queryThreadLock();
            try {
                mQuery.requery();
            } finally {
                queryThreadUnlock();
            }
        } finally {
            mDatabase.unlock();
        }
        if (Config.LOGV) {
            Log.v("DatabaseWindow", "closing window in requery()");
            Log.v(TAG, "--- Requery()ed cursor " + this + ": " + mQuery);
        }
        boolean result = super.requery();
        if (Config.LOGV) {
            long timeEnd = System.currentTimeMillis();
            Log.v(TAG, "requery (" + (timeEnd - timeStart) + " ms): " + mDriver.toString());
        }
        return result;
    }
    @Override
    public void setWindow(CursorWindow window) {        
        if (mWindow != null) {
            mCursorState++;
            queryThreadLock();
            try {
                mWindow.close();
            } finally {
                queryThreadUnlock();
            }
            mCount = NO_COUNT;
        }
        mWindow = window;
    }
    public void setSelectionArguments(String[] selectionArgs) {
        mDriver.setBindArguments(selectionArgs);
    }
    @Override
    protected void finalize() {
        try {
            if (mWindow != null) {
                int len = mQuery.mSql.length();
                Log.e(TAG, "Finalizing a Cursor that has not been deactivated or closed. " +
                        "database = " + mDatabase.getPath() + ", table = " + mEditTable +
                        ", query = " + mQuery.mSql.substring(0, (len > 100) ? 100 : len),
                        mStackTrace);
                close();
                SQLiteDebug.notifyActiveCursorFinalized();
            } else {
                if (Config.LOGV) {
                    Log.v(TAG, "Finalizing cursor on database = " + mDatabase.getPath() +
                            ", table = " + mEditTable + ", query = " + mQuery.mSql);
                }
            }
        } finally {
            super.finalize();
        }
    }
}
