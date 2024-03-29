public class ApplicationSettings extends PreferenceActivity implements
        DialogInterface.OnClickListener {
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_APP_INSTALL_LOCATION = "app_install_location";
    private static final String KEY_QUICK_LAUNCH = "quick_launch";
    private static final int APP_INSTALL_AUTO = 0;
    private static final int APP_INSTALL_DEVICE = 1;
    private static final int APP_INSTALL_SDCARD = 2;
    private static final String APP_INSTALL_DEVICE_ID = "device";
    private static final String APP_INSTALL_SDCARD_ID = "sdcard";
    private static final String APP_INSTALL_AUTO_ID = "auto";
    private CheckBoxPreference mToggleAppInstallation;
    private ListPreference mInstallLocation;
    private DialogInterface mWarnInstallApps;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.application_settings);
        mToggleAppInstallation = (CheckBoxPreference) findPreference(KEY_TOGGLE_INSTALL_APPLICATIONS);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
        mInstallLocation = (ListPreference) findPreference(KEY_APP_INSTALL_LOCATION);
        boolean userSetInstLocation = (Settings.System.getInt(getContentResolver(),
                Settings.Secure.SET_INSTALL_LOCATION, 0) != 0);
        if (!userSetInstLocation) {
            getPreferenceScreen().removePreference(mInstallLocation);
        } else {
            mInstallLocation.setValue(getAppInstallLocation());
            mInstallLocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = (String) newValue;
                    handleUpdateAppInstallLocation(value);
                    return false;
                }
            });
        }
        if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS) {
            Preference quickLaunchSetting = findPreference(KEY_QUICK_LAUNCH);
            getPreferenceScreen().removePreference(quickLaunchSetting);
        }
    }
    protected void handleUpdateAppInstallLocation(final String value) {
        if(APP_INSTALL_DEVICE_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_DEVICE);
        } else if (APP_INSTALL_SDCARD_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_SDCARD);
        } else if (APP_INSTALL_AUTO_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        } else {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        }
        mInstallLocation.setValue(value);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps && which == DialogInterface.BUTTON1) {
            setNonMarketAppsAllowed(true);
            mToggleAppInstallation.setChecked(true);
        }
    }
    private void setNonMarketAppsAllowed(boolean enabled) {
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 
                                enabled ? 1 : 0);
    }
    private boolean isNonMarketAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(), 
                                      Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
    }
    private String getAppInstallLocation() {
        int selectedLocation = Settings.System.getInt(getContentResolver(),
                Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        if (selectedLocation == APP_INSTALL_DEVICE) {
            return APP_INSTALL_DEVICE_ID;
        } else if (selectedLocation == APP_INSTALL_SDCARD) {
            return APP_INSTALL_SDCARD_ID;
        } else  if (selectedLocation == APP_INSTALL_AUTO) {
            return APP_INSTALL_AUTO_ID;
        } else {
            return APP_INSTALL_AUTO_ID;
        }
    }
    private void warnAppInstallation() {
        mWarnInstallApps = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error_title))
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
