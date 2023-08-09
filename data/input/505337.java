public class SettingsProvider extends ContentProvider {
    private static final String TAG = "SettingsProvider";
    private static final boolean LOCAL_LOGV = false;
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_OLD_FAVORITES = "old_favorites";
    private static final String[] COLUMN_VALUE = new String[] { "value" };
    private static final int MAX_CACHE_ENTRIES = 50;
    private static final SettingsCache sSystemCache = new SettingsCache();
    private static final SettingsCache sSecureCache = new SettingsCache();
    private static final int MAX_CACHE_ENTRY_SIZE = 500;
    private static final Bundle NULL_SETTING = Bundle.forPair("value", null);
    protected DatabaseHelper mOpenHelper;
    private BackupManager mBackupManager;
    private static class SqlArguments {
        public String table;
        public final String where;
        public final String[] args;
        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                if (!DatabaseHelper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                if (!DatabaseHelper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }
                if ("system".equals(this.table) || "secure".equals(this.table)) {
                    this.where = Settings.NameValueTable.NAME + "=?";
                    this.args = new String[] { url.getPathSegments().get(1) };
                } else {
                    this.where = "_id=" + ContentUris.parseId(url);
                    this.args = null;
                }
            }
        }
        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                if (!DatabaseHelper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }
                this.where = null;
                this.args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
    private Uri getUriFor(Uri tableUri, ContentValues values, long rowId) {
        if (tableUri.getPathSegments().size() != 1) {
            throw new IllegalArgumentException("Invalid URI: " + tableUri);
        }
        String table = tableUri.getPathSegments().get(0);
        if ("system".equals(table) || "secure".equals(table)) {
            String name = values.getAsString(Settings.NameValueTable.NAME);
            return Uri.withAppendedPath(tableUri, name);
        } else {
            return ContentUris.withAppendedId(tableUri, rowId);
        }
    }
    private void sendNotify(Uri uri) {
        boolean backedUpDataChanged = false;
        String property = null, table = uri.getPathSegments().get(0);
        if (table.equals("system")) {
            property = Settings.System.SYS_PROP_SETTING_VERSION;
            backedUpDataChanged = true;
        } else if (table.equals("secure")) {
            property = Settings.Secure.SYS_PROP_SETTING_VERSION;
            backedUpDataChanged = true;
        }
        if (property != null) {
            long version = SystemProperties.getLong(property, 0) + 1;
            if (LOCAL_LOGV) Log.v(TAG, "property: " + property + "=" + version);
            SystemProperties.set(property, Long.toString(version));
        }
        if (backedUpDataChanged) {
            mBackupManager.dataChanged();
        }
        String notify = uri.getQueryParameter("notify");
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
            if (LOCAL_LOGV) Log.v(TAG, "notifying: " + uri);
        } else {
            if (LOCAL_LOGV) Log.v(TAG, "notification suppressed: " + uri);
        }
    }
    private void checkWritePermissions(SqlArguments args) {
        if ("secure".equals(args.table) &&
            getContext().checkCallingOrSelfPermission(
                    android.Manifest.permission.WRITE_SECURE_SETTINGS) !=
            PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    String.format("Permission denial: writing to secure settings requires %1$s",
                                  android.Manifest.permission.WRITE_SECURE_SETTINGS));
        }
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        mBackupManager = new BackupManager(getContext());
        if (!ensureAndroidIdIsSet()) {
            return false;
        }
        return true;
    }
    private boolean ensureAndroidIdIsSet() {
        final Cursor c = query(Settings.Secure.CONTENT_URI,
                new String[] { Settings.NameValueTable.VALUE },
                Settings.NameValueTable.NAME + "=?",
                new String[] { Settings.Secure.ANDROID_ID }, null);
        try {
            final String value = c.moveToNext() ? c.getString(0) : null;
            if (value == null) {
                final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                String serial = SystemProperties.get("ro.serialno", "");
                random.setSeed(
                    (serial + System.nanoTime() + new SecureRandom().nextLong()).getBytes());
                final String newAndroidIdValue = Long.toHexString(random.nextLong());
                Log.d(TAG, "Generated and saved new ANDROID_ID [" + newAndroidIdValue + "]");
                final ContentValues values = new ContentValues();
                values.put(Settings.NameValueTable.NAME, Settings.Secure.ANDROID_ID);
                values.put(Settings.NameValueTable.VALUE, newAndroidIdValue);
                final Uri uri = insert(Settings.Secure.CONTENT_URI, values);
                if (uri == null) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } finally {
            c.close();
        }
    }
    @Override
    public Bundle call(String method, String request, Bundle args) {
        if (Settings.CALL_METHOD_GET_SYSTEM.equals(method)) {
            return lookupValue("system", sSystemCache, request);
        }
        if (Settings.CALL_METHOD_GET_SECURE.equals(method)) {
            return lookupValue("secure", sSecureCache, request);
        }
        return null;
    }
    private Bundle lookupValue(String table, SettingsCache cache, String key) {
        synchronized (cache) {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(table, COLUMN_VALUE, "name=?", new String[]{key},
                              null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cache.putIfAbsent(key, cursor.getString(0));
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "settings lookup error", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
        cache.putIfAbsent(key, null);
        return NULL_SETTING;
    }
    @Override
    public Cursor query(Uri url, String[] select, String where, String[] whereArgs, String sort) {
        SqlArguments args = new SqlArguments(url, where, whereArgs);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (TABLE_FAVORITES.equals(args.table)) {
            return null;
        } else if (TABLE_OLD_FAVORITES.equals(args.table)) {
            args.table = TABLE_FAVORITES;
            Cursor cursor = db.rawQuery("PRAGMA table_info(favorites);", null);
            if (cursor != null) {
                boolean exists = cursor.getCount() > 0;
                cursor.close();
                if (!exists) return null;
            } else {
                return null;
            }
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);
        Cursor ret = qb.query(db, select, args.where, args.args, null, null, sort);
        ret.setNotificationUri(getContext().getContentResolver(), url);
        return ret;
    }
    @Override
    public String getType(Uri url) {
        SqlArguments args = new SqlArguments(url, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);
        if (TABLE_FAVORITES.equals(args.table)) {
            return 0;
        }
        checkWritePermissions(args);
        SettingsCache cache = SettingsCache.forTable(args.table);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0) return 0;
                SettingsCache.populate(cache, values[i]);
                if (LOCAL_LOGV) Log.v(TAG, args.table + " <- " + values[i]);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        sendNotify(uri);
        return values.length;
    }
    private boolean parseProviderList(Uri url, ContentValues initialValues) {
        String value = initialValues.getAsString(Settings.Secure.VALUE);
        String newProviders = null;
        if (value != null && value.length() > 1) {
            char prefix = value.charAt(0);
            if (prefix == '+' || prefix == '-') {
                value = value.substring(1);
                String providers = "";
                String[] columns = {Settings.Secure.VALUE};
                String where = Settings.Secure.NAME + "=\'" + Settings.Secure.LOCATION_PROVIDERS_ALLOWED + "\'";
                Cursor cursor = query(url, columns, where, null, null);
                if (cursor != null && cursor.getCount() == 1) {
                    try {
                        cursor.moveToFirst();
                        providers = cursor.getString(0);
                    } finally {
                        cursor.close();
                    }
                }
                int index = providers.indexOf(value);
                int end = index + value.length();
                if (index > 0 && providers.charAt(index - 1) != ',') index = -1;
                if (end < providers.length() && providers.charAt(end) != ',') index = -1;
                if (prefix == '+' && index < 0) {
                    if (providers.length() == 0) {
                        newProviders = value;
                    } else {
                        newProviders = providers + ',' + value;
                    }
                } else if (prefix == '-' && index >= 0) {
                    if (index > 0) index--;
                    if (end < providers.length()) end++;
                    newProviders = providers.substring(0, index);
                    if (end < providers.length()) {
                        newProviders += providers.substring(end);
                    }
                } else {
                    return false;
                }
                if (newProviders != null) {
                    initialValues.put(Settings.Secure.VALUE, newProviders);
                }
            }
        }
        return true;
    }
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(url);
        if (TABLE_FAVORITES.equals(args.table)) {
            return null;
        }
        checkWritePermissions(args);
        String name = initialValues.getAsString(Settings.Secure.NAME);
        if (Settings.Secure.LOCATION_PROVIDERS_ALLOWED.equals(name)) {
            if (!parseProviderList(url, initialValues)) return null;
        }
        SettingsCache cache = SettingsCache.forTable(args.table);
        String value = initialValues.getAsString(Settings.NameValueTable.VALUE);
        if (SettingsCache.isRedundantSetValue(cache, name, value)) {
            return Uri.withAppendedPath(url, name);
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);
        if (rowId <= 0) return null;
        SettingsCache.populate(cache, initialValues);  
        if (LOCAL_LOGV) Log.v(TAG, args.table + " <- " + initialValues);
        url = getUriFor(url, initialValues, rowId);
        sendNotify(url);
        return url;
    }
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SqlArguments args = new SqlArguments(url, where, whereArgs);
        if (TABLE_FAVORITES.equals(args.table)) {
            return 0;
        } else if (TABLE_OLD_FAVORITES.equals(args.table)) {
            args.table = TABLE_FAVORITES;
        }
        checkWritePermissions(args);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) {
            SettingsCache.wipe(args.table);  
            sendNotify(url);
        }
        if (LOCAL_LOGV) Log.v(TAG, args.table + ": " + count + " row(s) deleted");
        return count;
    }
    @Override
    public int update(Uri url, ContentValues initialValues, String where, String[] whereArgs) {
        SqlArguments args = new SqlArguments(url, where, whereArgs);
        if (TABLE_FAVORITES.equals(args.table)) {
            return 0;
        }
        checkWritePermissions(args);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, initialValues, args.where, args.args);
        if (count > 0) {
            SettingsCache.wipe(args.table);  
            sendNotify(url);
        }
        if (LOCAL_LOGV) Log.v(TAG, args.table + ": " + count + " row(s) <- " + initialValues);
        return count;
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int ringtoneType = RingtoneManager.getDefaultType(uri);
        if (ringtoneType != -1) {
            Context context = getContext();
            Uri soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, ringtoneType);
            if (soundUri != null) {
                String authority = soundUri.getAuthority();
                boolean isDrmAuthority = authority.equals(DrmStore.AUTHORITY);
                if (isDrmAuthority || authority.equals(MediaStore.AUTHORITY)) {
                    if (isDrmAuthority) {
                        try {
                            DrmStore.enforceAccessDrmPermission(context);
                        } catch (SecurityException e) {
                            throw new FileNotFoundException(e.getMessage());
                        }
                    }
                    return context.getContentResolver().openFileDescriptor(soundUri, mode);
                }
            }
        }
        return super.openFile(uri, mode);
    }
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        int ringtoneType = RingtoneManager.getDefaultType(uri);
        if (ringtoneType != -1) {
            Context context = getContext();
            Uri soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, ringtoneType);
            if (soundUri != null) {
                String authority = soundUri.getAuthority();
                boolean isDrmAuthority = authority.equals(DrmStore.AUTHORITY);
                if (isDrmAuthority || authority.equals(MediaStore.AUTHORITY)) {
                    if (isDrmAuthority) {
                        try {
                            DrmStore.enforceAccessDrmPermission(context);
                        } catch (SecurityException e) {
                            throw new FileNotFoundException(e.getMessage());
                        }
                    }
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd = context.getContentResolver().openFileDescriptor(soundUri, mode);
                        return new AssetFileDescriptor(pfd, 0, -1);
                    } catch (FileNotFoundException ex) {
                    }
                }
                try {
                    return super.openAssetFile(soundUri, mode);
                } catch (FileNotFoundException ex) {
                    return context.getResources().openRawResourceFd(
                            com.android.internal.R.raw.fallbackring);
                }
            }
            throw new FileNotFoundException(); 
        }
        return super.openAssetFile(uri, mode);
    }
    private static final class SettingsCache extends LinkedHashMap<String, Bundle> {
        public SettingsCache() {
            super(MAX_CACHE_ENTRIES, 0.75f , true );
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
        public Bundle putIfAbsent(String key, String value) {
            Bundle bundle = (value == null) ? NULL_SETTING : Bundle.forPair("value", value);
            if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                synchronized (this) {
                    if (!containsKey(key)) {
                        put(key, bundle);
                    }
                }
            }
            return bundle;
        }
        public static SettingsCache forTable(String tableName) {
            if ("system".equals(tableName)) {
                return SettingsProvider.sSystemCache;
            }
            if ("secure".equals(tableName)) {
                return SettingsProvider.sSecureCache;
            }
            return null;
        }
        public static void populate(SettingsCache cache, ContentValues contentValues) {
            if (cache == null) {
                return;
            }
            String name = contentValues.getAsString(Settings.NameValueTable.NAME);
            if (name == null) {
                Log.w(TAG, "null name populating settings cache.");
                return;
            }
            String value = contentValues.getAsString(Settings.NameValueTable.VALUE);
            synchronized (cache) {
                if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                    cache.put(name, Bundle.forPair(Settings.NameValueTable.VALUE, value));
                } else {
                    cache.remove(name);
                }
            }
        }
        public static void wipe(String tableName) {
            SettingsCache cache = SettingsCache.forTable(tableName);
            if (cache == null) {
                return;
            }
            synchronized (cache) {
                cache.clear();
            }
        }
        public static boolean isRedundantSetValue(SettingsCache cache, String name, String value) {
            if (cache == null) return false;
            synchronized (cache) {
                Bundle bundle = cache.get(name);
                if (bundle == null) return false;
                String oldValue = bundle.getPairValue();
                if (oldValue == null && value == null) return true;
                if ((oldValue == null) != (value == null)) return false;
                return oldValue.equals(value);
            }
        }
    }
}
