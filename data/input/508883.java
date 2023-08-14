public class PreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    public PreferencePage() {
        super(GRID);
        setPreferenceStore(DdmsPlugin.getDefault().getPreferenceStore());
    }
    @Override
    public void createFieldEditors() {
        IntegerFieldEditor ife;
        ife = new PortFieldEditor(PreferenceInitializer.ATTR_DEBUG_PORT_BASE,
            "Base local debugger port:", getFieldEditorParent());
        addField(ife);
        BooleanFieldEditor bfe;
        bfe = new BooleanFieldEditor(PreferenceInitializer.ATTR_DEFAULT_THREAD_UPDATE,
            "Thread updates enabled by default", getFieldEditorParent());
        addField(bfe);
        bfe = new BooleanFieldEditor(PreferenceInitializer.ATTR_DEFAULT_HEAP_UPDATE,
            "Heap updates enabled by default", getFieldEditorParent());
        addField(bfe);
        ife = new IntegerFieldEditor(PreferenceInitializer.ATTR_THREAD_INTERVAL,
            "Thread status refresh interval (seconds):", getFieldEditorParent());
        ife.setValidRange(1, 60);
        addField(ife);
        ComboFieldEditor cfe = new ComboFieldEditor(PreferenceInitializer.ATTR_HPROF_ACTION,
                "HPROF Action:", new String[][] {
                    { "Save to disk", HProfHandler.ACTION_SAVE },
                    { "Open in Eclipse", HProfHandler.ACTION_OPEN },
                }, getFieldEditorParent());
        addField(cfe);
        ife = new IntegerFieldEditor(PreferenceInitializer.ATTR_TIME_OUT,
                "ADB connection time out (ms):", getFieldEditorParent());
            addField(ife);
        RadioGroupFieldEditor rgfe = new RadioGroupFieldEditor(PreferenceInitializer.ATTR_LOG_LEVEL,
                "Logging Level", 1, new String[][] {
                    { "Verbose", LogLevel.VERBOSE.getStringValue() },
                    { "Debug", LogLevel.DEBUG.getStringValue() },
                    { "Info", LogLevel.INFO.getStringValue() },
                    { "Warning", LogLevel.WARN.getStringValue() },
                    { "Error", LogLevel.ERROR.getStringValue() },
                    { "Assert", LogLevel.ASSERT.getStringValue() }
                    },
                getFieldEditorParent(), true);
        addField(rgfe);
    }
    public void init(IWorkbench workbench) {
    }
}
