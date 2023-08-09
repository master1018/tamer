public class SearchManager 
        implements DialogInterface.OnDismissListener, DialogInterface.OnCancelListener
{
    private static final boolean DBG = false;
    private static final String TAG = "SearchManager";
    public final static char MENU_KEY = 's';
    public final static int MENU_KEYCODE = KeyEvent.KEYCODE_S;
    public final static String QUERY = "query";
    public final static String USER_QUERY = "user_query";
    public final static String APP_DATA = "app_data";
    public final static String SEARCH_MODE = "search_mode";
    public final static String ACTION_KEY = "action_key";
    public final static String EXTRA_DATA_KEY = "intent_extra_data_key";
    public final static String EXTRA_SELECT_QUERY = "select_query";
    public final static String CURSOR_EXTRA_KEY_IN_PROGRESS = "in_progress";
    public final static String ACTION_MSG = "action_msg";
    public final static String SUGGEST_URI_PATH_QUERY = "search_suggest_query";
    public final static String SUGGEST_MIME_TYPE =
            "vnd.android.cursor.dir/vnd.android.search.suggest";
    public final static String SUGGEST_URI_PATH_SHORTCUT = "search_suggest_shortcut";
    public final static String SHORTCUT_MIME_TYPE = 
            "vnd.android.cursor.item/vnd.android.search.suggest";
    public final static String SUGGEST_COLUMN_FORMAT = "suggest_format";
    public final static String SUGGEST_COLUMN_TEXT_1 = "suggest_text_1";
    public final static String SUGGEST_COLUMN_TEXT_2 = "suggest_text_2";
    public final static String SUGGEST_COLUMN_TEXT_2_URL = "suggest_text_2_url";
    public final static String SUGGEST_COLUMN_ICON_1 = "suggest_icon_1";
    public final static String SUGGEST_COLUMN_ICON_2 = "suggest_icon_2";
    public final static String SUGGEST_COLUMN_INTENT_ACTION = "suggest_intent_action";
    public final static String SUGGEST_COLUMN_INTENT_DATA = "suggest_intent_data";
    public final static String SUGGEST_COLUMN_INTENT_EXTRA_DATA = "suggest_intent_extra_data";
    public final static String SUGGEST_COLUMN_INTENT_COMPONENT_NAME = "suggest_intent_component";
    public final static String SUGGEST_COLUMN_INTENT_DATA_ID = "suggest_intent_data_id";
    public final static String SUGGEST_COLUMN_QUERY = "suggest_intent_query";
    public final static String SUGGEST_COLUMN_SHORTCUT_ID = "suggest_shortcut_id";
    public final static String SUGGEST_COLUMN_BACKGROUND_COLOR = "suggest_background_color";
    public final static String SUGGEST_COLUMN_SPINNER_WHILE_REFRESHING =
            "suggest_spinner_while_refreshing";
    public final static String SUGGEST_NEVER_MAKE_SHORTCUT = "_-1";
    public final static String SUGGEST_PARAMETER_LIMIT = "limit";
    public final static String INTENT_ACTION_GLOBAL_SEARCH 
            = "android.search.action.GLOBAL_SEARCH";
    public final static String INTENT_ACTION_SEARCH_SETTINGS 
            = "android.search.action.SEARCH_SETTINGS";
    public final static String INTENT_ACTION_WEB_SEARCH_SETTINGS
            = "android.search.action.WEB_SEARCH_SETTINGS";
    public final static String INTENT_ACTION_SEARCHABLES_CHANGED
            = "android.search.action.SEARCHABLES_CHANGED";
    public final static String INTENT_ACTION_SEARCH_SETTINGS_CHANGED
            = "android.search.action.SETTINGS_CHANGED";
    public final static String INTENT_ACTION_NONE = "android.search.action.ZILCH";
    public final static String CONTEXT_IS_VOICE = "android.search.CONTEXT_IS_VOICE";
    public final static String DISABLE_VOICE_SEARCH
            = "android.search.DISABLE_VOICE_SEARCH";
    private static ISearchManager mService;
    private final Context mContext;
    private String mAssociatedPackage;
     final Handler mHandler;
     OnDismissListener mDismissListener = null;
     OnCancelListener mCancelListener = null;
    private SearchDialog mSearchDialog;
     SearchManager(Context context, Handler handler)  {
        mContext = context;
        mHandler = handler;
        mService = ISearchManager.Stub.asInterface(
                ServiceManager.getService(Context.SEARCH_SERVICE));
    }
    public void startSearch(String initialQuery, 
                            boolean selectInitialQuery,
                            ComponentName launchActivity,
                            Bundle appSearchData,
                            boolean globalSearch) {
        if (globalSearch) {
            startGlobalSearch(initialQuery, selectInitialQuery, appSearchData);
            return;
        }
        ensureSearchDialog();
        mSearchDialog.show(initialQuery, selectInitialQuery, launchActivity, appSearchData);
    }
    private void ensureSearchDialog() {
        if (mSearchDialog == null) {
            mSearchDialog = new SearchDialog(mContext, this);
            mSearchDialog.setOnCancelListener(this);
            mSearchDialog.setOnDismissListener(this);
        }
    }
     void startGlobalSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData) {
        ComponentName globalSearchActivity = getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", mContext.getPackageName());
        }
        intent.putExtra(APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        try {
            if (DBG) Log.d(TAG, "Starting global search: " + intent.toUri(0));
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }
    public ComponentName getGlobalSearchActivity() {
        try {
            return mService.getGlobalSearchActivity();
        } catch (RemoteException ex) {
            Log.e(TAG, "getGlobalSearchActivity() failed: " + ex);
            return null;
        }
    }
    public ComponentName getWebSearchActivity() {
        try {
            return mService.getWebSearchActivity();
        } catch (RemoteException ex) {
            Log.e(TAG, "getWebSearchActivity() failed: " + ex);
            return null;
        }
    }
    public void triggerSearch(String query,
                              ComponentName launchActivity,
                              Bundle appSearchData) {
        if (!mAssociatedPackage.equals(launchActivity.getPackageName())) {
            throw new IllegalArgumentException("invoking app search on a different package " +
                    "not associated with this search manager");
        }
        if (query == null || TextUtils.getTrimmedLength(query) == 0) {
            Log.w(TAG, "triggerSearch called with empty query, ignoring.");
            return;
        }
        startSearch(query, false, launchActivity, appSearchData, false);
        mSearchDialog.launchQuerySearch();
    }
    public void stopSearch() {
        if (mSearchDialog != null) {
            mSearchDialog.cancel();
        }
    }
    public boolean isVisible() {
        return mSearchDialog == null? false : mSearchDialog.isShowing();
    }
    public interface OnDismissListener {
        public void onDismiss();
    }
    public interface OnCancelListener {
        public void onCancel();
    }
    public void setOnDismissListener(final OnDismissListener listener) {
        mDismissListener = listener;
    }
    public void setOnCancelListener(OnCancelListener listener) {
        mCancelListener = listener;
    }
    @Deprecated
    public void onCancel(DialogInterface dialog) {
        if (mCancelListener != null) {
            mCancelListener.onCancel();
        }
    }
    @Deprecated
    public void onDismiss(DialogInterface dialog) {
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }
    public SearchableInfo getSearchableInfo(ComponentName componentName) {
        try {
            return mService.getSearchableInfo(componentName);
        } catch (RemoteException ex) {
            Log.e(TAG, "getSearchableInfo() failed: " + ex);
            return null;
        }
    }
    public Cursor getSuggestions(SearchableInfo searchable, String query) {
        return getSuggestions(searchable, query, -1);
    }
    public Cursor getSuggestions(SearchableInfo searchable, String query, int limit) {
        if (searchable == null) {
            return null;
        }
        String authority = searchable.getSuggestAuthority();
        if (authority == null) {
            return null;
        }
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .query("")  
                .fragment("");  
        final String contentPath = searchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);
        String selection = searchable.getSuggestSelection();
        String[] selArgs = null;
        if (selection != null) {    
            selArgs = new String[] { query };
        } else {                    
            uriBuilder.appendPath(query);
        }
        if (limit > 0) {
            uriBuilder.appendQueryParameter(SUGGEST_PARAMETER_LIMIT, String.valueOf(limit));
        }
        Uri uri = uriBuilder.build();
        return mContext.getContentResolver().query(uri, null, selection, selArgs, null);
    }
    public List<SearchableInfo> getSearchablesInGlobalSearch() {
        try {
            return mService.getSearchablesInGlobalSearch();
        } catch (RemoteException e) {
            Log.e(TAG, "getSearchablesInGlobalSearch() failed: " + e);
            return null;
        }
    }
}
