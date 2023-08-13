public class CombinedBookmarkHistoryActivity extends TabActivity
        implements TabHost.OnTabChangeListener {
    private String mExtraData;
    private Intent mResultData;
    private int mResultCode;
     static String BOOKMARKS_TAB = "bookmark";
     static String VISITED_TAB = "visited";
     static String HISTORY_TAB = "history";
     static String STARTING_TAB = "tab";
    static class IconListenerSet implements IconListener {
        private HashMap<String, Bitmap> mUrlsToIcons;
        private Vector<IconListener> mListeners;
        public IconListenerSet() {
            mUrlsToIcons = new HashMap<String, Bitmap>();
            mListeners = new Vector<IconListener>();
        }
        public void onReceivedIcon(String url, Bitmap icon) {
            mUrlsToIcons.put(url, icon);
            for (IconListener listener : mListeners) {
                listener.onReceivedIcon(url, icon);
            }
        }
        public void addListener(IconListener listener) {
            mListeners.add(listener);
        }
        public void removeListener(IconListener listener) {
            mListeners.remove(listener);
        }
        public Bitmap getFavicon(String url) {
            return (Bitmap) mUrlsToIcons.get(url);
        }
    }
    private static IconListenerSet sIconListenerSet;
    static IconListenerSet getIconListenerSet() {
        if (null == sIconListenerSet) {
            sIconListenerSet = new IconListenerSet();
        }
        return sIconListenerSet;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        getTabHost().setOnTabChangedListener(this);
        Bundle extras = getIntent().getExtras();
        Intent bookmarksIntent = new Intent(this, BrowserBookmarksPage.class);
        if (extras != null) {
            bookmarksIntent.putExtras(extras);
        }
        createTab(bookmarksIntent, R.string.tab_bookmarks,
                R.drawable.browser_bookmark_tab, BOOKMARKS_TAB);
        Intent visitedIntent = new Intent(this, BrowserBookmarksPage.class);
        Bundle visitedExtras = extras == null ? new Bundle() : new Bundle(extras);
        visitedExtras.putBoolean("mostVisited", true);
        visitedIntent.putExtras(visitedExtras);
        createTab(visitedIntent, R.string.tab_most_visited,
                R.drawable.browser_visited_tab, VISITED_TAB);
        Intent historyIntent = new Intent(this, BrowserHistoryPage.class);
        String defaultTab = null;
        if (extras != null) {
            historyIntent.putExtras(extras);
            defaultTab = extras.getString(STARTING_TAB);
        }
        createTab(historyIntent, R.string.tab_history,
                R.drawable.browser_history_tab, HISTORY_TAB);
        if (defaultTab != null) {
            getTabHost().setCurrentTab(2);
        }
        WebIconDatabase.getInstance();
        (new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... v) {
                Browser.requestAllIcons(getContentResolver(),
                    Browser.BookmarkColumns.FAVICON + " is NULL",
                    getIconListenerSet());
                return null;
            }
        }).execute();
    }
    private void createTab(Intent intent, int labelResId, int iconResId,
            String tab) {
        Resources resources = getResources();
        TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(tab).setIndicator(
                resources.getText(labelResId), resources.getDrawable(iconResId))
                .setContent(intent));
    }
    public void onTabChanged(String tabId) {
        Activity activity = getLocalActivityManager().getActivity(tabId);
        if (activity != null) {
            activity.onWindowFocusChanged(true);
        }
    }
     void removeParentChildRelationShips() {
        mExtraData = BrowserSettings.PREF_CLEAR_HISTORY;
    }
     void setResultFromChild(int resultCode, Intent data) {
        mResultCode = resultCode;
        mResultData = data;
    }
    @Override
    public void finish() {
        if (mExtraData != null) {
            mResultCode = RESULT_OK;
            if (mResultData == null) mResultData = new Intent();
            mResultData.putExtra(Intent.EXTRA_TEXT, mExtraData);
        }
        setResult(mResultCode, mResultData);
        super.finish();
    }
}
