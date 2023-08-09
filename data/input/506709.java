public class LogCatPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    public LogCatPreferencePage() {
        super(GRID);
        setPreferenceStore(DdmsPlugin.getDefault().getPreferenceStore());
    }
    @Override
    protected void createFieldEditors() {
        FontFieldEditor ffe = new FontFieldEditor(PreferenceInitializer.ATTR_LOGCAT_FONT,
                "Display Font:", getFieldEditorParent());
        addField(ffe);
        Preferences prefs = DdmsPlugin.getDefault().getPluginPreferences();
        prefs.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (PreferenceInitializer.ATTR_LOGCAT_FONT.equals(property)) {
                    try {
                        FontData fdat = new FontData((String)event.getNewValue());
                        LogCatView.setFont(new Font(getFieldEditorParent().getDisplay(), fdat));
                    } catch (IllegalArgumentException e) {
                    } catch (SWTError e2) {
                    }
                }
            }
        });
    }
    public void init(IWorkbench workbench) {
    }
}
