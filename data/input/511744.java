public abstract class SQLiteProgram extends SQLiteClosable {
    private static final String TAG = "SQLiteProgram";
    @Deprecated
    protected SQLiteDatabase mDatabase;
     final String mSql;
    @Deprecated
    protected int nHandle = 0;
    private SQLiteCompiledSql mCompiledSql;
    @Deprecated
    protected int nStatement = 0;
     SQLiteProgram(SQLiteDatabase db, String sql) {
        mDatabase = db;
        mSql = sql.trim();
        db.acquireReference();
        db.addSQLiteClosable(this);
        this.nHandle = db.mNativeHandle;
        String prefixSql = mSql.substring(0, 6);
        if (!prefixSql.equalsIgnoreCase("INSERT") && !prefixSql.equalsIgnoreCase("UPDATE") &&
                !prefixSql.equalsIgnoreCase("REPLAC") &&
                !prefixSql.equalsIgnoreCase("DELETE") && !prefixSql.equalsIgnoreCase("SELECT")) {
            mCompiledSql = new SQLiteCompiledSql(db, sql);
            nStatement = mCompiledSql.nStatement;
            return;
        }
        mCompiledSql = db.getCompiledStatementForSql(sql);
        if (mCompiledSql == null) {
            mCompiledSql = new SQLiteCompiledSql(db, sql);
            mCompiledSql.acquire();
            db.addToCompiledQueries(sql, mCompiledSql);
            if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                Log.v(TAG, "Created DbObj (id#" + mCompiledSql.nStatement +
                        ") for sql: " + sql);
            }
        } else {
            if (!mCompiledSql.acquire()) {
                int last = mCompiledSql.nStatement;
                mCompiledSql = new SQLiteCompiledSql(db, sql);
                if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                    Log.v(TAG, "** possible bug ** Created NEW DbObj (id#" +
                            mCompiledSql.nStatement +
                            ") because the previously created DbObj (id#" + last +
                            ") was not released for sql:" + sql);
                }
            }
        }
        nStatement = mCompiledSql.nStatement;
    }
    @Override
    protected void onAllReferencesReleased() {
        releaseCompiledSqlIfNotInCache();
        mDatabase.releaseReference();
        mDatabase.removeSQLiteClosable(this);
    }
    @Override
    protected void onAllReferencesReleasedFromContainer() {
        releaseCompiledSqlIfNotInCache();
        mDatabase.releaseReference();
    }
    private void releaseCompiledSqlIfNotInCache() {
        if (mCompiledSql == null) {
            return;
        }
        synchronized(mDatabase.mCompiledQueries) {
            if (!mDatabase.mCompiledQueries.containsValue(mCompiledSql)) {
                mCompiledSql.releaseSqlStatement();
                mCompiledSql = null;
                nStatement = 0;
            } else {
                mCompiledSql.release();
            }
        } 
    }
    public final int getUniqueId() {
        return nStatement;
    }
     String getSqlString() {
        return mSql;
    }
    @Deprecated
    protected void compile(String sql, boolean forceCompilation) {
    }
    public void bindNull(int index) {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_bind_null(index);
        } finally {
            releaseReference();
        }
    }
    public void bindLong(int index, long value) {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_bind_long(index, value);
        } finally {
            releaseReference();
        }
    }
    public void bindDouble(int index, double value) {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_bind_double(index, value);
        } finally {
            releaseReference();
        }
    }
    public void bindString(int index, String value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_bind_string(index, value);
        } finally {
            releaseReference();
        }
    }
    public void bindBlob(int index, byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_bind_blob(index, value);
        } finally {
            releaseReference();
        }
    }
    public void clearBindings() {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        acquireReference();
        try {
            native_clear_bindings();
        } finally {
            releaseReference();
        }
    }
    public void close() {
        if (!mDatabase.isOpen()) {
            return;
        }
        mDatabase.lock();
        try {
            releaseReference();
        } finally {
            mDatabase.unlock();
        }
    }
    @Deprecated
    protected final native void native_compile(String sql);
    @Deprecated
    protected final native void native_finalize();
    protected final native void native_bind_null(int index);
    protected final native void native_bind_long(int index, long value);
    protected final native void native_bind_double(int index, double value);
    protected final native void native_bind_string(int index, String value);
    protected final native void native_bind_blob(int index, byte[] value);
    private final native void native_clear_bindings();
}
