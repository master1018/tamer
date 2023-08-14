abstract public class ContentProviderNative extends Binder implements IContentProvider {
    private static final String TAG = "ContentProvider";
    public ContentProviderNative()
    {
        attachInterface(this, descriptor);
    }
    static public IContentProvider asInterface(IBinder obj)
    {
        if (obj == null) {
            return null;
        }
        IContentProvider in =
            (IContentProvider)obj.queryLocalInterface(descriptor);
        if (in != null) {
            return in;
        }
        return new ContentProviderProxy(obj);
    }
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
            switch (code) {
                case QUERY_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    int num = data.readInt();
                    String[] projection = null;
                    if (num > 0) {
                        projection = new String[num];
                        for (int i = 0; i < num; i++) {
                            projection[i] = data.readString();
                        }
                    }
                    String selection = data.readString();
                    num = data.readInt();
                    String[] selectionArgs = null;
                    if (num > 0) {
                        selectionArgs = new String[num];
                        for (int i = 0; i < num; i++) {
                            selectionArgs[i] = data.readString();
                        }
                    }
                    String sortOrder = data.readString();
                    IContentObserver observer = IContentObserver.Stub.
                        asInterface(data.readStrongBinder());
                    CursorWindow window = CursorWindow.CREATOR.createFromParcel(data);
                    boolean wantsCursorMetadata = data.readInt() != 0;
                    IBulkCursor bulkCursor = bulkQuery(url, projection, selection,
                            selectionArgs, sortOrder, observer, window);
                    reply.writeNoException();
                    if (bulkCursor != null) {
                        reply.writeStrongBinder(bulkCursor.asBinder());
                        if (wantsCursorMetadata) {
                            reply.writeInt(bulkCursor.count());
                            reply.writeInt(BulkCursorToCursorAdaptor.findRowIdColumnIndex(
                                bulkCursor.getColumnNames()));
                        }
                    } else {
                        reply.writeStrongBinder(null);
                    }
                    return true;
                }
                case GET_TYPE_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    String type = getType(url);
                    reply.writeNoException();
                    reply.writeString(type);
                    return true;
                }
                case INSERT_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    ContentValues values = ContentValues.CREATOR.createFromParcel(data);
                    Uri out = insert(url, values);
                    reply.writeNoException();
                    Uri.writeToParcel(reply, out);
                    return true;
                }
                case BULK_INSERT_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    ContentValues[] values = data.createTypedArray(ContentValues.CREATOR);
                    int count = bulkInsert(url, values);
                    reply.writeNoException();
                    reply.writeInt(count);
                    return true;
                }
                case APPLY_BATCH_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    final int numOperations = data.readInt();
                    final ArrayList<ContentProviderOperation> operations =
                            new ArrayList<ContentProviderOperation>(numOperations);
                    for (int i = 0; i < numOperations; i++) {
                        operations.add(i, ContentProviderOperation.CREATOR.createFromParcel(data));
                    }
                    final ContentProviderResult[] results = applyBatch(operations);
                    reply.writeNoException();
                    reply.writeTypedArray(results, 0);
                    return true;
                }
                case DELETE_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    String selection = data.readString();
                    String[] selectionArgs = data.readStringArray();
                    int count = delete(url, selection, selectionArgs);
                    reply.writeNoException();
                    reply.writeInt(count);
                    return true;
                }
                case UPDATE_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    ContentValues values = ContentValues.CREATOR.createFromParcel(data);
                    String selection = data.readString();
                    String[] selectionArgs = data.readStringArray();
                    int count = update(url, values, selection, selectionArgs);
                    reply.writeNoException();
                    reply.writeInt(count);
                    return true;
                }
                case OPEN_FILE_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    String mode = data.readString();
                    ParcelFileDescriptor fd;
                    fd = openFile(url, mode);
                    reply.writeNoException();
                    if (fd != null) {
                        reply.writeInt(1);
                        fd.writeToParcel(reply,
                                Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case OPEN_ASSET_FILE_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    Uri url = Uri.CREATOR.createFromParcel(data);
                    String mode = data.readString();
                    AssetFileDescriptor fd;
                    fd = openAssetFile(url, mode);
                    reply.writeNoException();
                    if (fd != null) {
                        reply.writeInt(1);
                        fd.writeToParcel(reply,
                                Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case CALL_TRANSACTION:
                {
                    data.enforceInterface(IContentProvider.descriptor);
                    String method = data.readString();
                    String stringArg = data.readString();
                    Bundle args = data.readBundle();
                    Bundle responseBundle = call(method, stringArg, args);
                    reply.writeNoException();
                    reply.writeBundle(responseBundle);
                    return true;
                }
            }
        } catch (Exception e) {
            DatabaseUtils.writeExceptionToParcel(reply, e);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }
    public IBinder asBinder()
    {
        return this;
    }
}
final class ContentProviderProxy implements IContentProvider
{
    public ContentProviderProxy(IBinder remote)
    {
        mRemote = remote;
    }
    public IBinder asBinder()
    {
        return mRemote;
    }
    private IBulkCursor bulkQueryInternal(
        Uri url, String[] projection,
        String selection, String[] selectionArgs, String sortOrder,
        IContentObserver observer, CursorWindow window,
        BulkCursorToCursorAdaptor adaptor) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        int length = 0;
        if (projection != null) {
            length = projection.length;
        }
        data.writeInt(length);
        for (int i = 0; i < length; i++) {
            data.writeString(projection[i]);
        }
        data.writeString(selection);
        if (selectionArgs != null) {
            length = selectionArgs.length;
        } else {
            length = 0;
        }
        data.writeInt(length);
        for (int i = 0; i < length; i++) {
            data.writeString(selectionArgs[i]);
        }
        data.writeString(sortOrder);
        data.writeStrongBinder(observer.asBinder());
        window.writeToParcel(data, 0);
        final boolean wantsCursorMetadata = (adaptor != null);
        data.writeInt(wantsCursorMetadata ? 1 : 0);
        mRemote.transact(IContentProvider.QUERY_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        IBulkCursor bulkCursor = null;
        IBinder bulkCursorBinder = reply.readStrongBinder();
        if (bulkCursorBinder != null) {
            bulkCursor = BulkCursorNative.asInterface(bulkCursorBinder);
            if (wantsCursorMetadata) {
                int rowCount = reply.readInt();
                int idColumnPosition = reply.readInt();
                if (bulkCursor != null) {
                    adaptor.set(bulkCursor, rowCount, idColumnPosition);
                }
            }
        }
        data.recycle();
        reply.recycle();
        return bulkCursor;
    }
    public IBulkCursor bulkQuery(Uri url, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, IContentObserver observer,
            CursorWindow window) throws RemoteException {
        return bulkQueryInternal(
            url, projection, selection, selectionArgs, sortOrder,
            observer, window,
            null );
    }
    public Cursor query(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) throws RemoteException {
        CursorWindow window = new CursorWindow(false );
        BulkCursorToCursorAdaptor adaptor = new BulkCursorToCursorAdaptor();
        IBulkCursor bulkCursor = bulkQueryInternal(
            url, projection, selection, selectionArgs, sortOrder,
            adaptor.getObserver(), window,
            adaptor);
        if (bulkCursor == null) {
            return null;
        }
        return adaptor;
    }
    public String getType(Uri url) throws RemoteException
    {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        mRemote.transact(IContentProvider.GET_TYPE_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        String out = reply.readString();
        data.recycle();
        reply.recycle();
        return out;
    }
    public Uri insert(Uri url, ContentValues values) throws RemoteException
    {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        values.writeToParcel(data, 0);
        mRemote.transact(IContentProvider.INSERT_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        Uri out = Uri.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return out;
    }
    public int bulkInsert(Uri url, ContentValues[] values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        data.writeTypedArray(values, 0);
        mRemote.transact(IContentProvider.BULK_INSERT_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        int count = reply.readInt();
        data.recycle();
        reply.recycle();
        return count;
    }
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        data.writeInt(operations.size());
        for (ContentProviderOperation operation : operations) {
            operation.writeToParcel(data, 0);
        }
        mRemote.transact(IContentProvider.APPLY_BATCH_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionWithOperationApplicationExceptionFromParcel(reply);
        final ContentProviderResult[] results =
                reply.createTypedArray(ContentProviderResult.CREATOR);
        data.recycle();
        reply.recycle();
        return results;
    }
    public int delete(Uri url, String selection, String[] selectionArgs)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        data.writeString(selection);
        data.writeStringArray(selectionArgs);
        mRemote.transact(IContentProvider.DELETE_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        int count = reply.readInt();
        data.recycle();
        reply.recycle();
        return count;
    }
    public int update(Uri url, ContentValues values, String selection,
            String[] selectionArgs) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        values.writeToParcel(data, 0);
        data.writeString(selection);
        data.writeStringArray(selectionArgs);
        mRemote.transact(IContentProvider.UPDATE_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        int count = reply.readInt();
        data.recycle();
        reply.recycle();
        return count;
    }
    public ParcelFileDescriptor openFile(Uri url, String mode)
            throws RemoteException, FileNotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        data.writeString(mode);
        mRemote.transact(IContentProvider.OPEN_FILE_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
        int has = reply.readInt();
        ParcelFileDescriptor fd = has != 0 ? reply.readFileDescriptor() : null;
        data.recycle();
        reply.recycle();
        return fd;
    }
    public AssetFileDescriptor openAssetFile(Uri url, String mode)
            throws RemoteException, FileNotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        url.writeToParcel(data, 0);
        data.writeString(mode);
        mRemote.transact(IContentProvider.OPEN_ASSET_FILE_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
        int has = reply.readInt();
        AssetFileDescriptor fd = has != 0
                ? AssetFileDescriptor.CREATOR.createFromParcel(reply) : null;
        data.recycle();
        reply.recycle();
        return fd;
    }
    public Bundle call(String method, String request, Bundle args)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IContentProvider.descriptor);
        data.writeString(method);
        data.writeString(request);
        data.writeBundle(args);
        mRemote.transact(IContentProvider.CALL_TRANSACTION, data, reply, 0);
        DatabaseUtils.readExceptionFromParcel(reply);
        Bundle bundle = reply.readBundle();
        data.recycle();
        reply.recycle();
        return bundle;
    }
    private IBinder mRemote;
}
