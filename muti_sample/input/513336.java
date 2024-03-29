public class PartialSuggestionProvider extends ContentProvider {
    private static final String TAG = PartialSuggestionProvider.class.getSimpleName();
    private static final int MSG_COMPLETE = 1;
    private MutableMatrixCursor mCursor;
    private int mType = -1;
    private static final String[] COLUMNS = {
        "_id",
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2,
        SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA,
    };
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_COMPLETE:
                mCursor.getExtras().putBoolean(SearchManager.CURSOR_EXTRA_KEY_IN_PROGRESS, false);
                addRows(mCursor, false);
                mCursor.notifyChange();
            }
        }
    };
    private static class MutableMatrixCursor extends MatrixCursor {
        Bundle mBundle;
        MutableMatrixCursor(String[] columns) {
            super(columns);
        }
        @Override
        public Bundle getExtras() {
            if (mBundle == null) mBundle = new Bundle();
            return mBundle;
        }
        public void notifyChange() {
            onChange(false);
        }
    }
    @Override
    public boolean onCreate() {
        return true;
    }
    private void addRows(MatrixCursor cursor, boolean partial) {
        for (int i = 0; i < 3; i++) {
            cursor.addRow(new Object[]{
                i,
                (partial? "Partial" : "Final ") + " suggestion " + i,
                "This is a suggestion",
                Intent.ACTION_VIEW,
                "content:
            });
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query(" + uri + ")");
        mType = (mType + 1) % 3;
        if (mType == 0) {
            Log.d(TAG, "returning null cursor");
            return null;
        }
        MutableMatrixCursor cursor = new MutableMatrixCursor(COLUMNS);
        if (mType == 1) {
            addRows(cursor, true);
        } else {
            Log.d(TAG, "returning empty cursor");
        }
        cursor.getExtras().putBoolean(SearchManager.CURSOR_EXTRA_KEY_IN_PROGRESS, true);
        mCursor = cursor;
        mHandler.removeMessages(MSG_COMPLETE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_COMPLETE), 2000);
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
