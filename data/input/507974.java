public class BuildPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    public BuildPreferencePage() {
        super(GRID);
        setPreferenceStore(AdtPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.BuildPreferencePage_Title);
    }
    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(AdtPrefs.PREFS_BUILD_RES_AUTO_REFRESH,
                Messages.BuildPreferencePage_Auto_Refresh_Resources_on_Build,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(AdtPrefs.PREFS_BUILD_FORCE_ERROR_ON_NATIVELIB_IN_JAR,
                "Force error when external jars contain native libraries",
                getFieldEditorParent()));
        RadioGroupFieldEditor rgfe = new RadioGroupFieldEditor(
                AdtPrefs.PREFS_BUILD_VERBOSITY,
                Messages.BuildPreferencePage_Build_Output, 1, new String[][] {
                    { Messages.BuildPreferencePage_Silent, BuildVerbosity.ALWAYS.name() },
                    { Messages.BuildPreferencePage_Normal, BuildVerbosity.NORMAL.name() },
                    { Messages.BuildPreferencePage_Verbose, BuildVerbosity.VERBOSE.name() }
                    },
                getFieldEditorParent(), true);
        addField(rgfe);
        addField(new ReadOnlyFieldEditor(AdtPrefs.PREFS_DEFAULT_DEBUG_KEYSTORE,
                Messages.BuildPreferencePage_Default_KeyStore, getFieldEditorParent()));
        addField(new KeystoreFieldEditor(AdtPrefs.PREFS_CUSTOM_DEBUG_KEYSTORE,
                Messages.BuildPreferencePage_Custom_Keystore, getFieldEditorParent()));
    }
    public void init(IWorkbench workbench) {
    }
    private static class ReadOnlyFieldEditor extends StringFieldEditor {
        public ReadOnlyFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }
        @Override
        protected void createControl(Composite parent) {
            super.createControl(parent);
            Text control = getTextControl();
            control.setEditable(false);
        }
    }
    private static class KeystoreFieldEditor extends FileFieldEditor {
        public KeystoreFieldEditor(String name, String label, Composite parent) {
            super(name, label, parent);
            setValidateStrategy(VALIDATE_ON_KEY_STROKE);
        }
        @Override
        protected boolean checkState() {
            String fileName = getTextControl().getText();
            fileName = fileName.trim();
            if (fileName.length() > 0) {
                File file = new File(fileName);
                if (file.isFile()) {
                    try {
                        DebugKeyProvider provider = new DebugKeyProvider(fileName,
                                null , null );
                        PrivateKey key = provider.getDebugKey();
                        X509Certificate certificate = (X509Certificate)provider.getCertificate();
                        if (key == null || certificate == null) {
                            showErrorMessage("Unable to find debug key in keystore!");
                            return false;
                        }
                        Date today = new Date();
                        if (certificate.getNotAfter().compareTo(today) < 0) {
                            showErrorMessage("Certificate is expired!");
                            return false;
                        }
                        if (certificate.getNotBefore().compareTo(today) > 0) {
                            showErrorMessage("Certificate validity is in the future!");
                            return false;
                        }
                        clearErrorMessage();
                        return true;
                    } catch (GeneralSecurityException e) {
                        handleException(e);
                        return false;
                    } catch (IOException e) {
                        handleException(e);
                        return false;
                    } catch (KeytoolException e) {
                        handleException(e);
                        return false;
                    } catch (AndroidLocationException e) {
                        handleException(e);
                        return false;
                    }
                } else {
                    showErrorMessage("Not a valid keystore path.");
                    return false;  
                }
            }
            clearErrorMessage();
            return true;
        }
        @Override
        public Text getTextControl(Composite parent) {
            setValidateStrategy(VALIDATE_ON_KEY_STROKE);
            return super.getTextControl(parent);
        }
        private void handleException(Throwable t) {
            String msg = t.getMessage();
            if (msg == null) {
                Throwable cause = t.getCause();
                if (cause != null) {
                    handleException(cause);
                } else {
                    setErrorMessage("Uknown error when getting the debug key!");
                }
                return;
            }
            showErrorMessage(msg);
        }
    }
}
