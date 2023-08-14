public class SyncStateContentProviderHelper {
    private static final String SELECT_BY_ACCOUNT =
            SyncStateContract.Columns.ACCOUNT_NAME + "=? AND "
                    + SyncStateContract.Columns.ACCOUNT_TYPE + "=?";
    private static final String SYNC_STATE_TABLE = "_sync_state";
    private static final String SYNC_STATE_META_TABLE = "_sync_state_metadata";
    private static final String SYNC_STATE_META_VERSION_COLUMN = "version";
    private static long DB_VERSION = 1;
    private static final String[] ACCOUNT_PROJECTION =
            new String[]{SyncStateContract.Columns.ACCOUNT_NAME,
                    SyncStateContract.Columns.ACCOUNT_TYPE};
    public static final String PATH = "syncstate";
    private static final String QUERY_COUNT_SYNC_STATE_ROWS =
            "SELECT count(*)"
                    + " FROM " + SYNC_STATE_TABLE
                    + " WHERE " + SyncStateContract.Columns._ID + "=?";
    public void createDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + SYNC_STATE_TABLE);
        db.execSQL("CREATE TABLE " + SYNC_STATE_TABLE + " ("
                + SyncStateContract.Columns._ID + " INTEGER PRIMARY KEY,"
                + SyncStateContract.Columns.ACCOUNT_NAME + " TEXT NOT NULL,"
                + SyncStateContract.Columns.ACCOUNT_TYPE + " TEXT NOT NULL,"
                + SyncStateContract.Columns.DATA + " TEXT,"
                + "UNIQUE(" + SyncStateContract.Columns.ACCOUNT_NAME + ", "
                + SyncStateContract.Columns.ACCOUNT_TYPE + "));");
        db.execSQL("DROP TABLE IF EXISTS " + SYNC_STATE_META_TABLE);
        db.execSQL("CREATE TABLE " + SYNC_STATE_META_TABLE + " ("
                + SYNC_STATE_META_VERSION_COLUMN + " INTEGER);");
        ContentValues values = new ContentValues();
        values.put(SYNC_STATE_META_VERSION_COLUMN, DB_VERSION);
        db.insert(SYNC_STATE_META_TABLE, SYNC_STATE_META_VERSION_COLUMN, values);
    }
    public void onDatabaseOpened(SQLiteDatabase db) {
        long version = DatabaseUtils.longForQuery(db,
                "SELECT " + SYNC_STATE_META_VERSION_COLUMN + " FROM " + SYNC_STATE_META_TABLE,
                null);
        if (version != DB_VERSION) {
            createDatabase(db);
        }
    }
    public Cursor query(SQLiteDatabase db, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return db.query(SYNC_STATE_TABLE, projection, selection, selectionArgs,
                null, null, sortOrder);
    }
    public long insert(SQLiteDatabase db, ContentValues values) {
        return db.replace(SYNC_STATE_TABLE, SyncStateContract.Columns.ACCOUNT_NAME, values);
    }
    public int delete(SQLiteDatabase db, String userWhere, String[] whereArgs) {
        return db.delete(SYNC_STATE_TABLE, userWhere, whereArgs);
    }
    public int update(SQLiteDatabase db, ContentValues values,
            String selection, String[] selectionArgs) {
        return db.update(SYNC_STATE_TABLE, values, selection, selectionArgs);
    }
    public int update(SQLiteDatabase db, long rowId, Object data) {
        if (DatabaseUtils.longForQuery(db, QUERY_COUNT_SYNC_STATE_ROWS,
                new String[]{Long.toString(rowId)}) < 1) {
            return 0;
        }
        db.execSQL("UPDATE " + SYNC_STATE_TABLE
                + " SET " + SyncStateContract.Columns.DATA + "=?"
                + " WHERE " + SyncStateContract.Columns._ID + "=" + rowId,
                new Object[]{data});
        return 1;
    }
    public void onAccountsChanged(SQLiteDatabase db, Account[] accounts) {
        Cursor c = db.query(SYNC_STATE_TABLE, ACCOUNT_PROJECTION, null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                final String accountName = c.getString(0);
                final String accountType = c.getString(1);
                Account account = new Account(accountName, accountType);
                if (!ArrayUtils.contains(accounts, account)) {
                    db.delete(SYNC_STATE_TABLE, SELECT_BY_ACCOUNT,
                            new String[]{accountName, accountType});
                }
            }
        } finally {
            c.close();
        }
    }
}