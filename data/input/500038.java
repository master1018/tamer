public class ShortcutsProvider extends ContentProvider {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.ExternalShortcutReceiver";
    public static final String EXTRA_SHORTCUT_SOURCE = "shortcut_source";
    private static final int URI_CODE_SHORTCUTS = 0;
    private UriMatcher mUriMatcher;
    @Override
    public boolean onCreate() {
        mUriMatcher = buildUriMatcher();
        return true;
    }
    private UriMatcher buildUriMatcher() {
        String authority = getAuthority();
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, "shortcuts", URI_CODE_SHORTCUTS);
        return matcher;
    }
    private String getAuthority() {
        return getContext().getPackageName() + ".shortcuts";
    }
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case URI_CODE_SHORTCUTS:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case URI_CODE_SHORTCUTS:
                addShortcut(values);
                return null;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    private void addShortcut(final ContentValues shortcut) {
        String sourceName = shortcut.getAsString(EXTRA_SHORTCUT_SOURCE);
        if (TextUtils.isEmpty(sourceName)) {
            Log.e(TAG, "Missing " + EXTRA_SHORTCUT_SOURCE);
            return;
        }
        final ComponentName sourceComponent = ComponentName.unflattenFromString(sourceName);
        if (!checkCallingPackage(sourceComponent.getPackageName())) {
            Log.w(TAG, "Got shortcut for " + sourceComponent + " from a different process");
            return;
        }
        getQsbApplication().runOnUiThread(new Runnable() {
            public void run() {
                storeShortcut(sourceComponent, shortcut);
            }
        });
    }
    private void storeShortcut(ComponentName sourceComponent, ContentValues shortcut) {
        if (DBG) Log.d(TAG, "Adding (PID: " + Binder.getCallingPid() + "): " + shortcut);
        Source source = getCorpora().getSource(sourceComponent.flattenToShortString());
        if (source == null) {
            Log.w(TAG, "Unknown shortcut source " + sourceComponent);
            return;
        }
        String userQuery = shortcut.getAsString(SearchManager.USER_QUERY);
        if (userQuery == null) userQuery = "";
        ListSuggestionCursor cursor = new ListSuggestionCursor(userQuery);
        cursor.add(makeSuggestion(source, shortcut));
        getShortcutRepository().reportClick(cursor, 0);
    }
    private boolean checkCallingPackage(String packageName) {
        int callingUid = Binder.getCallingUid();
        PackageManager pm = getContext().getPackageManager();
        String[] uidPkgs = pm.getPackagesForUid(callingUid);
        if (uidPkgs == null) return false;
        for (String uidPkg : uidPkgs) {
            if (packageName.equals(uidPkg)) return true;
        }
        return false;
    }
    private SuggestionData makeSuggestion(Source source, ContentValues shortcut) {
        String format = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_FORMAT);
        String text1 = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1);
        String text2 = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2);
        String text2Url = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2_URL);
        String icon1 = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_ICON_1);
        String icon2 = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_ICON_2);
        String shortcutId = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        boolean spinnerWhileRefreshing = unboxBoolean(
                shortcut.getAsBoolean(SearchManager.SUGGEST_COLUMN_SPINNER_WHILE_REFRESHING),
                false);
        String intentAction = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_INTENT_ACTION);
        String intentData = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_INTENT_DATA);
        String intentExtraData =
                shortcut.getAsString(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
        String query = shortcut.getAsString(SearchManager.SUGGEST_COLUMN_QUERY);
        SuggestionData suggestion = new SuggestionData(source);
        suggestion.setFormat(format);
        suggestion.setText1(text1);
        suggestion.setText2(text2);
        suggestion.setText2Url(text2Url);
        suggestion.setIcon1(icon1);
        suggestion.setIcon2(icon2);
        suggestion.setShortcutId(shortcutId);
        suggestion.setSpinnerWhileRefreshing(spinnerWhileRefreshing);
        suggestion.setIntentAction(intentAction);
        suggestion.setIntentData(intentData);
        suggestion.setIntentExtraData(intentExtraData);
        suggestion.setSuggestionQuery(query);
        return suggestion;
    }
    private static boolean unboxBoolean(Boolean value, boolean defValue) {
        return value == null ? defValue : value;
    }
    private QsbApplication getQsbApplication() {
        return QsbApplication.get(getContext());
    }
    private ShortcutRepository getShortcutRepository() {
        return getQsbApplication().getShortcutRepository();
    }
    private Corpora getCorpora() {
        return getQsbApplication().getCorpora();
    }
}
