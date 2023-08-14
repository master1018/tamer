public class MediaScannerConnection implements ServiceConnection {
    private static final String TAG = "MediaScannerConnection";
    private Context mContext;
    private MediaScannerConnectionClient mClient;
    private IMediaScannerService mService;
    private boolean mConnected; 
    private IMediaScannerListener.Stub mListener = new IMediaScannerListener.Stub() {
        public void scanCompleted(String path, Uri uri) {
            MediaScannerConnectionClient client = mClient;
            if (client != null) {
                client.onScanCompleted(path, uri);
            }
        }
    };
    public interface OnScanCompletedListener {
        public void onScanCompleted(String path, Uri uri);
    }
    public interface MediaScannerConnectionClient extends OnScanCompletedListener {
        public void onMediaScannerConnected();
        public void onScanCompleted(String path, Uri uri);
    }
    public MediaScannerConnection(Context context, MediaScannerConnectionClient client) {
        mContext = context;
        mClient = client;
    }
    public void connect() {
        synchronized (this) {
            if (!mConnected) {
                Intent intent = new Intent(IMediaScannerService.class.getName());
                mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
                mConnected = true;
            }
        }
    }
    public void disconnect() {
        synchronized (this) {
            if (mConnected) {
                if (Config.LOGV) {
                    Log.v(TAG, "Disconnecting from Media Scanner");
                }
                try {
                    mContext.unbindService(this);
                } catch (IllegalArgumentException ex) {
                    if (Config.LOGV) {
                        Log.v(TAG, "disconnect failed: " + ex);
                    }
                }
                mConnected = false;
            }
        }
    }
    public synchronized boolean isConnected() {
        return (mService != null && mConnected);
    }
     public void scanFile(String path, String mimeType) {
        synchronized (this) {
            if (mService == null || !mConnected) {
                throw new IllegalStateException("not connected to MediaScannerService");
            }
            try {
                if (Config.LOGV) {
                    Log.v(TAG, "Scanning file " + path);
                }
                mService.requestScanFile(path, mimeType, mListener);
            } catch (RemoteException e) {
                if (Config.LOGD) {
                    Log.d(TAG, "Failed to scan file " + path);
                }
            }
        }
    }
    static class ClientProxy implements MediaScannerConnectionClient {
        final String[] mPaths;
        final String[] mMimeTypes;
        final OnScanCompletedListener mClient;
        MediaScannerConnection mConnection;
        int mNextPath;
        ClientProxy(String[] paths, String[] mimeTypes, OnScanCompletedListener client) {
            mPaths = paths;
            mMimeTypes = mimeTypes;
            mClient = client;
        }
        public void onMediaScannerConnected() {
            scanNextPath();
        }
        public void onScanCompleted(String path, Uri uri) {
            if (mClient != null) {
                mClient.onScanCompleted(path, uri);
            }
            scanNextPath();
        }
        void scanNextPath() {
            if (mNextPath >= mPaths.length) {
                mConnection.disconnect();
                return;
            }
            String mimeType = mMimeTypes != null ? mMimeTypes[mNextPath] : null;
            mConnection.scanFile(mPaths[mNextPath], mimeType);
            mNextPath++;
        }
    }
    public static void scanFile(Context context, String[] paths, String[] mimeTypes,
            OnScanCompletedListener callback) {
        ClientProxy client = new ClientProxy(paths, mimeTypes, callback);
        MediaScannerConnection connection = new MediaScannerConnection(context, client);
        client.mConnection = connection;
        connection.connect();
    }
    public void onServiceConnected(ComponentName className, IBinder service) {
        if (Config.LOGV) {
            Log.v(TAG, "Connected to Media Scanner");
        }
        synchronized (this) {
            mService = IMediaScannerService.Stub.asInterface(service);
            if (mService != null && mClient != null) {
                mClient.onMediaScannerConnected();
            }
        }
    }
    public void onServiceDisconnected(ComponentName className) {
        if (Config.LOGV) {
            Log.v(TAG, "Disconnected from Media Scanner");
        }
        synchronized (this) {
            mService = null;
        }
    }
}
