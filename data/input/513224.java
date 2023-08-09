public class CallLogProvider extends ContentProvider {
    private static final int CALLS = 1;
    private static final int CALLS_ID = 2;
    private static final int CALLS_FILTER = 3;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls", CALLS);
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls/#", CALLS_ID);
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls/filter
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        return ContactsDatabaseHelper.getInstance(context);
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = sURIMatcher.match(uri);
        switch (match) {
            case CALLS: {
                qb.setTables("calls");
                qb.setProjectionMap(sCallsProjectionMap);
                break;
            }
            case CALLS_ID: {
                qb.setTables("calls");
                qb.setProjectionMap(sCallsProjectionMap);
                qb.appendWhere("calls._id=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            }
            case CALLS_FILTER: {
                qb.setTables("calls");
                qb.setProjectionMap(sCallsProjectionMap);
                String phoneNumber = uri.getPathSegments().get(2);
                qb.appendWhere("PHONE_NUMBERS_EQUAL(number, ");
                qb.appendWhereEscapeString(phoneNumber);
                qb.appendWhere(mUseStrictPhoneNumberComparation ? ", 1)" : ", 0)");
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), CallLog.CONTENT_URI);
        }
        return c;
    }
    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case CALLS:
                return Calls.CONTENT_TYPE;
            case CALLS_ID:
                return Calls.CONTENT_ITEM_TYPE;
            case CALLS_FILTER:
                return Calls.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = mCallsInserter.insert(values);
        if (rowId > 0) {
            notifyChange();
            return ContentUris.withAppendedId(uri, rowId);
        }
        return null;
    }
    @Override
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String where;
        final int matchedUriId = sURIMatcher.match(url);
        switch (matchedUriId) {
            case CALLS:
                where = selection;
                break;
            case CALLS_ID:
                where = DatabaseUtils.concatenateWhere(selection, Calls._ID + "="
                        + url.getPathSegments().get(1));
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }
        int count = db.update(Tables.CALLS, values, where, selectionArgs);
        if (count > 0) {
            notifyChange();
        }
        return count;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        switch (matchedUriId) {
            case CALLS:
                int count = db.delete(Tables.CALLS, selection, selectionArgs);
                if (count > 0) {
                    notifyChange();
                }
                return count;
            default:
                throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
        }
    }
    protected void notifyChange() {
        getContext().getContentResolver().notifyChange(CallLog.CONTENT_URI, null,
                false );
    }
}
