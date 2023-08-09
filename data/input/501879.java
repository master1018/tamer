public class SQLiteDirectCursorDriver implements SQLiteCursorDriver {
    private String mEditTable; 
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    private String mSql;
    private SQLiteQuery mQuery;
    public SQLiteDirectCursorDriver(SQLiteDatabase db, String sql, String editTable) {
        mDatabase = db;
        mEditTable = editTable;
        mSql = sql;
    }
    public Cursor query(CursorFactory factory, String[] selectionArgs) {
        SQLiteQuery query = new SQLiteQuery(mDatabase, mSql, 0, selectionArgs);
        try {
            int numArgs = selectionArgs == null ? 0 : selectionArgs.length;
            for (int i = 0; i < numArgs; i++) {
                query.bindString(i + 1, selectionArgs[i]);
            }
            if (factory == null) {
                mCursor = new SQLiteCursor(mDatabase, this, mEditTable, query);
            } else {
                mCursor = factory.newCursor(mDatabase, this, mEditTable, query);
            }
            mQuery = query;
            query = null;
            return mCursor;
        } finally {
            if (query != null) query.close();
        }
    }
    public void cursorClosed() {
        mCursor = null;
    }
    public void setBindArguments(String[] bindArgs) {
        final int numArgs = bindArgs.length;
        for (int i = 0; i < numArgs; i++) {
            mQuery.bindString(i + 1, bindArgs[i]);
        }
    }
    public void cursorDeactivated() {
    }
    public void cursorRequeried(Cursor cursor) {
    }
    @Override
    public String toString() {
        return "SQLiteDirectCursorDriver: " + mSql;
    }
}
