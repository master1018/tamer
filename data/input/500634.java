public class SQLiteStatement extends SQLiteProgram
{
     SQLiteStatement(SQLiteDatabase db, String sql) {
        super(db, sql);
    }
    public void execute() {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        long timeStart = SystemClock.uptimeMillis();
        mDatabase.lock();
        acquireReference();
        try {
            native_execute();
            mDatabase.logTimeStat(mSql, timeStart);
        } finally {
            releaseReference();
            mDatabase.unlock();
        }
    }
    public long executeInsert() {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        long timeStart = SystemClock.uptimeMillis();
        mDatabase.lock();
        acquireReference();
        try {
            native_execute();
            mDatabase.logTimeStat(mSql, timeStart);
            return (mDatabase.lastChangeCount() > 0) ? mDatabase.lastInsertRow() : -1;
        } finally {
            releaseReference();
            mDatabase.unlock();
        }
    }
    public long simpleQueryForLong() {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        long timeStart = SystemClock.uptimeMillis();
        mDatabase.lock();
        acquireReference();
        try {
            long retValue = native_1x1_long();
            mDatabase.logTimeStat(mSql, timeStart);
            return retValue;
        } finally {
            releaseReference();
            mDatabase.unlock();
        }
    }
    public String simpleQueryForString() {
        if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
        long timeStart = SystemClock.uptimeMillis();
        mDatabase.lock();
        acquireReference();
        try {
            String retValue = native_1x1_string();
            mDatabase.logTimeStat(mSql, timeStart);
            return retValue;
        } finally {
            releaseReference();
            mDatabase.unlock();
        }
    }
    private final native void native_execute();
    private final native long native_1x1_long();
    private final native String native_1x1_string();
}
