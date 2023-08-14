public class BrowserProvider extends ContentProvider {
    private SQLiteOpenHelper mOpenHelper;
    private BackupManager mBackupManager;
    private static final String sDatabaseName = "browser.db";
    private static final String TAG = "BrowserProvider";
    private static final String ORDER_BY = "visits DESC, date DESC";
    private static final String PICASA_URL = "http:
            "viewer?source=androidclient";
    private static final String[] TABLE_NAMES = new String[] {
        "bookmarks", "searches"
    };
    private static final String[] SUGGEST_PROJECTION = new String[] {
            "_id", "url", "title", "bookmark", "user_entered"
    };
    private static final String SUGGEST_SELECTION =
            "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ?"
                + " OR title LIKE ?) AND (bookmark = 1 OR user_entered = 1)";
    private String[] SUGGEST_ARGS = new String[5];
    private static final int SUGGEST_COLUMN_INTENT_ACTION_ID = 1;
    private static final int SUGGEST_COLUMN_INTENT_DATA_ID = 2;
    private static final int SUGGEST_COLUMN_TEXT_1_ID = 3;
    private static final int SUGGEST_COLUMN_TEXT_2_ID = 4;
    private static final int SUGGEST_COLUMN_TEXT_2_URL_ID = 5;
    private static final int SUGGEST_COLUMN_ICON_1_ID = 6;
    private static final int SUGGEST_COLUMN_ICON_2_ID = 7;
    private static final int SUGGEST_COLUMN_QUERY_ID = 8;
    private static final int SUGGEST_COLUMN_INTENT_EXTRA_DATA = 9;
    private static final String[] COLUMNS = new String[] {
            "_id",
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_TEXT_2_URL,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA};
    private static final int MAX_SUGGESTION_SHORT_ENTRIES = 3;
    private static final int MAX_SUGGESTION_LONG_ENTRIES = 6;
    private static final String MAX_SUGGESTION_LONG_ENTRIES_STRING =
            Integer.valueOf(MAX_SUGGESTION_LONG_ENTRIES).toString();
    private static final int URI_MATCH_BOOKMARKS = 0;
    private static final int URI_MATCH_SEARCHES = 1;
    private static final int URI_MATCH_BOOKMARKS_ID = 10;
    private static final int URI_MATCH_SEARCHES_ID = 11;
    private static final int URI_MATCH_SUGGEST = 20;
    private static final int URI_MATCH_BOOKMARKS_SUGGEST = 21;
    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_BOOKMARKS],
                URI_MATCH_BOOKMARKS);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_BOOKMARKS] + "/#",
                URI_MATCH_BOOKMARKS_ID);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_SEARCHES],
                URI_MATCH_SEARCHES);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_SEARCHES] + "/#",
                URI_MATCH_SEARCHES_ID);
        URI_MATCHER.addURI("browser", SearchManager.SUGGEST_URI_PATH_QUERY,
                URI_MATCH_SUGGEST);
        URI_MATCHER.addURI("browser",
                TABLE_NAMES[URI_MATCH_BOOKMARKS] + "/" + SearchManager.SUGGEST_URI_PATH_QUERY,
                URI_MATCH_BOOKMARKS_SUGGEST);
    }
    private static final int DATABASE_VERSION = 23;
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^(http:
    private BrowserSettings mSettings;
    public BrowserProvider() {
    }
    static String getClientId(ContentResolver cr) {
        String ret = "android-google";
        Cursor c = null;
        try {
            c = cr.query(Uri.parse("content:
                    new String[] { "value" }, "name='client_id'", null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getString(0);
            }
        } catch (RuntimeException ex) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return ret;
    }
    private static CharSequence replaceSystemPropertyInString(Context context, CharSequence srcString) {
        StringBuffer sb = new StringBuffer();
        int lastCharLoc = 0;
        final String client_id = getClientId(context.getContentResolver());
        for (int i = 0; i < srcString.length(); ++i) {
            char c = srcString.charAt(i);
            if (c == '{') {
                sb.append(srcString.subSequence(lastCharLoc, i));
                lastCharLoc = i;
          inner:
                for (int j = i; j < srcString.length(); ++j) {
                    char k = srcString.charAt(j);
                    if (k == '}') {
                        String propertyKeyValue = srcString.subSequence(i + 1, j).toString();
                        if (propertyKeyValue.equals("CLIENT_ID")) {
                            sb.append(client_id);
                        } else {
                            sb.append("unknown");
                        }
                        lastCharLoc = j + 1;
                        i = j;
                        break inner;
                    }
                }
            }
        }
        if (srcString.length() - lastCharLoc > 0) {
            sb.append(srcString.subSequence(lastCharLoc, srcString.length()));
        }
        return sb;
    }
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private Context mContext;
        public DatabaseHelper(Context context) {
            super(context, sDatabaseName, null, DATABASE_VERSION);
            mContext = context;
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE bookmarks (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "url TEXT," +
                    "visits INTEGER," +
                    "date LONG," +
                    "created LONG," +
                    "description TEXT," +
                    "bookmark INTEGER," +
                    "favicon BLOB DEFAULT NULL," +
                    "thumbnail BLOB DEFAULT NULL," +
                    "touch_icon BLOB DEFAULT NULL," +
                    "user_entered INTEGER" +
                    ");");
            final CharSequence[] bookmarks = mContext.getResources()
                    .getTextArray(R.array.bookmarks);
            int size = bookmarks.length;
            try {
                for (int i = 0; i < size; i = i + 2) {
                    CharSequence bookmarkDestination = replaceSystemPropertyInString(mContext, bookmarks[i + 1]);
                    db.execSQL("INSERT INTO bookmarks (title, url, visits, " +
                            "date, created, bookmark)" + " VALUES('" +
                            bookmarks[i] + "', '" + bookmarkDestination +
                            "', 0, 0, 0, 1);");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            db.execSQL("CREATE TABLE searches (" +
                    "_id INTEGER PRIMARY KEY," +
                    "search TEXT," +
                    "date LONG" +
                    ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            if (oldVersion == 18) {
                db.execSQL("DROP TABLE IF EXISTS labels");
            }
            if (oldVersion <= 19) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN thumbnail BLOB DEFAULT NULL;");
            }
            if (oldVersion < 21) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN touch_icon BLOB DEFAULT NULL;");
            }
            if (oldVersion < 22) {
                db.execSQL("DELETE FROM bookmarks WHERE (bookmark = 0 AND url LIKE \"%.google.%client=ms-%\")");
                removeGears();
            }
            if (oldVersion < 23) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN user_entered INTEGER;");
            } else {
                db.execSQL("DROP TABLE IF EXISTS bookmarks");
                db.execSQL("DROP TABLE IF EXISTS searches");
                onCreate(db);
            }
        }
        private void removeGears() {
            new Thread() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    String browserDataDirString = mContext.getApplicationInfo().dataDir;
                    final String appPluginsDirString = "app_plugins";
                    final String gearsPrefix = "gears";
                    File appPluginsDir = new File(browserDataDirString + File.separator
                            + appPluginsDirString);
                    if (!appPluginsDir.exists()) {
                        return;
                    }
                    File[] gearsFiles = appPluginsDir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String filename) {
                            return filename.startsWith(gearsPrefix);
                        }
                    });
                    for (int i = 0; i < gearsFiles.length; ++i) {
                        if (gearsFiles[i].isDirectory()) {
                            deleteDirectory(gearsFiles[i]);
                        } else {
                            gearsFiles[i].delete();
                        }
                    }
                    File gearsDataDir = new File(browserDataDirString + File.separator
                            + gearsPrefix);
                    if (!gearsDataDir.exists()) {
                        return;
                    }
                    deleteDirectory(gearsDataDir);
                }
                private void deleteDirectory(File currentDir) {
                    File[] files = currentDir.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        if (files[i].isDirectory()) {
                            deleteDirectory(files[i]);
                        }
                        files[i].delete();
                    }
                    currentDir.delete();
                }
            }.start();
        }
    }
    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new DatabaseHelper(context);
        mBackupManager = new BackupManager(context);
        if (DATABASE_VERSION == 18 || DATABASE_VERSION == 19) {
            SharedPreferences p = PreferenceManager
                    .getDefaultSharedPreferences(context);
            boolean fix = p.getBoolean("fix_picasa", true);
            if (fix) {
                fixPicasaBookmark();
                Editor ed = p.edit();
                ed.putBoolean("fix_picasa", false);
                ed.commit();
            }
        }
        mSettings = BrowserSettings.getInstance();
        return true;
    }
    private void fixPicasaBookmark() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM bookmarks WHERE " +
                "bookmark = 1 AND url = ?", new String[] { PICASA_URL });
        try {
            if (!cursor.moveToFirst()) {
                db.execSQL("INSERT INTO bookmarks (title, url, visits, " +
                        "date, created, bookmark)" + " VALUES('" +
                        getContext().getString(R.string.picasa) + "', '"
                        + PICASA_URL + "', 0, 0, " + new Date().getTime()
                        + ", 1);");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private class MySuggestionCursor extends AbstractCursor {
        private Cursor  mHistoryCursor;
        private Cursor  mSuggestCursor;
        private int     mHistoryCount;
        private int     mSuggestionCount;
        private boolean mIncludeWebSearch;
        private String  mString;
        private int     mSuggestText1Id;
        private int     mSuggestText2Id;
        private int     mSuggestText2UrlId;
        private int     mSuggestQueryId;
        private int     mSuggestIntentExtraDataId;
        public MySuggestionCursor(Cursor hc, Cursor sc, String string) {
            mHistoryCursor = hc;
            mSuggestCursor = sc;
            mHistoryCount = hc.getCount();
            mSuggestionCount = sc != null ? sc.getCount() : 0;
            if (mSuggestionCount > (MAX_SUGGESTION_LONG_ENTRIES - mHistoryCount)) {
                mSuggestionCount = MAX_SUGGESTION_LONG_ENTRIES - mHistoryCount;
            }
            mString = string;
            mIncludeWebSearch = string.length() > 0;
            if (mSuggestCursor == null) {
                mSuggestText1Id = -1;
                mSuggestText2Id = -1;
                mSuggestText2UrlId = -1;
                mSuggestQueryId = -1;
                mSuggestIntentExtraDataId = -1;
            } else {
                mSuggestText1Id = mSuggestCursor.getColumnIndex(
                                SearchManager.SUGGEST_COLUMN_TEXT_1);
                mSuggestText2Id = mSuggestCursor.getColumnIndex(
                                SearchManager.SUGGEST_COLUMN_TEXT_2);
                mSuggestText2UrlId = mSuggestCursor.getColumnIndex(
                        SearchManager.SUGGEST_COLUMN_TEXT_2_URL);
                mSuggestQueryId = mSuggestCursor.getColumnIndex(
                                SearchManager.SUGGEST_COLUMN_QUERY);
                mSuggestIntentExtraDataId = mSuggestCursor.getColumnIndex(
                                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
            }
        }
        @Override
        public boolean onMove(int oldPosition, int newPosition) {
            if (mHistoryCursor == null) {
                return false;
            }
            if (mIncludeWebSearch) {
                if (mHistoryCount == 0 && newPosition == 0) {
                    return true;
                } else if (mHistoryCount > 0) {
                    if (newPosition == 0) {
                        mHistoryCursor.moveToPosition(0);
                        return true;
                    } else if (newPosition == 1) {
                        return true;
                    }
                }
                newPosition--;
            }
            if (mHistoryCount > newPosition) {
                mHistoryCursor.moveToPosition(newPosition);
            } else {
                mSuggestCursor.moveToPosition(newPosition - mHistoryCount);
            }
            return true;
        }
        @Override
        public int getCount() {
            if (mIncludeWebSearch) {
                return mHistoryCount + mSuggestionCount + 1;
            } else {
                return mHistoryCount + mSuggestionCount;
            }
        }
        @Override
        public String[] getColumnNames() {
            return COLUMNS;
        }
        @Override
        public String getString(int columnIndex) {
            if ((mPos != -1 && mHistoryCursor != null)) {
                int type = -1; 
                if (mIncludeWebSearch) {
                    if (mHistoryCount == 0 && mPos == 0) {
                        type = 0;
                    } else if (mHistoryCount > 0) {
                        if (mPos == 0) {
                            type = 1;
                        } else if (mPos == 1) {
                            type = 0;
                        }
                    }
                    if (type == -1) type = (mPos - 1) < mHistoryCount ? 1 : 2;
                } else {
                    type = mPos < mHistoryCount ? 1 : 2;
                }
                switch(columnIndex) {
                    case SUGGEST_COLUMN_INTENT_ACTION_ID:
                        if (type == 1) {
                            return Intent.ACTION_VIEW;
                        } else {
                            return Intent.ACTION_SEARCH;
                        }
                    case SUGGEST_COLUMN_INTENT_DATA_ID:
                        if (type == 1) {
                            return mHistoryCursor.getString(1);
                        } else {
                            return null;
                        }
                    case SUGGEST_COLUMN_TEXT_1_ID:
                        if (type == 0) {
                            return mString;
                        } else if (type == 1) {
                            return getHistoryTitle();
                        } else {
                            if (mSuggestText1Id == -1) return null;
                            return mSuggestCursor.getString(mSuggestText1Id);
                        }
                    case SUGGEST_COLUMN_TEXT_2_ID:
                        if (type == 0) {
                            return getContext().getString(R.string.search_the_web);
                        } else if (type == 1) {
                            return null;  
                        } else {
                            if (mSuggestText2Id == -1) return null;
                            return mSuggestCursor.getString(mSuggestText2Id);
                        }
                    case SUGGEST_COLUMN_TEXT_2_URL_ID:
                        if (type == 0) {
                            return null;
                        } else if (type == 1) {
                            return getHistoryUrl();
                        } else {
                            if (mSuggestText2UrlId == -1) return null;
                            return mSuggestCursor.getString(mSuggestText2UrlId);
                        }
                    case SUGGEST_COLUMN_ICON_1_ID:
                        if (type == 1) {
                            if (mHistoryCursor.getInt(3) == 1) {
                                return Integer.valueOf(
                                        R.drawable.ic_search_category_bookmark)
                                        .toString();
                            } else {
                                return Integer.valueOf(
                                        R.drawable.ic_search_category_history)
                                        .toString();
                            }
                        } else {
                            return Integer.valueOf(
                                    R.drawable.ic_search_category_suggest)
                                    .toString();
                        }
                    case SUGGEST_COLUMN_ICON_2_ID:
                        return "0";
                    case SUGGEST_COLUMN_QUERY_ID:
                        if (type == 0) {
                            return mString;
                        } else if (type == 1) {
                            return mHistoryCursor.getString(1);
                        } else {
                            if (mSuggestQueryId == -1) return null;
                            return mSuggestCursor.getString(mSuggestQueryId);
                        }
                    case SUGGEST_COLUMN_INTENT_EXTRA_DATA:
                        if (type == 0) {
                            return null;
                        } else if (type == 1) {
                            return null;
                        } else {
                            if (mSuggestIntentExtraDataId == -1) return null;
                            return mSuggestCursor.getString(mSuggestIntentExtraDataId);
                        }
                }
            }
            return null;
        }
        @Override
        public double getDouble(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public float getFloat(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public int getInt(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public long getLong(int column) {
            if ((mPos != -1) && column == 0) {
                return mPos;        
            }
            throw new UnsupportedOperationException();
        }
        @Override
        public short getShort(int column) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean isNull(int column) {
            throw new UnsupportedOperationException();
        }
        public void deactivate() {
            if (mHistoryCursor != null) {
                mHistoryCursor.deactivate();
            }
            if (mSuggestCursor != null) {
                mSuggestCursor.deactivate();
            }
            super.deactivate();
        }
        public boolean requery() {
            return (mHistoryCursor != null ? mHistoryCursor.requery() : false) |
                    (mSuggestCursor != null ? mSuggestCursor.requery() : false);
        }
        public void close() {
            super.close();
            if (mHistoryCursor != null) {
                mHistoryCursor.close();
                mHistoryCursor = null;
            }
            if (mSuggestCursor != null) {
                mSuggestCursor.close();
                mSuggestCursor = null;
            }
        }
        private String getHistoryTitle() {
            String title = mHistoryCursor.getString(2 );
            if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) == 0) {
                title = stripUrl(mHistoryCursor.getString(1 ));
            }
            return title;
        }
        private String getHistoryUrl() {
            String title = mHistoryCursor.getString(2 );
            if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) == 0) {
                return null;
            } else {
                return stripUrl(mHistoryCursor.getString(1 ));
            }
        }
    }
    private static class ResultsCursor extends AbstractCursor {
        private static final int RESULT_ACTION_ID = 1;
        private static final int RESULT_DATA_ID = 2;
        private static final int RESULT_TEXT_ID = 3;
        private static final int RESULT_ICON_ID = 4;
        private static final int RESULT_EXTRA_ID = 5;
        private static final String[] RESULTS_COLUMNS = new String[] {
                "_id",
                SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
        };
        private final ArrayList<String> mResults;
        public ResultsCursor(ArrayList<String> results) {
            mResults = results;
        }
        public int getCount() { return mResults.size(); }
        public String[] getColumnNames() {
            return RESULTS_COLUMNS;
        }
        public String getString(int column) {
            switch (column) {
                case RESULT_ACTION_ID:
                    return RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS;
                case RESULT_TEXT_ID:
                case RESULT_DATA_ID:
                    return mResults.get(mPos);
                case RESULT_EXTRA_ID:
                    return Integer.toString(mPos);
                case RESULT_ICON_ID:
                    return Integer.valueOf(R.drawable.magnifying_glass)
                            .toString();
                default:
                    return null;
            }
        }
        public short getShort(int column) {
            throw new UnsupportedOperationException();
        }
        public int getInt(int column) {
            throw new UnsupportedOperationException();
        }
        public long getLong(int column) {
            if ((mPos != -1) && column == 0) {
                return mPos;        
            }
            throw new UnsupportedOperationException();
        }
        public float getFloat(int column) {
            throw new UnsupportedOperationException();
        }
        public double getDouble(int column) {
            throw new UnsupportedOperationException();
        }
        public boolean isNull(int column) {
            throw new UnsupportedOperationException();
        }
    }
    private ResultsCursor mResultsCursor;
     void setQueryResults(ArrayList<String> results) {
        if (results == null) {
            mResultsCursor = null;
        } else {
            mResultsCursor = new ResultsCursor(results);
        }
    }
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sortOrder)
            throws IllegalStateException {
        int match = URI_MATCHER.match(url);
        if (match == -1) {
            throw new IllegalArgumentException("Unknown URL");
        }
        if (match == URI_MATCH_SUGGEST && mResultsCursor != null) {
            Cursor results = mResultsCursor;
            mResultsCursor = null;
            return results;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (match == URI_MATCH_SUGGEST || match == URI_MATCH_BOOKMARKS_SUGGEST) {
            String suggestSelection;
            String [] myArgs;
            if (selectionArgs[0] == null || selectionArgs[0].equals("")) {
                suggestSelection = null;
                myArgs = null;
            } else {
                String like = selectionArgs[0] + "%";
                if (selectionArgs[0].startsWith("http")
                        || selectionArgs[0].startsWith("file")) {
                    myArgs = new String[1];
                    myArgs[0] = like;
                    suggestSelection = selection;
                } else {
                    SUGGEST_ARGS[0] = "http:
                    SUGGEST_ARGS[1] = "http:
                    SUGGEST_ARGS[2] = "https:
                    SUGGEST_ARGS[3] = "https:
                    SUGGEST_ARGS[4] = like;
                    myArgs = SUGGEST_ARGS;
                    suggestSelection = SUGGEST_SELECTION;
                }
            }
            Cursor c = db.query(TABLE_NAMES[URI_MATCH_BOOKMARKS],
                    SUGGEST_PROJECTION, suggestSelection, myArgs, null, null,
                    ORDER_BY, MAX_SUGGESTION_LONG_ENTRIES_STRING);
            if (match == URI_MATCH_BOOKMARKS_SUGGEST
                    || Patterns.WEB_URL.matcher(selectionArgs[0]).matches()) {
                return new MySuggestionCursor(c, null, "");
            } else {
                if (myArgs != null && myArgs.length > 1
                        && c.getCount() < (MAX_SUGGESTION_SHORT_ENTRIES - 1)) {
                    SearchEngine searchEngine = mSettings.getSearchEngine();
                    if (searchEngine != null && searchEngine.supportsSuggestions()) {
                        Cursor sc = searchEngine.getSuggestions(getContext(), selectionArgs[0]);
                        return new MySuggestionCursor(c, sc, selectionArgs[0]);
                    }
                }
                return new MySuggestionCursor(c, null, selectionArgs[0]);
            }
        }
        String[] projection = null;
        if (projectionIn != null && projectionIn.length > 0) {
            projection = new String[projectionIn.length + 1];
            System.arraycopy(projectionIn, 0, projection, 0, projectionIn.length);
            projection[projectionIn.length] = "_id AS _id";
        }
        StringBuilder whereClause = new StringBuilder(256);
        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_SEARCHES_ID) {
            whereClause.append("(_id = ").append(url.getPathSegments().get(1))
                    .append(")");
        }
        if (selection != null && selection.length() > 0) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append('(');
            whereClause.append(selection);
            whereClause.append(')');
        }
        Cursor c = db.query(TABLE_NAMES[match % 10], projection,
                whereClause.toString(), selectionArgs, null, null, sortOrder,
                null);
        c.setNotificationUri(getContext().getContentResolver(), url);
        return c;
    }
    @Override
    public String getType(Uri url) {
        int match = URI_MATCHER.match(url);
        switch (match) {
            case URI_MATCH_BOOKMARKS:
                return "vnd.android.cursor.dir/bookmark";
            case URI_MATCH_BOOKMARKS_ID:
                return "vnd.android.cursor.item/bookmark";
            case URI_MATCH_SEARCHES:
                return "vnd.android.cursor.dir/searches";
            case URI_MATCH_SEARCHES_ID:
                return "vnd.android.cursor.item/searches";
            case URI_MATCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        boolean isBookmarkTable = false;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(url);
        Uri uri = null;
        switch (match) {
            case URI_MATCH_BOOKMARKS: {
                long rowID = db.insert(TABLE_NAMES[URI_MATCH_BOOKMARKS], "url",
                        initialValues);
                if (rowID > 0) {
                    uri = ContentUris.withAppendedId(Browser.BOOKMARKS_URI,
                            rowID);
                }
                isBookmarkTable = true;
                break;
            }
            case URI_MATCH_SEARCHES: {
                long rowID = db.insert(TABLE_NAMES[URI_MATCH_SEARCHES], "url",
                        initialValues);
                if (rowID > 0) {
                    uri = ContentUris.withAppendedId(Browser.SEARCHES_URI,
                            rowID);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
        if (uri == null) {
            throw new IllegalArgumentException("Unknown URL");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (isBookmarkTable
                && initialValues.containsKey(BookmarkColumns.BOOKMARK)
                && initialValues.getAsInteger(BookmarkColumns.BOOKMARK) != 0) {
            mBackupManager.dataChanged();
        }
        return uri;
    }
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(url);
        if (match == -1 || match == URI_MATCH_SUGGEST) {
            throw new IllegalArgumentException("Unknown URL");
        }
        boolean isBookmarkTable = (match == URI_MATCH_BOOKMARKS_ID);
        String id = null;
        if (isBookmarkTable || match == URI_MATCH_SEARCHES_ID) {
            StringBuilder sb = new StringBuilder();
            if (where != null && where.length() > 0) {
                sb.append("( ");
                sb.append(where);
                sb.append(" ) AND ");
            }
            id = url.getPathSegments().get(1);
            sb.append("_id = ");
            sb.append(id);
            where = sb.toString();
        }
        ContentResolver cr = getContext().getContentResolver();
        if (isBookmarkTable) {
            Cursor cursor = cr.query(Browser.BOOKMARKS_URI,
                    new String[] { BookmarkColumns.BOOKMARK },
                    "_id = " + id, null, null);
            if (cursor.moveToNext()) {
                if (cursor.getInt(0) != 0) {
                    mBackupManager.dataChanged();
                }
            }
            cursor.close();
        }
        int count = db.delete(TABLE_NAMES[match % 10], where, whereArgs);
        cr.notifyChange(url, null);
        return count;
    }
    @Override
    public int update(Uri url, ContentValues values, String where,
            String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(url);
        if (match == -1 || match == URI_MATCH_SUGGEST) {
            throw new IllegalArgumentException("Unknown URL");
        }
        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_SEARCHES_ID) {
            StringBuilder sb = new StringBuilder();
            if (where != null && where.length() > 0) {
                sb.append("( ");
                sb.append(where);
                sb.append(" ) AND ");
            }
            String id = url.getPathSegments().get(1);
            sb.append("_id = ");
            sb.append(id);
            where = sb.toString();
        }
        ContentResolver cr = getContext().getContentResolver();
        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_BOOKMARKS) {
            boolean changingBookmarks = false;
            if (values.containsKey(BookmarkColumns.BOOKMARK)) {
                changingBookmarks = true;
            } else if ((values.containsKey(BookmarkColumns.TITLE)
                     || values.containsKey(BookmarkColumns.URL))
                     && values.containsKey(BookmarkColumns._ID)) {
                Cursor cursor = cr.query(Browser.BOOKMARKS_URI,
                        new String[] { BookmarkColumns.BOOKMARK },
                        BookmarkColumns._ID + " = "
                        + values.getAsString(BookmarkColumns._ID), null, null);
                if (cursor.moveToNext()) {
                    changingBookmarks = (cursor.getInt(0) != 0);
                }
                cursor.close();
            }
            if (changingBookmarks) {
                mBackupManager.dataChanged();
            }
        }
        int ret = db.update(TABLE_NAMES[match % 10], values, where, whereArgs);
        cr.notifyChange(url, null);
        return ret;
    }
    private static String stripUrl(String url) {
        if (url == null) return null;
        Matcher m = STRIP_URL_PATTERN.matcher(url);
        if (m.matches() && m.groupCount() == 3) {
            return m.group(2);
        } else {
            return url;
        }
    }
}
