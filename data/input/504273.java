public class Searchables {
    private static final String LOG_TAG = "Searchables";
    private static final String MD_LABEL_DEFAULT_SEARCHABLE = "android.app.default_searchable";
    private static final String MD_SEARCHABLE_SYSTEM_SEARCH = "*";
    private Context mContext;
    private HashMap<ComponentName, SearchableInfo> mSearchablesMap = null;
    private ArrayList<SearchableInfo> mSearchablesList = null;
    private ArrayList<SearchableInfo> mSearchablesInGlobalSearchList = null;
    private ComponentName mGlobalSearchActivity = null;
    private ComponentName mWebSearchActivity = null;
    public static String GOOGLE_SEARCH_COMPONENT_NAME =
            "com.android.googlesearch/.GoogleSearch";
    public static String ENHANCED_GOOGLE_SEARCH_COMPONENT_NAME =
            "com.google.android.providers.enhancedgooglesearch/.Launcher";
    public Searchables (Context context) {
        mContext = context;
    }
    public SearchableInfo getSearchableInfo(ComponentName activity) {
        SearchableInfo result;
        synchronized (this) {
            result = mSearchablesMap.get(activity);
            if (result != null) return result;
        }
        ActivityInfo ai = null;
        try {
            ai = mContext.getPackageManager().
                       getActivityInfo(activity, PackageManager.GET_META_DATA );
            String refActivityName = null;
            Bundle md = ai.metaData;
            if (md != null) {
                refActivityName = md.getString(MD_LABEL_DEFAULT_SEARCHABLE);
            }
            if (refActivityName == null) {
                md = ai.applicationInfo.metaData;
                if (md != null) {
                    refActivityName = md.getString(MD_LABEL_DEFAULT_SEARCHABLE);
                }
            }
            if (refActivityName != null)
            {
                if (refActivityName.equals(MD_SEARCHABLE_SYSTEM_SEARCH)) {
                    return null;
                }
                String pkg = activity.getPackageName();
                ComponentName referredActivity;
                if (refActivityName.charAt(0) == '.') {
                    referredActivity = new ComponentName(pkg, pkg + refActivityName);
                } else {
                    referredActivity = new ComponentName(pkg, refActivityName);
                }
                synchronized (this) {
                    result = mSearchablesMap.get(referredActivity);
                    if (result != null) {
                        mSearchablesMap.put(activity, result);
                        return result;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }
    public void buildSearchableList() {
        HashMap<ComponentName, SearchableInfo> newSearchablesMap
                                = new HashMap<ComponentName, SearchableInfo>();
        ArrayList<SearchableInfo> newSearchablesList
                                = new ArrayList<SearchableInfo>();
        ArrayList<SearchableInfo> newSearchablesInGlobalSearchList
                                = new ArrayList<SearchableInfo>();
        final PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> searchList;
        final Intent intent = new Intent(Intent.ACTION_SEARCH);
        searchList = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        List<ResolveInfo> webSearchInfoList;
        final Intent webSearchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        webSearchInfoList = pm.queryIntentActivities(webSearchIntent, PackageManager.GET_META_DATA);
        if (searchList != null || webSearchInfoList != null) {
            int search_count = (searchList == null ? 0 : searchList.size());
            int web_search_count = (webSearchInfoList == null ? 0 : webSearchInfoList.size());
            int count = search_count + web_search_count;
            for (int ii = 0; ii < count; ii++) {
                ResolveInfo info = (ii < search_count)
                        ? searchList.get(ii)
                        : webSearchInfoList.get(ii - search_count);
                ActivityInfo ai = info.activityInfo;
                if (newSearchablesMap.get(new ComponentName(ai.packageName, ai.name)) == null) {
                    SearchableInfo searchable = SearchableInfo.getActivityMetaData(mContext, ai);
                    if (searchable != null) {
                        newSearchablesList.add(searchable);
                        newSearchablesMap.put(searchable.getSearchActivity(), searchable);
                        if (searchable.shouldIncludeInGlobalSearch()) {
                            newSearchablesInGlobalSearchList.add(searchable);
                        }
                    }
                }
            }
        }
        ComponentName newGlobalSearchActivity = findGlobalSearchActivity();
        ComponentName newWebSearchActivity = findWebSearchActivity(newGlobalSearchActivity);
        synchronized (this) {
            mSearchablesMap = newSearchablesMap;
            mSearchablesList = newSearchablesList;
            mSearchablesInGlobalSearchList = newSearchablesInGlobalSearchList;
            mGlobalSearchActivity = newGlobalSearchActivity;
            mWebSearchActivity = newWebSearchActivity;
        }
    }
    private ComponentName findGlobalSearchActivity() {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> activities =
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        int count = activities == null ? 0 : activities.size();
        for (int i = 0; i < count; i++) {
            ActivityInfo ai = activities.get(i).activityInfo;
            if (pm.checkPermission(Manifest.permission.GLOBAL_SEARCH,
                    ai.packageName) == PackageManager.PERMISSION_GRANTED) {
                return new ComponentName(ai.packageName, ai.name);
            } else {
                Log.w(LOG_TAG, "Package " + ai.packageName + " wants to handle GLOBAL_SEARCH, "
                        + "but does not have the GLOBAL_SEARCH permission.");
            }
        }
        Log.w(LOG_TAG, "No global search activity found");
        return null;
    }
    private ComponentName findWebSearchActivity(ComponentName globalSearchActivity) {
        if (globalSearchActivity == null) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.setPackage(globalSearchActivity.getPackageName());
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> activities =
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        int count = activities == null ? 0 : activities.size();
        for (int i = 0; i < count; i++) {
            ActivityInfo ai = activities.get(i).activityInfo;
            return new ComponentName(ai.packageName, ai.name);
        }
        Log.w(LOG_TAG, "No web search activity found");
        return null;
    }
    public synchronized ArrayList<SearchableInfo> getSearchablesList() {
        ArrayList<SearchableInfo> result = new ArrayList<SearchableInfo>(mSearchablesList);
        return result;
    }
    public synchronized ArrayList<SearchableInfo> getSearchablesInGlobalSearchList() {
        return new ArrayList<SearchableInfo>(mSearchablesInGlobalSearchList);
    }
    public synchronized ComponentName getGlobalSearchActivity() {
        return mGlobalSearchActivity;
    }
    public synchronized ComponentName getWebSearchActivity() {
        return mWebSearchActivity;
    }
}
