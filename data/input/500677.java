public class BatchOperation {
    private final String TAG = "BatchOperation";
    private final ContentResolver mResolver;
    ArrayList<ContentProviderOperation> mOperations;
    public BatchOperation(Context context, ContentResolver resolver) {
        mResolver = resolver;
        mOperations = new ArrayList<ContentProviderOperation>();
    }
    public int size() {
        return mOperations.size();
    }
    public void add(ContentProviderOperation cpo) {
        mOperations.add(cpo);
    }
    public void execute() {
        if (mOperations.size() == 0) {
            return;
        }
        try {
            mResolver.applyBatch(ContactsContract.AUTHORITY, mOperations);
        } catch (final OperationApplicationException e1) {
            Log.e(TAG, "storing contact data failed", e1);
        } catch (final RemoteException e2) {
            Log.e(TAG, "storing contact data failed", e2);
        }
        mOperations.clear();
    }
}
