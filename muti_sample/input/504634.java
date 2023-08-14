public class LocalBluetoothManager {
    private static final String TAG = "LocalBluetoothManager";
    static final boolean V = Config.LOGV;
    static final boolean D = Config.LOGD;
    private static final String SHARED_PREFERENCES_NAME = "bluetooth_settings";
    private static LocalBluetoothManager INSTANCE;
    private static Object INSTANCE_LOCK = new Object();
    private boolean mInitialized;
    private Context mContext;
    private Activity mForegroundActivity;
    private AlertDialog mErrorDialog = null;
    private BluetoothAdapter mAdapter;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    private BluetoothEventRedirector mEventRedirector;
    private BluetoothA2dp mBluetoothA2dp;
    private int mState = BluetoothAdapter.ERROR;
    private List<Callback> mCallbacks = new ArrayList<Callback>();
    private static final int SCAN_EXPIRATION_MS = 5 * 60 * 1000; 
    private static long GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND = 60 * 1000;
    public static final String SHARED_PREFERENCES_KEY_DISCOVERING_TIMESTAMP =
        "last_discovering_time";
    private static final String SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE =
        "last_selected_device";
    private static final String SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE_TIME =
        "last_selected_device_time";
    private static final String SHARED_PREFERENCES_KEY_DOCK_AUTO_CONNECT = "auto_connect_to_dock";
    private long mLastScan;
    public static LocalBluetoothManager getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new LocalBluetoothManager();
            }
            if (!INSTANCE.init(context)) {
                return null;
            }
            LocalBluetoothProfileManager.init(INSTANCE);
            return INSTANCE;
        }
    }
    private boolean init(Context context) {
        if (mInitialized) return true;
        mInitialized = true;
        mContext = context.getApplicationContext();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            return false;
        }
        mCachedDeviceManager = new CachedBluetoothDeviceManager(this);
        mEventRedirector = new BluetoothEventRedirector(this);
        mEventRedirector.start();
        mBluetoothA2dp = new BluetoothA2dp(context);
        return true;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return mAdapter;
    }
    public Context getContext() {
        return mContext;
    }
    public Activity getForegroundActivity() {
        return mForegroundActivity;
    }
    public void setForegroundActivity(Activity activity) {
        if (mErrorDialog != null) {
            mErrorDialog.dismiss();
            mErrorDialog = null;
        }
        mForegroundActivity = activity;
    }
    public SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    public CachedBluetoothDeviceManager getCachedDeviceManager() {
        return mCachedDeviceManager;
    }
    List<Callback> getCallbacks() {
        return mCallbacks;
    }
    public void registerCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.add(callback);
        }
    }
    public void unregisterCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }
    public void startScanning(boolean force) {
        if (mAdapter.isDiscovering()) {
            dispatchScanningStateChanged(true);
        } else {
            if (!force) {
                if (mLastScan + SCAN_EXPIRATION_MS > System.currentTimeMillis()) {
                    return;
                }
                Set<BluetoothDevice> sinks = mBluetoothA2dp.getConnectedSinks();
                if (sinks != null) {
                    for (BluetoothDevice sink : sinks) {
                        if (mBluetoothA2dp.getSinkState(sink) == BluetoothA2dp.STATE_PLAYING) {
                            return;
                        }
                    }
                }
            }
            if (mAdapter.startDiscovery()) {
                mLastScan = System.currentTimeMillis();
            }
        }
    }
    public void stopScanning() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }
    public int getBluetoothState() {
        if (mState == BluetoothAdapter.ERROR) {
            syncBluetoothState();
        }
        return mState;
    }
    void setBluetoothStateInt(int state) {
        mState = state;
        if (state == BluetoothAdapter.STATE_ON ||
            state == BluetoothAdapter.STATE_OFF) {
            mCachedDeviceManager.onBluetoothStateChanged(state ==
                    BluetoothAdapter.STATE_ON);
        }
    }
    private void syncBluetoothState() {
        int bluetoothState;
        if (mAdapter != null) {
            bluetoothState = mAdapter.isEnabled()
                    ? BluetoothAdapter.STATE_ON
                    : BluetoothAdapter.STATE_OFF;
        } else {
            bluetoothState = BluetoothAdapter.ERROR;
        }
        setBluetoothStateInt(bluetoothState);
    }
    public void setBluetoothEnabled(boolean enabled) {
        boolean wasSetStateSuccessful = enabled
                ? mAdapter.enable()
                : mAdapter.disable();
        if (wasSetStateSuccessful) {
            setBluetoothStateInt(enabled
                ? BluetoothAdapter.STATE_TURNING_ON
                : BluetoothAdapter.STATE_TURNING_OFF);
        } else {
            if (V) {
                Log.v(TAG,
                        "setBluetoothEnabled call, manager didn't return success for enabled: "
                                + enabled);
            }
            syncBluetoothState();
        }
    }
    void onScanningStateChanged(boolean started) {
        mCachedDeviceManager.onScanningStateChanged(started);
        dispatchScanningStateChanged(started);
    }
    private void dispatchScanningStateChanged(boolean started) {
        synchronized (mCallbacks) {
            for (Callback callback : mCallbacks) {
                callback.onScanningStateChanged(started);
            }
        }
    }
    public void showError(BluetoothDevice device, int titleResId, int messageResId) {
        CachedBluetoothDevice cachedDevice = mCachedDeviceManager.findDevice(device);
        String name = null;
        if (cachedDevice == null) {
            if (device != null) name = device.getName();
            if (name == null) {
                name = mContext.getString(R.string.bluetooth_remote_device);
            }
        } else {
            name = cachedDevice.getName();
        }
        String message = mContext.getString(messageResId, name);
        if (mForegroundActivity != null) {
            mErrorDialog = new AlertDialog.Builder(mForegroundActivity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(titleResId)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        } else {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
    public interface Callback {
        void onScanningStateChanged(boolean started);
        void onDeviceAdded(CachedBluetoothDevice cachedDevice);
        void onDeviceDeleted(CachedBluetoothDevice cachedDevice);
    }
    public boolean shouldShowDialogInForeground(String deviceAddress) {
        if (mForegroundActivity != null) return true;
        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences();
        long lastDiscoverableEndTime = sharedPreferences.getLong(
                BluetoothDiscoverableEnabler.SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP, 0);
        if ((lastDiscoverableEndTime + GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND)
                > currentTimeMillis) {
            return true;
        }
        if (mAdapter != null && mAdapter.isDiscovering()) {
            return true;
        } else if ((sharedPreferences.getLong(SHARED_PREFERENCES_KEY_DISCOVERING_TIMESTAMP, 0) +
                GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND) > currentTimeMillis) {
            return true;
        }
        if (deviceAddress != null) {
            String lastSelectedDevice = sharedPreferences.getString(
                    SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE, null);
            if (deviceAddress.equals(lastSelectedDevice)) {
                long lastDeviceSelectedTime = sharedPreferences.getLong(
                        SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE_TIME, 0);
                if ((lastDeviceSelectedTime + GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND)
                        > currentTimeMillis) {
                    return true;
                }
            }
        }
        return false;
    }
    void persistSelectedDeviceInPicker(String deviceAddress) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(LocalBluetoothManager.SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE,
                deviceAddress);
        editor.putLong(LocalBluetoothManager.SHARED_PREFERENCES_KEY_LAST_SELECTED_DEVICE_TIME,
                System.currentTimeMillis());
        editor.commit();
    }
    public boolean hasDockAutoConnectSetting(String addr) {
        return getSharedPreferences().contains(SHARED_PREFERENCES_KEY_DOCK_AUTO_CONNECT + addr);
    }
    public boolean getDockAutoConnectSetting(String addr) {
        return getSharedPreferences().getBoolean(SHARED_PREFERENCES_KEY_DOCK_AUTO_CONNECT + addr,
                false);
    }
    public void saveDockAutoConnectSetting(String addr, boolean autoConnect) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(SHARED_PREFERENCES_KEY_DOCK_AUTO_CONNECT + addr, autoConnect);
        editor.commit();
    }
    public void removeDockAutoConnectSetting(String addr) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(SHARED_PREFERENCES_KEY_DOCK_AUTO_CONNECT + addr);
        editor.commit();
    }
}
