public final class PrefsDialog {
    private static PreferenceStore mPrefStore;
    public final static String SHELL_X = "shellX"; 
    public final static String SHELL_Y = "shellY"; 
    public final static String SHELL_WIDTH = "shellWidth"; 
    public final static String SHELL_HEIGHT = "shellHeight"; 
    public final static String EXPLORER_SHELL_X = "explorerShellX"; 
    public final static String EXPLORER_SHELL_Y = "explorerShellY"; 
    public final static String EXPLORER_SHELL_WIDTH = "explorerShellWidth"; 
    public final static String EXPLORER_SHELL_HEIGHT = "explorerShellHeight"; 
    public final static String SHOW_NATIVE_HEAP = "native"; 
    public final static String LOGCAT_COLUMN_MODE = "ddmsLogColumnMode"; 
    public final static String LOGCAT_FONT = "ddmsLogFont"; 
    public final static String LOGCAT_COLUMN_MODE_AUTO = "auto"; 
    public final static String LOGCAT_COLUMN_MODE_MANUAL = "manual"; 
    private final static String PREFS_DEBUG_PORT_BASE = "adbDebugBasePort"; 
    private final static String PREFS_SELECTED_DEBUG_PORT = "debugSelectedPort"; 
    private final static String PREFS_DEFAULT_THREAD_UPDATE = "defaultThreadUpdateEnabled"; 
    private final static String PREFS_DEFAULT_HEAP_UPDATE = "defaultHeapUpdateEnabled"; 
    private final static String PREFS_THREAD_REFRESH_INTERVAL = "threadStatusInterval"; 
    private final static String PREFS_LOG_LEVEL = "ddmsLogLevel"; 
    private final static String PREFS_TIMEOUT = "timeOut"; 
    private PrefsDialog() {}
    public static PreferenceStore getStore() {
        return mPrefStore;
    }
    public static void save() {
        try {
            mPrefStore.save();
        }
        catch (IOException ioe) {
            Log.w("ddms", "Failed saving prefs file: " + ioe.getMessage());
        }
    }
    public static void init() {
        assert mPrefStore == null;
        mPrefStore = SdkStatsService.getPreferenceStore();
        if (mPrefStore == null) {
            Log.e("ddms",
                    "failed to access both the user HOME directory and the system wide temp folder. Quitting.");
            System.exit(1);
        }
        setDefaults(System.getProperty("user.home")); 
        mPrefStore.addPropertyChangeListener(new ChangeListener());
        DdmPreferences.setDebugPortBase(mPrefStore.getInt(PREFS_DEBUG_PORT_BASE));
        DdmPreferences.setSelectedDebugPort(mPrefStore.getInt(PREFS_SELECTED_DEBUG_PORT));
        DdmPreferences.setLogLevel(mPrefStore.getString(PREFS_LOG_LEVEL));
        DdmPreferences.setInitialThreadUpdate(mPrefStore.getBoolean(PREFS_DEFAULT_THREAD_UPDATE));
        DdmPreferences.setInitialHeapUpdate(mPrefStore.getBoolean(PREFS_DEFAULT_HEAP_UPDATE));
        DdmPreferences.setTimeOut(mPrefStore.getInt(PREFS_TIMEOUT));
        String out = System.getenv("ANDROID_PRODUCT_OUT"); 
        DdmUiPreferences.setSymbolsLocation(out + File.separator + "symbols"); 
        DdmUiPreferences.setAddr2LineLocation("arm-eabi-addr2line"); 
        String traceview = System.getProperty("com.android.ddms.bindir");  
        if (traceview != null && traceview.length() != 0) {
            traceview += File.separator + DdmConstants.FN_TRACEVIEW;
        } else {
            traceview = DdmConstants.FN_TRACEVIEW;
        }
        DdmUiPreferences.setTraceviewLocation(traceview);
        DdmUiPreferences.setStore(mPrefStore);
        DdmUiPreferences.setThreadRefreshInterval(mPrefStore.getInt(PREFS_THREAD_REFRESH_INTERVAL));
    }
    private static void setDefaults(String homeDir) {
        mPrefStore.setDefault(PREFS_DEBUG_PORT_BASE, DdmPreferences.DEFAULT_DEBUG_PORT_BASE);
        mPrefStore.setDefault(PREFS_SELECTED_DEBUG_PORT,
                DdmPreferences.DEFAULT_SELECTED_DEBUG_PORT);
        mPrefStore.setDefault(PREFS_DEFAULT_THREAD_UPDATE, true);
        mPrefStore.setDefault(PREFS_DEFAULT_HEAP_UPDATE, false);
        mPrefStore.setDefault(PREFS_THREAD_REFRESH_INTERVAL,
            DdmUiPreferences.DEFAULT_THREAD_REFRESH_INTERVAL);
        mPrefStore.setDefault("textSaveDir", homeDir); 
        mPrefStore.setDefault("imageSaveDir", homeDir); 
        mPrefStore.setDefault(PREFS_LOG_LEVEL, "info"); 
        mPrefStore.setDefault(PREFS_TIMEOUT, DdmPreferences.DEFAULT_TIMEOUT);
        FontData fdat = new FontData("Courier", 10, SWT.NORMAL); 
        mPrefStore.setDefault("textOutputFont", fdat.toString()); 
        mPrefStore.setDefault(SHELL_X, 100);
        mPrefStore.setDefault(SHELL_Y, 100);
        mPrefStore.setDefault(SHELL_WIDTH, 800);
        mPrefStore.setDefault(SHELL_HEIGHT, 600);
        mPrefStore.setDefault(EXPLORER_SHELL_X, 50);
        mPrefStore.setDefault(EXPLORER_SHELL_Y, 50);
        mPrefStore.setDefault(SHOW_NATIVE_HEAP, false);
    }
    private static class ChangeListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            String changed = event.getProperty();
            if (changed.equals(PREFS_DEBUG_PORT_BASE)) {
                DdmPreferences.setDebugPortBase(mPrefStore.getInt(PREFS_DEBUG_PORT_BASE));
            } else if (changed.equals(PREFS_SELECTED_DEBUG_PORT)) {
                DdmPreferences.setSelectedDebugPort(mPrefStore.getInt(PREFS_SELECTED_DEBUG_PORT));
            } else if (changed.equals(PREFS_LOG_LEVEL)) {
                DdmPreferences.setLogLevel((String)event.getNewValue());
            } else if (changed.equals("textSaveDir")) {
                mPrefStore.setValue("lastTextSaveDir",
                    (String) event.getNewValue());
            } else if (changed.equals("imageSaveDir")) {
                mPrefStore.setValue("lastImageSaveDir",
                    (String) event.getNewValue());
            } else if (changed.equals(PREFS_TIMEOUT)) {
                DdmPreferences.setTimeOut(mPrefStore.getInt(PREFS_TIMEOUT));
            } else {
                Log.v("ddms", "Preference change: " + event.getProperty()
                    + ": '" + event.getOldValue()
                    + "' --> '" + event.getNewValue() + "'");
            }
        }
    }
    public static void run(Shell shell) {
        assert mPrefStore != null;
        PreferenceManager prefMgr = new PreferenceManager();
        PreferenceNode node, subNode;
        node = new PreferenceNode("debugger", new DebuggerPrefs());
        prefMgr.addToRoot(node);
        subNode = new PreferenceNode("panel", new PanelPrefs());
        prefMgr.addToRoot(subNode);
        node = new PreferenceNode("LogCat", new LogCatPrefs());
        prefMgr.addToRoot(node);
        node = new PreferenceNode("misc", new MiscPrefs());
        prefMgr.addToRoot(node);
        node = new PreferenceNode("stats", new UsageStatsPrefs());
        prefMgr.addToRoot(node);
        PreferenceDialog dlg = new PreferenceDialog(shell, prefMgr);
        dlg.setPreferenceStore(mPrefStore);
        dlg.open();
        try {
            mPrefStore.save();
        }
        catch (IOException ioe) {
        }
    }
    private static class DebuggerPrefs extends FieldEditorPreferencePage {
        public DebuggerPrefs() {
            super(GRID);        
            setTitle("Debugger");
        }
        @Override
        protected void createFieldEditors() {
            IntegerFieldEditor ife;
            ife = new PortFieldEditor(PREFS_DEBUG_PORT_BASE,
                "Starting value for local port:", getFieldEditorParent());
            addField(ife);
            ife = new PortFieldEditor(PREFS_SELECTED_DEBUG_PORT,
                "Port of Selected VM:", getFieldEditorParent());
            addField(ife);
        }
    }
    private static class PanelPrefs extends FieldEditorPreferencePage {
        public PanelPrefs() {
            super(FLAT);        
            setTitle("Info Panels");
        }
        @Override
        protected void createFieldEditors() {
            BooleanFieldEditor bfe;
            IntegerFieldEditor ife;
            bfe = new BooleanFieldEditor(PREFS_DEFAULT_THREAD_UPDATE,
                "Thread updates enabled by default", getFieldEditorParent());
            addField(bfe);
            bfe = new BooleanFieldEditor(PREFS_DEFAULT_HEAP_UPDATE,
                "Heap updates enabled by default", getFieldEditorParent());
            addField(bfe);
            ife = new IntegerFieldEditor(PREFS_THREAD_REFRESH_INTERVAL,
                "Thread status interval (seconds):", getFieldEditorParent());
            ife.setValidRange(1, 60);
            addField(ife);
        }
    }
    private static class LogCatPrefs extends FieldEditorPreferencePage {
        public LogCatPrefs() {
            super(FLAT);        
            setTitle("Logcat");
        }
        @Override
        protected void createFieldEditors() {
            RadioGroupFieldEditor rgfe;
            rgfe = new RadioGroupFieldEditor(PrefsDialog.LOGCAT_COLUMN_MODE,
                "Message Column Resizing Mode", 1, new String[][] {
                    { "Manual", PrefsDialog.LOGCAT_COLUMN_MODE_MANUAL },
                    { "Automatic", PrefsDialog.LOGCAT_COLUMN_MODE_AUTO },
                    },
                getFieldEditorParent(), true);
            addField(rgfe);
            FontFieldEditor ffe = new FontFieldEditor(PrefsDialog.LOGCAT_FONT, "Text output font:",
                    getFieldEditorParent());
            addField(ffe);
        }
    }
    private static class MiscPrefs extends FieldEditorPreferencePage {
        public MiscPrefs() {
            super(FLAT);        
            setTitle("Misc");
        }
        @Override
        protected void createFieldEditors() {
            DirectoryFieldEditor dfe;
            FontFieldEditor ffe;
            IntegerFieldEditor ife = new IntegerFieldEditor(PREFS_TIMEOUT,
                    "ADB connection time out (ms):", getFieldEditorParent());
            addField(ife);
            dfe = new DirectoryFieldEditor("textSaveDir",
                "Default text save dir:", getFieldEditorParent());
            addField(dfe);
            dfe = new DirectoryFieldEditor("imageSaveDir",
                "Default image save dir:", getFieldEditorParent());
            addField(dfe);
            ffe = new FontFieldEditor("textOutputFont", "Text output font:",
                getFieldEditorParent());
            addField(ffe);
            RadioGroupFieldEditor rgfe;
            rgfe = new RadioGroupFieldEditor(PREFS_LOG_LEVEL,
                "Logging Level", 1, new String[][] {
                    { "Verbose", LogLevel.VERBOSE.getStringValue() },
                    { "Debug", LogLevel.DEBUG.getStringValue() },
                    { "Info", LogLevel.INFO.getStringValue() },
                    { "Warning", LogLevel.WARN.getStringValue() },
                    { "Error", LogLevel.ERROR.getStringValue() },
                    { "Assert", LogLevel.ASSERT.getStringValue() },
                    },
                getFieldEditorParent(), true);
            addField(rgfe);
        }
    }
    private static class UsageStatsPrefs extends PreferencePage {
        private BooleanFieldEditor mOptInCheckbox;
        private Composite mTop;
        public UsageStatsPrefs() {
            setTitle("Usage Stats");
        }
        @Override
        protected Control createContents(Composite parent) {
            mTop = new Composite(parent, SWT.NONE);
            mTop.setLayout(new GridLayout(1, false));
            mTop.setLayoutData(new GridData(GridData.FILL_BOTH));
            Link text = new Link(mTop, SWT.WRAP);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setText(SdkStatsService.BODY_TEXT);
            text.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    SdkStatsService.openUrl(event.text);
                }
            });
            mOptInCheckbox = new BooleanFieldEditor(SdkStatsService.PING_OPT_IN,
                    SdkStatsService.CHECKBOX_TEXT, mTop);
            mOptInCheckbox.setPage(this);
            mOptInCheckbox.setPreferenceStore(getPreferenceStore());
            mOptInCheckbox.load();
            return null;
        }
        @Override
        protected Point doComputeSize() {
            if (mTop != null) {
                return mTop.computeSize(450, SWT.DEFAULT, true);
            }
            return super.doComputeSize();
        }
        @Override
        protected void performDefaults() {
            if (mOptInCheckbox != null) {
                mOptInCheckbox.loadDefault();
            }
            super.performDefaults();
        }
        @Override
        public void performApply() {
            if (mOptInCheckbox != null) {
                mOptInCheckbox.store();
            }
            super.performApply();
        }
        @Override
        public boolean performOk() {
            if (mOptInCheckbox != null) {
                mOptInCheckbox.store();
            }
            return super.performOk();
        }
    }
}
