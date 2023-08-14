public class SQLiteDatabase extends SQLiteClosable {
    private static final String TAG = "Database";
    private static final int EVENT_DB_OPERATION = 52000;
    private static final int EVENT_DB_CORRUPT = 75004;
    public static final int CONFLICT_ROLLBACK = 1;
    public static final int CONFLICT_ABORT = 2;
    public static final int CONFLICT_FAIL = 3;
    public static final int CONFLICT_IGNORE = 4;
    public static final int CONFLICT_REPLACE = 5;
    public static final int CONFLICT_NONE = 0;
    private static final String[] CONFLICT_VALUES = new String[]
            {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
    public static final int SQLITE_MAX_LIKE_PATTERN_LENGTH = 50000;
    public static final int OPEN_READWRITE = 0x00000000;          
    public static final int OPEN_READONLY = 0x00000001;           
    private static final int OPEN_READ_MASK = 0x00000001;         
    public static final int NO_LOCALIZED_COLLATORS = 0x00000010;  
    public static final int CREATE_IF_NECESSARY = 0x10000000;     
    private boolean mInnerTransactionIsSuccessful;
    private boolean mTransactionIsSuccessful;
    private SQLiteTransactionListener mTransactionListener;
    private final ReentrantLock mLock = new ReentrantLock(true);
    private long mLockAcquiredWallTime = 0L;
    private long mLockAcquiredThreadTime = 0L;
    private static final int LOCK_WARNING_WINDOW_IN_MS = 20000;
    private static final int LOCK_ACQUIRED_WARNING_TIME_IN_MS = 300;
    private static final int LOCK_ACQUIRED_WARNING_THREAD_TIME_IN_MS = 100;
    private static final int LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT = 2000;
    private static final int SLEEP_AFTER_YIELD_QUANTUM = 1000;
    private static final Pattern EMAIL_IN_DB_PATTERN = Pattern.compile("[\\w\\.\\-]+@[\\w\\.\\-]+");
    private long mLastLockMessageTime = 0L;
    private static int sQueryLogTimeInMillis = 0;  
    private static final int QUERY_LOG_SQL_LENGTH = 64;
    private static final String COMMIT_SQL = "COMMIT;";
    private final Random mRandom = new Random();
    private String mLastSqlStatement = null;
     static final String GET_LOCK_LOG_PREFIX = "GETLOCK:";
     int mNativeHandle = 0;
     int mTempTableSequence = 0;
    private String mPath;
    private String mPathForLogs = null;  
    private int mFlags;
    private CursorFactory mFactory;
    private WeakHashMap<SQLiteClosable, Object> mPrograms;
     Map<String, SQLiteCompiledSql> mCompiledQueries = Maps.newHashMap();
    public static final int MAX_SQL_CACHE_SIZE = 250;
    private int mMaxSqlCacheSize = MAX_SQL_CACHE_SIZE; 
    private int mCacheFullWarnings;
    private static final int MAX_WARNINGS_ON_CACHESIZE_CONDITION = 1;
    private int mNumCacheHits;
    private int mNumCacheMisses;
    private String mTimeOpened = null;
    private String mTimeClosed = null;
    private Throwable mStackTrace = null;
    private static final String LOG_SLOW_QUERIES_PROPERTY = "db.log.slow_query_threshold";
    private final int mSlowQueryThreshold;
    void addSQLiteClosable(SQLiteClosable closable) {
        lock();
        try {
            mPrograms.put(closable, null);
        } finally {
            unlock();
        }
    }
    void removeSQLiteClosable(SQLiteClosable closable) {
        lock();
        try {
            mPrograms.remove(closable);
        } finally {
            unlock();
        }
    }
    @Override
    protected void onAllReferencesReleased() {
        if (isOpen()) {
            if (SQLiteDebug.DEBUG_SQL_CACHE) {
                mTimeClosed = getTime();
            }
            dbclose();
        }
    }
    static public native int releaseMemory();
    public void setLockingEnabled(boolean lockingEnabled) {
        mLockingEnabled = lockingEnabled;
    }
    private boolean mLockingEnabled = true;
     void onCorruption() {
        Log.e(TAG, "Removing corrupt database: " + mPath);
        EventLog.writeEvent(EVENT_DB_CORRUPT, mPath);
        try {
            close();
        } finally {
            if (!mPath.equalsIgnoreCase(":memory")) {
                new File(mPath).delete();
            }
        }
    }
     void lock() {
        if (!mLockingEnabled) return;
        mLock.lock();
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                mLockAcquiredWallTime = SystemClock.elapsedRealtime();
                mLockAcquiredThreadTime = Debug.threadCpuTimeNanos();
            }
        }
    }
    private void lockForced() {
        mLock.lock();
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                mLockAcquiredWallTime = SystemClock.elapsedRealtime();
                mLockAcquiredThreadTime = Debug.threadCpuTimeNanos();
            }
        }
    }
     void unlock() {
        if (!mLockingEnabled) return;
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                checkLockHoldTime();
            }
        }
        mLock.unlock();
    }
    private void unlockForced() {
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                checkLockHoldTime();
            }
        }
        mLock.unlock();
    }
    private void checkLockHoldTime() {
        long elapsedTime = SystemClock.elapsedRealtime();
        long lockedTime = elapsedTime - mLockAcquiredWallTime;
        if (lockedTime < LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT &&
                !Log.isLoggable(TAG, Log.VERBOSE) &&
                (elapsedTime - mLastLockMessageTime) < LOCK_WARNING_WINDOW_IN_MS) {
            return;
        }
        if (lockedTime > LOCK_ACQUIRED_WARNING_TIME_IN_MS) {
            int threadTime = (int)
                    ((Debug.threadCpuTimeNanos() - mLockAcquiredThreadTime) / 1000000);
            if (threadTime > LOCK_ACQUIRED_WARNING_THREAD_TIME_IN_MS ||
                    lockedTime > LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT) {
                mLastLockMessageTime = elapsedTime;
                String msg = "lock held on " + mPath + " for " + lockedTime + "ms. Thread time was "
                        + threadTime + "ms";
                if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING_STACK_TRACE) {
                    Log.d(TAG, msg, new Exception());
                } else {
                    Log.d(TAG, msg);
                }
            }
        }
    }
    public void beginTransaction() {
        beginTransactionWithListener(null );
    }
    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        lockForced();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        boolean ok = false;
        try {
            if (mLock.getHoldCount() > 1) {
                if (mInnerTransactionIsSuccessful) {
                    String msg = "Cannot call beginTransaction between "
                            + "calling setTransactionSuccessful and endTransaction";
                    IllegalStateException e = new IllegalStateException(msg);
                    Log.e(TAG, "beginTransaction() failed", e);
                    throw e;
                }
                ok = true;
                return;
            }
            execSQL("BEGIN EXCLUSIVE;");
            mTransactionListener = transactionListener;
            mTransactionIsSuccessful = true;
            mInnerTransactionIsSuccessful = false;
            if (transactionListener != null) {
                try {
                    transactionListener.onBegin();
                } catch (RuntimeException e) {
                    execSQL("ROLLBACK;");
                    throw e;
                }
            }
            ok = true;
        } finally {
            if (!ok) {
                unlockForced();
            }
        }
    }
    public void endTransaction() {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        if (!mLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("no transaction pending");
        }
        try {
            if (mInnerTransactionIsSuccessful) {
                mInnerTransactionIsSuccessful = false;
            } else {
                mTransactionIsSuccessful = false;
            }
            if (mLock.getHoldCount() != 1) {
                return;
            }
            RuntimeException savedException = null;
            if (mTransactionListener != null) {
                try {
                    if (mTransactionIsSuccessful) {
                        mTransactionListener.onCommit();
                    } else {
                        mTransactionListener.onRollback();
                    }
                } catch (RuntimeException e) {
                    savedException = e;
                    mTransactionIsSuccessful = false;
                }
            }
            if (mTransactionIsSuccessful) {
                execSQL(COMMIT_SQL);
            } else {
                try {
                    execSQL("ROLLBACK;");
                    if (savedException != null) {
                        throw savedException;
                    }
                } catch (SQLException e) {
                    if (Config.LOGD) {
                        Log.d(TAG, "exception during rollback, maybe the DB previously "
                                + "performed an auto-rollback");
                    }
                }
            }
        } finally {
            mTransactionListener = null;
            unlockForced();
            if (Config.LOGV) {
                Log.v(TAG, "unlocked " + Thread.currentThread()
                        + ", holdCount is " + mLock.getHoldCount());
            }
        }
    }
    public void setTransactionSuccessful() {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        if (!mLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("no transaction pending");
        }
        if (mInnerTransactionIsSuccessful) {
            throw new IllegalStateException(
                    "setTransactionSuccessful may only be called once per call to beginTransaction");
        }
        mInnerTransactionIsSuccessful = true;
    }
    public boolean inTransaction() {
        return mLock.getHoldCount() > 0;
    }
    public boolean isDbLockedByCurrentThread() {
        return mLock.isHeldByCurrentThread();
    }
    public boolean isDbLockedByOtherThreads() {
        return !mLock.isHeldByCurrentThread() && mLock.isLocked();
    }
    @Deprecated
    public boolean yieldIfContended() {
        return yieldIfContendedHelper(false ,
                -1 );
    }
    public boolean yieldIfContendedSafely() {
        return yieldIfContendedHelper(true , -1 );
    }
    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return yieldIfContendedHelper(true , sleepAfterYieldDelay);
    }
    private boolean yieldIfContendedHelper(boolean checkFullyYielded, long sleepAfterYieldDelay) {
        if (mLock.getQueueLength() == 0) {
            mLockAcquiredWallTime = SystemClock.elapsedRealtime();
            mLockAcquiredThreadTime = Debug.threadCpuTimeNanos();
            return false;
        }
        setTransactionSuccessful();
        SQLiteTransactionListener transactionListener = mTransactionListener;
        endTransaction();
        if (checkFullyYielded) {
            if (this.isDbLockedByCurrentThread()) {
                throw new IllegalStateException(
                        "Db locked more than once. yielfIfContended cannot yield");
            }
        }
        if (sleepAfterYieldDelay > 0) {
            long remainingDelay = sleepAfterYieldDelay;
            while (remainingDelay > 0) {
                try {
                    Thread.sleep(remainingDelay < SLEEP_AFTER_YIELD_QUANTUM ?
                            remainingDelay : SLEEP_AFTER_YIELD_QUANTUM);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                remainingDelay -= SLEEP_AFTER_YIELD_QUANTUM;
                if (mLock.getQueueLength() == 0) {
                    break;
                }
            }
        }
        beginTransactionWithListener(transactionListener);
        return true;
    }
    private final Map<String, SyncUpdateInfo> mSyncUpdateInfo =
            new HashMap<String, SyncUpdateInfo>();
    public Map<String, String> getSyncedTables() {
        synchronized(mSyncUpdateInfo) {
            HashMap<String, String> tables = new HashMap<String, String>();
            for (String table : mSyncUpdateInfo.keySet()) {
                SyncUpdateInfo info = mSyncUpdateInfo.get(table);
                if (info.deletedTable != null) {
                    tables.put(table, info.deletedTable);
                }
            }
            return tables;
        }
    }
    static private class SyncUpdateInfo {
        SyncUpdateInfo(String masterTable, String deletedTable,
                String foreignKey) {
            this.masterTable = masterTable;
            this.deletedTable = deletedTable;
            this.foreignKey = foreignKey;
        }
        String masterTable;
        String deletedTable;
        String foreignKey;
    }
    public interface CursorFactory {
        public Cursor newCursor(SQLiteDatabase db,
                SQLiteCursorDriver masterQuery, String editTable,
                SQLiteQuery query);
    }
    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags) {
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = new SQLiteDatabase(path, factory, flags);
            if (SQLiteDebug.DEBUG_SQL_STATEMENTS) {
                sqliteDatabase.enableSqlTracing(path);
            }
            if (SQLiteDebug.DEBUG_SQL_TIME) {
                sqliteDatabase.enableSqlProfiling(path);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            Log.e(TAG, "Deleting and re-creating corrupt database " + path, e);
            EventLog.writeEvent(EVENT_DB_CORRUPT, path);
            if (!path.equalsIgnoreCase(":memory")) {
                new File(path).delete();
            }
            sqliteDatabase = new SQLiteDatabase(path, factory, flags);
        }
        ActiveDatabases.getInstance().mActiveDatabases.add(
                new WeakReference<SQLiteDatabase>(sqliteDatabase));
        return sqliteDatabase;
    }
    public static SQLiteDatabase openOrCreateDatabase(File file, CursorFactory factory) {
        return openOrCreateDatabase(file.getPath(), factory);
    }
    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) {
        return openDatabase(path, factory, CREATE_IF_NECESSARY);
    }
    public static SQLiteDatabase create(CursorFactory factory) {
        return openDatabase(":memory:", factory, CREATE_IF_NECESSARY);
    }
    public void close() {
        if (!isOpen()) {
            return; 
        }
        lock();
        try {
            closeClosable();
            onAllReferencesReleased();
        } finally {
            unlock();
        }
    }
    private void closeClosable() {
        deallocCachedSqlStatements();
        Iterator<Map.Entry<SQLiteClosable, Object>> iter = mPrograms.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<SQLiteClosable, Object> entry = iter.next();
            SQLiteClosable program = entry.getKey();
            if (program != null) {
                program.onAllReferencesReleasedFromContainer();
            }
        }
    }
    private native void dbclose();
    public int getVersion() {
        SQLiteStatement prog = null;
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        try {
            prog = new SQLiteStatement(this, "PRAGMA user_version;");
            long version = prog.simpleQueryForLong();
            return (int) version;
        } finally {
            if (prog != null) prog.close();
            unlock();
        }
    }
    public void setVersion(int version) {
        execSQL("PRAGMA user_version = " + version);
    }
    public long getMaximumSize() {
        SQLiteStatement prog = null;
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        try {
            prog = new SQLiteStatement(this,
                    "PRAGMA max_page_count;");
            long pageCount = prog.simpleQueryForLong();
            return pageCount * getPageSize();
        } finally {
            if (prog != null) prog.close();
            unlock();
        }
    }
    public long setMaximumSize(long numBytes) {
        SQLiteStatement prog = null;
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        try {
            long pageSize = getPageSize();
            long numPages = numBytes / pageSize;
            if ((numBytes % pageSize) != 0) {
                numPages++;
            }
            prog = new SQLiteStatement(this,
                    "PRAGMA max_page_count = " + numPages);
            long newPageCount = prog.simpleQueryForLong();
            return newPageCount * pageSize;
        } finally {
            if (prog != null) prog.close();
            unlock();
        }
    }
    public long getPageSize() {
        SQLiteStatement prog = null;
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        try {
            prog = new SQLiteStatement(this,
                    "PRAGMA page_size;");
            long size = prog.simpleQueryForLong();
            return size;
        } finally {
            if (prog != null) prog.close();
            unlock();
        }
    }
    public void setPageSize(long numBytes) {
        execSQL("PRAGMA page_size = " + numBytes);
    }
    public void markTableSyncable(String table, String deletedTable) {
        markTableSyncable(table, "_id", table, deletedTable);
    }
    public void markTableSyncable(String table, String foreignKey,
            String updateTable) {
        markTableSyncable(table, foreignKey, updateTable, null);
    }
    private void markTableSyncable(String table, String foreignKey,
            String updateTable, String deletedTable) {
        lock();
        try {
            native_execSQL("SELECT _sync_dirty FROM " + updateTable
                    + " LIMIT 0");
            native_execSQL("SELECT " + foreignKey + " FROM " + table
                    + " LIMIT 0");
        } finally {
            unlock();
        }
        SyncUpdateInfo info = new SyncUpdateInfo(updateTable, deletedTable,
                foreignKey);
        synchronized (mSyncUpdateInfo) {
            mSyncUpdateInfo.put(table, info);
        }
    }
     void rowUpdated(String table, long rowId) {
        SyncUpdateInfo info;
        synchronized (mSyncUpdateInfo) {
            info = mSyncUpdateInfo.get(table);
        }
        if (info != null) {
            execSQL("UPDATE " + info.masterTable
                    + " SET _sync_dirty=1 WHERE _id=(SELECT " + info.foreignKey
                    + " FROM " + table + " WHERE _id=" + rowId + ")");
        }
    }
    public static String findEditTable(String tables) {
        if (!TextUtils.isEmpty(tables)) {
            int spacepos = tables.indexOf(' ');
            int commapos = tables.indexOf(',');
            if (spacepos > 0 && (spacepos < commapos || commapos < 0)) {
                return tables.substring(0, spacepos);
            } else if (commapos > 0 && (commapos < spacepos || spacepos < 0) ) {
                return tables.substring(0, commapos);
            }
            return tables;
        } else {
            throw new IllegalStateException("Invalid tables");
        }
    }
    public SQLiteStatement compileStatement(String sql) throws SQLException {
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        try {
            return new SQLiteStatement(this, sql);
        } finally {
            unlock();
        }
    }
    public Cursor query(boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        return queryWithFactory(null, distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }
    public Cursor queryWithFactory(CursorFactory cursorFactory,
            boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        String sql = SQLiteQueryBuilder.buildQueryString(
                distinct, table, columns, selection, groupBy, having, orderBy, limit);
        return rawQueryWithFactory(
                cursorFactory, sql, selectionArgs, findEditTable(table));
    }
    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        return query(false, table, columns, selection, selectionArgs, groupBy,
                having, orderBy, null );
    }
    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy, String limit) {
        return query(false, table, columns, selection, selectionArgs, groupBy,
                having, orderBy, limit);
    }
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return rawQueryWithFactory(null, sql, selectionArgs, null);
    }
    public Cursor rawQueryWithFactory(
            CursorFactory cursorFactory, String sql, String[] selectionArgs,
            String editTable) {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        long timeStart = 0;
        if (Config.LOGV || mSlowQueryThreshold != -1) {
            timeStart = System.currentTimeMillis();
        }
        SQLiteCursorDriver driver = new SQLiteDirectCursorDriver(this, sql, editTable);
        Cursor cursor = null;
        try {
            cursor = driver.query(
                    cursorFactory != null ? cursorFactory : mFactory,
                    selectionArgs);
        } finally {
            if (Config.LOGV || mSlowQueryThreshold != -1) {
                int count = -1;
                if (cursor != null) {
                    count = cursor.getCount();
                }
                long duration = System.currentTimeMillis() - timeStart;
                if (Config.LOGV || duration >= mSlowQueryThreshold) {
                    Log.v(SQLiteCursor.TAG,
                          "query (" + duration + " ms): " + driver.toString() + ", args are "
                                  + (selectionArgs != null
                                  ? TextUtils.join(",", selectionArgs)
                                  : "<null>")  + ", count is " + count);
                }
            }
        }
        return cursor;
    }
    public Cursor rawQuery(String sql, String[] selectionArgs,
            int initialRead, int maxRead) {
        SQLiteCursor c = (SQLiteCursor)rawQueryWithFactory(
                null, sql, selectionArgs, null);
        c.setLoadStyle(initialRead, maxRead);
        return c;
    }
    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return insertWithOnConflict(table, nullColumnHack, values, CONFLICT_NONE);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + values, e);
            return -1;
        }
    }
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, values, CONFLICT_NONE);
    }
    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        try {
            return insertWithOnConflict(table, nullColumnHack, initialValues,
                    CONFLICT_REPLACE);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + initialValues, e);
            return -1;
        }
    }
    public long replaceOrThrow(String table, String nullColumnHack,
            ContentValues initialValues) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, initialValues,
                CONFLICT_REPLACE);
    }
    public long insertWithOnConflict(String table, String nullColumnHack,
            ContentValues initialValues, int conflictAlgorithm) {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        StringBuilder sql = new StringBuilder(152);
        sql.append("INSERT");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(" INTO ");
        sql.append(table);
        StringBuilder values = new StringBuilder(40);
        Set<Map.Entry<String, Object>> entrySet = null;
        if (initialValues != null && initialValues.size() > 0) {
            entrySet = initialValues.valueSet();
            Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();
            sql.append('(');
            boolean needSeparator = false;
            while (entriesIter.hasNext()) {
                if (needSeparator) {
                    sql.append(", ");
                    values.append(", ");
                }
                needSeparator = true;
                Map.Entry<String, Object> entry = entriesIter.next();
                sql.append(entry.getKey());
                values.append('?');
            }
            sql.append(')');
        } else {
            sql.append("(" + nullColumnHack + ") ");
            values.append("NULL");
        }
        sql.append(" VALUES(");
        sql.append(values);
        sql.append(");");
        lock();
        SQLiteStatement statement = null;
        try {
            statement = compileStatement(sql.toString());
            if (entrySet != null) {
                int size = entrySet.size();
                Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();
                for (int i = 0; i < size; i++) {
                    Map.Entry<String, Object> entry = entriesIter.next();
                    DatabaseUtils.bindObjectToProgram(statement, i + 1, entry.getValue());
                }
            }
            statement.execute();
            long insertedRowId = lastInsertRow();
            if (insertedRowId == -1) {
                Log.e(TAG, "Error inserting " + initialValues + " using " + sql);
            } else {
                if (Config.LOGD && Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Inserting row " + insertedRowId + " from "
                            + initialValues + " using " + sql);
                }
            }
            return insertedRowId;
        } catch (SQLiteDatabaseCorruptException e) {
            onCorruption();
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            unlock();
        }
    }
    public int delete(String table, String whereClause, String[] whereArgs) {
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        SQLiteStatement statement = null;
        try {
            statement = compileStatement("DELETE FROM " + table
                    + (!TextUtils.isEmpty(whereClause)
                    ? " WHERE " + whereClause : ""));
            if (whereArgs != null) {
                int numArgs = whereArgs.length;
                for (int i = 0; i < numArgs; i++) {
                    DatabaseUtils.bindObjectToProgram(statement, i + 1, whereArgs[i]);
                }
            }
            statement.execute();
            return lastChangeCount();
        } catch (SQLiteDatabaseCorruptException e) {
            onCorruption();
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            unlock();
        }
    }
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return updateWithOnConflict(table, values, whereClause, whereArgs, CONFLICT_NONE);
    }
    public int updateWithOnConflict(String table, ContentValues values,
            String whereClause, String[] whereArgs, int conflictAlgorithm) {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(table);
        sql.append(" SET ");
        Set<Map.Entry<String, Object>> entrySet = values.valueSet();
        Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();
        while (entriesIter.hasNext()) {
            Map.Entry<String, Object> entry = entriesIter.next();
            sql.append(entry.getKey());
            sql.append("=?");
            if (entriesIter.hasNext()) {
                sql.append(", ");
            }
        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        SQLiteStatement statement = null;
        try {
            statement = compileStatement(sql.toString());
            int size = entrySet.size();
            entriesIter = entrySet.iterator();
            int bindArg = 1;
            for (int i = 0; i < size; i++) {
                Map.Entry<String, Object> entry = entriesIter.next();
                DatabaseUtils.bindObjectToProgram(statement, bindArg, entry.getValue());
                bindArg++;
            }
            if (whereArgs != null) {
                size = whereArgs.length;
                for (int i = 0; i < size; i++) {
                    statement.bindString(bindArg, whereArgs[i]);
                    bindArg++;
                }
            }
            statement.execute();
            int numChangedRows = lastChangeCount();
            if (Config.LOGD && Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Updated " + numChangedRows + " using " + values + " and " + sql);
            }
            return numChangedRows;
        } catch (SQLiteDatabaseCorruptException e) {
            onCorruption();
            throw e;
        } catch (SQLException e) {
            Log.e(TAG, "Error updating " + values + " using " + sql);
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            unlock();
        }
    }
    public void execSQL(String sql) throws SQLException {
        long timeStart = SystemClock.uptimeMillis();
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        logTimeStat(mLastSqlStatement, timeStart, GET_LOCK_LOG_PREFIX);
        try {
            native_execSQL(sql);
        } catch (SQLiteDatabaseCorruptException e) {
            onCorruption();
            throw e;
        } finally {
            unlock();
        }
        if (sql == COMMIT_SQL) {
            logTimeStat(mLastSqlStatement, timeStart, COMMIT_SQL);
        } else {
            logTimeStat(sql, timeStart, null);
        }
    }
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs == null) {
            throw new IllegalArgumentException("Empty bindArgs");
        }
        long timeStart = SystemClock.uptimeMillis();
        lock();
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
        SQLiteStatement statement = null;
        try {
            statement = compileStatement(sql);
            if (bindArgs != null) {
                int numArgs = bindArgs.length;
                for (int i = 0; i < numArgs; i++) {
                    DatabaseUtils.bindObjectToProgram(statement, i + 1, bindArgs[i]);
                }
            }
            statement.execute();
        } catch (SQLiteDatabaseCorruptException e) {
            onCorruption();
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            unlock();
        }
        logTimeStat(sql, timeStart);
    }
    @Override
    protected void finalize() {
        if (isOpen()) {
            Log.e(TAG, "close() was never explicitly called on database '" +
                    mPath + "' ", mStackTrace);
            closeClosable();
            onAllReferencesReleased();
        }
    }
    private SQLiteDatabase(String path, CursorFactory factory, int flags) {
        if (path == null) {
            throw new IllegalArgumentException("path should not be null");
        }
        mFlags = flags;
        mPath = path;
        mSlowQueryThreshold = SystemProperties.getInt(LOG_SLOW_QUERIES_PROPERTY, -1);
        mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();
        mFactory = factory;
        dbopen(mPath, mFlags);
        if (SQLiteDebug.DEBUG_SQL_CACHE) {
            mTimeOpened = getTime();
        }
        mPrograms = new WeakHashMap<SQLiteClosable,Object>();
        try {
            setLocale(Locale.getDefault());
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to setLocale() when constructing, closing the database", e);
            dbclose();
            if (SQLiteDebug.DEBUG_SQL_CACHE) {
                mTimeClosed = getTime();
            }
            throw e;
        }
    }
    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(System.currentTimeMillis());
    }
    public boolean isReadOnly() {
        return (mFlags & OPEN_READ_MASK) == OPEN_READONLY;
    }
    public boolean isOpen() {
        return mNativeHandle != 0;
    }
    public boolean needUpgrade(int newVersion) {
        return newVersion > getVersion();
    }
    public final String getPath() {
        return mPath;
    }
     void logTimeStat(String sql, long beginMillis) {
        logTimeStat(sql, beginMillis, null);
    }
     void logTimeStat(String sql, long beginMillis, String prefix) {
        mLastSqlStatement = sql;
        int samplePercent;
        long durationMillis = SystemClock.uptimeMillis() - beginMillis;
        if (durationMillis == 0 && prefix == GET_LOCK_LOG_PREFIX) {
            return;
        }
        if (sQueryLogTimeInMillis == 0) {
            sQueryLogTimeInMillis = SystemProperties.getInt("db.db_operation.threshold_ms", 500);
        }
        if (durationMillis >= sQueryLogTimeInMillis) {
            samplePercent = 100;
        } else {;
            samplePercent = (int) (100 * durationMillis / sQueryLogTimeInMillis) + 1;
            if (mRandom.nextInt(100) >= samplePercent) return;
        }
        if (prefix != null) {
            sql = prefix + sql;
        }
        if (sql.length() > QUERY_LOG_SQL_LENGTH) sql = sql.substring(0, QUERY_LOG_SQL_LENGTH);
        String blockingPackage = ActivityThread.currentPackageName();
        if (blockingPackage == null) blockingPackage = "";
        EventLog.writeEvent(
            EVENT_DB_OPERATION,
            getPathForLogs(),
            sql,
            durationMillis,
            blockingPackage,
            samplePercent);
    }
    private String getPathForLogs() {
        if (mPathForLogs != null) {
            return mPathForLogs;
        }
        if (mPath == null) {
            return null;
        }
        if (mPath.indexOf('@') == -1) {
            mPathForLogs = mPath;
        } else {
            mPathForLogs = EMAIL_IN_DB_PATTERN.matcher(mPath).replaceAll("XX@YY");
        }
        return mPathForLogs;
    }
    public void setLocale(Locale locale) {
        lock();
        try {
            native_setLocale(locale.toString(), mFlags);
        } finally {
            unlock();
        }
    }
     void addToCompiledQueries(String sql, SQLiteCompiledSql compiledStatement) {
        if (mMaxSqlCacheSize == 0) {
            if (SQLiteDebug.DEBUG_SQL_CACHE) {
                Log.v(TAG, "|NOT adding_sql_to_cache|" + getPath() + "|" + sql);
            }
            return;
        }
        SQLiteCompiledSql compiledSql = null;
        synchronized(mCompiledQueries) {
            compiledSql = mCompiledQueries.get(sql);
            if (compiledSql != null) {
                return;
            }
            if (mCompiledQueries.size() == mMaxSqlCacheSize) {
                if (++mCacheFullWarnings == MAX_WARNINGS_ON_CACHESIZE_CONDITION) {
                    Log.w(TAG, "Reached MAX size for compiled-sql statement cache for database " +
                            getPath() + "; i.e., NO space for this sql statement in cache: " +
                            sql + ". Please change your sql statements to use '?' for " +
                            "bindargs, instead of using actual values");
                }
            } else {
                mCompiledQueries.put(sql, compiledStatement);
                if (SQLiteDebug.DEBUG_SQL_CACHE) {
                    Log.v(TAG, "|adding_sql_to_cache|" + getPath() + "|" +
                            mCompiledQueries.size() + "|" + sql);
                }
            }
        }
        return;
    }
    private void deallocCachedSqlStatements() {
        synchronized (mCompiledQueries) {
            for (SQLiteCompiledSql compiledSql : mCompiledQueries.values()) {
                compiledSql.releaseSqlStatement();
            }
            mCompiledQueries.clear();
        }
    }
     SQLiteCompiledSql getCompiledStatementForSql(String sql) {
        SQLiteCompiledSql compiledStatement = null;
        boolean cacheHit;
        synchronized(mCompiledQueries) {
            if (mMaxSqlCacheSize == 0) {
                if (SQLiteDebug.DEBUG_SQL_CACHE) {
                    Log.v(TAG, "|cache NOT found|" + getPath());
                }
                return null;
            }
            cacheHit = (compiledStatement = mCompiledQueries.get(sql)) != null;
        }
        if (cacheHit) {
            mNumCacheHits++;
        } else {
            mNumCacheMisses++;
        }
        if (SQLiteDebug.DEBUG_SQL_CACHE) {
            Log.v(TAG, "|cache_stats|" +
                    getPath() + "|" + mCompiledQueries.size() +
                    "|" + mNumCacheHits + "|" + mNumCacheMisses +
                    "|" + cacheHit + "|" + mTimeOpened + "|" + mTimeClosed + "|" + sql);
        }
        return compiledStatement;
    }
    public boolean isInCompiledSqlCache(String sql) {
        synchronized(mCompiledQueries) {
            return mCompiledQueries.containsKey(sql);
        }
    }
    public void purgeFromCompiledSqlCache(String sql) {
        synchronized(mCompiledQueries) {
            mCompiledQueries.remove(sql);
        }
    }
    public void resetCompiledSqlCache() {
        synchronized(mCompiledQueries) {
            mCompiledQueries.clear();
        }
    }
    public synchronized int getMaxSqlCacheSize() {
        return mMaxSqlCacheSize;
    }
    public synchronized void setMaxSqlCacheSize(int cacheSize) {
        if (cacheSize > MAX_SQL_CACHE_SIZE || cacheSize < 0) {
            throw new IllegalStateException("expected value between 0 and " + MAX_SQL_CACHE_SIZE);
        } else if (cacheSize < mMaxSqlCacheSize) {
            throw new IllegalStateException("cannot set cacheSize to a value less than the value " +
                    "set with previous setMaxSqlCacheSize() call.");
        }
        mMaxSqlCacheSize = cacheSize;
    }
    static class ActiveDatabases {
        private static final ActiveDatabases activeDatabases = new ActiveDatabases();
        private HashSet<WeakReference<SQLiteDatabase>> mActiveDatabases =
            new HashSet<WeakReference<SQLiteDatabase>>();
        private ActiveDatabases() {} 
        static ActiveDatabases getInstance() {return activeDatabases;}
    }
     static ArrayList<DbStats> getDbStats() {
        ArrayList<DbStats> dbStatsList = new ArrayList<DbStats>();
        for (WeakReference<SQLiteDatabase> w : ActiveDatabases.getInstance().mActiveDatabases) {
            SQLiteDatabase db = w.get();
            if (db == null || !db.isOpen()) {
                continue;
            }
            int lookasideUsed = db.native_getDbLookaside();
            String path = db.getPath();
            int indx = path.lastIndexOf("/");
            String lastnode = path.substring((indx != -1) ? ++indx : 0);
            ArrayList<Pair<String, String>> attachedDbs = getAttachedDbs(db);
            if (attachedDbs == null) {
                continue;
            }
            for (int i = 0; i < attachedDbs.size(); i++) {
                Pair<String, String> p = attachedDbs.get(i);
                long pageCount = getPragmaVal(db, p.first + ".page_count;");
                String dbName;
                if (i == 0) {
                    dbName = lastnode;
                } else {
                    lookasideUsed = 0;
                    dbName = "  (attached) " + p.first;
                    if (p.second.trim().length() > 0) {
                        int idx = p.second.lastIndexOf("/");
                        dbName += " : " + p.second.substring((idx != -1) ? ++idx : 0);
                    }
                }
                if (pageCount > 0) {
                    dbStatsList.add(new DbStats(dbName, pageCount, db.getPageSize(),
                            lookasideUsed));
                }
            }
        }
        return dbStatsList;
    }
    private static long getPragmaVal(SQLiteDatabase db, String pragma) {
        if (!db.isOpen()) {
            return 0;
        }
        SQLiteStatement prog = null;
        try {
            prog = new SQLiteStatement(db, "PRAGMA " + pragma);
            long val = prog.simpleQueryForLong();
            return val;
        } finally {
            if (prog != null) prog.close();
        }
    }
    private static ArrayList<Pair<String, String>> getAttachedDbs(SQLiteDatabase dbObj) {
        if (!dbObj.isOpen()) {
            return null;
        }
        ArrayList<Pair<String, String>> attachedDbs = new ArrayList<Pair<String, String>>();
        Cursor c = dbObj.rawQuery("pragma database_list;", null);
        while (c.moveToNext()) {
             attachedDbs.add(new Pair<String, String>(c.getString(1), c.getString(2)));
        }
        c.close();
        return attachedDbs;
    }
    private native void dbopen(String path, int flags);
    private native void enableSqlTracing(String path);
    private native void enableSqlProfiling(String path);
     native void native_execSQL(String sql) throws SQLException;
     native void native_setLocale(String loc, int flags);
     native long lastInsertRow();
     native int lastChangeCount();
    private native int native_getDbLookaside();
}
