public class SearchableSource extends AbstractSource {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchableSource";
    private static final String EXTRA_CALLING_PACKAGE = "calling_package";
    private final SearchableInfo mSearchable;
    private final String mName;
    private final ActivityInfo mActivityInfo;
    private final int mVersionCode;
    private CharSequence mLabel = null;
    private Drawable.ConstantState mSourceIcon = null;
    public SearchableSource(Context context, SearchableInfo searchable)
            throws NameNotFoundException {
        super(context);
        ComponentName componentName = searchable.getSearchActivity();
        mSearchable = searchable;
        mName = componentName.flattenToShortString();
        PackageManager pm = context.getPackageManager();
        mActivityInfo = pm.getActivityInfo(componentName, 0);
        PackageInfo pkgInfo = pm.getPackageInfo(componentName.getPackageName(), 0);
        mVersionCode = pkgInfo.versionCode;
    }
    protected SearchableInfo getSearchableInfo() {
        return mSearchable;
    }
    public boolean canRead() {
        String authority = mSearchable.getSuggestAuthority();
        if (authority == null) {
            return true;
        }
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority);
        String contentPath = mSearchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }
        uriBuilder.appendEncodedPath(SearchManager.SUGGEST_URI_PATH_QUERY);
        Uri uri = uriBuilder.build();
        return canRead(uri);
    }
    private boolean canRead(Uri uri) {
        ProviderInfo provider = getContext().getPackageManager().resolveContentProvider(
                uri.getAuthority(), 0);
        if (provider == null) {
            Log.w(TAG, getName() + " has bad suggestion authority " + uri.getAuthority());
            return false;
        }
        String readPermission = provider.readPermission;
        if (readPermission == null) {
            return true;
        }
        int pid = android.os.Process.myPid();
        int uid = android.os.Process.myUid();
        if (getContext().checkPermission(readPermission, pid, uid)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        PathPermission[] pathPermissions = provider.pathPermissions;
        if (pathPermissions == null || pathPermissions.length == 0) {
            if (DBG) Log.d(TAG, "Missing " + readPermission);
            return false;
        }
        String path = uri.getPath();
        for (PathPermission perm : pathPermissions) {
            String pathReadPermission = perm.getReadPermission();
            if (pathReadPermission != null
                    && perm.match(path)
                    && getContext().checkPermission(pathReadPermission, pid, uid)
                            == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        if (DBG) Log.d(TAG, "Missing " + readPermission + " and no path permission applies");
        return false;
    }
    public ComponentName getIntentComponent() {
        return mSearchable.getSearchActivity();
    }
    public int getVersionCode() {
        return mVersionCode;
    }
    public String getName() {
        return mName;
    }
    @Override
    protected String getIconPackage() {
        String iconPackage = mSearchable.getSuggestPackage();
        if (iconPackage != null) {
            return iconPackage;
        } else {
            return mSearchable.getSearchActivity().getPackageName();
        }
    }
    public CharSequence getLabel() {
        if (mLabel == null) {
            mLabel = mActivityInfo.loadLabel(getContext().getPackageManager());
        }
        return mLabel;
    }
    public CharSequence getHint() {
        return getText(mSearchable.getHintId());
    }
    public int getQueryThreshold() {
        return mSearchable.getSuggestThreshold();
    }
    public CharSequence getSettingsDescription() {
        return getText(mSearchable.getSettingsDescriptionId());
    }
    public Drawable getSourceIcon() {
        if (mSourceIcon == null) {
            Drawable icon = loadSourceIcon();
            if (icon == null) {
                icon = getContext().getResources().getDrawable(R.drawable.corpus_icon_default);
            }
            mSourceIcon = (icon != null) ? icon.getConstantState() : null;
            return icon;
        }
        return (mSourceIcon != null) ? mSourceIcon.newDrawable() : null;
    }
    private Drawable loadSourceIcon() {
        int iconRes = getSourceIconResource();
        if (iconRes == 0) return null;
        PackageManager pm = getContext().getPackageManager();
        return pm.getDrawable(mActivityInfo.packageName, iconRes,
                mActivityInfo.applicationInfo);
    }
    public Uri getSourceIconUri() {
        int resourceId = getSourceIconResource();
        if (resourceId == 0) {
            return Util.getResourceUri(getContext(), R.drawable.corpus_icon_default);
        } else {
            return Util.getResourceUri(getContext(), mActivityInfo.applicationInfo, resourceId);
        }
    }
    private int getSourceIconResource() {
        return mActivityInfo.getIconResource();
    }
    public boolean voiceSearchEnabled() {
        return mSearchable.getVoiceSearchEnabled();
    }
    public boolean isLocationAware() {
        return false;
    }
    public Intent createVoiceSearchIntent(Bundle appData) {
        if (mSearchable.getVoiceSearchLaunchWebSearch()) {
            return createVoiceWebSearchIntent(appData);
        } else if (mSearchable.getVoiceSearchLaunchRecognizer()) {
            return createVoiceAppSearchIntent(appData);
        }
        return null;
    }
    private Intent createVoiceAppSearchIntent(Bundle appData) {
        ComponentName searchActivity = mSearchable.getSearchActivity();
        Intent queryIntent = new Intent(Intent.ACTION_SEARCH);
        queryIntent.setComponent(searchActivity);
        PendingIntent pending = PendingIntent.getActivity(
                getContext(), 0, queryIntent, PendingIntent.FLAG_ONE_SHOT);
        Bundle queryExtras = new Bundle();
        if (appData != null) {
            queryExtras.putBundle(SearchManager.APP_DATA, appData);
        }
        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String languageModel = getString(mSearchable.getVoiceLanguageModeId());
        if (languageModel == null) {
            languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
        }
        String prompt = getString(mSearchable.getVoicePromptTextId());
        String language = getString(mSearchable.getVoiceLanguageId());
        int maxResults = mSearchable.getVoiceMaxResults();
        if (maxResults <= 0) {
            maxResults = 1;
        }
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);
        voiceIntent.putExtra(EXTRA_CALLING_PACKAGE,
                searchActivity == null ? null : searchActivity.toShortString());
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, pending);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, queryExtras);
        return voiceIntent;
    }
    public SourceResult getSuggestions(String query, int queryLimit, boolean onlySource) {
        try {
            Cursor cursor = getSuggestions(getContext(), mSearchable, query, queryLimit);
            if (DBG) Log.d(TAG, toString() + "[" + query + "] returned.");
            return new CursorBackedSourceResult(this, query, cursor);
        } catch (RuntimeException ex) {
            Log.e(TAG, toString() + "[" + query + "] failed", ex);
            return new CursorBackedSourceResult(this, query);
        }
    }
    public SuggestionCursor refreshShortcut(String shortcutId, String extraData) {
        Cursor cursor = null;
        try {
            cursor = getValidationCursor(getContext(), mSearchable, shortcutId, extraData);
            if (DBG) Log.d(TAG, toString() + "[" + shortcutId + "] returned.");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
            }
            return new CursorBackedSourceResult(this, null, cursor);
        } catch (RuntimeException ex) {
            Log.e(TAG, toString() + "[" + shortcutId + "] failed", ex);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }
    private static Cursor getSuggestions(Context context, SearchableInfo searchable, String query,
            int queryLimit) {
        if (searchable == null) {
            return null;
        }
        String authority = searchable.getSuggestAuthority();
        if (authority == null) {
            return null;
        }
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority);
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
        uriBuilder.appendQueryParameter("limit", String.valueOf(queryLimit));
        Uri uri = uriBuilder.build();
        if (DBG) {
            Log.d(TAG, "query(" + uri + ",null," + selection + ","
                    + Arrays.toString(selArgs) + ",null)");
        }
        return context.getContentResolver().query(uri, null, selection, selArgs, null);
    }
    private static Cursor getValidationCursor(Context context, SearchableInfo searchable,
            String shortcutId, String extraData) {
        String authority = searchable.getSuggestAuthority();
        if (authority == null) {
            return null;
        }
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority);
        final String contentPath = searchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        uriBuilder.appendPath(shortcutId);
        Uri uri = uriBuilder
                .appendQueryParameter(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, extraData)
                .build();
        if (DBG) Log.d(TAG, "Requesting refresh " + uri);
        return context.getContentResolver().query(uri, null, null, null, null);
    }
    public boolean isWebSuggestionSource() {
        return false;
    }
    public boolean queryAfterZeroResults() {
        return mSearchable.queryAfterZeroResults();
    }
    public String getDefaultIntentAction() {
        String action = mSearchable.getSuggestIntentAction();
        if (action != null) return action;
        return Intent.ACTION_SEARCH;
    }
    public String getDefaultIntentData() {
        return mSearchable.getSuggestIntentData();
    }
    private CharSequence getText(int id) {
        if (id == 0) return null;
        return getContext().getPackageManager().getText(mActivityInfo.packageName, id,
                mActivityInfo.applicationInfo);
    }
    private String getString(int id) {
        CharSequence text = getText(id);
        return text == null ? null : text.toString();
    }
}
