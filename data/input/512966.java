public class ADNList extends ListActivity {
    protected static final String TAG = "ADNList";
    protected static final boolean DBG = false;
    private static final String[] COLUMN_NAMES = new String[] {
        "name",
        "number",
        "emails"
    };
    protected static final int NAME_COLUMN = 0;
    protected static final int NUMBER_COLUMN = 1;
    protected static final int EMAILS_COLUMN = 2;
    private static final int[] VIEW_NAMES = new int[] {
        android.R.id.text1,
        android.R.id.text2
    };
    protected static final int QUERY_TOKEN = 0;
    protected static final int INSERT_TOKEN = 1;
    protected static final int UPDATE_TOKEN = 2;
    protected static final int DELETE_TOKEN = 3;
    protected QueryHandler mQueryHandler;
    protected CursorAdapter mCursorAdapter;
    protected Cursor mCursor = null;
    private TextView mEmptyText;
    protected int mInitialSelection = -1;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.adn_list);
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mQueryHandler = new QueryHandler(getContentResolver());
    }
    @Override
    protected void onResume() {
        super.onResume();
        query();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mCursor != null) {
            mCursor.deactivate();
        }
    }
    protected Uri resolveIntent() {
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Uri.parse("content:
        }
        return intent.getData();
    }
    private void query() {
        Uri uri = resolveIntent();
        if (DBG) log("query: starting an async query");
        mQueryHandler.startQuery(QUERY_TOKEN, null, uri, COLUMN_NAMES,
                null, null, null);
        displayProgress(true);
    }
    private void reQuery() {
        query();
    }
    private void setAdapter() {
        if (mCursorAdapter == null) {
            mCursorAdapter = newAdapter();
            setListAdapter(mCursorAdapter);
        } else {
            mCursorAdapter.changeCursor(mCursor);
        }
        if (mInitialSelection >=0 && mInitialSelection < mCursorAdapter.getCount()) {
            setSelection(mInitialSelection);
            getListView().setFocusableInTouchMode(true);
            boolean gotfocus = getListView().requestFocus();
        }
    }
    protected CursorAdapter newAdapter() {
        return new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2,
                    mCursor, COLUMN_NAMES, VIEW_NAMES);
    }
    private void displayProgress(boolean flag) {
        if (DBG) log("displayProgress: " + flag);
        mEmptyText.setText(flag ? R.string.simContacts_emptyLoading: R.string.simContacts_empty);
        getWindow().setFeatureInt(
                Window.FEATURE_INDETERMINATE_PROGRESS,
                flag ? PROGRESS_VISIBILITY_ON : PROGRESS_VISIBILITY_OFF);
    }
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            if (DBG) log("onQueryComplete: cursor.count=" + c.getCount());
            mCursor = c;
            setAdapter();
            displayProgress(false);
        }
        @Override
        protected void onInsertComplete(int token, Object cookie,
                                        Uri uri) {
            if (DBG) log("onInsertComplete: requery");
            reQuery();
        }
        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if (DBG) log("onUpdateComplete: requery");
            reQuery();
        }
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            if (DBG) log("onDeleteComplete: requery");
            reQuery();
        }
    }
    protected void log(String msg) {
        Log.d(TAG, "[ADNList] " + msg);
    }
}
