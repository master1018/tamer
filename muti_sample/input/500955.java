public class ApplicationLauncher extends ListActivity {
    private static final String TAG = "ApplicationLauncher";
    private Cursor mCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_MAIN.equals(action)) {
            Uri contentUri = intent.getData();
            launchApplication(contentUri);
            finish();
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showSearchResults(query);
        }
    }
    private void showSearchResults(String query) {
        setTitle(query);
        mCursor = Applications.search(getContentResolver(), query);
        startManagingCursor(mCursor);
        ApplicationsAdapter adapter = new ApplicationsAdapter(this, mCursor);
        setListAdapter(adapter);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (mCursor == null) {
            Log.e(TAG, "Got click on position " + position + " but there is no cursor");
            return;
        }
        if (mCursor.isClosed()) {
            Log.e(TAG, "Got click on position " + position + " but the cursor is closed");
            return;
        }
        if (!mCursor.moveToPosition(position)) {
            Log.e(TAG, "Failed to move to position " + position);
            return;
        }
        Uri uri = ApplicationsAdapter.getColumnUri(mCursor, Applications.ApplicationColumns.URI);
        launchApplication(uri);
    }
    private void launchApplication(Uri uri) {
        ComponentName componentName = Applications.uriToComponentName(uri);
        Log.i(TAG, "Launching " + componentName);
        if (componentName != null) {
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launchIntent.setComponent(componentName);
            try {
                startActivity(launchIntent);
            } catch (ActivityNotFoundException ex) {
                Log.w(TAG, "Activity not found: " + componentName);
            }
        }
    }
}
