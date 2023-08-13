public class DockSettings extends PreferenceActivity {
    private static final int DIALOG_NOT_DOCKED = 1;
    private static final String KEY_AUDIO_SETTINGS = "dock_audio";
    private static final String KEY_DOCK_SOUNDS = "dock_sounds";
    private Preference mAudioSettings;
    private CheckBoxPreference mDockSounds;
    private Intent mDockIntent;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT)) {
                handleDockChange(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.dock_settings);
        initDockSettings();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        registerReceiver(mReceiver, filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    private void initDockSettings() {
        ContentResolver resolver = getContentResolver();
        mAudioSettings = findPreference(KEY_AUDIO_SETTINGS);
        if (mAudioSettings != null) {
            mAudioSettings.setSummary(R.string.dock_audio_summary_none);
        }
        mDockSounds = (CheckBoxPreference) findPreference(KEY_DOCK_SOUNDS);
        mDockSounds.setPersistent(false);
        mDockSounds.setChecked(Settings.System.getInt(resolver,
                Settings.System.DOCK_SOUNDS_ENABLED, 0) != 0);
    }
    private void handleDockChange(Intent intent) {
        if (mAudioSettings != null) {
            int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, 0);
            boolean isBluetooth = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) != null;
            if (!isBluetooth) {
                mAudioSettings.setEnabled(false);
                mAudioSettings.setSummary(R.string.dock_audio_summary_unknown);
            } else {
                mAudioSettings.setEnabled(true);
                mDockIntent = intent;
                int resId = R.string.dock_audio_summary_unknown;
                switch (dockState) {
                case Intent.EXTRA_DOCK_STATE_CAR:
                    resId = R.string.dock_audio_summary_car;
                    break;
                case Intent.EXTRA_DOCK_STATE_DESK:
                    resId = R.string.dock_audio_summary_desk;
                    break;
                case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                    resId = R.string.dock_audio_summary_none;
                }
                mAudioSettings.setSummary(resId);
            }
            if (dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                try {
                    dismissDialog(DIALOG_NOT_DOCKED);
                } catch (IllegalArgumentException iae) {
                }
            }
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAudioSettings) {
            int dockState = mDockIntent != null
                    ? mDockIntent.getIntExtra(Intent.EXTRA_DOCK_STATE, 0)
                    : Intent.EXTRA_DOCK_STATE_UNDOCKED;
            if (dockState == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                showDialog(DIALOG_NOT_DOCKED);
            } else {
                Intent i = new Intent(mDockIntent);
                i.setAction(DockEventReceiver.ACTION_DOCK_SHOW_UI);
                i.setClass(this, DockEventReceiver.class);
                sendBroadcast(i);
            }
        } else if (preference == mDockSounds) {
            Settings.System.putInt(getContentResolver(), Settings.System.DOCK_SOUNDS_ENABLED,
                    mDockSounds.isChecked() ? 1 : 0);
        }
        return true;
    }
    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOT_DOCKED) {
            return createUndockedMessage();
        }
        return null;
    }
    private Dialog createUndockedMessage() {
        final AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.dock_not_found_title);
        ab.setMessage(R.string.dock_not_found_text);
        ab.setPositiveButton(android.R.string.ok, null);
        return ab.create();
    }
}
