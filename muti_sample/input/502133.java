public final class BulkCursorToCursorAdaptor extends AbstractWindowedCursor {
    private static final String TAG = "BulkCursor";
    private SelfContentObserver mObserverBridge;
    private IBulkCursor mBulkCursor;
    private int mCount;
    private String[] mColumns;
    private boolean mWantsAllOnMoveCalls;
    public void set(IBulkCursor bulkCursor) {
        mBulkCursor = bulkCursor;
        try {
            mCount = mBulkCursor.count();
            mWantsAllOnMoveCalls = mBulkCursor.getWantsAllOnMoveCalls();
            mColumns = mBulkCursor.getColumnNames();
            mRowIdColumnIndex = findRowIdColumnIndex(mColumns);
        } catch (RemoteException ex) {
            Log.e(TAG, "Setup failed because the remote process is dead");
        }
    }
    public void set(IBulkCursor bulkCursor, int count, int idIndex) {
        mBulkCursor = bulkCursor;
        mColumns = null;  
        mCount = count;
        mRowIdColumnIndex = idIndex;
    }
    public static int findRowIdColumnIndex(String[] columnNames) {
        int length = columnNames.length;
        for (int i = 0; i < length; i++) {
            if (columnNames[i].equals("_id")) {
                return i;
            }
        }
        return -1;
    }
    public synchronized IContentObserver getObserver() {
        if (mObserverBridge == null) {
            mObserverBridge = new SelfContentObserver(this);
        }
        return mObserverBridge.getContentObserver();
    }
    @Override
    public int getCount() {
        return mCount;
    }
    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        try {
            if (mWindow != null) {
                if (newPosition < mWindow.getStartPosition() ||
                        newPosition >= (mWindow.getStartPosition() + mWindow.getNumRows())) {
                    mWindow = mBulkCursor.getWindow(newPosition);
                } else if (mWantsAllOnMoveCalls) {
                    mBulkCursor.onMove(newPosition);
                }
            } else {
                mWindow = mBulkCursor.getWindow(newPosition);
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "Unable to get window because the remote process is dead");
            return false;
        }
        if (mWindow == null) {
            return false;
        }
        return true;
    }
    @Override
    public void deactivate() {
        super.deactivate();
        try {
            mBulkCursor.deactivate();
        } catch (RemoteException ex) {
            Log.w(TAG, "Remote process exception when deactivating");
        }
        mWindow = null;
    }
    @Override
    public void close() {
        super.close();
        try {
            mBulkCursor.close();
        } catch (RemoteException ex) {
            Log.w(TAG, "Remote process exception when closing");
        }
        mWindow = null;        
    }
    @Override
    public boolean requery() {
        try {
            int oldCount = mCount;
            mCount = mBulkCursor.requery(getObserver(), new CursorWindow(
                    false ));
            if (mCount != -1) {
                mPos = -1;
                mWindow = null;
                super.requery();
                return true;
            } else {
                deactivate();
                return false;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to requery because the remote process exception " + ex.getMessage());
            deactivate();
            return false;
        }
    }
    @Override
    public boolean deleteRow() {
        try {
            boolean result = mBulkCursor.deleteRow(mPos);
            if (result != false) {
                mWindow = null;
                mCount = mBulkCursor.count();
                if (mPos < mCount) {
                    int oldPos = mPos;
                    mPos = -1;
                    moveToPosition(oldPos);
                } else {
                    mPos = mCount;
                }
                onChange(true);
            }
            return result;
        } catch (RemoteException ex) {
            Log.e(TAG, "Unable to delete row because the remote process is dead");
            return false;
        }
    }
    @Override
    public String[] getColumnNames() {
        if (mColumns == null) {
            try {
                mColumns = mBulkCursor.getColumnNames();
            } catch (RemoteException ex) {
                Log.e(TAG, "Unable to fetch column names because the remote process is dead");
                return null;
            }
        }
        return mColumns;
    }
    @Override
    public boolean commitUpdates(Map<? extends Long,
            ? extends Map<String,Object>> additionalValues) {
        if (!supportsUpdates()) {
            Log.e(TAG, "commitUpdates not supported on this cursor, did you include the _id column?");
            return false;
        }
        synchronized(mUpdatedRows) {
            if (additionalValues != null) {
                mUpdatedRows.putAll(additionalValues);
            }
            if (mUpdatedRows.size() <= 0) {
                return false;
            }
            try {
                boolean result = mBulkCursor.updateRows(mUpdatedRows);
                if (result == true) {
                    mUpdatedRows.clear();
                    onChange(true);
                }
                return result;
            } catch (RemoteException ex) {
                Log.e(TAG, "Unable to commit updates because the remote process is dead");
                return false;
            }
        }
    }
    @Override
    public Bundle getExtras() {
        try {
            return mBulkCursor.getExtras();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Bundle respond(Bundle extras) {
        try {
            return mBulkCursor.respond(extras);
        } catch (RemoteException e) {
            Log.w(TAG, "respond() threw RemoteException, returning an empty bundle.", e);
            return Bundle.EMPTY;
        }
    }
}
