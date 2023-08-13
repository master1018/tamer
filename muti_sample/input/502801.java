public class LaunchPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    public LaunchPreferencePage() {
        super(GRID);
        setPreferenceStore(AdtPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.LaunchPreferencePage_Title);
    }
    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(AdtPrefs.PREFS_EMU_OPTIONS,
                Messages.LaunchPreferencePage_Default_Emu_Options, getFieldEditorParent()));
        addField(new StringFieldEditor(AdtPrefs.PREFS_HOME_PACKAGE,
                Messages.LaunchPreferencePage_Default_HOME_Package, getFieldEditorParent()));
    }
    public void init(IWorkbench workbench) {
    }
}
