public class BluetoothPbap {
    private static final String TAG = "BluetoothPbap";
    private static final boolean DBG = false;
    public static final String PBAP_STATE =
        "android.bluetooth.pbap.intent.PBAP_STATE";
    public static final String PBAP_PREVIOUS_STATE =
        "android.bluetooth.pbap.intent.PBAP_PREVIOUS_STATE";
    public static final String PBAP_STATE_CHANGED_ACTION =
        "android.bluetooth.pbap.intent.action.PBAP_STATE_CHANGED";
    private IBluetoothPbap mService;
    private final Context mContext;
    private final ServiceListener mServiceListener;
    public static final int STATE_ERROR        = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING   = 1;
    public static final int STATE_CONNECTED    = 2;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_CANCELED = 2;
    public interface ServiceListener {
        public void onServiceConnected();
        public void onServiceDisconnected();
    }
    public BluetoothPbap(Context context, ServiceListener l) {
        mContext = context;
        mServiceListener = l;
        if (!context.bindService(new Intent(IBluetoothPbap.class.getName()), mConnection, 0)) {
            Log.e(TAG, "Could not bind to Bluetooth Pbap Service");
        }
    }
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
    public synchronized void close() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }
    public int getState() {
        if (DBG) log("getState()");
        if (mService != null) {
            try {
                return mService.getState();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) log(Log.getStackTraceString(new Throwable()));
        }
        return BluetoothPbap.STATE_ERROR;
    }
    public BluetoothDevice getClient() {
        if (DBG) log("getClient()");
        if (mService != null) {
            try {
                return mService.getClient();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) log(Log.getStackTraceString(new Throwable()));
        }
        return null;
    }
    public boolean isConnected(BluetoothDevice device) {
        if (DBG) log("isConnected(" + device + ")");
        if (mService != null) {
            try {
                return mService.isConnected(device);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) log(Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean disconnect() {
        if (DBG) log("disconnect()");
        if (mService != null) {
            try {
                mService.disconnect();
                return true;
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) log(Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public static boolean doesClassMatchSink(BluetoothClass btClass) {
        switch (btClass.getDeviceClass()) {
        case BluetoothClass.Device.COMPUTER_DESKTOP:
        case BluetoothClass.Device.COMPUTER_LAPTOP:
        case BluetoothClass.Device.COMPUTER_SERVER:
        case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
            return true;
        default:
            return false;
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DBG) log("Proxy object connected");
            mService = IBluetoothPbap.Stub.asInterface(service);
            if (mServiceListener != null) {
                mServiceListener.onServiceConnected();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) log("Proxy object disconnected");
            mService = null;
            if (mServiceListener != null) {
                mServiceListener.onServiceDisconnected();
            }
        }
    };
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
