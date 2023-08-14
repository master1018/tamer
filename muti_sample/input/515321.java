public class CalendarCache {
    private static final String TAG = "CalendarCache";
    public static final String DATABASE_NAME = "CalendarCache";
    public static final String KEY_TIMEZONE_DATABASE_VERSION = "timezoneDatabaseVersion";
    public static final String DEFAULT_TIMEZONE_DATABASE_VERSION = "2009s";
    private static final String COLUMN_NAME_ID = "_id";
    private static final String COLUMN_NAME_KEY = "key";
    private static final String COLUMN_NAME_VALUE = "value";
    private static final String[] sProjection = {
        COLUMN_NAME_KEY,
        COLUMN_NAME_VALUE
    };
    private static final int COLUMN_INDEX_KEY = 0;
    private static final int COLUMN_INDEX_VALUE = 1;
    private final SQLiteOpenHelper mOpenHelper;
    public static class CacheException extends Exception {
        public CacheException() {
        }
        public CacheException(String detailMessage) {
            super(detailMessage);
        }
    }
    public CalendarCache(SQLiteOpenHelper openHelper) {
        mOpenHelper = openHelper;
    }
    public void writeTimezoneDatabaseVersion(String timezoneDatabaseVersion) throws CacheException {
        writeData(KEY_TIMEZONE_DATABASE_VERSION,
                timezoneDatabaseVersion);
    }
    public String readTimezoneDatabaseVersion() throws CacheException {
        return readData(KEY_TIMEZONE_DATABASE_VERSION);
    }
    public void writeData(String key, String value) throws CacheException {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            writeDataLocked(db, key, value);
            db.setTransactionSuccessful();
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.i(TAG, "Wrote (key, value) = [ " + key + ", " + value + "] ");
            }
        } finally {
            db.endTransaction();
        }
    }
    protected void writeDataLocked(SQLiteDatabase db, String key, String value)
            throws CacheException {
        if (null == db) {
            throw new CacheException("Database cannot be null");
        }
        if (null == key) {
            throw new CacheException("Cannot use null key for write");
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, key.hashCode());
        values.put(COLUMN_NAME_KEY, key);
        values.put(COLUMN_NAME_VALUE, value);
        db.replace(DATABASE_NAME, null , values);
    }
    public String readData(String key) throws CacheException {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return readDataLocked(db, key);
    }
    protected String readDataLocked(SQLiteDatabase db, String key) throws CacheException {
        if (null == db) {
            throw new CacheException("Database cannot be null");
        }
        if (null == key) {
            throw new CacheException("Cannot use null key for read");
        }
        String rowValue = null;
        Cursor cursor = db.query(DATABASE_NAME, sProjection,
                COLUMN_NAME_KEY + "=?", new String[] { key }, null, null, null);
        try {
            if (cursor.moveToNext()) {
                rowValue = cursor.getString(COLUMN_INDEX_VALUE);
            }
            else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.i(TAG, "Could not find key = [ " + key + " ]");
                }
            }
        } finally {
            cursor.close();
            cursor = null;
        }
        return rowValue;
    }
}
