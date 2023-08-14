public final class BluetoothHeadset {
    private static final String TAG = "BluetoothHeadset";
    private static final boolean DBG = false;
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_STATE_CHANGED =
            "android.bluetooth.headset.action.STATE_CHANGED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_AUDIO_STATE_CHANGED =
            "android.bluetooth.headset.action.AUDIO_STATE_CHANGED";
    public static final String EXTRA_STATE =
            "android.bluetooth.headset.extra.STATE";
    public static final String EXTRA_PREVIOUS_STATE =
            "android.bluetooth.headset.extra.PREVIOUS_STATE";
    public static final String EXTRA_AUDIO_STATE =
            "android.bluetooth.headset.extra.AUDIO_STATE";
    public static final String EXTRA_DISCONNECT_INITIATOR =
            "android.bluetooth.headset.extra.DISCONNECT_INITIATOR";
    private IBluetoothHeadset mService;
    private final Context mContext;
    private final ServiceListener mServiceListener;
    public static final int STATE_ERROR        = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING   = 1;
    public static final int STATE_CONNECTED    = 2;
    public static final int AUDIO_STATE_DISCONNECTED = 0;
    public static final int AUDIO_STATE_CONNECTED = 1;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_CANCELED = 2;
    public static final int REMOTE_DISCONNECT = 0;
    public static final int LOCAL_DISCONNECT = 1;
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_OFF = 0;
    public static final int PRIORITY_UNDEFINED = -1;
    public interface ServiceListener {
        public void onServiceConnected();
        public void onServiceDisconnected();
    }
    public BluetoothHeadset(Context context, ServiceListener l) {
        mContext = context;
        mServiceListener = l;
        if (!context.bindService(new Intent(IBluetoothHeadset.class.getName()), mConnection, 0)) {
            Log.e(TAG, "Could not bind to Bluetooth Headset Service");
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
        if (DBG) log("close()");
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
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return BluetoothHeadset.STATE_ERROR;
    }
    public BluetoothDevice getCurrentHeadset() {
        if (DBG) log("getCurrentHeadset()");
        if (mService != null) {
            try {
                return mService.getCurrentHeadset();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return null;
    }
    public boolean connectHeadset(BluetoothDevice device) {
        if (DBG) log("connectHeadset(" + device + ")");
        if (mService != null) {
            try {
                if (mService.connectHeadset(device)) {
                    return true;
                }
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean isConnected(BluetoothDevice device) {
        if (DBG) log("isConnected(" + device + ")");
        if (mService != null) {
            try {
                return mService.isConnected(device);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean disconnectHeadset() {
        if (DBG) log("disconnectHeadset()");
        if (mService != null) {
            try {
                mService.disconnectHeadset();
                return true;
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean startVoiceRecognition() {
        if (DBG) log("startVoiceRecognition()");
        if (mService != null) {
            try {
                return mService.startVoiceRecognition();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean stopVoiceRecognition() {
        if (DBG) log("stopVoiceRecognition()");
        if (mService != null) {
            try {
                return mService.stopVoiceRecognition();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public boolean setPriority(BluetoothDevice device, int priority) {
        if (DBG) log("setPriority(" + device + ", " + priority + ")");
        if (mService != null) {
            try {
                return mService.setPriority(device, priority);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }
    public int getPriority(BluetoothDevice device) {
        if (DBG) log("getPriority(" + device + ")");
        if (mService != null) {
            try {
                return mService.getPriority(device);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return -1;
    }
    public int getBatteryUsageHint() {
        if (DBG) log("getBatteryUsageHint()");
        if (mService != null) {
            try {
                return mService.getBatteryUsageHint();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return -1;
    }
    public static boolean isBluetoothVoiceDialingEnabled(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_bluetooth_sco_off_call);
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DBG) Log.d(TAG, "Proxy object connected");
            mService = IBluetoothHeadset.Stub.asInterface(service);
            if (mServiceListener != null) {
                mServiceListener.onServiceConnected();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) Log.d(TAG, "Proxy object disconnected");
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
