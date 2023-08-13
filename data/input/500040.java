public class EmailServiceProxy implements IEmailService {
    private static final boolean DEBUG_PROXY = false; 
    private static final String TAG = "EmailServiceProxy";
    public static final String AUTO_DISCOVER_BUNDLE_ERROR_CODE = "autodiscover_error_code";
    public static final String AUTO_DISCOVER_BUNDLE_HOST_AUTH = "autodiscover_host_auth";
    private final Context mContext;
    private final Class<?> mClass;
    private final IEmailServiceCallback mCallback;
    private Runnable mRunnable;
    private ServiceConnection mSyncManagerConnection = new EmailServiceConnection ();
    private IEmailService mService = null;
    private Object mReturn = null;
    private int mTimeout = 45;
    private boolean mDead = false;
    public EmailServiceProxy(Context _context, Class<?> _class) {
        this(_context, _class, null);
    }
    public EmailServiceProxy(Context _context, Class<?> _class, IEmailServiceCallback _callback) {
        mContext = _context;
        mClass = _class;
        mCallback = _callback;
        if (Debug.isDebuggerConnected()) {
            mTimeout <<= 2;
        }
    }
    class EmailServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = IEmailService.Stub.asInterface(binder);
            if (DEBUG_PROXY) {
                Log.v(TAG, "Service " + mClass.getSimpleName() + " connected");
            }
            new Thread(new Runnable() {
                public void run() {
                    runTask();
                }}).start();
        }
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG_PROXY) {
                Log.v(TAG, "Service " + mClass.getSimpleName() + " disconnected");
            }
        }
    }
    public EmailServiceProxy setTimeout(int secs) {
        mTimeout = secs;
        return this;
    }
    private void runTask() {
        Thread thread = new Thread(mRunnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        try {
            mContext.unbindService(mSyncManagerConnection);
        } catch (IllegalArgumentException e) {
        }
        mDead = true;
        synchronized(mSyncManagerConnection) {
            if (DEBUG_PROXY) {
                Log.v(TAG, "Service task completed; disconnecting");
            }
            mSyncManagerConnection.notify();
        }
    }
    private void setTask(Runnable runnable) throws RemoteException {
        if (mDead) {
            throw new RemoteException();
        }
        mRunnable = runnable;
        if (DEBUG_PROXY) {
            Log.v(TAG, "Service " + mClass.getSimpleName() + " bind requested");
        }
        mContext.bindService(new Intent(mContext, mClass), mSyncManagerConnection,
                Context.BIND_AUTO_CREATE);
    }
    public void waitForCompletion() {
        synchronized (mSyncManagerConnection) {
            long time = System.currentTimeMillis();
            try {
                if (DEBUG_PROXY) {
                    Log.v(TAG, "Waiting for task to complete...");
                }
                mSyncManagerConnection.wait(mTimeout * 1000L);
            } catch (InterruptedException e) {
            }
            if (DEBUG_PROXY) {
                Log.v(TAG, "Wait finished in " + (System.currentTimeMillis() - time) + "ms");
            }
        }
    }
    public void loadAttachment(final long attachmentId, final String destinationFile,
            final String contentUriString) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.loadAttachment(attachmentId, destinationFile, contentUriString);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void startSync(final long mailboxId) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.startSync(mailboxId);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void stopSync(final long mailboxId) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.stopSync(mailboxId);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public int validate(final String protocol, final String host, final String userName,
            final String password, final int port, final boolean ssl,
            final boolean trustCertificates) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mReturn = mService.validate(protocol, host, userName, password, port, ssl,
                            trustCertificates);
                } catch (RemoteException e) {
                }
            }
        });
        waitForCompletion();
        if (mReturn == null) {
            return MessagingException.UNSPECIFIED_EXCEPTION;
        } else {
            Log.v(TAG, "validate returns " + mReturn);
            return (Integer)mReturn;
        }
    }
    public Bundle autoDiscover(final String userName, final String password)
            throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mReturn = mService.autoDiscover(userName, password);
                } catch (RemoteException e) {
                }
            }
        });
        waitForCompletion();
        if (mReturn == null) {
            return null;
        } else {
            Bundle bundle = (Bundle) mReturn;
            Log.v(TAG, "autoDiscover returns " + bundle.getInt(AUTO_DISCOVER_BUNDLE_ERROR_CODE));
            return bundle;
        }
    }
    public void updateFolderList(final long accountId) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.updateFolderList(accountId);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void setLogging(final int on) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.setLogging(on);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void setCallback(final IEmailServiceCallback cb) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    mService.setCallback(cb);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void hostChanged(final long accountId) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    mService.hostChanged(accountId);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void sendMeetingResponse(final long messageId, final int response) throws RemoteException {
        setTask(new Runnable () {
            public void run() {
                try {
                    if (mCallback != null) mService.setCallback(mCallback);
                    mService.sendMeetingResponse(messageId, response);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public void loadMore(long messageId) throws RemoteException {
    }
    public boolean createFolder(long accountId, String name) throws RemoteException {
        return false;
    }
    public boolean deleteFolder(long accountId, String name) throws RemoteException {
        return false;
    }
    public boolean renameFolder(long accountId, String oldName, String newName)
            throws RemoteException {
        return false;
    }
    public IBinder asBinder() {
        return null;
    }
}
