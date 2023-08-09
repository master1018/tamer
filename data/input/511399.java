public class ApplicationsProvider extends ContentProvider {
    private static final boolean DBG = false;
    private static final String TAG = "ApplicationsProvider";
    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final int SEARCH = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private static final int THREAD_PRIORITY = android.os.Process.THREAD_PRIORITY_BACKGROUND;
    private static final int MSG_UPDATE_ALL = 0;
    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String PACKAGE = "package";
    public static final String CLASS = "class";
    public static final String ICON = "icon";
    private static final String APPLICATIONS_TABLE = "applications";
    private static final String APPLICATIONS_LOOKUP_JOIN =
            "applicationsLookup JOIN " + APPLICATIONS_TABLE + " ON"
            + " applicationsLookup.source = " + APPLICATIONS_TABLE + "." + _ID;
    private static final HashMap<String, String> sSearchSuggestionsProjectionMap =
            buildSuggestionsProjectionMap();
    private static final HashMap<String, String> sSearchProjectionMap =
            buildSearchProjectionMap();
    private SQLiteDatabase mDb;
    private Handler mHandler;
    private static final long UPDATE_DELAY_MILLIS = 1000L;
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Applications.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
                SEARCH_SUGGEST);
        matcher.addURI(Applications.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "
    private class MyPackageMonitor extends PackageMonitor {
        @Override
        public void onSomePackagesChanged() {
            postUpdateAll();
        }
    }
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                if (DBG) Log.d(TAG, "locale changed");
                postUpdateAll();
            }
        }
    };
    @Override
    public boolean onCreate() {
        createDatabase();
        new MyPackageMonitor().register(getContext(), true);
        IntentFilter localeFilter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        getContext().registerReceiver(mLocaleChangeReceiver, localeFilter);
        HandlerThread thread = new HandlerThread("ApplicationsProviderUpdater", THREAD_PRIORITY);
        thread.start();
        mHandler = new UpdateHandler(thread.getLooper());
        postUpdateAll();
        return true;
    }
    private class UpdateHandler extends Handler {
        public UpdateHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_ALL:
                    updateApplicationsList(null);
                    break;
                default:
                    Log.e(TAG, "Unknown message: " + msg.what);
                    break;
            }
        }
    }
    private void postUpdateAll() {
        mHandler.removeMessages(MSG_UPDATE_ALL);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_ALL;
        mHandler.sendMessageDelayed(msg, UPDATE_DELAY_MILLIS);
    }
    private void createDatabase() {
        mDb = SQLiteDatabase.create(null);
        mDb.execSQL("CREATE TABLE IF NOT EXISTS " + APPLICATIONS_TABLE + " ("+
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NAME + " TEXT COLLATE LOCALIZED," +
                DESCRIPTION + " description TEXT," +
                PACKAGE + " TEXT," +
                CLASS + " TEXT," +
                ICON + " TEXT" +
                ");");
        mDb.execSQL("CREATE INDEX applicationsComponentIndex ON " + APPLICATIONS_TABLE + " (" 
                + PACKAGE + "," + CLASS + ");");
        mDb.execSQL("CREATE TABLE applicationsLookup (" +
                "token TEXT," +
                "source INTEGER REFERENCES " + APPLICATIONS_TABLE + "(" + _ID + ")," +
                "token_index INTEGER" +
                ");");
        mDb.execSQL("CREATE INDEX applicationsLookupIndex ON applicationsLookup (" +
                "token," +
                "source" +
                ");");
        mDb.execSQL("CREATE TRIGGER applicationsLookup_update UPDATE OF " + NAME + " ON " + 
                APPLICATIONS_TABLE + " " +
                "BEGIN " +
                "DELETE FROM applicationsLookup WHERE source = new." + _ID + ";" +
                "SELECT _TOKENIZE('applicationsLookup', new." + _ID + ", new." + NAME + ", ' ', 1);" +
                "END");
        mDb.execSQL("CREATE TRIGGER applicationsLookup_insert AFTER INSERT ON " + 
                APPLICATIONS_TABLE + " " +
                "BEGIN " +
                "SELECT _TOKENIZE('applicationsLookup', new." + _ID + ", new." + NAME + ", ' ', 1);" +
                "END");
        mDb.execSQL("CREATE TRIGGER applicationsLookup_delete DELETE ON " + 
                APPLICATIONS_TABLE + " " +
                "BEGIN " +
                "DELETE FROM applicationsLookup WHERE source = old." + _ID + ";" +
                "END");
    }
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SHORTCUT_REFRESH:
                return SearchManager.SHORTCUT_MIME_TYPE;
            case SEARCH:
                return Applications.APPLICATION_DIR_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sortOrder) {
        if (DBG) Log.d(TAG, "query(" + uri + ")");
        if (!TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("selection not allowed for " + uri);
        }
        if (selectionArgs != null && selectionArgs.length != 0) {
            throw new IllegalArgumentException("selectionArgs not allowed for " + uri);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            throw new IllegalArgumentException("sortOrder not allowed for " + uri);
        }
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST: {
                String query = null;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                }
                return getSuggestions(query, projectionIn);
            }
            case SHORTCUT_REFRESH: {
                String shortcutId = null;
                if (uri.getPathSegments().size() > 1) {
                    shortcutId = uri.getLastPathSegment();
                }
                return refreshShortcut(shortcutId, projectionIn);
            }
            case SEARCH: {
                String query = null;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                }
                return getSearchResults(query, projectionIn);
            }
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
    private Cursor getSuggestions(String query, String[] projectionIn) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        return searchApplications(query, projectionIn, sSearchSuggestionsProjectionMap);
    }
    private Cursor refreshShortcut(String shortcutId, String[] projectionIn) {
        ComponentName component = ComponentName.unflattenFromString(shortcutId);
        if (component == null) {
            Log.w(TAG, "Bad shortcut id: " + shortcutId);
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(APPLICATIONS_TABLE);
        qb.setProjectionMap(sSearchSuggestionsProjectionMap);
        qb.appendWhere("package = ? AND class = ?");
        String[] selectionArgs = { component.getPackageName(), component.getClassName() };
        Cursor cursor = qb.query(mDb, projectionIn, null, selectionArgs, null, null, null);
        if (DBG) Log.d(TAG, "Returning " + cursor.getCount() + " results for shortcut refresh.");
        return cursor;
    }
    private Cursor getSearchResults(String query, String[] projectionIn) {
        return searchApplications(query, projectionIn, sSearchProjectionMap);
    }
    private Cursor searchApplications(String query, String[] projectionIn,
            Map<String, String> columnMap) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(APPLICATIONS_LOOKUP_JOIN);
        qb.setProjectionMap(columnMap);
        if (!TextUtils.isEmpty(query)) {
            qb.appendWhere(buildTokenFilter(query));
        }
        String groupBy = APPLICATIONS_TABLE + "." + _ID;
        String order = "MIN(token_index) != 0, " + NAME;
        Cursor cursor = qb.query(mDb, projectionIn, null, null, groupBy, null, order);
        if (DBG) Log.d(TAG, "Returning " + cursor.getCount() + " results for " + query);
        return cursor;
    }
    @SuppressWarnings("deprecation")
    private String buildTokenFilter(String filterParam) {
        StringBuilder filter = new StringBuilder("token GLOB ");
        DatabaseUtils.appendEscapedSQLString(filter, 
                DatabaseUtils.getHexCollationKey(filterParam) + "*");
        return filter.toString();
    }
    private static HashMap<String, String> buildSuggestionsProjectionMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        addProjection(map, Applications.ApplicationColumns._ID, _ID);
        addProjection(map, SearchManager.SUGGEST_COLUMN_TEXT_1, NAME);
        addProjection(map, SearchManager.SUGGEST_COLUMN_TEXT_2, DESCRIPTION);
        addProjection(map, SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                "'content:
                + " || " + PACKAGE + " || '/' || " + CLASS);
        addProjection(map, SearchManager.SUGGEST_COLUMN_ICON_1, ICON);
        addProjection(map, SearchManager.SUGGEST_COLUMN_ICON_2, "NULL");
        addProjection(map, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                PACKAGE + " || '/' || " + CLASS);
        return map;
    }
    private static HashMap<String, String> buildSearchProjectionMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        addProjection(map, Applications.ApplicationColumns._ID, _ID);
        addProjection(map, Applications.ApplicationColumns.NAME, NAME);
        addProjection(map, Applications.ApplicationColumns.ICON, ICON);
        addProjection(map, Applications.ApplicationColumns.URI,
                "'content:
                + " || " + PACKAGE + " || '/' || " + CLASS);
        return map;
    }
    private static void addProjection(HashMap<String, String> map, String name, String value) {
        if (!value.equals(name)) {
            value = value + " AS " + name;
        }
        map.put(name, value);
    }
    private void updateApplicationsList(String packageName) {
        if (DBG) Log.d(TAG, "Updating database (packageName = " + packageName + ")...");
        DatabaseUtils.InsertHelper inserter = 
                new DatabaseUtils.InsertHelper(mDb, APPLICATIONS_TABLE);
        int nameCol = inserter.getColumnIndex(NAME);
        int descriptionCol = inserter.getColumnIndex(DESCRIPTION);
        int packageCol = inserter.getColumnIndex(PACKAGE);
        int classCol = inserter.getColumnIndex(CLASS);
        int iconCol = inserter.getColumnIndex(ICON);
        mDb.beginTransaction();
        try {
            removeApplications(packageName);
            String description = getContext().getString(R.string.application_desc);
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            if (packageName != null) {
                mainIntent.setPackage(packageName);
            }
            final PackageManager manager = getContext().getPackageManager();
            List<ResolveInfo> activities = manager.queryIntentActivities(mainIntent, 0);
            int activityCount = activities == null ? 0 : activities.size();
            for (int i = 0; i < activityCount; i++) {
                ResolveInfo info = activities.get(i);
                String title = info.loadLabel(manager).toString();
                if (TextUtils.isEmpty(title)) {
                    title = info.activityInfo.name;
                }
                String icon = getActivityIconUri(info.activityInfo);
                inserter.prepareForInsert();
                inserter.bind(nameCol, title);
                inserter.bind(descriptionCol, description);
                inserter.bind(packageCol, info.activityInfo.applicationInfo.packageName);
                inserter.bind(classCol, info.activityInfo.name);
                inserter.bind(iconCol, icon);
                inserter.execute();
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
            inserter.close();
        }
        if (DBG) Log.d(TAG, "Finished updating database.");
    }
    private String getActivityIconUri(ActivityInfo activityInfo) {
        int icon = activityInfo.getIconResource();
        if (icon == 0) return null;
        Uri uri = getResourceUri(getContext(), activityInfo.applicationInfo, icon);
        return uri == null ? null : uri.toString();
    }
    private void removeApplications(String packageName) {
        if (packageName == null) {
            mDb.delete(APPLICATIONS_TABLE, null, null);
        } else {
            mDb.delete(APPLICATIONS_TABLE, PACKAGE + " = ?", new String[] { packageName });
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
    private static Uri getResourceUri(Context context, ApplicationInfo appInfo, int res) {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(appInfo);
            return getResourceUri(resources, appInfo.packageName, res);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
    private static Uri getResourceUri(Resources resources, String appPkg, int res)
            throws Resources.NotFoundException {
        String resPkg = resources.getResourcePackageName(res);
        String type = resources.getResourceTypeName(res);
        String name = resources.getResourceEntryName(res);
        return makeResourceUri(appPkg, resPkg, type, name);
    }
    private static Uri makeResourceUri(String appPkg, String resPkg, String type, String name)
            throws Resources.NotFoundException {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE);
        uriBuilder.encodedAuthority(appPkg);
        uriBuilder.appendEncodedPath(type);
        if (!appPkg.equals(resPkg)) {
            uriBuilder.appendEncodedPath(resPkg + ":" + name);
        } else {
            uriBuilder.appendEncodedPath(name);
        }
        return uriBuilder.build();
    }
}
