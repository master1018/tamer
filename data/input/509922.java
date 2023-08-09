public class BrightnessPreference extends SeekBarPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {
    private SeekBar mSeekBar;
    private CheckBox mCheckBox;
    private int mOldBrightness;
    private int mOldAutomatic;
    private boolean mAutomaticAvailable;
    private static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM + 10;
    private static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;
    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAutomaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        setDialogIcon(R.drawable.ic_settings_display);
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
        try {
            mOldBrightness = Settings.System.getInt(getContext().getContentResolver(), 
                Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException snfe) {
            mOldBrightness = MAXIMUM_BACKLIGHT;
        }
        mSeekBar.setProgress(mOldBrightness - MINIMUM_BACKLIGHT);
        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            try {
                mOldAutomatic = Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
            } catch (SettingNotFoundException snfe) {
                mOldAutomatic = 0;
            }
            mCheckBox.setChecked(mOldAutomatic != 0);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setBrightness(progress + MINIMUM_BACKLIGHT);
    }
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (!isChecked) {
            setBrightness(mSeekBar.getProgress() + MINIMUM_BACKLIGHT);
        }
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Settings.System.putInt(getContext().getContentResolver(), 
                    Settings.System.SCREEN_BRIGHTNESS,
                    mSeekBar.getProgress() + MINIMUM_BACKLIGHT);
        } else {
            if (mAutomaticAvailable) {
                setMode(mOldAutomatic);
            }
            if (!mAutomaticAvailable || mOldAutomatic == 0) {
                setBrightness(mOldBrightness);
            }
        }
    }
    private void setBrightness(int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                power.setBacklightBrightness(brightness);
            }
        } catch (RemoteException doe) {
        }        
    }
    private void setMode(int mode) {
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            mSeekBar.setVisibility(View.GONE);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }
}
