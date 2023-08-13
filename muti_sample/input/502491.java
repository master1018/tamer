public class SettingsHelper {
    private static final String TAG = "SettingsHelper";
    private Context mContext;
    private AudioManager mAudioManager;
    private IContentService mContentService;
    private IPowerManager mPowerManager;
    private boolean mSilent;
    private boolean mVibrate;
    public SettingsHelper(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        mContentService = ContentResolver.getContentService();
        mPowerManager = IPowerManager.Stub.asInterface(
                ServiceManager.getService("power"));
    }
    public boolean restoreValue(String name, String value) {
        if (Settings.System.SCREEN_BRIGHTNESS.equals(name)) {
            setBrightness(Integer.parseInt(value));
        } else if (Settings.System.SOUND_EFFECTS_ENABLED.equals(name)) {
            setSoundEffects(Integer.parseInt(value) == 1);
        } else if (Settings.Secure.LOCATION_PROVIDERS_ALLOWED.equals(name)) {
            setGpsLocation(value);
            return false;
        } else if (Settings.Secure.BACKUP_AUTO_RESTORE.equals(name)) {
            setAutoRestore(Integer.parseInt(value) == 1);
        }
        return true;
    }
    private void setAutoRestore(boolean enabled) {
        try {
            IBackupManager bm = IBackupManager.Stub.asInterface(
                    ServiceManager.getService(Context.BACKUP_SERVICE));
            if (bm != null) {
                bm.setAutoRestore(enabled);
            }
        } catch (RemoteException e) {}
    }
    private void setGpsLocation(String value) {
        final String GPS = LocationManager.GPS_PROVIDER;
        boolean enabled = 
                GPS.equals(value) ||
                value.startsWith(GPS + ",") ||
                value.endsWith("," + GPS) ||
                value.contains("," + GPS + ",");
        Settings.Secure.setLocationProviderEnabled(
                mContext.getContentResolver(), GPS, enabled);
    }
    private void setSoundEffects(boolean enable) {
        if (enable) {
            mAudioManager.loadSoundEffects();
        } else {
            mAudioManager.unloadSoundEffects();
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
    private void setRingerMode() {
        if (mSilent) {
            mAudioManager.setRingerMode(mVibrate ? AudioManager.RINGER_MODE_VIBRATE :
                AudioManager.RINGER_MODE_SILENT);
        } else {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                    mVibrate ? AudioManager.VIBRATE_SETTING_ON
                            : AudioManager.VIBRATE_SETTING_OFF);
        }
    }
    byte[] getLocaleData() {
        Configuration conf = mContext.getResources().getConfiguration();
        final Locale loc = conf.locale;
        String localeString = loc.getLanguage();
        String country = loc.getCountry();
        if (!TextUtils.isEmpty(country)) {
            localeString += "_" + country;
        }
        return localeString.getBytes();
    }
    void setLocaleData(byte[] data) {
        Configuration conf = mContext.getResources().getConfiguration();
        Locale loc = conf.locale;
        if (conf.userSetLocale) return; 
        final String[] availableLocales = mContext.getAssets().getLocales();
        String localeCode = new String(data);
        String language = new String(data, 0, 2);
        String country = data.length > 4 ? new String(data, 3, 2) : "";
        loc = null;
        for (int i = 0; i < availableLocales.length; i++) {
            if (availableLocales[i].equals(localeCode)) {
                loc = new Locale(language, country);
                break;
            }
        }
        if (loc == null) return; 
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            config.locale = loc;
            config.userSetLocale = true;
            am.updateConfiguration(config);
        } catch (RemoteException e) {
        }
    }
    void applyAudioSettings() {
        AudioManager am = new AudioManager(mContext);
        am.reloadAudioSettings();
    }
}
