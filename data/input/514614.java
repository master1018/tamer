public class SocialProvider extends ContentProvider {
    private static final String TAG = "SocialProvider ~~~~";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ACTIVITIES = 1000;
    private static final int ACTIVITIES_ID = 1001;
    private static final int ACTIVITIES_AUTHORED_BY = 1002;
    private static final int CONTACT_STATUS_ID = 3000;
    private static final String DEFAULT_SORT_ORDER = Activities.THREAD_PUBLISHED + " DESC, "
            + Activities.PUBLISHED + " ASC";
    private static final HashMap<String, String> sContactsProjectionMap;
    private static final HashMap<String, String> sRawContactsProjectionMap;
    private static final HashMap<String, String> sActivitiesProjectionMap;
    private static final HashMap<String, String> sActivitiesContactsProjectionMap;
    static {
        final UriMatcher matcher = sUriMatcher;
        matcher.addURI(SocialContract.AUTHORITY, "activities", ACTIVITIES);
        matcher.addURI(SocialContract.AUTHORITY, "activities/#", ACTIVITIES_ID);
        matcher.addURI(SocialContract.AUTHORITY, "activities/authored_by/#", ACTIVITIES_AUTHORED_BY);
        matcher.addURI(SocialContract.AUTHORITY, "contact_status/#", CONTACT_STATUS_ID);
        HashMap<String, String> columns;
        columns = new HashMap<String, String>();
        columns.put(Contacts.DISPLAY_NAME, "contact." + Contacts.DISPLAY_NAME + " AS "
                + Contacts.DISPLAY_NAME);
        sContactsProjectionMap = columns;
        columns = new HashMap<String, String>();
        columns.put(RawContacts._ID, Tables.RAW_CONTACTS + "." + RawContacts._ID + " AS _id");
        columns.put(RawContacts.CONTACT_ID, RawContacts.CONTACT_ID);
        sRawContactsProjectionMap = columns;
        columns = new HashMap<String, String>();
        columns.put(Activities._ID, "activities._id AS _id");
        columns.put(Activities.RES_PACKAGE, PackagesColumns.PACKAGE + " AS "
                + Activities.RES_PACKAGE);
        columns.put(Activities.MIMETYPE, Activities.MIMETYPE);
        columns.put(Activities.RAW_ID, Activities.RAW_ID);
        columns.put(Activities.IN_REPLY_TO, Activities.IN_REPLY_TO);
        columns.put(Activities.AUTHOR_CONTACT_ID, Activities.AUTHOR_CONTACT_ID);
        columns.put(Activities.TARGET_CONTACT_ID, Activities.TARGET_CONTACT_ID);
        columns.put(Activities.PUBLISHED, Activities.PUBLISHED);
        columns.put(Activities.THREAD_PUBLISHED, Activities.THREAD_PUBLISHED);
        columns.put(Activities.TITLE, Activities.TITLE);
        columns.put(Activities.SUMMARY, Activities.SUMMARY);
        columns.put(Activities.LINK, Activities.LINK);
        columns.put(Activities.THUMBNAIL, Activities.THUMBNAIL);
        sActivitiesProjectionMap = columns;
        columns = new HashMap<String, String>();
        columns.putAll(sContactsProjectionMap);
        columns.putAll(sRawContactsProjectionMap);
        columns.putAll(sActivitiesProjectionMap); 
        sActivitiesContactsProjectionMap = columns;
    }
    private ContactsDatabaseHelper mDbHelper;
    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mDbHelper = ContactsDatabaseHelper.getInstance(context);
        return true;
    }
    private void onChange(Uri uri) {
        getContext().getContentResolver().notifyChange(ContactsContract.AUTHORITY_URI, null);
    }
    @Override
    public boolean isTemporary() {
        return false;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        long id = 0;
        switch (match) {
            case ACTIVITIES: {
                id = insertActivity(values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        final Uri result = ContentUris.withAppendedId(Activities.CONTENT_URI, id);
        onChange(result);
        return result;
    }
    private long insertActivity(ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = 0;
        db.beginTransaction();
        try {
            final String packageName = values.getAsString(Activities.RES_PACKAGE);
            if (packageName != null) {
                values.put(ActivitiesColumns.PACKAGE_ID, mDbHelper.getPackageId(packageName));
            }
            values.remove(Activities.RES_PACKAGE);
            final String mimeType = values.getAsString(Activities.MIMETYPE);
            values.put(ActivitiesColumns.MIMETYPE_ID, mDbHelper.getMimeTypeId(mimeType));
            values.remove(Activities.MIMETYPE);
            long published = values.getAsLong(Activities.PUBLISHED);
            long threadPublished = published;
            String inReplyTo = values.getAsString(Activities.IN_REPLY_TO);
            if (inReplyTo != null) {
                threadPublished = getThreadPublished(db, inReplyTo, published);
            }
            values.put(Activities.THREAD_PUBLISHED, threadPublished);
            id = db.insert(Tables.ACTIVITIES, Activities.RAW_ID, values);
            if (values.containsKey(Activities.RAW_ID)) {
                adjustReplyTimestamps(db, values.getAsString(Activities.RAW_ID), published);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return id;
    }
    private long getThreadPublished(SQLiteDatabase db, String rawId, long defaultValue) {
        String inReplyTo = null;
        long threadPublished = defaultValue;
        final Cursor c = db.query(Tables.ACTIVITIES,
                new String[]{Activities.IN_REPLY_TO, Activities.PUBLISHED},
                Activities.RAW_ID + " = ?", new String[]{rawId}, null, null, null);
        try {
            if (c.moveToFirst()) {
                inReplyTo = c.getString(0);
                threadPublished = c.getLong(1);
            }
        } finally {
            c.close();
        }
        if (inReplyTo != null) {
            return getThreadPublished(db, inReplyTo, threadPublished);
        }
        return threadPublished;
    }
    private void adjustReplyTimestamps(SQLiteDatabase db, String inReplyTo, long threadPublished) {
        ContentValues values = new ContentValues();
        values.put(Activities.THREAD_PUBLISHED, threadPublished);
        int replies = db.update(Tables.ACTIVITIES, values,
                Activities.IN_REPLY_TO + "= ?", new String[] {inReplyTo});
        if (replies == 0) {
            return;
        }
        ArrayList<String> rawIds = new ArrayList<String>(replies);
        final Cursor c = db.query(Tables.ACTIVITIES,
                new String[]{Activities.RAW_ID},
                Activities.IN_REPLY_TO + " = ?", new String[] {inReplyTo}, null, null, null);
        try {
            while (c.moveToNext()) {
                rawIds.add(c.getString(0));
            }
        } finally {
            c.close();
        }
        for (String rawId : rawIds) {
            adjustReplyTimestamps(db, rawId, threadPublished);
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES_ID: {
                final long activityId = ContentUris.parseId(uri);
                return db.delete(Tables.ACTIVITIES, Activities._ID + "=" + activityId, null);
            }
            case ACTIVITIES_AUTHORED_BY: {
                final long contactId = ContentUris.parseId(uri);
                return db.delete(Tables.ACTIVITIES, Activities.AUTHOR_CONTACT_ID + "=" + contactId, null);
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES: {
                qb.setTables(Tables.ACTIVITIES_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_CONTACTS);
                qb.setProjectionMap(sActivitiesContactsProjectionMap);
                break;
            }
            case ACTIVITIES_ID: {
                long activityId = ContentUris.parseId(uri);
                qb.setTables(Tables.ACTIVITIES_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_CONTACTS);
                qb.setProjectionMap(sActivitiesContactsProjectionMap);
                qb.appendWhere(Activities._ID + "=" + activityId);
                break;
            }
            case ACTIVITIES_AUTHORED_BY: {
                long contactId = ContentUris.parseId(uri);
                qb.setTables(Tables.ACTIVITIES_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_CONTACTS);
                qb.setProjectionMap(sActivitiesContactsProjectionMap);
                qb.appendWhere(Activities.AUTHOR_CONTACT_ID + "=" + contactId);
                break;
            }
            case CONTACT_STATUS_ID: {
                long aggId = ContentUris.parseId(uri);
                qb.setTables(Tables.ACTIVITIES_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_CONTACTS);
                qb.setProjectionMap(sActivitiesContactsProjectionMap);
                qb.appendWhere(Activities.IN_REPLY_TO + " IS NULL AND ");
                qb.appendWhere(Activities.AUTHOR_CONTACT_ID + " IN (SELECT " + BaseColumns._ID
                        + " FROM " + Tables.RAW_CONTACTS + " WHERE " + RawContacts.CONTACT_ID + "="
                        + aggId + ")");
                sortOrder = Activities.PUBLISHED + " DESC";
                limit = "1";
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (sortOrder == null) {
            sortOrder = DEFAULT_SORT_ORDER;
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), ContactsContract.AUTHORITY_URI);
        }
        return c;
    }
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES:
            case ACTIVITIES_AUTHORED_BY:
                return Activities.CONTENT_TYPE;
            case ACTIVITIES_ID:
                final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                long activityId = ContentUris.parseId(uri);
                return mDbHelper.getActivityMimeType(activityId);
            case CONTACT_STATUS_ID:
                return Contacts.CONTENT_ITEM_TYPE;
        }
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
}
