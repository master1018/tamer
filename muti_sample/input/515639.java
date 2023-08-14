@TestTargetClass(ContentProvider.class)
public class DummyProvider extends ContentProvider {
    private static final String MOCK_OPERATION = "mockOperation";
    private static final String DATABASE_NAME = "dummy.db";
    private static final String NAME_VALUE_TABLE = "name_value";
    private DatabaseHelper mDbHelper;
    private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int MATCH_NAME_VALUE      = 1;
    private static final int MATCH_MOCK_OPERATION  = 2;
    public static final long MOCK_OPERATION_SLEEP_TIME = 2000;
    public static final String AUTHORITY = "android.content.cts.dummyprovider";
    public static final Uri CONTENT_URI = Uri.parse("content:
    public static final Uri CONTENT_URI_MOCK_OPERATION = Uri.withAppendedPath(CONTENT_URI,
            MOCK_OPERATION);
    public static final String _ID   = "_id";
    public static final String NAME  = "name";
    public static final String VALUE = "value";
    static {
        sMatcher.addURI(AUTHORITY, null, MATCH_NAME_VALUE);
        sMatcher.addURI(AUTHORITY, MOCK_OPERATION, MATCH_MOCK_OPERATION);
    }
    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }
    private class DatabaseHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NAME_VALUE_TABLE + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + NAME + " TEXT," + VALUE + " TEXT"+ ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tbName = getTableName(uri);
        if (tbName == null) {
            return null;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.insert(tbName, VALUE, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String tbName = getTableName(uri);
        if (tbName == null) {
            return null;
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(tbName, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    private String getTableName(Uri uri) {
        switch (sMatcher.match(uri)) {
            case MATCH_NAME_VALUE:
                return NAME_VALUE_TABLE;
            case MATCH_MOCK_OPERATION:
                doMockOperation();
                return null;
            default:
                throw new UnsupportedOperationException();
        }
    }
    private void doMockOperation() {
        long time = System.currentTimeMillis();
        long target = time + MOCK_OPERATION_SLEEP_TIME;
        while (time < target) {
            try {
                Thread.sleep(target - time);
            } catch (InterruptedException ignored) {
            }
            time = System.currentTimeMillis();
        }
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tbName = getTableName(uri);
        if (tbName == null) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.update(tbName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tbName = getTableName(uri);
        if (tbName == null) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(tbName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
