public class ContentProviderClient {
    private final IContentProvider mContentProvider;
    private final ContentResolver mContentResolver;
    ContentProviderClient(ContentResolver contentResolver, IContentProvider contentProvider) {
        mContentProvider = contentProvider;
        mContentResolver = contentResolver;
    }
    public Cursor query(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) throws RemoteException {
        return mContentProvider.query(url, projection, selection,  selectionArgs, sortOrder);
    }
    public String getType(Uri url) throws RemoteException {
        return mContentProvider.getType(url);
    }
    public Uri insert(Uri url, ContentValues initialValues)
            throws RemoteException {
        return mContentProvider.insert(url, initialValues);
    }
    public int bulkInsert(Uri url, ContentValues[] initialValues) throws RemoteException {
        return mContentProvider.bulkInsert(url, initialValues);
    }
    public int delete(Uri url, String selection, String[] selectionArgs)
            throws RemoteException {
        return mContentProvider.delete(url, selection, selectionArgs);
    }
    public int update(Uri url, ContentValues values, String selection,
            String[] selectionArgs) throws RemoteException {
        return mContentProvider.update(url, values, selection, selectionArgs);
    }
    public ParcelFileDescriptor openFile(Uri url, String mode)
            throws RemoteException, FileNotFoundException {
        return mContentProvider.openFile(url, mode);
    }
    public AssetFileDescriptor openAssetFile(Uri url, String mode)
            throws RemoteException, FileNotFoundException {
        return mContentProvider.openAssetFile(url, mode);
    }
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        return mContentProvider.applyBatch(operations);
    }
    public boolean release() {
        return mContentResolver.releaseProvider(mContentProvider);
    }
    public ContentProvider getLocalContentProvider() {
        return ContentProvider.coerceToLocalContentProvider(mContentProvider);
    }
}
