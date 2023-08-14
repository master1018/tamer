public final class BluetoothA2dp {
    private static final String TAG = "BluetoothA2dp";
    private static final boolean DBG = false;
    public static final String EXTRA_SINK_STATE =
        "android.bluetooth.a2dp.extra.SINK_STATE";
    public static final String EXTRA_PREVIOUS_SINK_STATE =
        "android.bluetooth.a2dp.extra.PREVIOUS_SINK_STATE";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_SINK_STATE_CHANGED =
        "android.bluetooth.a2dp.action.SINK_STATE_CHANGED";
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING   = 1;
    public static final int STATE_CONNECTED    = 2;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_PLAYING    = 4;
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_OFF = 0;
    public static final int PRIORITY_UNDEFINED = -1;
    private final IBluetoothA2dp mService;
    private final Context mContext;
    public BluetoothA2dp(Context c) {
        mContext = c;
        IBinder b = ServiceManager.getService(BluetoothA2dpService.BLUETOOTH_A2DP_SERVICE);
        if (b != null) {
            mService = IBluetoothA2dp.Stub.asInterface(b);
        } else {
            Log.w(TAG, "Bluetooth A2DP service not available!");
            mService = null;
        }
    }
    public boolean connectSink(BluetoothDevice device) {
        if (DBG) log("connectSink(" + device + ")");
        try {
            return mService.connectSink(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public boolean disconnectSink(BluetoothDevice device) {
        if (DBG) log("disconnectSink(" + device + ")");
        try {
            return mService.disconnectSink(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public boolean suspendSink(BluetoothDevice device) {
        try {
            return mService.suspendSink(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public boolean resumeSink(BluetoothDevice device) {
        try {
            return mService.resumeSink(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public boolean isSinkConnected(BluetoothDevice device) {
        if (DBG) log("isSinkConnected(" + device + ")");
        int state = getSinkState(device);
        return state == STATE_CONNECTED || state == STATE_PLAYING;
    }
    public Set<BluetoothDevice> getConnectedSinks() {
        if (DBG) log("getConnectedSinks()");
        try {
            return Collections.unmodifiableSet(
                    new HashSet<BluetoothDevice>(Arrays.asList(mService.getConnectedSinks())));
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }
    public Set<BluetoothDevice> getNonDisconnectedSinks() {
        if (DBG) log("getNonDisconnectedSinks()");
        try {
            return Collections.unmodifiableSet(
                    new HashSet<BluetoothDevice>(Arrays.asList(mService.getNonDisconnectedSinks())));
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }
    public int getSinkState(BluetoothDevice device) {
        if (DBG) log("getSinkState(" + device + ")");
        try {
            return mService.getSinkState(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return BluetoothA2dp.STATE_DISCONNECTED;
        }
    }
    public boolean setSinkPriority(BluetoothDevice device, int priority) {
        if (DBG) log("setSinkPriority(" + device + ", " + priority + ")");
        try {
            return mService.setSinkPriority(device, priority);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public int getSinkPriority(BluetoothDevice device) {
        if (DBG) log("getSinkPriority(" + device + ")");
        try {
            return mService.getSinkPriority(device);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return PRIORITY_OFF;
        }
    }
    public static String stateToString(int state) {
        switch (state) {
        case STATE_DISCONNECTED:
            return "disconnected";
        case STATE_CONNECTING:
            return "connecting";
        case STATE_CONNECTED:
            return "connected";
        case STATE_DISCONNECTING:
            return "disconnecting";
        case STATE_PLAYING:
            return "playing";
        default:
            return "<unknown state " + state + ">";
        }
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
