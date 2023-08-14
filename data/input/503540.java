final class KeystoreSelectionPage extends ExportWizardPage {
    private final ExportWizard mWizard;
    private Button mUseExistingKeystore;
    private Button mCreateKeystore;
    private Text mKeystore;
    private Text mKeystorePassword;
    private Label mConfirmLabel;
    private Text mKeystorePassword2;
    private boolean mDisableOnChange = false;
    protected KeystoreSelectionPage(ExportWizard wizard, String pageName) {
        super(pageName);
        mWizard = wizard;
        setTitle("Keystore selection");
        setDescription(""); 
    }
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout(3, false);
        composite.setLayout(gl);
        GridData gd;
        mUseExistingKeystore = new Button(composite, SWT.RADIO);
        mUseExistingKeystore.setText("Use existing keystore");
        mUseExistingKeystore.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        mUseExistingKeystore.setSelection(true);
        mCreateKeystore = new Button(composite, SWT.RADIO);
        mCreateKeystore.setText("Create new keystore");
        mCreateKeystore.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        new Label(composite, SWT.NONE).setText("Location:");
        mKeystore = new Text(composite, SWT.BORDER);
        mKeystore.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        final Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
           @Override
           public void widgetSelected(SelectionEvent e) {
               FileDialog fileDialog;
               if (mUseExistingKeystore.getSelection()) {
                   fileDialog = new FileDialog(browseButton.getShell(),SWT.OPEN);
                   fileDialog.setText("Load Keystore");
               } else {
                   fileDialog = new FileDialog(browseButton.getShell(),SWT.SAVE);
                   fileDialog.setText("Select Keystore Name");
               }
               String fileName = fileDialog.open();
               if (fileName != null) {
                   mKeystore.setText(fileName);
               }
           }
        });
        new Label(composite, SWT.NONE).setText("Password:");
        mKeystorePassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        mKeystorePassword.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        mKeystorePassword.addVerifyListener(sPasswordVerifier);
        new Composite(composite, SWT.NONE).setLayoutData(gd = new GridData());
        gd.heightHint = gd.widthHint = 0;
        mConfirmLabel = new Label(composite, SWT.NONE);
        mConfirmLabel.setText("Confirm:");
        mKeystorePassword2 = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        mKeystorePassword2.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        mKeystorePassword2.addVerifyListener(sPasswordVerifier);
        new Composite(composite, SWT.NONE).setLayoutData(gd = new GridData());
        gd.heightHint = gd.widthHint = 0;
        mKeystorePassword2.setEnabled(false);
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
        mUseExistingKeystore.addSelectionListener(new SelectionAdapter() {
           @Override
           public void widgetSelected(SelectionEvent e) {
               boolean createStore = !mUseExistingKeystore.getSelection();
               mKeystorePassword2.setEnabled(createStore);
               mConfirmLabel.setEnabled(createStore);
               mWizard.setKeystoreCreationMode(createStore);
               onChange();
            }
        });
        mKeystore.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mWizard.setKeystore(mKeystore.getText().trim());
                onChange();
            }
        });
        mKeystorePassword.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mWizard.setKeystorePassword(mKeystorePassword.getText());
                onChange();
            }
        });
        mKeystorePassword2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
    }
    @Override
    public IWizardPage getNextPage() {
        if (mUseExistingKeystore.getSelection()) {
            return mWizard.getKeySelectionPage();
        }
        return mWizard.getKeyCreationPage();
    }
    @Override
    void onShow() {
        if ((mProjectDataChanged & DATA_PROJECT) != 0) {
            IProject project = mWizard.getProject();
            mDisableOnChange = true;
            String keystore = ProjectHelper.loadStringProperty(project,
                    ExportWizard.PROPERTY_KEYSTORE);
            if (keystore != null) {
                mKeystore.setText(keystore);
            }
            mKeystorePassword.setText(""); 
            mKeystorePassword2.setText(""); 
            mDisableOnChange = false;
            onChange();
        }
    }
    private void onChange() {
        if (mDisableOnChange) {
            return;
        }
        setErrorMessage(null);
        setMessage(null);
        boolean createStore = !mUseExistingKeystore.getSelection();
        String keystore = mKeystore.getText().trim();
        if (keystore.length() == 0) {
            setErrorMessage("Enter path to keystore.");
            setPageComplete(false);
            return;
        } else {
            File f = new File(keystore);
            if (f.exists() == false) {
                if (createStore == false) {
                    setErrorMessage("Keystore does not exist.");
                    setPageComplete(false);
                    return;
                }
            } else if (f.isDirectory()) {
                setErrorMessage("Keystore path is a directory.");
                setPageComplete(false);
                return;
            } else if (f.isFile()) {
                if (createStore) {
                    setErrorMessage("File already exists.");
                    setPageComplete(false);
                    return;
                }
            }
        }
        String value = mKeystorePassword.getText();
        if (value.length() == 0) {
            setErrorMessage("Enter keystore password.");
            setPageComplete(false);
            return;
        } else if (createStore && value.length() < 6) {
            setErrorMessage("Keystore password is too short - must be at least 6 characters.");
            setPageComplete(false);
            return;
        }
        if (createStore) {
            if (mKeystorePassword2.getText().length() == 0) {
                setErrorMessage("Confirm keystore password.");
                setPageComplete(false);
                return;
            }
            if (mKeystorePassword.getText().equals(mKeystorePassword2.getText()) == false) {
                setErrorMessage("Keystore passwords do not match.");
                setPageComplete(false);
                return;
            }
        }
        setPageComplete(true);
    }
}
