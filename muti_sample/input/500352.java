public class RssContentProvider extends ContentProvider {
    private Logger mLogger = Logger.getLogger("com.example.codelab.rssexample");
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper = new DatabaseHelper();
    private static final String DATABASE_NAME = "rssitems.db";
    private static final String DATABASE_TABLE_NAME = "rssItems";
    private static final int DB_VERSION = 1;
    private static final int ALL_MESSAGES = 1;
    private static final int SPECIFIC_MESSAGE = 2;
    private static final UriMatcher URI_MATCHER;
    static{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI("my_rss_item", "rssitem", ALL_MESSAGES);
        URI_MATCHER.addURI("my_rss_item", "rssitem/#", SPECIFIC_MESSAGE);
    }
    public static final Uri CONTENT_URI = Uri.parse( "content:
    public static final String ID = "_id";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String HAS_BEEN_READ = "hasbeenread";
    public static final String CONTENT = "rawcontent";
    public static final String LAST_UPDATED = "lastupdated";
    public static final String DEFAULT_SORT_ORDER = TITLE + " DESC";
    private static class DatabaseHelper extends ContentProviderDatabaseHelper{
        @Override
        public void onCreate(SQLiteDatabase db){
            try{
                String sql = "CREATE TABLE " + DATABASE_TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                URL + " TEXT," +
                TITLE + " TEXT," +
                HAS_BEEN_READ + " BOOLEAN DEFAULT 0," +
                CONTENT + " TEXT," +
                LAST_UPDATED + " INTEGER DEFAULT 0);";
                Logger.getLogger("com.example.codelab.rssexample").info("DatabaseHelper.onCreate(): SQL statement: " + sql);
                db.execSQL(sql);
                Logger.getLogger("com.example.codelab.rssexample").info("DatabaseHelper.onCreate(): Created a database");
            } catch (SQLException e) {
                Logger.getLogger("com.example.codelab.rssexample").warning("DatabaseHelper.onCreate(): Couldn't create a database!");
            }
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME + ";");
        }
    }
    @Override
    public boolean onCreate() {
        final Context con = getContext();
        try{
            mDb = mDbHelper.openDatabase(getContext(), DATABASE_NAME, null, DB_VERSION);
            mLogger.info("RssContentProvider.onCreate(): Opened a database");
        } catch (Exception ex) {
              return false;
        }
        if(mDb == null){
            return false;
        } else {
            return true;
        }
    }
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case ALL_MESSAGES:
                return "vnd.android.cursor.dir/rssitem"; 
            case SPECIFIC_MESSAGE:
                return "vnd.android.cursor.item/rssitem";     
            default:
                return null;
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String groupBy, String having, String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(DATABASE_TABLE_NAME);
        if((URI_MATCHER.match(uri)) == SPECIFIC_MESSAGE){
            qBuilder.appendWhere("_id=" + uri.getPathLeafId());
        }
        if(TextUtils.isEmpty(sortOrder)){
            sortOrder = DEFAULT_SORT_ORDER;
        }
        Cursor c = qBuilder.query(mDb,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    @Override
    public int update(Uri uri, ContentValues values, String whereClause) {
        int updateCount = mDb.update(DATABASE_TABLE_NAME, values, whereClause);
        getContext().getContentResolver().notifyUpdate(uri, null);
        return updateCount;
    }
    @Override
    public Uri insert(Uri requestUri, ContentValues initialValues) {
       long rowId = -1;
       rowId = mDb.insert(DATABASE_TABLE_NAME, "rawcontent", initialValues);
       Uri newUri = CONTENT_URI.addId(rowId);
       getContext().getContentResolver().notifyInsert(CONTENT_URI, null);
       return newUri;
    }
    @Override
    public int delete(Uri uri, String where) {
        int rowCount = mDb.delete(DATABASE_TABLE_NAME, ID + " = " + uri.getPathLeafId());
        getContext().getContentResolver().notifyDelete(uri, null);
        return rowCount;
    }
}
