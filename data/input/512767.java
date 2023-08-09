public class KeyguardUpdateMonitor {
    static private final String TAG = "KeyguardUpdateMonitor";
    static private final boolean DEBUG = false;
    private static final int LOW_BATTERY_THRESHOLD = 20;
    private final Context mContext;
    private IccCard.State mSimState = IccCard.State.READY;
    private boolean mKeyguardBypassEnabled;
    private boolean mDevicePluggedIn;
    private boolean mDeviceProvisioned;
    private int mBatteryLevel;
    private CharSequence mTelephonyPlmn;
    private CharSequence mTelephonySpn;
    private int mFailedAttempts = 0;
    private Handler mHandler;
    private ArrayList<InfoCallback> mInfoCallbacks = Lists.newArrayList();
    private ArrayList<SimStateCallback> mSimStateCallbacks = Lists.newArrayList();
    private ContentObserver mContentObserver;
    private static final int MSG_TIME_UPDATE = 301;
    private static final int MSG_BATTERY_UPDATE = 302;
    private static final int MSG_CARRIER_INFO_UPDATE = 303;
    private static final int MSG_SIM_STATE_CHANGE = 304;
    private static final int MSG_RINGER_MODE_CHANGED = 305;
    private static final int MSG_PHONE_STATE_CHANGED = 306;
    private static class SimArgs {
        public final IccCard.State simState;
        private SimArgs(Intent intent) {
            if (!TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
                throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
            }
            String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
            if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
                this.simState = IccCard.State.ABSENT;
            } else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
                this.simState = IccCard.State.READY;
            } else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
                final String lockedReason = intent
                        .getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
                if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                    this.simState = IccCard.State.PIN_REQUIRED;
                } else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                    this.simState = IccCard.State.PUK_REQUIRED;
                } else {
                    this.simState = IccCard.State.UNKNOWN;
                }
            } else if (IccCard.INTENT_VALUE_LOCKED_NETWORK.equals(stateExtra)) {
                this.simState = IccCard.State.NETWORK_LOCKED;
            } else {
                this.simState = IccCard.State.UNKNOWN;
            }
        }
        public String toString() {
            return simState.toString();
        }
    }
    public KeyguardUpdateMonitor(Context context) {
        mContext = context;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TIME_UPDATE:
                        handleTimeUpdate();
                        break;
                    case MSG_BATTERY_UPDATE:
                        handleBatteryUpdate(msg.arg1,  msg.arg2);
                        break;
                    case MSG_CARRIER_INFO_UPDATE:
                        handleCarrierInfoUpdate();
                        break;
                    case MSG_SIM_STATE_CHANGE:
                        handleSimStateChange((SimArgs) msg.obj);
                        break;
                    case MSG_RINGER_MODE_CHANGED:
                        handleRingerModeChange(msg.arg1);
                        break;
                    case MSG_PHONE_STATE_CHANGED:
                        handlePhoneStateChanged((String)msg.obj);
                        break;
                }
            }
        };
        mKeyguardBypassEnabled = context.getResources().getBoolean(
                com.android.internal.R.bool.config_bypass_keyguard_if_slider_open);
        mDeviceProvisioned = Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
        if (!mDeviceProvisioned) {
            mContentObserver = new ContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    mDeviceProvisioned = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
                    if (mDeviceProvisioned && mContentObserver != null) {
                        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
                        mContentObserver = null;
                    }
                    if (DEBUG) Log.d(TAG, "DEVICE_PROVISIONED state = " + mDeviceProvisioned);
                }
            };
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.DEVICE_PROVISIONED),
                    false, mContentObserver);
            mDeviceProvisioned = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
        }
        mSimState = IccCard.State.READY;
        mDevicePluggedIn = true;
        mBatteryLevel = 100;
        mTelephonyPlmn = getDefaultPlmn();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(SPN_STRINGS_UPDATED_ACTION);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (DEBUG) Log.d(TAG, "received broadcast " + action);
                if (Intent.ACTION_TIME_TICK.equals(action)
                        || Intent.ACTION_TIME_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_TIME_UPDATE));
                } else if (SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                    mTelephonyPlmn = getTelephonyPlmnFrom(intent);
                    mTelephonySpn = getTelephonySpnFrom(intent);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_CARRIER_INFO_UPDATE));
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    final int pluggedInStatus = intent
                            .getIntExtra("status", BATTERY_STATUS_UNKNOWN);
                    int batteryLevel = intent.getIntExtra("level", 0);
                    final Message msg = mHandler.obtainMessage(
                            MSG_BATTERY_UPDATE,
                            pluggedInStatus,
                            batteryLevel);
                    mHandler.sendMessage(msg);
                } else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_SIM_STATE_CHANGE,
                            new SimArgs(intent)));
                } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_RINGER_MODE_CHANGED,
                            intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1), 0));
                } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_PHONE_STATE_CHANGED, state));
                }
            }
        }, filter);
    }
    protected void handlePhoneStateChanged(String newState) {
        if (DEBUG) Log.d(TAG, "handlePhoneStateChanged(" + newState + ")");
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onPhoneStateChanged(newState);
        }
    }
    protected void handleRingerModeChange(int mode) {
        if (DEBUG) Log.d(TAG, "handleRingerModeChange(" + mode + ")");
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onRingerModeChanged(mode);
        }
    }
    private void handleTimeUpdate() {
        if (DEBUG) Log.d(TAG, "handleTimeUpdate");
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onTimeChanged();
        }
    }
    private void handleBatteryUpdate(int pluggedInStatus, int batteryLevel) {
        if (DEBUG) Log.d(TAG, "handleBatteryUpdate");
        final boolean pluggedIn = isPluggedIn(pluggedInStatus);
        if (isBatteryUpdateInteresting(pluggedIn, batteryLevel)) {
            mBatteryLevel = batteryLevel;
            mDevicePluggedIn = pluggedIn;
            for (int i = 0; i < mInfoCallbacks.size(); i++) {
                mInfoCallbacks.get(i).onRefreshBatteryInfo(
                        shouldShowBatteryInfo(), pluggedIn, batteryLevel);
            }
        }
    }
    private void handleCarrierInfoUpdate() {
        if (DEBUG) Log.d(TAG, "handleCarrierInfoUpdate: plmn = " + mTelephonyPlmn
            + ", spn = " + mTelephonySpn);
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onRefreshCarrierInfo(mTelephonyPlmn, mTelephonySpn);
        }
    }
    private void handleSimStateChange(SimArgs simArgs) {
        final IccCard.State state = simArgs.simState;
        if (DEBUG) {
            Log.d(TAG, "handleSimStateChange: intentValue = " + simArgs + " "
                    + "state resolved to " + state.toString());
        }
        if (state != IccCard.State.UNKNOWN && state != mSimState) {
            mSimState = state;
            for (int i = 0; i < mSimStateCallbacks.size(); i++) {
                mSimStateCallbacks.get(i).onSimStateChanged(state);
            }
        }
    }
    private boolean isPluggedIn(int status) {
        return status == BATTERY_STATUS_CHARGING || status == BATTERY_STATUS_FULL;
    }
    private boolean isBatteryUpdateInteresting(boolean pluggedIn, int batteryLevel) {
        if (mDevicePluggedIn != pluggedIn) {
            return true;
        }
        if (pluggedIn && mBatteryLevel != batteryLevel) {
            return true;
        }
        if (!pluggedIn) {
            if (batteryLevel < LOW_BATTERY_THRESHOLD && batteryLevel != mBatteryLevel) {
                return true;
            }
        }
        return false;
    }
    private CharSequence getTelephonyPlmnFrom(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_PLMN, false)) {
            final String plmn = intent.getStringExtra(EXTRA_PLMN);
            if (plmn != null) {
                return plmn;
            } else {
                return getDefaultPlmn();
            }
        }
        return null;
    }
    private CharSequence getDefaultPlmn() {
        return mContext.getResources().getText(
                        R.string.lockscreen_carrier_default);
    }
    private CharSequence getTelephonySpnFrom(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_SPN, false)) {
            final String spn = intent.getStringExtra(EXTRA_SPN);
            if (spn != null) {
                return spn;
            }
        }
        return null;
    }
    public void removeCallback(Object observer) {
        mInfoCallbacks.remove(observer);
        mSimStateCallbacks.remove(observer);
    }
    interface InfoCallback {
        void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel);
        void onTimeChanged();
        void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn);
        void onRingerModeChanged(int state);
        void onPhoneStateChanged(String newState);
    }
    interface SimStateCallback {
        void onSimStateChanged(IccCard.State simState);
    }
    public void registerInfoCallback(InfoCallback callback) {
        if (!mInfoCallbacks.contains(callback)) {
            mInfoCallbacks.add(callback);
        } else {
            Log.e(TAG, "Object tried to add another INFO callback", new Exception("Whoops"));
        }
    }
    public void registerSimStateCallback(SimStateCallback callback) {
        if (!mSimStateCallbacks.contains(callback)) {
            mSimStateCallbacks.add(callback);
        } else {
            Log.e(TAG, "Object tried to add another SIM callback", new Exception("Whoops"));
        }
    }
    public IccCard.State getSimState() {
        return mSimState;
    }
    public void reportSimPinUnlocked() {
        mSimState = IccCard.State.READY;
    }
    public boolean isKeyguardBypassEnabled() {
        return mKeyguardBypassEnabled;
    }
    public boolean isDevicePluggedIn() {
        return mDevicePluggedIn;
    }
    public int getBatteryLevel() {
        return mBatteryLevel;
    }
    public boolean shouldShowBatteryInfo() {
        return mDevicePluggedIn || mBatteryLevel < LOW_BATTERY_THRESHOLD;
    }
    public CharSequence getTelephonyPlmn() {
        return mTelephonyPlmn;
    }
    public CharSequence getTelephonySpn() {
        return mTelephonySpn;
    }
    public boolean isDeviceProvisioned() {
        return mDeviceProvisioned;
    }
    public int getFailedAttempts() {
        return mFailedAttempts;
    }
    public void clearFailedAttempts() {
        mFailedAttempts = 0;
    }
    public void reportFailedAttempt() {
        mFailedAttempts++;
    }
}
