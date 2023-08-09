public abstract class AbstractSyncService implements Runnable {
    public String TAG = "AbstractSyncService";
    public static final int SECONDS = 1000;
    public static final int MINUTES = 60*SECONDS;
    public static final int HOURS = 60*MINUTES;
    public static final int DAYS = 24*HOURS;
    public static final int CONNECT_TIMEOUT = 30*SECONDS;
    public static final int NETWORK_WAIT = 15*SECONDS;
    public static final String EAS_PROTOCOL = "eas";
    public static final int EXIT_DONE = 0;
    public static final int EXIT_IO_ERROR = 1;
    public static final int EXIT_LOGIN_FAILURE = 2;
    public static final int EXIT_EXCEPTION = 3;
    public static final int EXIT_SECURITY_FAILURE = 4;
    public Mailbox mMailbox;
    protected long mMailboxId;
    protected Thread mThread;
    protected int mExitStatus = EXIT_EXCEPTION;
    protected String mMailboxName;
    public Account mAccount;
    public Context mContext;
    public int mChangeCount = 0;
    public int mSyncReason = 0;
    protected volatile boolean mStop = false;
    protected Object mSynchronizer = new Object();
    protected volatile long mRequestTime = 0;
    protected ArrayList<Request> mRequests = new ArrayList<Request>();
    protected PartRequest mPendingRequest = null;
    public abstract void stop();
    public abstract boolean alarm();
    public abstract void reset();
    public abstract void validateAccount(String host, String userName, String password, int port,
            boolean ssl, boolean trustCertificates, Context context) throws MessagingException;
    public AbstractSyncService(Context _context, Mailbox _mailbox) {
        mContext = _context;
        mMailbox = _mailbox;
        mMailboxId = _mailbox.mId;
        mMailboxName = _mailbox.mServerId;
        mAccount = Account.restoreAccountWithId(_context, _mailbox.mAccountKey);
    }
    public AbstractSyncService(String prefix) {
    }
    static public void validate(Class<? extends AbstractSyncService> klass, String host,
            String userName, String password, int port, boolean ssl, boolean trustCertificates,
            Context context)
            throws MessagingException {
        AbstractSyncService svc;
        try {
            svc = klass.newInstance();
            svc.validateAccount(host, userName, password, port, ssl, trustCertificates, context);
        } catch (IllegalAccessException e) {
            throw new MessagingException("internal error", e);
        } catch (InstantiationException e) {
            throw new MessagingException("internal error", e);
        }
    }
    public static class ValidationResult {
        static final int NO_FAILURE = 0;
        static final int CONNECTION_FAILURE = 1;
        static final int VALIDATION_FAILURE = 2;
        static final int EXCEPTION = 3;
        static final ValidationResult succeeded = new ValidationResult(true, NO_FAILURE, null);
        boolean success;
        int failure = NO_FAILURE;
        String reason = null;
        Exception exception = null;
        ValidationResult(boolean _success, int _failure, String _reason) {
            success = _success;
            failure = _failure;
            reason = _reason;
        }
        ValidationResult(boolean _success) {
            success = _success;
        }
        ValidationResult(Exception e) {
            success = false;
            failure = EXCEPTION;
            exception = e;
        }
        public boolean isSuccess() {
            return success;
        }
        public String getReason() {
            return reason;
        }
    }
    public boolean isStopped() {
        return mStop;
    }
    public Object getSynchronizer() {
        return mSynchronizer;
    }
    public void userLog(String string, int code, String string2) {
        if (Eas.USER_LOG) {
            userLog(string + code + string2);
        }
    }
    public void userLog(String string, int code) {
        if (Eas.USER_LOG) {
            userLog(string + code);
        }
    }
    public void userLog(String str, Exception e) {
        if (Eas.USER_LOG) {
            Log.e(TAG, str, e);
        } else {
            Log.e(TAG, str + e);
        }
        if (Eas.FILE_LOG) {
            FileLogger.log(e);
        }
    }
    public void userLog(String ...strings) {
        if (Eas.USER_LOG) {
            String logText;
            if (strings.length == 1) {
                logText = strings[0];
            } else {
                StringBuilder sb = new StringBuilder(64);
                for (String string: strings) {
                    sb.append(string);
                }
                logText = sb.toString();
            }
            Log.d(TAG, logText);
            if (Eas.FILE_LOG) {
                FileLogger.log(TAG, logText);
            }
        }
    }
    public void errorLog(String str) {
        Log.e(TAG, str);
        if (Eas.FILE_LOG) {
            FileLogger.log(TAG, str);
        }
    }
    public boolean hasConnectivity() {
        ConnectivityManager cm =
            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        int tries = 0;
        while (tries++ < 1) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                DetailedState state = info.getDetailedState();
                if (state == DetailedState.CONNECTED) {
                    return true;
                }
            }
            try {
                Thread.sleep(10*SECONDS);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }
    public void addRequest(Request req) {
        synchronized (mRequests) {
            mRequests.add(req);
            mRequestTime = System.currentTimeMillis();
        }
    }
    public void removeRequest(Request req) {
        synchronized (mRequests) {
            mRequests.remove(req);
        }
    }
    protected String[] getRowColumns(Uri contentUri, String[] projection, String selection,
            String[] selectionArgs) {
        String[] values = new String[projection.length];
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(contentUri, projection, selection, selectionArgs, null);
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < projection.length; i++) {
                    values[i] = c.getString(i);
                }
            } else {
                return null;
            }
        } finally {
            c.close();
        }
        return values;
    }
    protected String[] getRowColumns(Uri baseUri, long id, String ... projection) {
        return getRowColumns(ContentUris.withAppendedId(baseUri, id), projection, null, null);
    }
}
