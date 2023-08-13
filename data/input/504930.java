public class PreferenceInitializer extends AbstractPreferenceInitializer {
    public final static String ATTR_LOG_LEVEL =
        DdmsPlugin.PLUGIN_ID + ".logLevel"; 
    public final static String ATTR_DEBUG_PORT_BASE =
        DdmsPlugin.PLUGIN_ID + ".adbDebugBasePort"; 
    public final static String ATTR_SELECTED_DEBUG_PORT =
        DdmsPlugin.PLUGIN_ID + ".debugSelectedPort"; 
    public final static String ATTR_DEFAULT_THREAD_UPDATE =
        DdmsPlugin.PLUGIN_ID + ".defaultThreadUpdateEnabled"; 
    public final static String ATTR_DEFAULT_HEAP_UPDATE =
        DdmsPlugin.PLUGIN_ID + ".defaultHeapUpdateEnabled"; 
    public final static String ATTR_THREAD_INTERVAL =
        DdmsPlugin.PLUGIN_ID + ".threadStatusInterval"; 
    public final static String ATTR_IMAGE_SAVE_DIR =
        DdmsPlugin.PLUGIN_ID + ".imageSaveDir"; 
    public final static String ATTR_LAST_IMAGE_SAVE_DIR =
        DdmsPlugin.PLUGIN_ID + ".lastImageSaveDir"; 
    public final static String ATTR_LOGCAT_FONT =
        DdmsPlugin.PLUGIN_ID + ".logcatFont"; 
    public final static String ATTR_HPROF_ACTION =
        DdmsPlugin.PLUGIN_ID + ".hprofAction"; 
    public final static String ATTR_TIME_OUT =
        DdmsPlugin.PLUGIN_ID + ".timeOut"; 
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DdmsPlugin.getDefault().getPreferenceStore();
        store.setDefault(ATTR_DEBUG_PORT_BASE, DdmPreferences.DEFAULT_DEBUG_PORT_BASE);
        store.setDefault(ATTR_SELECTED_DEBUG_PORT, DdmPreferences.DEFAULT_SELECTED_DEBUG_PORT);
        store.setDefault(ATTR_DEFAULT_THREAD_UPDATE, DdmPreferences.DEFAULT_INITIAL_THREAD_UPDATE);
        store.setDefault(ATTR_DEFAULT_HEAP_UPDATE,
                DdmPreferences.DEFAULT_INITIAL_HEAP_UPDATE);
        store.setDefault(ATTR_THREAD_INTERVAL, DdmUiPreferences.DEFAULT_THREAD_REFRESH_INTERVAL);
        String homeDir = System.getProperty("user.home"); 
        store.setDefault(ATTR_IMAGE_SAVE_DIR, homeDir);
        store.setDefault(ATTR_LOG_LEVEL, DdmPreferences.DEFAULT_LOG_LEVEL.getStringValue());
        store.setDefault(ATTR_LOGCAT_FONT,
                new FontData("Courier", 10, SWT.NORMAL).toString()); 
        store.setDefault(ATTR_HPROF_ACTION, HProfHandler.ACTION_OPEN);
        store.setDefault(ATTR_TIME_OUT, DdmPreferences.DEFAULT_TIMEOUT);
    }
    public synchronized static void setupPreferences() {
        IPreferenceStore store = DdmsPlugin.getDefault().getPreferenceStore();
        DdmPreferences.setDebugPortBase(store.getInt(ATTR_DEBUG_PORT_BASE));
        DdmPreferences.setSelectedDebugPort(store.getInt(ATTR_SELECTED_DEBUG_PORT));
        DdmPreferences.setLogLevel(store.getString(ATTR_LOG_LEVEL));
        DdmPreferences.setInitialThreadUpdate(store.getBoolean(ATTR_DEFAULT_THREAD_UPDATE));
        DdmPreferences.setInitialHeapUpdate(store.getBoolean(ATTR_DEFAULT_HEAP_UPDATE));
        DdmUiPreferences.setThreadRefreshInterval(store.getInt(ATTR_THREAD_INTERVAL));
        DdmPreferences.setTimeOut(store.getInt(ATTR_TIME_OUT));
    }
}
