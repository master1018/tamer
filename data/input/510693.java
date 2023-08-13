public abstract class ContentProvider implements ComponentCallbacks {
    private Context mContext = null;
    private int mMyUid;
    private String mReadPermission;
    private String mWritePermission;
    private PathPermission[] mPathPermissions;
    private Transport mTransport = new Transport();
    public ContentProvider() {
    }
    public ContentProvider(
            Context context,
            String readPermission,
            String writePermission,
            PathPermission[] pathPermissions) {
        mContext = context;
        mReadPermission = readPermission;
        mWritePermission = writePermission;
        mPathPermissions = pathPermissions;
    }
    public static ContentProvider coerceToLocalContentProvider(
            IContentProvider abstractInterface) {
        if (abstractInterface instanceof Transport) {
            return ((Transport)abstractInterface).getContentProvider();
        }
        return null;
    }
    class Transport extends ContentProviderNative {
        ContentProvider getContentProvider() {
            return ContentProvider.this;
        }
        public IBulkCursor bulkQuery(Uri uri, String[] projection,
                String selection, String[] selectionArgs, String sortOrder,
                IContentObserver observer, CursorWindow window) {
            enforceReadPermission(uri);
            Cursor cursor = ContentProvider.this.query(uri, projection,
                    selection, selectionArgs, sortOrder);
            if (cursor == null) {
                return null;
            }
            return new CursorToBulkCursorAdaptor(cursor, observer,
                    ContentProvider.this.getClass().getName(),
                    hasWritePermission(uri), window);
        }
        public Cursor query(Uri uri, String[] projection,
                String selection, String[] selectionArgs, String sortOrder) {
            enforceReadPermission(uri);
            return ContentProvider.this.query(uri, projection, selection,
                    selectionArgs, sortOrder);
        }
        public String getType(Uri uri) {
            return ContentProvider.this.getType(uri);
        }
        public Uri insert(Uri uri, ContentValues initialValues) {
            enforceWritePermission(uri);
            return ContentProvider.this.insert(uri, initialValues);
        }
        public int bulkInsert(Uri uri, ContentValues[] initialValues) {
            enforceWritePermission(uri);
            return ContentProvider.this.bulkInsert(uri, initialValues);
        }
        public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
                throws OperationApplicationException {
            for (ContentProviderOperation operation : operations) {
                if (operation.isReadOperation()) {
                    enforceReadPermission(operation.getUri());
                }
                if (operation.isWriteOperation()) {
                    enforceWritePermission(operation.getUri());
                }
            }
            return ContentProvider.this.applyBatch(operations);
        }
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            enforceWritePermission(uri);
            return ContentProvider.this.delete(uri, selection, selectionArgs);
        }
        public int update(Uri uri, ContentValues values, String selection,
                String[] selectionArgs) {
            enforceWritePermission(uri);
            return ContentProvider.this.update(uri, values, selection, selectionArgs);
        }
        public ParcelFileDescriptor openFile(Uri uri, String mode)
                throws FileNotFoundException {
            if (mode != null && mode.startsWith("rw")) enforceWritePermission(uri);
            else enforceReadPermission(uri);
            return ContentProvider.this.openFile(uri, mode);
        }
        public AssetFileDescriptor openAssetFile(Uri uri, String mode)
                throws FileNotFoundException {
            if (mode != null && mode.startsWith("rw")) enforceWritePermission(uri);
            else enforceReadPermission(uri);
            return ContentProvider.this.openAssetFile(uri, mode);
        }
        public Bundle call(String method, String request, Bundle args) {
            return ContentProvider.this.call(method, request, args);
        }
        private void enforceReadPermission(Uri uri) {
            final int uid = Binder.getCallingUid();
            if (uid == mMyUid) {
                return;
            }
            final Context context = getContext();
            final String rperm = getReadPermission();
            final int pid = Binder.getCallingPid();
            if (rperm == null
                    || context.checkPermission(rperm, pid, uid)
                    == PackageManager.PERMISSION_GRANTED) {
                return;
            }
            PathPermission[] pps = getPathPermissions();
            if (pps != null) {
                final String path = uri.getPath();
                int i = pps.length;
                while (i > 0) {
                    i--;
                    final PathPermission pp = pps[i];
                    final String pprperm = pp.getReadPermission();
                    if (pprperm != null && pp.match(path)) {
                        if (context.checkPermission(pprperm, pid, uid)
                                == PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                }
            }
            if (context.checkUriPermission(uri, pid, uid,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String msg = "Permission Denial: reading "
                    + ContentProvider.this.getClass().getName()
                    + " uri " + uri + " from pid=" + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " requires " + rperm;
            throw new SecurityException(msg);
        }
        private boolean hasWritePermission(Uri uri) {
            final int uid = Binder.getCallingUid();
            if (uid == mMyUid) {
                return true;
            }
            final Context context = getContext();
            final String wperm = getWritePermission();
            final int pid = Binder.getCallingPid();
            if (wperm == null
                    || context.checkPermission(wperm, pid, uid)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            PathPermission[] pps = getPathPermissions();
            if (pps != null) {
                final String path = uri.getPath();
                int i = pps.length;
                while (i > 0) {
                    i--;
                    final PathPermission pp = pps[i];
                    final String ppwperm = pp.getWritePermission();
                    if (ppwperm != null && pp.match(path)) {
                        if (context.checkPermission(ppwperm, pid, uid)
                                == PackageManager.PERMISSION_GRANTED) {
                            return true;
                        }
                    }
                }
            }
            if (context.checkUriPermission(uri, pid, uid,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            return false;
        }
        private void enforceWritePermission(Uri uri) {
            if (hasWritePermission(uri)) {
                return;
            }
            String msg = "Permission Denial: writing "
                    + ContentProvider.this.getClass().getName()
                    + " uri " + uri + " from pid=" + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " requires " + getWritePermission();
            throw new SecurityException(msg);
        }
    }
    public final Context getContext() {
        return mContext;
    }
    protected final void setReadPermission(String permission) {
        mReadPermission = permission;
    }
    public final String getReadPermission() {
        return mReadPermission;
    }
    protected final void setWritePermission(String permission) {
        mWritePermission = permission;
    }
    public final String getWritePermission() {
        return mWritePermission;
    }
    protected final void setPathPermissions(PathPermission[] permissions) {
        mPathPermissions = permissions;
    }
    public final PathPermission[] getPathPermissions() {
        return mPathPermissions;
    }
    public abstract boolean onCreate();
    public void onConfigurationChanged(Configuration newConfig) {
    }
    public void onLowMemory() {
    }
    public abstract Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder);
    public abstract String getType(Uri uri);
    public abstract Uri insert(Uri uri, ContentValues values);
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        for (int i = 0; i < numValues; i++) {
            insert(uri, values[i]);
        }
        return numValues;
    }
    public abstract int delete(Uri uri, String selection, String[] selectionArgs);
    public abstract int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs);
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        throw new FileNotFoundException("No files supported by provider at "
                + uri);
    }
    public AssetFileDescriptor openAssetFile(Uri uri, String mode)
            throws FileNotFoundException {
        ParcelFileDescriptor fd = openFile(uri, mode);
        return fd != null ? new AssetFileDescriptor(fd, 0, -1) : null;
    }
    protected final ParcelFileDescriptor openFileHelper(Uri uri,
            String mode) throws FileNotFoundException {
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }
        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        String path = (i >= 0 ? c.getString(i) : null);
        c.close();
        if (path == null) {
            throw new FileNotFoundException("Column _data not found.");
        }
        int modeBits = ContentResolver.modeToMode(uri, mode);
        return ParcelFileDescriptor.open(new File(path), modeBits);
    }
    protected boolean isTemporary() {
        return false;
    }
    public IContentProvider getIContentProvider() {
        return mTransport;
    }
    public void attachInfo(Context context, ProviderInfo info) {
        if (mContext == null) {
            mContext = context;
            mMyUid = Process.myUid();
            if (info != null) {
                setReadPermission(info.readPermission);
                setWritePermission(info.writePermission);
                setPathPermissions(info.pathPermissions);
            }
            ContentProvider.this.onCreate();
        }
    }
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final int numOperations = operations.size();
        final ContentProviderResult[] results = new ContentProviderResult[numOperations];
        for (int i = 0; i < numOperations; i++) {
            results[i] = operations.get(i).apply(this, results, i);
        }
        return results;
    }
    public Bundle call(String method, String request, Bundle args) {
        return null;
    }
}
