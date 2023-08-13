public class DrmProvider extends ContentProvider
{
    private final class OpenDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "drm.db";
        private static final int DATABASE_VERSION = 1;
        OpenDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(final SQLiteDatabase db) {
            createTables(db);
        }
        @Override
        public void onOpen(final SQLiteDatabase db) {
            super.onOpen(db);
        }
        @Override
        public void onUpgrade(final SQLiteDatabase db,
                final int oldV, final int newV) {
            Log.i(TAG, "Upgrading downloads database from version " +
                  oldV+ " to " + newV +
                  ", which will destroy all old data");
            dropTables(db);
            createTables(db);
        }
    }
    @Override
    public boolean onCreate()
    {
        mOpenHelper = new OpenDatabaseHelper(getContext());
        return true;
    }
    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE audio (" +
                   "_id INTEGER PRIMARY KEY," +
                   "_data TEXT," +
                   "_size INTEGER," +
                   "title TEXT," +
                   "mime_type TEXT" +
                  ");");
        db.execSQL("CREATE TABLE images (" +
                   "_id INTEGER PRIMARY KEY," +
                   "_data TEXT," +
                   "_size INTEGER," +
                   "title TEXT," +
                   "mime_type TEXT" +
                  ");");
    }
    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS audio");
        db.execSQL("DROP TABLE IF EXISTS images");
    }
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        String groupBy = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case AUDIO:
                qb.setTables("audio");
                break;
            case AUDIO_ID:
                qb.setTables("audio");
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            case IMAGES:
                qb.setTables("images");
                break;
            case IMAGES_ID:
                qb.setTables("images");
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalStateException("Unknown URL: " + uri.toString());
        }
        if (projectionIn != null) {
            for (int i = 0; i < projectionIn.length; i++) {
                if (projectionIn[i].equals(OpenableColumns.DISPLAY_NAME)) {
                    projectionIn[i] = "title AS " + OpenableColumns.DISPLAY_NAME;
                }
            }
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projectionIn, selection,
                selectionArgs, groupBy, null, sort);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }
    @Override
    public String getType(Uri url)
    {
        switch (URI_MATCHER.match(url)) {
            case AUDIO_ID:
            case IMAGES_ID:
                Cursor c = query(url, MIME_TYPE_PROJECTION, null, null, null);
                if (c != null && c.getCount() == 1) {
                    c.moveToFirst();
                    String mimeType = c.getString(1);
                    c.deactivate();
                    return mimeType;
                }
                break;
        }
        throw new IllegalStateException("Unknown URL");
    }
    private ContentValues ensureFile(ContentValues initialValues) {
        try {
            File parent = getContext().getFilesDir();
            parent.mkdirs();
            File file = File.createTempFile("DRM-", ".data", parent);
            ContentValues values = new ContentValues(initialValues);
            values.put("_data", file.toString());
            return values;
       } catch (IOException e) {
            Log.e(TAG, "Failed to create data file in ensureFile");
            return null;
       }
    }
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.INSTALL_DRM)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires INSTALL_DRM permission");
        }
        long rowId;
        int match = URI_MATCHER.match(uri);
        Uri newUri = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (initialValues == null) {
            initialValues = new ContentValues();
        }
        switch (match) {
            case AUDIO: {
                ContentValues values = ensureFile(initialValues);
                if (values == null) return null;
                rowId = db.insert("audio", "title", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(DrmStore.Audio.CONTENT_URI, rowId);
                }
                break;
            }
            case IMAGES: {
                ContentValues values = ensureFile(initialValues);
                if (values == null) return null;
                rowId = db.insert("images", "title", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(DrmStore.Images.CONTENT_URI, rowId);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid URI " + uri);
        }
        if (newUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newUri;
    }
    private static final class GetTableAndWhereOutParameter {
        public String table;
        public String where;
    }
    static final GetTableAndWhereOutParameter sGetTableAndWhereParam =
            new GetTableAndWhereOutParameter();
    private void getTableAndWhere(Uri uri, int match, String userWhere,
            GetTableAndWhereOutParameter out) {
        String where = null;
        switch (match) {
            case AUDIO:
                out.table = "audio";
                break;
            case AUDIO_ID:
                out.table = "audio";
                where = "_id=" + uri.getPathSegments().get(1);
                break;
            case IMAGES:
                out.table = "images";
                break;
            case IMAGES_ID:
                out.table = "images";
                where = "_id=" + uri.getPathSegments().get(1);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unknown or unsupported URL: " + uri.toString());
        }
        if (!TextUtils.isEmpty(userWhere)) {
            if (!TextUtils.isEmpty(where)) {
                out.where = where + " AND (" + userWhere + ")";
            } else {
                out.where = userWhere;
            }
        } else {
            out.where = where;
        }
    }
    private void deleteFiles(Uri uri, String userWhere, String[] whereArgs) {
        Cursor c = query(uri, new String [] { "_data" }, userWhere, whereArgs, null);
        try {
            if (c != null && c.moveToFirst()) {
                String prefix = getContext().getFilesDir().getPath();
                do {
                    String path = c.getString(0);
                    if (!path.startsWith(prefix)) {
                        throw new SecurityException("Attempted to delete a non-DRM file");
                    }
                    new File(path).delete();
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    @Override
    public int delete(Uri uri, String userWhere, String[] whereArgs) {
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_DRM)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires DRM permission");
        }
        int count;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        synchronized (sGetTableAndWhereParam) {
            getTableAndWhere(uri, match, userWhere, sGetTableAndWhereParam);
            switch (match) {
                default:
                    deleteFiles(uri, userWhere, whereArgs);
                    count = db.delete(sGetTableAndWhereParam.table,
                            sGetTableAndWhereParam.where, whereArgs);
                    break;
            }
        }
        return count;
    }
    @Override
    public int update(Uri uri, ContentValues initialValues, String userWhere,
            String[] whereArgs) {
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_DRM)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires ACCESS_DRM permission");
        }
        int count;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        synchronized (sGetTableAndWhereParam) {
            getTableAndWhere(uri, match, userWhere, sGetTableAndWhereParam);
            switch (match) {
                default:
                    count = db.update(sGetTableAndWhereParam.table, initialValues,
                        sGetTableAndWhereParam.where, whereArgs);
                    break;
            }
        }
        return count;
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        String requiredPermission = mode.equals("w") ?
                Manifest.permission.INSTALL_DRM : Manifest.permission.ACCESS_DRM;
        if (getContext().checkCallingOrSelfPermission(requiredPermission)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires " + requiredPermission);
        }
        return openFileHelper(uri, mode);
    }
    private static String TAG = "DrmProvider";
    private static final int AUDIO = 100;
    private static final int AUDIO_ID = 101;
    private static final int IMAGES = 102;
    private static final int IMAGES_ID = 103;
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String[] MIME_TYPE_PROJECTION = new String[] {
            DrmStore.Columns._ID, 
            DrmStore.Columns.MIME_TYPE, 
    };
    private SQLiteOpenHelper mOpenHelper;
    static
    {
        URI_MATCHER.addURI(DrmStore.AUTHORITY, "audio", AUDIO);
        URI_MATCHER.addURI(DrmStore.AUTHORITY, "audio/#", AUDIO_ID);
        URI_MATCHER.addURI(DrmStore.AUTHORITY, "images", IMAGES);
        URI_MATCHER.addURI(DrmStore.AUTHORITY, "images/#", IMAGES_ID);
    }
}
