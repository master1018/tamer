public class AirplaneModeEnabler implements Preference.OnPreferenceChangeListener {
    private final Context mContext;
    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private final CheckBoxPreference mCheckBoxPref;
    private static final int EVENT_SERVICE_STATE_CHANGED = 3;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SERVICE_STATE_CHANGED:
                    onAirplaneModeChanged();
                    break;
            }
        }
    };
    public AirplaneModeEnabler(Context context, CheckBoxPreference airplaneModeCheckBoxPreference) {
        mContext = context;
        mCheckBoxPref = airplaneModeCheckBoxPreference;
        airplaneModeCheckBoxPreference.setPersistent(false);
        mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
    }
    public void resume() {
        mCheckBoxPref.setEnabled(true);
        mCheckBoxPref.setChecked(isAirplaneModeOn(mContext));
        mPhoneStateReceiver.registerIntent();
        mCheckBoxPref.setOnPreferenceChangeListener(this);
    }
    public void pause() {
        mPhoneStateReceiver.unregisterIntent();
        mCheckBoxPref.setOnPreferenceChangeListener(null);
    }
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
    private void setAirplaneModeOn(boolean enabling) {
        mCheckBoxPref.setEnabled(false);
        mCheckBoxPref.setSummary(enabling ? R.string.airplane_mode_turning_on
                : R.string.airplane_mode_turning_off);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 
                                enabling ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        mContext.sendBroadcast(intent);
    }
    private void onAirplaneModeChanged() {
        ServiceState serviceState = mPhoneStateReceiver.getServiceState();
        boolean airplaneModeEnabled = serviceState.getState() == ServiceState.STATE_POWER_OFF;
        mCheckBoxPref.setChecked(airplaneModeEnabled);
        mCheckBoxPref.setSummary(airplaneModeEnabled ? null : 
                mContext.getString(R.string.airplane_mode_summary));            
        mCheckBoxPref.setEnabled(true);
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
        } else {
            setAirplaneModeOn((Boolean) newValue);
        }
        return true;
    }
    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            onAirplaneModeChanged();
        }
    }
}
