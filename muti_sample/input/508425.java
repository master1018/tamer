public class ConnectionListenerAdapter extends IConnectionListener.Stub {
    private static final String TAG = ImApp.LOG_TAG;
    private Handler mHandler;
    public ConnectionListenerAdapter(Handler handler) {
        mHandler = handler;
    }
    public void onConnectionStateChange(IImConnection connection, int state, ImErrorInfo error) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionStateChange(" + state + ", " + error + ")");
        }
    }
    public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onUpdateSelfPresenceError(" + error + ")");
        }
    }
    public void onSelfPresenceUpdated(IImConnection connection) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onSelfPresenceUpdated()");
        }
    }
    final public void onStateChanged(final IImConnection conn,
            final int state, final ImErrorInfo error) {
        mHandler.post(new Runnable() {
            public void run() {
                onConnectionStateChange(conn, state, error);
            }
        });
    }
    final public void onUpdatePresenceError(final IImConnection conn,
            final ImErrorInfo error) {
        mHandler.post(new Runnable() {
            public void run() {
                onUpdateSelfPresenceError(conn, error);
            }
        });
    }
    final public void onUserPresenceUpdated(final IImConnection conn) {
        mHandler.post(new Runnable() {
            public void run() {
                onSelfPresenceUpdated(conn);
            }
        });
    }
}
