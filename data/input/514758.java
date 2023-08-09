public class SearchableDictionary extends Activity {
    private TextView mTextView;
    private ListView mListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Intent wordIntent = new Intent(this, WordActivity.class);
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
            finish();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }
    private void showResults(String query) {
        Cursor cursor = managedQuery(DictionaryProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
        if (cursor == null) {
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                                    count, new Object[] {count, query});
            mTextView.setText(countString);
            String[] from = new String[] { DictionaryDatabase.KEY_WORD,
                                           DictionaryDatabase.KEY_DEFINITION };
            int[] to = new int[] { R.id.word,
                                   R.id.definition };
            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor, from, to);
            mListView.setAdapter(words);
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
                                                    String.valueOf(id));
                    wordIntent.setData(data);
                    startActivity(wordIntent);
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
}
