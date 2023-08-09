public class MockIContentProvider implements IContentProvider {
    public int bulkInsert(Uri url, ContentValues[] initialValues) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public IBulkCursor bulkQuery(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, IContentObserver observer,
            CursorWindow window) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @SuppressWarnings("unused")
    public int delete(Uri url, String selection, String[] selectionArgs)
            throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public String getType(Uri url) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @SuppressWarnings("unused")
    public Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public ParcelFileDescriptor openFile(Uri url, String mode) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public EntityIterator queryEntities(Uri url, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs)
            throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public Bundle call(String method, String request, Bundle args)
            throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public IBinder asBinder() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
}
