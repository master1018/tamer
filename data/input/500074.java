public class SQLiteQuery extends SQLiteProgram {
    private static final String TAG = "Cursor";
    private int mOffsetIndex;
    private String[] mBindArgs;
    private boolean mClosed = false;
     SQLiteQuery(SQLiteDatabase db, String query, int offsetIndex, String[] bindArgs) {
        super(db, query);
        mOffsetIndex = offsetIndex;
        mBindArgs = bindArgs;
    }
     int fillWindow(CursorWindow window,
            int maxRead, int lastPos) {
        long timeStart = SystemClock.uptimeMillis();
        mDatabase.lock();
        mDatabase.logTimeStat(mSql, timeStart, SQLiteDatabase.GET_LOCK_LOG_PREFIX);
        try {
            acquireReference();
            try {
                window.acquireReference();
                int numRows = native_fill_window(window, window.getStartPosition(), mOffsetIndex,
                        maxRead, lastPos);
                if (SQLiteDebug.DEBUG_SQL_STATEMENTS) {
                    Log.d(TAG, "fillWindow(): " + mSql);
                }
                mDatabase.logTimeStat(mSql, timeStart);
                return numRows;
            } catch (IllegalStateException e){
                return 0;
            } catch (SQLiteDatabaseCorruptException e) {
                mDatabase.onCorruption();
                throw e;
            } finally {
                window.releaseReference();
            }
        } finally {
            releaseReference();
            mDatabase.unlock();
        }
    }
     int columnCountLocked() {
        acquireReference();
        try {
            return native_column_count();
        } finally {
            releaseReference();
        }
    }
     String columnNameLocked(int columnIndex) {
        acquireReference();
        try {
            return native_column_name(columnIndex);
        } finally {
            releaseReference();
        }
    }
    @Override
    public String toString() {
        return "SQLiteQuery: " + mSql;
    }
    @Override
    public void close() {
        super.close();
        mClosed = true;
    }
     void requery() {
        if (mBindArgs != null) {
            int len = mBindArgs.length;
            try {
                for (int i = 0; i < len; i++) {
                    super.bindString(i + 1, mBindArgs[i]);
                }
            } catch (SQLiteMisuseException e) {
                StringBuilder errMsg = new StringBuilder("mSql " + mSql);
                for (int i = 0; i < len; i++) {
                    errMsg.append(" ");
                    errMsg.append(mBindArgs[i]);
                }
                errMsg.append(" ");
                IllegalStateException leakProgram = new IllegalStateException(
                        errMsg.toString(), e);
                throw leakProgram;                
            }
        }
    }
    @Override
    public void bindNull(int index) {
        mBindArgs[index - 1] = null;
        if (!mClosed) super.bindNull(index);
    }
    @Override
    public void bindLong(int index, long value) {
        mBindArgs[index - 1] = Long.toString(value);
        if (!mClosed) super.bindLong(index, value);
    }
    @Override
    public void bindDouble(int index, double value) {
        mBindArgs[index - 1] = Double.toString(value);
        if (!mClosed) super.bindDouble(index, value);
    }
    @Override
    public void bindString(int index, String value) {
        mBindArgs[index - 1] = value;
        if (!mClosed) super.bindString(index, value);
    }
    private final native int native_fill_window(CursorWindow window, 
            int startPos, int offsetParam, int maxRead, int lastPos);
    private final native int native_column_count();
    private final native String native_column_name(int columnIndex);
}
