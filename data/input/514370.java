public class SearchRecentSuggestions {
    private static final String LOG_TAG = "SearchSuggestions";
    private static final int DBG_SUGGESTION_TIMESTAMPS = 0;
    private static class SuggestionColumns implements BaseColumns {
        public static final String DISPLAY1 = "display1";
        public static final String DISPLAY2 = "display2";
        public static final String QUERY = "query";
        public static final String DATE = "date";
    }
    public static final String[] QUERIES_PROJECTION_1LINE = new String[] {
        SuggestionColumns._ID, 
        SuggestionColumns.DATE,
        SuggestionColumns.QUERY, 
        SuggestionColumns.DISPLAY1,
    };
    public static final String[] QUERIES_PROJECTION_2LINE = new String[] {
        SuggestionColumns._ID, 
        SuggestionColumns.DATE,
        SuggestionColumns.QUERY, 
        SuggestionColumns.DISPLAY1,
        SuggestionColumns.DISPLAY2,
    };
    public static final int QUERIES_PROJECTION_DATE_INDEX = 1;
    public static final int QUERIES_PROJECTION_QUERY_INDEX = 2;
    public static final int QUERIES_PROJECTION_DISPLAY1_INDEX = 3;
    public static final int QUERIES_PROJECTION_DISPLAY2_INDEX = 4;  
    private static final String[] TRUNCATE_HISTORY_PROJECTION = new String[] {
        SuggestionColumns._ID, SuggestionColumns.DATE
    };
    private static final int MAX_HISTORY_COUNT = 250;
    private Context mContext;
    private String mAuthority;
    private boolean mTwoLineDisplay;
    private Uri mSuggestionsUri;
    private String[] mQueriesProjection;
    public SearchRecentSuggestions(Context context, String authority, int mode) {
        if (TextUtils.isEmpty(authority) || 
                ((mode & SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES) == 0)) {
            throw new IllegalArgumentException();
        }
        mTwoLineDisplay = (0 != (mode & SearchRecentSuggestionsProvider.DATABASE_MODE_2LINES));
        mContext = context;
        mAuthority = new String(authority);
        mSuggestionsUri = Uri.parse("content:
        if (mTwoLineDisplay) {
            mQueriesProjection = QUERIES_PROJECTION_2LINE;
        } else {
            mQueriesProjection = QUERIES_PROJECTION_1LINE;
        }
    }
    public void saveRecentQuery(String queryString, String line2) {
        if (TextUtils.isEmpty(queryString)) {
            return;
        }
        if (!mTwoLineDisplay && !TextUtils.isEmpty(line2)) {
            throw new IllegalArgumentException();
        }
        ContentResolver cr = mContext.getContentResolver();
        long now = System.currentTimeMillis();
        try {
            ContentValues values = new ContentValues();
            values.put(SuggestionColumns.DISPLAY1, queryString);
            if (mTwoLineDisplay) {
                values.put(SuggestionColumns.DISPLAY2, line2);
            }
            values.put(SuggestionColumns.QUERY, queryString);
            values.put(SuggestionColumns.DATE, now);
            cr.insert(mSuggestionsUri, values);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "saveRecentQuery", e);
        }
        truncateHistory(cr, MAX_HISTORY_COUNT);
    }
    public void clearHistory() {
        ContentResolver cr = mContext.getContentResolver();
        truncateHistory(cr, 0);
    }
    protected void truncateHistory(ContentResolver cr, int maxEntries) {
        if (maxEntries < 0) {
            throw new IllegalArgumentException();
        }
        try {
            String selection = null;
            if (maxEntries > 0) {
                selection = "_id IN " +
                        "(SELECT _id FROM suggestions" +
                        " ORDER BY " + SuggestionColumns.DATE + " DESC" +
                        " LIMIT -1 OFFSET " + String.valueOf(maxEntries) + ")";
            }
            cr.delete(mSuggestionsUri, selection, null);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "truncateHistory", e);
        }
    }
}
