public class RestoreSession {
    static final String TAG = "RestoreSession";
    final Context mContext;
    IRestoreSession mBinder;
    RestoreObserverWrapper mObserver = null;
    public int getAvailableRestoreSets(RestoreObserver observer) {
        int err = -1;
        RestoreObserverWrapper obsWrapper = new RestoreObserverWrapper(mContext, observer);
        try {
            err = mBinder.getAvailableRestoreSets(obsWrapper);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't contact server to get available sets");
        }
        return err;
    }
    public int restoreAll(long token, RestoreObserver observer) {
        int err = -1;
        if (mObserver != null) {
            Log.d(TAG, "restoreAll() called during active restore");
            return -1;
        }
        mObserver = new RestoreObserverWrapper(mContext, observer);
        try {
            err = mBinder.restoreAll(token, mObserver);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't contact server to restore");
        }
        return err;
    }
    public int restorePackage(String packageName, RestoreObserver observer) {
        int err = -1;
        if (mObserver != null) {
            Log.d(TAG, "restorePackage() called during active restore");
            return -1;
        }
        mObserver = new RestoreObserverWrapper(mContext, observer);
        try {
            err = mBinder.restorePackage(packageName, mObserver);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't contact server to restore package");
        }
        return err;
    }
    public void endRestoreSession() {
        try {
            mBinder.endRestoreSession();
        } catch (RemoteException e) {
            Log.d(TAG, "Can't contact server to get available sets");
        } finally {
            mBinder = null;
        }
    }
    RestoreSession(Context context, IRestoreSession binder) {
        mContext = context;
        mBinder = binder;
    }
    private class RestoreObserverWrapper extends IRestoreObserver.Stub {
        final Handler mHandler;
        final RestoreObserver mAppObserver;
        static final int MSG_RESTORE_STARTING = 1;
        static final int MSG_UPDATE = 2;
        static final int MSG_RESTORE_FINISHED = 3;
        static final int MSG_RESTORE_SETS_AVAILABLE = 4;
        RestoreObserverWrapper(Context context, RestoreObserver appObserver) {
            mHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case MSG_RESTORE_STARTING:
                        mAppObserver.restoreStarting(msg.arg1);
                        break;
                    case MSG_UPDATE:
                        mAppObserver.onUpdate(msg.arg1, (String)msg.obj);
                        break;
                    case MSG_RESTORE_FINISHED:
                        mAppObserver.restoreFinished(msg.arg1);
                        break;
                    case MSG_RESTORE_SETS_AVAILABLE:
                        mAppObserver.restoreSetsAvailable((RestoreSet[])msg.obj);
                        break;
                    }
                }
            };
            mAppObserver = appObserver;
        }
        public void restoreSetsAvailable(RestoreSet[] result) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_RESTORE_SETS_AVAILABLE, result));
        }
        public void restoreStarting(int numPackages) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_RESTORE_STARTING, numPackages, 0));
        }
        public void onUpdate(int nowBeingRestored, String currentPackage) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_UPDATE, nowBeingRestored, 0, currentPackage));
        }
        public void restoreFinished(int error) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_RESTORE_FINISHED, error, 0));
        }
    }
}
