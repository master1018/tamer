public final class AdtPrefs extends AbstractPreferenceInitializer {
    public final static String PREFS_SDK_DIR = AdtPlugin.PLUGIN_ID + ".sdk"; 
    public final static String PREFS_BUILD_RES_AUTO_REFRESH = AdtPlugin.PLUGIN_ID + ".resAutoRefresh"; 
    public final static String PREFS_BUILD_FORCE_ERROR_ON_NATIVELIB_IN_JAR = AdtPlugin.PLUGIN_ID + ".forceErrorNativeLibInJar"; 
    public final static String PREFS_BUILD_VERBOSITY = AdtPlugin.PLUGIN_ID + ".buildVerbosity"; 
    public final static String PREFS_DEFAULT_DEBUG_KEYSTORE = AdtPlugin.PLUGIN_ID + ".defaultDebugKeyStore"; 
    public final static String PREFS_CUSTOM_DEBUG_KEYSTORE = AdtPlugin.PLUGIN_ID + ".customDebugKeyStore"; 
    public final static String PREFS_HOME_PACKAGE = AdtPlugin.PLUGIN_ID + ".homePackage"; 
    public final static String PREFS_EMU_OPTIONS = AdtPlugin.PLUGIN_ID + ".emuOptions"; 
    private final static AdtPrefs sThis = new AdtPrefs();
    private IPreferenceStore mStore;
    private String mOsSdkLocation;
    private BuildVerbosity mBuildVerbosity = BuildVerbosity.NORMAL;
    private boolean mBuildForceResResfresh = false;
    private boolean mBuildForceErrorOnNativeLibInJar = true;
    public static enum BuildVerbosity {
        ALWAYS(0),
        NORMAL(1),
        VERBOSE(2);
        private int mLevel;
        BuildVerbosity(int level) {
            mLevel = level;
        }
        public int getLevel() {
            return mLevel;
        }
        public static BuildVerbosity find(String name) {
            for (BuildVerbosity v : values()) {
                if (v.name().equals(name)) {
                    return v;
                }
            }
            return null;
        }
    }
    public static void init(IPreferenceStore preferenceStore) {
        sThis.mStore = preferenceStore;
    }
    public static AdtPrefs getPrefs() {
        return sThis;
    }
    public synchronized void loadValues(PropertyChangeEvent event) {
        String property = event != null ? event.getProperty() : null;
        if (property == null || PREFS_SDK_DIR.equals(property)) {
            mOsSdkLocation = mStore.getString(PREFS_SDK_DIR);
            if (mOsSdkLocation.length() > 0 && mOsSdkLocation.endsWith(File.separator) == false) {
                mOsSdkLocation = mOsSdkLocation + File.separator;
            }
        }
        if (property == null || PREFS_BUILD_VERBOSITY.equals(property)) {
            mBuildVerbosity = BuildVerbosity.find(mStore.getString(PREFS_BUILD_VERBOSITY));
            if (mBuildVerbosity == null) {
                mBuildVerbosity = BuildVerbosity.NORMAL;
            }
        }
        if (property == null || PREFS_BUILD_RES_AUTO_REFRESH.equals(property)) {
            mBuildForceResResfresh = mStore.getBoolean(PREFS_BUILD_RES_AUTO_REFRESH);
        }
        if (property == null || PREFS_BUILD_FORCE_ERROR_ON_NATIVELIB_IN_JAR.equals(property)) {
            mBuildForceErrorOnNativeLibInJar = mStore.getBoolean(PREFS_BUILD_RES_AUTO_REFRESH);
        }
    }
    public synchronized String getOsSdkFolder() {
        return mOsSdkLocation;
    }
    public synchronized BuildVerbosity getBuildVerbosity() {
        return mBuildVerbosity;
    }
    public  boolean getBuildForceResResfresh() {
        return mBuildForceResResfresh;
    }
    public boolean getBuildForceErrorOnNativeLibInJar() {
        return mBuildForceErrorOnNativeLibInJar;
    }
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
        store.setDefault(PREFS_BUILD_RES_AUTO_REFRESH, true);
        store.setDefault(PREFS_BUILD_FORCE_ERROR_ON_NATIVELIB_IN_JAR, true);
        store.setDefault(PREFS_BUILD_VERBOSITY, BuildVerbosity.ALWAYS.name());
        store.setDefault(PREFS_HOME_PACKAGE, "android.process.acore"); 
        try {
            store.setDefault(PREFS_DEFAULT_DEBUG_KEYSTORE,
                    DebugKeyProvider.getDefaultKeyStoreOsPath());
        } catch (KeytoolException e) {
            AdtPlugin.log(e, "Get default debug keystore path failed"); 
        } catch (AndroidLocationException e) {
            AdtPlugin.log(e, "Get default debug keystore path failed"); 
        }
    }
}
