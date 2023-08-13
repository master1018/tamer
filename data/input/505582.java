public class SearchInvoke extends Activity
{  
    Button mStartSearch;
    Spinner mMenuMode;
    EditText mQueryPrefill;
    EditText mQueryAppData;
    final static int MENUMODE_SEARCH_KEY = 0;
    final static int MENUMODE_MENU_ITEM = 1;
    final static int MENUMODE_TYPE_TO_SEARCH = 2;
    final static int MENUMODE_DISABLED = 3;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_invoke);
        mStartSearch = (Button) findViewById(R.id.btn_start_search);
        mMenuMode = (Spinner) findViewById(R.id.spinner_menu_mode);
        mQueryPrefill = (EditText) findViewById(R.id.txt_query_prefill);
        mQueryAppData = (EditText) findViewById(R.id.txt_query_appdata);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            this, R.array.search_menuModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMenuMode.setAdapter(adapter);
        mMenuMode.setOnItemSelectedListener(
            new OnItemSelectedListener() {
                public void onItemSelected(
                        AdapterView<?> parent, View view, int position, long id) {
                    if (position == MENUMODE_TYPE_TO_SEARCH) {
                        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
                    } else {
                        setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {
                    setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
                }
            });
        mStartSearch.setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    onSearchRequested();
                }
            });
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item;
        menu.removeItem(0);
        menu.removeItem(1);
        switch (mMenuMode.getSelectedItemPosition())
        {
        case MENUMODE_SEARCH_KEY:
            item = menu.add( 0, 0, 0, "(Search Key)");
            break;
        case MENUMODE_MENU_ITEM:
            item = menu.add( 0, 0, 0, "Search");
            item.setAlphabeticShortcut(SearchManager.MENU_KEY);
            break;
        case MENUMODE_TYPE_TO_SEARCH:
            item = menu.add( 0, 0, 0, "(Type-To-Search)");
            break;
        case MENUMODE_DISABLED:
            item = menu.add( 0, 0, 0, "(Disabled)");
            break;
        }
        item = menu.add(0, 1, 0, "Clear History");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 0:
            switch (mMenuMode.getSelectedItemPosition()) {
            case MENUMODE_SEARCH_KEY:
                new AlertDialog.Builder(this)
                    .setMessage("To invoke search, dismiss this dialog and press the search key" +
                                " (F5 on the simulator).")
                    .setPositiveButton("OK", null)
                    .show();
                break;
            case MENUMODE_MENU_ITEM:
                onSearchRequested();
                break;
            case MENUMODE_TYPE_TO_SEARCH:
                new AlertDialog.Builder(this)
                    .setMessage("To invoke search, dismiss this dialog and start typing.")
                    .setPositiveButton("OK", null)
                    .show();
                break;
            case MENUMODE_DISABLED:
                new AlertDialog.Builder(this)
                    .setMessage("You have disabled search.")
                    .setPositiveButton("OK", null)
                    .show();
                break;
            }
            break;
        case 1:
            clearSearchHistory();
            break;
        }
         return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSearchRequested() {
        if (mMenuMode.getSelectedItemPosition() == MENUMODE_DISABLED) {
            return false;
        }
        final String queryPrefill = mQueryPrefill.getText().toString();
        Bundle appDataBundle = null;
        final String queryAppDataString = mQueryAppData.getText().toString();
        if (queryAppDataString != null) {
            appDataBundle = new Bundle();
            appDataBundle.putString("demo_key", queryAppDataString);
        }
        startSearch(queryPrefill, false, appDataBundle, false); 
        return true;
    }
    private void clearSearchHistory() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE);
        suggestions.clearHistory();
    }
}
