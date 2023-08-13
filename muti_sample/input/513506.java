public class SearchableSources implements Sources {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchableSources";
    private final Context mContext;
    private final SearchManager mSearchManager;
    private HashMap<String, Source> mSources;
    private Source mWebSearchSource;
    public SearchableSources(Context context) {
        mContext = context;
        mSearchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
    }
    protected Context getContext() {
        return mContext;
    }
    protected SearchManager getSearchManager() {
        return mSearchManager;
    }
    public Collection<Source> getSources() {
        return mSources.values();
    }
    public Source getSource(String name) {
        return mSources.get(name);
    }
    public Source getWebSearchSource() {
        return mWebSearchSource;
    }
    public void update() {
        if (DBG) Log.d(TAG, "update()");
        mSources = new HashMap<String,Source>();
        addSearchableSources();
        mWebSearchSource = createWebSearchSource();
        addSource(mWebSearchSource);
    }
    private void addSearchableSources() {
        List<SearchableInfo> searchables = mSearchManager.getSearchablesInGlobalSearch();
        if (searchables == null) {
            Log.e(TAG, "getSearchablesInGlobalSearch() returned null");
            return;
        }
        for (SearchableInfo searchable : searchables) {
            SearchableSource source = createSearchableSource(searchable);
            if (source != null) {
                if (DBG) Log.d(TAG, "Created source " + source);
                addSource(source);
            }
        }
    }
    private void addSource(Source source) {
        mSources.put(source.getName(), source);
    }
    protected Source createWebSearchSource() {
        return QsbApplication.get(getContext()).getGoogleSource();
    }
    protected SearchableSource createSearchableSource(SearchableInfo searchable) {
        if (searchable == null) return null;
        try {
            return new SearchableSource(mContext, searchable);
        } catch (NameNotFoundException ex) {
            Log.e(TAG, "Source not found: " + ex);
            return null;
        }
    }
    public Source createSourceFor(ComponentName component) {
        SearchableInfo info = mSearchManager.getSearchableInfo(component);
        SearchableSource source = createSearchableSource(info);
        if (DBG) Log.d(TAG, "SearchableSource for " + component + ": " + source);
        return source;
    }
}
