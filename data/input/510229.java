public class OpenSearchSearchEngine implements SearchEngine {
    private static final String TAG = "OpenSearchSearchEngine";
    private static final String USER_AGENT = "Android/1.0";
    private static final int HTTP_TIMEOUT_MS = 1000;
    private static final String HTTP_TIMEOUT = "http.connection-manager.timeout";
    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_QUERY = 1;
    private static final int COLUMN_INDEX_ICON = 2;
    private static final int COLUMN_INDEX_TEXT_1 = 3;
    private static final int COLUMN_INDEX_TEXT_2 = 4;
    private static final String[] COLUMNS = new String[] {
        "_id",
        SearchManager.SUGGEST_COLUMN_QUERY,
        SearchManager.SUGGEST_COLUMN_ICON_1,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2,
    };
    private static final String[] COLUMNS_WITHOUT_DESCRIPTION = new String[] {
        "_id",
        SearchManager.SUGGEST_COLUMN_QUERY,
        SearchManager.SUGGEST_COLUMN_ICON_1,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
    };
    private final SearchEngineInfo mSearchEngineInfo;
    private final AndroidHttpClient mHttpClient;
    public OpenSearchSearchEngine(Context context, SearchEngineInfo searchEngineInfo) {
        mSearchEngineInfo = searchEngineInfo;
        mHttpClient = AndroidHttpClient.newInstance(USER_AGENT);
        HttpParams params = mHttpClient.getParams();
        params.setLongParameter(HTTP_TIMEOUT, HTTP_TIMEOUT_MS);
    }
    public String getName() {
        return mSearchEngineInfo.getName();
    }
    public CharSequence getLabel() {
        return mSearchEngineInfo.getLabel();
    }
    public void startSearch(Context context, String query, Bundle appData, String extraData) {
        String uri = mSearchEngineInfo.getSearchUriForQuery(query);
        if (uri == null) {
            Log.e(TAG, "Unable to get search URI for " + mSearchEngineInfo);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage(context.getPackageName());
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(SearchManager.QUERY, query);
            if (appData != null) {
                intent.putExtra(SearchManager.APP_DATA, appData);
            }
            if (extraData != null) {
                intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
            }
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            context.startActivity(intent);
        }
    }
    public Cursor getSuggestions(Context context, String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        if (!isNetworkConnected(context)) {
            Log.i(TAG, "Not connected to network.");
            return null;
        }
        String suggestUri = mSearchEngineInfo.getSuggestUriForQuery(query);
        if (TextUtils.isEmpty(suggestUri)) {
            return null;
        }
        try {
            String content = readUrl(suggestUri);
            if (content == null) return null;
            JSONArray results = new JSONArray(content);
            JSONArray suggestions = results.getJSONArray(1);
            JSONArray descriptions = null;
            if (results.length() > 2) {
                descriptions = results.getJSONArray(2);
                if (descriptions.length() == 0) {
                    descriptions = null;
                }
            }
            return new SuggestionsCursor(suggestions, descriptions);
        } catch (JSONException e) {
            Log.w(TAG, "Error", e);
        }
        return null;
    }
    public String readUrl(String url) {
        try {
            HttpGet method = new HttpGet(url);
            HttpResponse response = mHttpClient.execute(method);
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                Log.i(TAG, "Suggestion request failed");
                return null;
            }
        } catch (IOException e) {
            Log.w(TAG, "Error", e);
            return null;
        }
    }
    public boolean supportsSuggestions() {
        return mSearchEngineInfo.supportsSuggestions();
    }
    public void close() {
        mHttpClient.close();
    }
    public boolean supportsVoiceSearch() {
        return getName().equals(SearchEngine.GOOGLE);
    }
    private boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }
    private NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }
    private static class SuggestionsCursor extends AbstractCursor {
        private final JSONArray mSuggestions;
        private final JSONArray mDescriptions;
        public SuggestionsCursor(JSONArray suggestions, JSONArray descriptions) {
            mSuggestions = suggestions;
            mDescriptions = descriptions;
        }
        @Override
        public int getCount() {
            return mSuggestions.length();
        }
        @Override
        public String[] getColumnNames() {
            return (mDescriptions != null ? COLUMNS : COLUMNS_WITHOUT_DESCRIPTION);
        }
        @Override
        public String getString(int column) {
            if (mPos != -1) {
                if ((column == COLUMN_INDEX_QUERY) || (column == COLUMN_INDEX_TEXT_1)) {
                    try {
                        return mSuggestions.getString(mPos);
                    } catch (JSONException e) {
                        Log.w(TAG, "Error", e);
                    }
                } else if (column == COLUMN_INDEX_TEXT_2) {
                    try {
                        return mDescriptions.getString(mPos);
                    } catch (JSONException e) {
                        Log.w(TAG, "Error", e);
                    }
                } else if (column == COLUMN_INDEX_ICON) {
                    return String.valueOf(R.drawable.magnifying_glass);
                }
            }
            return null;
        }
        @Override
        public double getDouble(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public float getFloat(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public int getInt(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public long getLong(int column) {
            if (column == COLUMN_INDEX_ID) {
                return mPos;        
            }
            throw new UnsupportedOperationException();
        }
        @Override
        public short getShort(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean isNull(int column) {
            throw new UnsupportedOperationException();
        }
    }
    @Override
    public String toString() {
        return "OpenSearchSearchEngine{" + mSearchEngineInfo + "}";
    }
}
