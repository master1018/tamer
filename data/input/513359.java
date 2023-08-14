public class GoogleSuggestionProvider extends ContentProvider {
    private static final int SEARCH_SUGGEST = 0;
    private static final int SEARCH_SHORTCUT = 1;
    private UriMatcher mUriMatcher;
    private GoogleSource mSource;
    @Override
    public boolean onCreate() {
        mSource = QsbApplication.get(getContext()).getGoogleSource();
        mUriMatcher = buildUriMatcher(getContext());
        return true;
    }
    @Override
    public String getType(Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        int match = mUriMatcher.match(uri);
        if (match == SEARCH_SUGGEST) {
            String query = getQuery(uri);
            return new SuggestionCursorBackedCursor(mSource.getSuggestionsExternal(query));
        } else if (match == SEARCH_SHORTCUT) {
            String shortcutId = getQuery(uri);
            String extraData =
                uri.getQueryParameter(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
            return new SuggestionCursorBackedCursor(mSource.refreshShortcut(shortcutId, extraData));
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    private String getQuery(Uri uri) {
        if (uri.getPathSegments().size() > 1) {
            return uri.getLastPathSegment();
        } else {
            return "";
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    private UriMatcher buildUriMatcher(Context context) {
        String authority = getAuthority(context);
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY,
                SEARCH_SUGGEST);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
                SEARCH_SUGGEST);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_SHORTCUT,
                SEARCH_SHORTCUT);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*",
                SEARCH_SHORTCUT);
        return matcher;
    }
    protected String getAuthority(Context context) {
        return context.getPackageName() + ".google";
    }
}
