public final class PicasaContentProvider extends TableContentProvider {
    public static final String AUTHORITY = "com.cooliris.picasa.contentprovider";
    public static final Uri BASE_URI = Uri.parse("content:
    public static final Uri PHOTOS_URI = Uri.withAppendedPath(BASE_URI, "photos");
    public static final Uri ALBUMS_URI = Uri.withAppendedPath(BASE_URI, "albums");
    private static final String TAG = "PicasaContentProvider";
    private static final String[] ID_EDITED_PROJECTION = { "_id", "date_edited" };
    private static final String[] ID_EDITED_INDEX_PROJECTION = { "_id", "date_edited", "display_index" };
    private static final String WHERE_ACCOUNT = "sync_account=?";
    private static final String WHERE_ALBUM_ID = "album_id=?";
    private final PhotoEntry mPhotoInstance = new PhotoEntry();
    private final AlbumEntry mAlbumInstance = new AlbumEntry();
    private SyncContext mSyncContext = null;
    private Account mActiveAccount;
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        setDatabase(new Database(context, Database.DATABASE_NAME));
        addMapping(AUTHORITY, "photos", "vnd.cooliris.picasa.photo", PhotoEntry.SCHEMA);
        addMapping(AUTHORITY, "albums", "vnd.cooliris.picasa.album", AlbumEntry.SCHEMA);
        try {
            mSyncContext = new SyncContext();
        } catch (Exception e) {
            setDatabase(new Database(context, null));
        }
    }
    public static final class Database extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "picasa.db";
        public static final int DATABASE_VERSION = 83;
        public Database(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            PhotoEntry.SCHEMA.createTables(db);
            AlbumEntry.SCHEMA.createTables(db);
            UserEntry.SCHEMA.createTables(db);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            PhotoEntry.SCHEMA.dropTables(db);
            AlbumEntry.SCHEMA.dropTables(db);
            UserEntry.SCHEMA.dropTables(db);
            onCreate(db);
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        List<String> path = uri.getPathSegments();
        if (path.size() != 2 || !uri.getAuthority().equals(AUTHORITY) || selection != null) {
            return 0;
        }
        SyncContext context = mSyncContext;
        String type = path.get(0);
        long id = Long.parseLong(path.get(1));
        SQLiteDatabase db = context.db;
        if (type.equals("photos")) {
            PhotoEntry photo = mPhotoInstance;
            if (PhotoEntry.SCHEMA.queryWithId(db, id, photo)) {
                if (context.login(photo.syncAccount)) {
                    if (context.api.deleteEntry(photo.editUri) == PicasaApi.RESULT_OK) {
                        deletePhoto(db, id);
                        context.photosChanged = true;
                        return 1;
                    }
                }
            }
        } else if (type.equals("albums")) {
            AlbumEntry album = mAlbumInstance;
            if (AlbumEntry.SCHEMA.queryWithId(db, id, album)) {
                if (context.login(album.syncAccount)) {
                    if (context.api.deleteEntry(album.editUri) == PicasaApi.RESULT_OK) {
                        deleteAlbum(db, id);
                        context.albumsChanged = true;
                        return 1;
                    }
                }
            }
        }
        context.finish();
        return 0;
    }
    public void reloadAccounts() {
        mSyncContext.reloadAccounts();
    }
    public void setActiveSyncAccount(Account account) {
        mActiveAccount = account;
    }
    public void syncUsers(SyncResult syncResult) {
        syncUsers(mSyncContext, syncResult);
    }
    public void syncUsersAndAlbums(final boolean syncAlbumPhotos, SyncResult syncResult) {
        SyncContext context = mSyncContext;
        UserEntry[] users = syncUsers(context, syncResult);
        String activeUsername = null;
        if (mActiveAccount != null) {
            activeUsername = PicasaApi.canonicalizeUsername(mActiveAccount.name);
        }
        boolean didSyncActiveUserName = false;
        for (int i = 0, numUsers = users.length; i != numUsers; ++i) {
            if (activeUsername != null && !context.accounts[i].user.equals(activeUsername))
                continue;
            if (!ContentResolver.getSyncAutomatically(context.accounts[i].account, AUTHORITY))
                continue;
            didSyncActiveUserName = true;
            context.api.setAuth(context.accounts[i]);
            syncUserAlbums(context, users[i], syncResult);
            if (syncAlbumPhotos) {
                syncUserPhotos(context, users[i].account, syncResult);
            } else {
            }
        }
        if (!didSyncActiveUserName) {
            ++syncResult.stats.numAuthExceptions;
        }
        context.finish();
    }
    public void syncAlbumPhotos(final long albumId, final boolean forceRefresh, SyncResult syncResult) {
        SyncContext context = mSyncContext;
        AlbumEntry album = new AlbumEntry();
        if (AlbumEntry.SCHEMA.queryWithId(context.db, albumId, album)) {
            if ((album.photosDirty || forceRefresh) && context.login(album.syncAccount)) {
                if (isSyncEnabled(album.syncAccount, context)) {
                    syncAlbumPhotos(context, album.syncAccount, album, syncResult);
                }
            }
        }
        context.finish();
    }
    public static boolean isSyncEnabled(String accountName, SyncContext context) {
        if (context.accounts == null) {
            context.reloadAccounts();
        }
        PicasaApi.AuthAccount[] accounts = context.accounts;
        int numAccounts = accounts.length;
        for (int i = 0; i < numAccounts; ++i) {
            PicasaApi.AuthAccount account = accounts[i];
            if (account.user.equals(accountName)) {
                return ContentResolver.getSyncAutomatically(account.account, AUTHORITY);
            }
        }
        return true;
    }
    private UserEntry[] syncUsers(SyncContext context, SyncResult syncResult) {
        context.reloadAccounts();
        PicasaApi.AuthAccount[] accounts = context.accounts;
        int numUsers = accounts.length;
        UserEntry[] users = new UserEntry[numUsers];
        EntrySchema schema = UserEntry.SCHEMA;
        SQLiteDatabase db = context.db;
        Cursor cursor = schema.queryAll(db);
        if (cursor.moveToFirst()) {
            do {
                UserEntry entry = new UserEntry();
                schema.cursorToObject(cursor, entry);
                int i;
                for (i = 0; i != numUsers; ++i) {
                    if (accounts[i].user.equals(entry.account)) {
                        users[i] = entry;
                        break;
                    }
                }
                if (i == numUsers) {
                    Log.e(TAG, "Deleting user " + entry.account);
                    entry.albumsEtag = null;
                    deleteUser(db, entry.account);
                }
            } while (cursor.moveToNext());
        } else {
        }
        cursor.close();
        for (int i = 0; i != numUsers; ++i) {
            UserEntry entry = users[i];
            PicasaApi.AuthAccount account = accounts[i];
            if (entry == null) {
                entry = new UserEntry();
                entry.account = account.user;
                users[i] = entry;
                Log.e(TAG, "Inserting user " + entry.account);
            }
        }
        return users;
    }
    private void syncUserAlbums(final SyncContext context, final UserEntry user, final SyncResult syncResult) {
        final SQLiteDatabase db = context.db;
        Cursor cursor = db.query(AlbumEntry.SCHEMA.getTableName(), ID_EDITED_PROJECTION, WHERE_ACCOUNT,
                new String[] { user.account }, null, null, AlbumEntry.Columns.DATE_EDITED);
        int localCount = cursor.getCount();
        final EntryMetadata local[] = new EntryMetadata[localCount];
        for (int i = 0; i != localCount; ++i) {
            cursor.moveToPosition(i); 
            local[i] = new EntryMetadata(cursor.getLong(0), cursor.getLong(1), 0);
        }
        cursor.close();
        Arrays.sort(local);
        final EntrySchema albumSchema = AlbumEntry.SCHEMA;
        final EntryMetadata key = new EntryMetadata();
        final AccountManager accountManager = AccountManager.get(getContext());
        int result = context.api.getAlbums(accountManager, syncResult, user, new GDataParser.EntryHandler() {
            public void handleEntry(Entry entry) {
                AlbumEntry album = (AlbumEntry) entry;
                long albumId = album.id;
                key.id = albumId;
                int index = Arrays.binarySearch(local, key);
                EntryMetadata metadata = index >= 0 ? local[index] : null;
                if (metadata == null || metadata.dateEdited < album.dateEdited) {
                    Log.i(TAG, "insert / update album " + album.title);
                    album.syncAccount = user.account;
                    album.photosDirty = true;
                    albumSchema.insertOrReplace(db, album);
                    if (metadata == null) {
                        context.albumsAdded.add(albumId);
                    }
                    ++syncResult.stats.numUpdates;
                } else {
                }
                if (metadata != null) {
                    metadata.survived = true;
                }
            }
        });
        switch (result) {
        case PicasaApi.RESULT_ERROR:
            ++syncResult.stats.numParseExceptions;
        case PicasaApi.RESULT_NOT_MODIFIED:
            return;
        }
        UserEntry.SCHEMA.insertOrReplace(db, user);
        for (int i = 0; i != localCount; ++i) {
            EntryMetadata metadata = local[i];
            if (!metadata.survived) {
                deleteAlbum(db, metadata.id);
                ++syncResult.stats.numDeletes;
                Log.i(TAG, "delete album " + metadata.id);
            }
        }
        context.albumsChanged = true;
    }
    private void syncUserPhotos(SyncContext context, String account, SyncResult syncResult) {
        SQLiteDatabase db = context.db;
        Cursor cursor = db.query(AlbumEntry.SCHEMA.getTableName(), Entry.ID_PROJECTION, "sync_account=? AND photos_dirty=1",
                new String[] { account }, null, null, null);
        AlbumEntry album = new AlbumEntry();
        for (int i = 0, count = cursor.getCount(); i != count; ++i) {
            cursor.moveToPosition(i);
            if (AlbumEntry.SCHEMA.queryWithId(db, cursor.getLong(0), album)) {
                syncAlbumPhotos(context, account, album, syncResult);
            }
            if (Thread.interrupted()) {
                ++syncResult.stats.numIoExceptions;
                Log.e(TAG, "syncUserPhotos interrupted");
            }
        }
        cursor.close();
    }
    private void syncAlbumPhotos(SyncContext context, final String account, AlbumEntry album, final SyncResult syncResult) {
        Log.i(TAG, "Syncing Picasa album: " + album.title);
        final SQLiteDatabase db = context.db;
        long albumId = album.id;
        String[] albumIdArgs = { Long.toString(albumId) };
        Cursor cursor = db.query(PhotoEntry.SCHEMA.getTableName(), ID_EDITED_INDEX_PROJECTION, WHERE_ALBUM_ID, albumIdArgs, null,
                null, "date_edited");
        int localCount = cursor.getCount();
        final EntryMetadata local[] = new EntryMetadata[localCount];
        final EntryMetadata key = new EntryMetadata();
        for (int i = 0; i != localCount; ++i) {
            cursor.moveToPosition(i); 
            local[i] = new EntryMetadata(cursor.getLong(0), cursor.getLong(1), cursor.getInt(2));
        }
        cursor.close();
        Arrays.sort(local);
        final EntrySchema photoSchema = PhotoEntry.SCHEMA;
        final int[] displayIndex = { 0 };
        final AccountManager accountManager = AccountManager.get(getContext());
        int result = context.api.getAlbumPhotos(accountManager, syncResult, album, new GDataParser.EntryHandler() {
            public void handleEntry(Entry entry) {
                PhotoEntry photo = (PhotoEntry) entry;
                long photoId = photo.id;
                int newDisplayIndex = displayIndex[0];
                key.id = photoId;
                int index = Arrays.binarySearch(local, key);
                EntryMetadata metadata = index >= 0 ? local[index] : null;
                if (metadata == null || metadata.dateEdited < photo.dateEdited || metadata.displayIndex != newDisplayIndex) {
                    photo.syncAccount = account;
                    photo.displayIndex = newDisplayIndex;
                    photoSchema.insertOrReplace(db, photo);
                    ++syncResult.stats.numUpdates;
                } else {
                }
                if (metadata != null) {
                    metadata.survived = true;
                }
                displayIndex[0] = newDisplayIndex + 1;
            }
        });
        switch (result) {
        case PicasaApi.RESULT_ERROR:
            ++syncResult.stats.numParseExceptions;
            Log.e(TAG, "syncAlbumPhotos error");
        case PicasaApi.RESULT_NOT_MODIFIED:
            return;
        }
        for (int i = 0; i != localCount; ++i) {
            EntryMetadata metadata = local[i];
            if (!metadata.survived) {
                deletePhoto(db, metadata.id);
                ++syncResult.stats.numDeletes;
            }
        }
        album.photosDirty = false;
        AlbumEntry.SCHEMA.insertOrReplace(db, album);
        getContext().getContentResolver().notifyChange(ALBUMS_URI, null, false);
        getContext().getContentResolver().notifyChange(PHOTOS_URI, null, false);
    }
    private void deleteUser(SQLiteDatabase db, String account) {
        Log.w(TAG, "deleteUser(" + account + ")");
        String albumTableName = AlbumEntry.SCHEMA.getTableName();
        String[] whereArgs = { account };
        Cursor cursor = db.query(AlbumEntry.SCHEMA.getTableName(), Entry.ID_PROJECTION, WHERE_ACCOUNT, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                deleteAlbumPhotos(db, cursor.getLong(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.delete(albumTableName, WHERE_ACCOUNT, whereArgs);
        db.delete(UserEntry.SCHEMA.getTableName(), "account=?", whereArgs);
    }
    private void deleteAlbum(SQLiteDatabase db, long albumId) {
        deleteAlbumPhotos(db, albumId);
        AlbumEntry.SCHEMA.deleteWithId(db, albumId);
    }
    private void deleteAlbumPhotos(SQLiteDatabase db, long albumId) {
        Log.v(TAG, "deleteAlbumPhotos(" + albumId + ")");
        String photoTableName = PhotoEntry.SCHEMA.getTableName();
        String[] whereArgs = { Long.toString(albumId) };
        Cursor cursor = db.query(photoTableName, Entry.ID_PROJECTION, WHERE_ALBUM_ID, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                deletePhotoCache(cursor.getLong(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.delete(photoTableName, WHERE_ALBUM_ID, whereArgs);
    }
    private void deletePhoto(SQLiteDatabase db, long photoId) {
        PhotoEntry.SCHEMA.deleteWithId(db, photoId);
        deletePhotoCache(photoId);
    }
    private void deletePhotoCache(long photoId) {
    }
    private final class SyncContext {
        public PicasaApi.AuthAccount[] accounts;
        public PicasaApi api = new PicasaApi();
        public SQLiteDatabase db;
        public final ArrayList<Long> albumsAdded = new ArrayList<Long>();
        public boolean albumsChanged = false;
        public boolean photosChanged = false;
        public SyncContext() {
            db = mDatabase.getWritableDatabase();
        }
        public void reloadAccounts() {
            accounts = PicasaApi.getAuthenticatedAccounts(getContext());
        }
        public void finish() {
            ContentResolver cr = getContext().getContentResolver();
            if (albumsChanged) {
                cr.notifyChange(ALBUMS_URI, null, false);
            }
            if (photosChanged) {
                cr.notifyChange(PHOTOS_URI, null, false);
            }
            albumsChanged = false;
            photosChanged = false;
        }
        public boolean login(String user) {
            if (accounts == null) {
                reloadAccounts();
            }
            final PicasaApi.AuthAccount[] authAccounts = accounts;
            for (PicasaApi.AuthAccount auth : authAccounts) {
                if (auth.user.equals(user)) {
                    api.setAuth(auth);
                    return true;
                }
            }
            return false;
        }
    }
    private static final class EntryMetadata implements Comparable<EntryMetadata> {
        public long id;
        public long dateEdited;
        public int displayIndex;
        public boolean survived = false;
        public EntryMetadata() {
        }
        public EntryMetadata(long id, long dateEdited, int displayIndex) {
            this.id = id;
            this.dateEdited = dateEdited;
            this.displayIndex = displayIndex;
        }
        public int compareTo(EntryMetadata other) {
            return Long.signum(id - other.id);
        }
    }
}
