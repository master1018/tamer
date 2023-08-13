public class DatabaseSessionCache implements SSLClientSessionCache {
    private static final String TAG = "SslSessionCache";
    static DatabaseHelper sDefaultDatabaseHelper;
    private DatabaseHelper mDatabaseHelper;
    public static final String SSL_CACHE_TABLE = "ssl_sessions";
    private static final String SSL_CACHE_ID = "_id";
    private static final String SSL_CACHE_HOSTPORT = "hostport";
    private static final String SSL_CACHE_SESSION = "session";
    private static final String SSL_CACHE_TIME_SEC = "time_sec";
    public static final String DATABASE_NAME = "ssl_sessions.db";
    public static final int DATABASE_VERSION = 1;
    public static final int SSL_CACHE_ID_COL = 0;
    public static final int SSL_CACHE_HOSTPORT_COL = 1;
    public static final int SSL_CACHE_SESSION_COL = 2;
    public static final int SSL_CACHE_TIME_SEC_COL = 3;
    private static final String SAVE_ON_ADD = "save_on_add";
    static boolean sHookInitializationDone = false;
    public static final int MAX_CACHE_SIZE = 256;
    private static final Map<String, byte[]> mExternalCache =
        new LinkedHashMap<String, byte[]>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        public boolean removeEldestEntry(
                Map.Entry<String, byte[]> eldest) {
            boolean shouldDelete = this.size() > MAX_CACHE_SIZE;
            return shouldDelete;
        }
    };
    static boolean mNeedsCacheLoad = true;
    public static final String[] PROJECTION = new String[] {
      SSL_CACHE_ID,
      SSL_CACHE_HOSTPORT,
      SSL_CACHE_SESSION,
      SSL_CACHE_TIME_SEC
    };
    public DatabaseSessionCache() {
        Log.v(TAG, "Instance created.");
        this.mDatabaseHelper = sDefaultDatabaseHelper;
    }
    public DatabaseSessionCache(Context activityContext) {
        init(activityContext);
        this.mDatabaseHelper = sDefaultDatabaseHelper;
    }
    public DatabaseSessionCache(DatabaseHelper database) {
        this.mDatabaseHelper = database;
    }
    public synchronized static void init(Context activityContext) {
        if (sHookInitializationDone) {
            return;
        }
            Context appContext = activityContext.getApplicationContext();
            sDefaultDatabaseHelper = new DatabaseHelper(appContext);
        sHookInitializationDone = true;
    }
    public void putSessionData(SSLSession session, byte[] der) {
        if (mDatabaseHelper == null) {
            return;
        }
        if (mExternalCache.size() > MAX_CACHE_SIZE) {
            Cursor byTime = mDatabaseHelper.getWritableDatabase().query(SSL_CACHE_TABLE,
                    PROJECTION, null, null, null, null, SSL_CACHE_TIME_SEC);
            byTime.moveToFirst();
            String hostPort = byTime.getString(SSL_CACHE_HOSTPORT_COL);
            mDatabaseHelper.getWritableDatabase().delete(SSL_CACHE_TABLE,
                    SSL_CACHE_HOSTPORT + "= ?" , new String[] { hostPort });
        }
        long t0 = System.currentTimeMillis();
        String b64 = new String(Base64.encodeBase64(der));
        String key = session.getPeerHost() + ":" + session.getPeerPort();
        ContentValues values = new ContentValues();
        values.put(SSL_CACHE_HOSTPORT, key);
        values.put(SSL_CACHE_SESSION, b64);
        values.put(SSL_CACHE_TIME_SEC, System.currentTimeMillis() / 1000);
        synchronized (this.getClass()) {
            mExternalCache.put(key, der);
            try {
                mDatabaseHelper.getWritableDatabase().insert(SSL_CACHE_TABLE, null  , values);
            } catch(SQLException ex) {
                Log.w(TAG, "Ignoring SQL exception when caching session", ex);
            }
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            long t1 = System.currentTimeMillis();
            Log.d(TAG, "New SSL session " + session.getPeerHost() +
                    " DER len: " + der.length + " " + (t1 - t0));
        }
    }
    public byte[] getSessionData(String host, int port) {
        if (mDatabaseHelper == null) {
            return null;
        }
        synchronized(this.getClass()) {
            if (mNeedsCacheLoad) {
                mNeedsCacheLoad = false;
                long t0 = System.currentTimeMillis();
                Cursor cur = null;
                try {
                    cur = mDatabaseHelper.getReadableDatabase().query(SSL_CACHE_TABLE, PROJECTION, null,
                            null, null, null, null);
                    if (cur.moveToFirst()) {
                        do {
                            String hostPort = cur.getString(SSL_CACHE_HOSTPORT_COL);
                            String value = cur.getString(SSL_CACHE_SESSION_COL);
                            if (hostPort == null || value == null) {
                                continue;
                            }
                            byte[] der = Base64.decodeBase64(value.getBytes());
                            mExternalCache.put(hostPort, der);
                        } while (cur.moveToNext());
                    }
                } catch (SQLException ex) {
                    Log.d(TAG, "Error loading SSL cached entries ", ex);
                } finally {
                    if (cur != null) {
                        cur.close();
                    }
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        long t1 = System.currentTimeMillis();
                        Log.d(TAG, "LOADED CACHED SSL " + (t1 - t0) + " ms");
                    }
                }
            }
            String key = host + ":" + port;
            return mExternalCache.get(key);
        }
    }
    public byte[] getSessionData(byte[] id) {
        return null;
    }
    public static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null , DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SSL_CACHE_TABLE + " (" +
                    SSL_CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SSL_CACHE_HOSTPORT + " TEXT UNIQUE ON CONFLICT REPLACE," +
                    SSL_CACHE_SESSION + " TEXT," +
                    SSL_CACHE_TIME_SEC + " INTEGER" +
            ");");
            db.execSQL("CREATE INDEX ssl_sessions_idx1 ON ssl_sessions (" +
                    SSL_CACHE_HOSTPORT + ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SSL_CACHE_TABLE );
            onCreate(db);
        }
    }
}
