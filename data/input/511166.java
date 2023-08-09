public class TrackerProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri
            .parse("content:
    private static final String DB_NAME = "tracking.db";
    private static final String TABLE_NAME = "tracking";
    private static final int DB_VERSION = 1;
    private static final String LOG_TAG = "TrackerProvider";
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(String.format("CREATE TABLE %s (", TABLE_NAME));
            TrackerEntry.buildCreationString(queryBuilder);
            queryBuilder.append(");");
            db.execSQL(queryBuilder.toString());
            db.setVersion(DB_VERSION);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion
                            + " to " + newVersion
                            + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    private DatabaseHelper mOpenHelper;
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int result = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri addedUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(addedUri, null);
            return addedUri;
        }
        return null;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
