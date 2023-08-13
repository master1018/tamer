public class SlowSuggestionProvider extends ContentProvider {
    private static final String TAG = SlowSuggestionProvider.class.getSimpleName();
    private static final String[] COLUMNS = {
        "_id",
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2,
        SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA,
    };
    @Override
    public boolean onCreate() {
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query(" + uri + ")");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            Log.d(TAG, "Interrupted");
        }
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for (int i = 0; i < 3; i++) {
            cursor.addRow(new Object[]{
                i,
                "Slow suggestion " + i,
                "This suggestion takes a long time to appear",
                Intent.ACTION_VIEW,
                "content:
            });
        }
        return cursor;
    }
    @Override
    public String getType(Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
