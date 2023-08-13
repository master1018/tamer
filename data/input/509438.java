public class SearchManagerTest extends ActivityInstrumentationTestCase2<LocalActivity> {
    private ComponentName SEARCHABLE_ACTIVITY =
            new ComponentName("com.android.frameworks.coretests",
                    "android.app.activity.SearchableActivity");
    Context mContext;
    public SearchManagerTest() {
        super("com.android.frameworks.coretests", LocalActivity.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Activity testActivity = getActivity();
        mContext = testActivity;
    }
    private ISearchManager getSearchManagerService() {
        return ISearchManager.Stub.asInterface(
                ServiceManager.getService(Context.SEARCH_SERVICE));
    }
    @MediumTest
    public void testSearchManagerInterfaceAvailable() {
        assertNotNull(getSearchManagerService());
    }
    @LargeTest
    public void testSearchManagerAvailable() {
        SearchManager searchManager1 = (SearchManager)
                mContext.getSystemService(Context.SEARCH_SERVICE);
        assertNotNull(searchManager1);
        SearchManager searchManager2 = (SearchManager)
                mContext.getSystemService(Context.SEARCH_SERVICE);
        assertNotNull(searchManager2);
        assertSame(searchManager1, searchManager2 );
    }
    public void testStartSearchIdempotent() throws Exception {
         SearchManager searchManager = (SearchManager)
                 mContext.getSystemService(Context.SEARCH_SERVICE);
         assertNotNull(searchManager);
         searchManager.startSearch(null, false, SEARCHABLE_ACTIVITY, null, false);
         searchManager.startSearch(null, false, SEARCHABLE_ACTIVITY, null, false);
         searchManager.stopSearch();
    }
    public void testStopSearchIdempotent() throws Exception {
         SearchManager searchManager = (SearchManager)
                 mContext.getSystemService(Context.SEARCH_SERVICE);
         assertNotNull(searchManager);
         searchManager.stopSearch();
         searchManager.startSearch(null, false, SEARCHABLE_ACTIVITY, null, false);
         searchManager.stopSearch();
         searchManager.stopSearch();
    }
    public void testSearchManagerInvocations() throws Exception {
        SearchManager searchManager = (SearchManager)
                mContext.getSystemService(Context.SEARCH_SERVICE);
        assertNotNull(searchManager);
        searchManager.startSearch(null, false, SEARCHABLE_ACTIVITY, null, false);
        searchManager.stopSearch();
        searchManager.startSearch("", false, SEARCHABLE_ACTIVITY, null, false);
        searchManager.stopSearch();
        searchManager.startSearch("test search string", false, SEARCHABLE_ACTIVITY, null, false);
        searchManager.stopSearch();
        searchManager.startSearch("test search string", true, SEARCHABLE_ACTIVITY, null, false);
        searchManager.stopSearch();
    }
}
