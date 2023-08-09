 class SQLiteCompiledSql {
    private static final String TAG = "SQLiteCompiledSql";
     SQLiteDatabase mDatabase;
     int nHandle = 0;
     int nStatement = 0;
    private String mSqlStmt = null;
    private Throwable mStackTrace = null;
    private boolean mInUse = false;
     SQLiteCompiledSql(SQLiteDatabase db, String sql) {
        if (!db.isOpen()) {
            throw new IllegalStateException("database " + db.getPath() + " already closed");
        }
        mDatabase = db;
        mSqlStmt = sql;
        mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();
        this.nHandle = db.mNativeHandle;
        compile(sql, true);
    }
    private void compile(String sql, boolean forceCompilation) {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        if (forceCompilation) {
            mDatabase.lock();
            try {
                native_compile(sql);
            } finally {
                mDatabase.unlock();
            }
        }
    }
     void releaseSqlStatement() {
        if (nStatement != 0) {
            if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                Log.v(TAG, "closed and deallocated DbObj (id#" + nStatement +")");
            }
            try {
                mDatabase.lock();
                native_finalize();
                nStatement = 0;
            } finally {
                mDatabase.unlock();
            }
        }
    }
     synchronized boolean acquire() {
        if (mInUse) {
            return false;
        }
        mInUse = true;
        if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
            Log.v(TAG, "Acquired DbObj (id#" + nStatement + ") from DB cache");
        }
        return true;
    }
     synchronized void release() {
        if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
            Log.v(TAG, "Released DbObj (id#" + nStatement + ") back to DB cache");
        }
        mInUse = false;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (nStatement == 0) return;
            if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                Log.v(TAG, "** warning ** Finalized DbObj (id#" + nStatement + ")");
            }
            int len = mSqlStmt.length();
            Log.w(TAG, "Releasing statement in a finalizer. Please ensure " +
                    "that you explicitly call close() on your cursor: " +
                    mSqlStmt.substring(0, (len > 100) ? 100 : len), mStackTrace);
            releaseSqlStatement();
        } finally {
            super.finalize();
        }
    }
    private final native void native_compile(String sql);
    private final native void native_finalize();
}
