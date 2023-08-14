public class Applications {
    public static final String AUTHORITY = "applications";
    public static final Uri CONTENT_URI = Uri.parse("content:
    public static final String APPLICATION_PATH = "applications";
    public static final String SEARCH_PATH = "search";
    private static final String APPLICATION_SUB_TYPE = "vnd.android.application";
    public static final String APPLICATION_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + APPLICATION_SUB_TYPE;
    public static final String APPLICATION_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + APPLICATION_SUB_TYPE;
    private Applications() {}
    public static Cursor search(ContentResolver resolver, String query) {
        Uri searchUri = CONTENT_URI.buildUpon().appendPath(SEARCH_PATH).appendPath(query).build();
        return resolver.query(searchUri, null, null, null, null);
    }
    public static ComponentName uriToComponentName(Uri appUri) {
        if (appUri == null) return null;
        if (!ContentResolver.SCHEME_CONTENT.equals(appUri.getScheme())) return null;
        if (!AUTHORITY.equals(appUri.getAuthority())) return null;
        List<String> pathSegments = appUri.getPathSegments();
        if (pathSegments.size() != 3) return null;
        if (!APPLICATION_PATH.equals(pathSegments.get(0))) return null;
        String packageName = pathSegments.get(1);
        String name = pathSegments.get(2);
        return new ComponentName(packageName, name);
    }
    public static Uri componentNameToUri(String packageName, String className) {
        return Applications.CONTENT_URI.buildUpon()
                .appendEncodedPath(APPLICATION_PATH)
                .appendPath(packageName)
                .appendPath(className)
                .build();
    }
    public interface ApplicationColumns extends BaseColumns {
        public static final String NAME = "name";
        public static final String ICON = "icon";
        public static final String URI = "uri";
    }
}
