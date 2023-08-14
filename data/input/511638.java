public class BluetoothDiscoverableEnabler implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "BluetoothDiscoverableEnabler";
    private static final String SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT =
            "debug.bt.discoverable_time";
      static final int DEFAULT_DISCOVERABLE_TIMEOUT = 120;
     static final String SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP =
            "discoverable_end_timestamp";
    private final Context mContext;
    private final Handler mUiHandler;
    private final CheckBoxPreference mCheckBoxPreference;
    private final LocalBluetoothManager mLocalManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                if (mode != BluetoothAdapter.ERROR) {
                    handleModeChanged(mode);
                }
            }
        }
    };
    private final Runnable mUpdateCountdownSummaryRunnable = new Runnable() {
        public void run() {
            updateCountdownSummary();
        }
    };
    public BluetoothDiscoverableEnabler(Context context, CheckBoxPreference checkBoxPreference) {
        mContext = context;
        mUiHandler = new Handler();
        mCheckBoxPreference = checkBoxPreference;
        checkBoxPreference.setPersistent(false);
        mLocalManager = LocalBluetoothManager.getInstance(context);
        if (mLocalManager == null) {
            checkBoxPreference.setEnabled(false);
        }
    }
    public void resume() {
        if (mLocalManager == null) {
            return;
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mCheckBoxPreference.setOnPreferenceChangeListener(this);
        handleModeChanged(mLocalManager.getBluetoothAdapter().getScanMode());
    }
    public void pause() {
        if (mLocalManager == null) {
            return;
        }
        mUiHandler.removeCallbacks(mUpdateCountdownSummaryRunnable);
        mCheckBoxPreference.setOnPreferenceChangeListener(null);
        mContext.unregisterReceiver(mReceiver);
    }
    public boolean onPreferenceChange(Preference preference, Object value) {
        setEnabled((Boolean) value);
        return true;
    }
    private void setEnabled(final boolean enable) {
        BluetoothAdapter manager = mLocalManager.getBluetoothAdapter();
        if (enable) {
            int timeout = getDiscoverableTimeout();
            manager.setDiscoverableTimeout(timeout);
            mCheckBoxPreference.setSummaryOn(
                    mContext.getResources().getString(R.string.bluetooth_is_discoverable, timeout));
            long endTimestamp = System.currentTimeMillis() + timeout * 1000;
            persistDiscoverableEndTimestamp(endTimestamp);
            manager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
        } else {
            manager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
        }
    }
    private int getDiscoverableTimeout() {
        int timeout = SystemProperties.getInt(SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT, -1);
        if (timeout <= 0) {
            timeout = DEFAULT_DISCOVERABLE_TIMEOUT;
        }
        return timeout;
    }
    private void persistDiscoverableEndTimestamp(long endTimestamp) {
        SharedPreferences.Editor editor = mLocalManager.getSharedPreferences().edit();
        editor.putLong(SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP, endTimestamp);
        editor.commit();
    }
    private void handleModeChanged(int mode) {
        if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            mCheckBoxPreference.setChecked(true);
            updateCountdownSummary();
        } else {
            mCheckBoxPreference.setChecked(false);
        }
    }
    private void updateCountdownSummary() {
        int mode = mLocalManager.getBluetoothAdapter().getScanMode();
        if (mode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            return;
        }
        long currentTimestamp = System.currentTimeMillis();
        long endTimestamp = mLocalManager.getSharedPreferences().getLong(
                SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP, 0);
        if (currentTimestamp > endTimestamp) {
            mCheckBoxPreference.setSummaryOn(null);
            return;
        }
        String formattedTimeLeft = String.valueOf((endTimestamp - currentTimestamp) / 1000);
        mCheckBoxPreference.setSummaryOn(
                mContext.getResources().getString(R.string.bluetooth_is_discoverable,
                        formattedTimeLeft));
        synchronized (this) {
            mUiHandler.removeCallbacks(mUpdateCountdownSummaryRunnable);
            mUiHandler.postDelayed(mUpdateCountdownSummaryRunnable, 1000);
        }
    }
}
