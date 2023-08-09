public class SearchQueryResults extends Activity
{  
    TextView mQueryText;
    TextView mAppDataText;
    TextView mDeliveredByText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_query_results);
        mQueryText = (TextView) findViewById(R.id.txt_query);
        mAppDataText = (TextView) findViewById(R.id.txt_appdata);
        mDeliveredByText = (TextView) findViewById(R.id.txt_deliveredby);
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent, "onCreate()");
        }
        else {
            mDeliveredByText.setText("onCreate(), but no ACTION_SEARCH intent");
        }
    }
    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent, "onNewIntent()");
        }
        else {
            mDeliveredByText.setText("onNewIntent(), but no ACTION_SEARCH intent");
        }
    }
    private void doSearchQuery(final Intent queryIntent, final String entryPoint) {
        final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
        mQueryText.setText(queryString);
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE);
        suggestions.saveRecentQuery(queryString, null);
        final Bundle appData = queryIntent.getBundleExtra(SearchManager.APP_DATA);
        if (appData == null) {
            mAppDataText.setText("<no app data bundle>");
        }
        if (appData != null) {
            String testStr = appData.getString("demo_key");
            mAppDataText.setText((testStr == null) ? "<no app data>" : testStr);
        }
        mDeliveredByText.setText(entryPoint);
    }
}
