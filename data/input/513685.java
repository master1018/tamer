public class AndroidPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    private SdkDirectoryFieldEditor mDirectoryField;
    public AndroidPreferencePage() {
        super(GRID);
        setPreferenceStore(AdtPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.AndroidPreferencePage_Title);
    }
    @Override
    public void createFieldEditors() {
        mDirectoryField = new SdkDirectoryFieldEditor(AdtPrefs.PREFS_SDK_DIR,
                Messages.AndroidPreferencePage_SDK_Location_, getFieldEditorParent());
        addField(mDirectoryField);
    }
    public void init(IWorkbench workbench) {
    }
    @Override
    public void dispose() {
        super.dispose();
        if (mDirectoryField != null) {
            mDirectoryField.dispose();
            mDirectoryField = null;
        }
    }
    private static class SdkDirectoryFieldEditor extends DirectoryFieldEditor {
        private SdkTargetSelector mTargetSelector;
        private TargetChangedListener mTargetChangeListener;
        public SdkDirectoryFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            setEmptyStringAllowed(false);
        }
        @Override
        protected boolean doCheckState() {
            String fileName = getTextControl().getText();
            fileName = fileName.trim();
            if (fileName.indexOf(',') >= 0 || fileName.indexOf(';') >= 0) {
                setErrorMessage(Messages.AndroidPreferencePage_ERROR_Reserved_Char);
                return false;  
            }
            File file = new File(fileName);
            if (!file.isDirectory()) {
                setErrorMessage(JFaceResources.getString(
                    "DirectoryFieldEditor.errorMessage")); 
                return false;
            }
            boolean ok = AdtPlugin.getDefault().checkSdkLocationAndId(fileName,
                    new AdtPlugin.CheckSdkErrorHandler() {
                @Override
                public boolean handleError(String message) {
                    setErrorMessage(message.replaceAll("\n", " ")); 
                    return false;  
                }
                @Override
                public boolean handleWarning(String message) {
                    showMessage(message.replaceAll("\n", " ")); 
                    return true;  
                }
            });
            if (ok) clearMessage();
            return ok;
        }
        @Override
        public Text getTextControl(Composite parent) {
            setValidateStrategy(VALIDATE_ON_KEY_STROKE);
            return super.getTextControl(parent);
        }
        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            super.doFillIntoGrid(parent, numColumns);
            GridData gd;
            Label l = new Label(parent, SWT.NONE);
            l.setText("Note: The list of SDK Targets below is only reloaded once you hit 'Apply' or 'OK'.");
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = numColumns;
            l.setLayoutData(gd);
            try {
                Sdk sdk = Sdk.getCurrent();
                IAndroidTarget[] targets = sdk != null ? sdk.getTargets() : null;
                mTargetSelector = new SdkTargetSelector(parent,
                        targets,
                        false );
                gd = (GridData) mTargetSelector.getLayoutData();
                gd.horizontalSpan = numColumns;
                if (mTargetChangeListener == null) {
                    mTargetChangeListener = new TargetChangedListener();
                    AdtPlugin.getDefault().addTargetListener(mTargetChangeListener);
                }
            } catch (Exception e) {
                AdtPlugin.log(e, "SdkTargetSelector failed");
            }
        }
        @Override
        public void dispose() {
            super.dispose();
            if (mTargetChangeListener != null) {
                AdtPlugin.getDefault().removeTargetListener(mTargetChangeListener);
                mTargetChangeListener = null;
            }
        }
        private class TargetChangedListener implements ITargetChangeListener {
            public void onSdkLoaded() {
                if (mTargetSelector != null) {
                    Sdk sdk = Sdk.getCurrent();
                    IAndroidTarget[] targets = sdk != null ? sdk.getTargets() : null;
                    mTargetSelector.setTargets(targets);
                }
            }
            public void onProjectTargetChange(IProject changedProject) {
            }
            public void onTargetLoaded(IAndroidTarget target) {
            }
        }
    }
}
