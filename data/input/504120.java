public class MockContentProvider extends ContentProvider {
    private class InversionIContentProvider implements IContentProvider {
        @SuppressWarnings("unused")
        public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
                throws RemoteException, OperationApplicationException {
            return MockContentProvider.this.applyBatch(operations);
        }
        @SuppressWarnings("unused")
        public int bulkInsert(Uri url, ContentValues[] initialValues) throws RemoteException {
            return MockContentProvider.this.bulkInsert(url, initialValues);
        }
        @SuppressWarnings("unused")
        public IBulkCursor bulkQuery(Uri url, String[] projection, String selection,
                String[] selectionArgs, String sortOrder, IContentObserver observer,
                CursorWindow window) throws RemoteException {
            throw new UnsupportedOperationException("Must not come here");
        }
        @SuppressWarnings("unused")
        public int delete(Uri url, String selection, String[] selectionArgs)
                throws RemoteException {
            return MockContentProvider.this.delete(url, selection, selectionArgs);
        }
        @SuppressWarnings("unused")
        public String getType(Uri url) throws RemoteException {
            return MockContentProvider.this.getType(url);
        }
        @SuppressWarnings("unused")
        public Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
            return MockContentProvider.this.insert(url, initialValues);
        }
        @SuppressWarnings("unused")
        public AssetFileDescriptor openAssetFile(Uri url, String mode) throws RemoteException,
                FileNotFoundException {
            return MockContentProvider.this.openAssetFile(url, mode);
        }
        @SuppressWarnings("unused")
        public ParcelFileDescriptor openFile(Uri url, String mode) throws RemoteException,
                FileNotFoundException {
            return MockContentProvider.this.openFile(url, mode);
        }
        @SuppressWarnings("unused")
        public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) throws RemoteException {
            return MockContentProvider.this.query(url, projection, selection,
                    selectionArgs, sortOrder);
        }
        @SuppressWarnings("unused")
        public int update(Uri url, ContentValues values, String selection, String[] selectionArgs)
                throws RemoteException {
            return MockContentProvider.this.update(url, values, selection, selectionArgs);
        }
        @SuppressWarnings("unused")
        public Bundle call(String method, String request, Bundle args)
                throws RemoteException {
            return MockContentProvider.this.call(method, request, args);
        }
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
    private final InversionIContentProvider mIContentProvider = new InversionIContentProvider();
    protected MockContentProvider() {
        super(new MockContext(), "", "", null);
    }
    public MockContentProvider(Context context) {
        super(context, "", "", null);
    }
    public MockContentProvider(Context context,
            String readPermission,
            String writePermission,
            PathPermission[] pathPermissions) {
        super(context, readPermission, writePermission, pathPermissions);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public boolean onCreate() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public Bundle call(String method, String request, Bundle args) {
        throw new UnsupportedOperationException("unimplemented mock method call");
    }
    @Override
    public final IContentProvider getIContentProvider() {
        return mIContentProvider;
    }
}
