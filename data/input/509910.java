final class KeySelectionPage extends ExportWizardPage {
    private final ExportWizard mWizard;
    private Label mKeyAliasesLabel;
    private Combo mKeyAliases;
    private Label mKeyPasswordLabel;
    private Text mKeyPassword;
    private boolean mDisableOnChange = false;
    private Button mUseExistingKey;
    private Button mCreateKey;
    protected KeySelectionPage(ExportWizard wizard, String pageName) {
        super(pageName);
        mWizard = wizard;
        setTitle("Key alias selection");
        setDescription(""); 
    }
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout(3, false);
        composite.setLayout(gl);
        GridData gd;
        mUseExistingKey = new Button(composite, SWT.RADIO);
        mUseExistingKey.setText("Use existing key");
        mUseExistingKey.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        mUseExistingKey.setSelection(true);
        new Composite(composite, SWT.NONE).setLayoutData(gd = new GridData());
        gd.heightHint = 0;
        gd.widthHint = 50;
        mKeyAliasesLabel = new Label(composite, SWT.NONE);
        mKeyAliasesLabel.setText("Alias:");
        mKeyAliases = new Combo(composite, SWT.READ_ONLY);
        mKeyAliases.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Composite(composite, SWT.NONE).setLayoutData(gd = new GridData());
        gd.heightHint = 0;
        gd.widthHint = 50;
        mKeyPasswordLabel = new Label(composite, SWT.NONE);
        mKeyPasswordLabel.setText("Password:");
        mKeyPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        mKeyPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mCreateKey = new Button(composite, SWT.RADIO);
        mCreateKey.setText("Create new key");
        mCreateKey.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
        mUseExistingKey.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mWizard.setKeyCreationMode(!mUseExistingKey.getSelection());
                enableWidgets();
                onChange();
            }
        });
        mKeyAliases.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mWizard.setKeyAlias(mKeyAliases.getItem(mKeyAliases.getSelectionIndex()));
                onChange();
            }
        });
        mKeyPassword.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mWizard.setKeyPassword(mKeyPassword.getText());
                onChange();
            }
        });
    }
    @Override
    void onShow() {
        if ((mProjectDataChanged & (DATA_PROJECT | DATA_KEYSTORE)) != 0) {
            mDisableOnChange = true;
            try {
                mWizard.setKeyCreationMode(false);
                mUseExistingKey.setSelection(true);
                mCreateKey.setSelection(false);
                enableWidgets();
                mKeyAliases.removeAll();
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                FileInputStream fis = new FileInputStream(mWizard.getKeystore());
                keyStore.load(fis, mWizard.getKeystorePassword().toCharArray());
                fis.close();
                Enumeration<String> aliases = keyStore.aliases();
                IProject project = mWizard.getProject();
                String keyAlias = ProjectHelper.loadStringProperty(project,
                        ExportWizard.PROPERTY_ALIAS);
                ArrayList<String> aliasList = new ArrayList<String>();
                int selection = -1;
                int count = 0;
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    mKeyAliases.add(alias);
                    aliasList.add(alias);
                    if (selection == -1 && alias.equalsIgnoreCase(keyAlias)) {
                        selection = count;
                    }
                    count++;
                }
                mWizard.setExistingAliases(aliasList);
                if (selection != -1) {
                    mKeyAliases.select(selection);
                    mWizard.setKeyAlias(keyAlias);
                } else {
                    mKeyAliases.clearSelection();
                }
                mKeyPassword.setText(""); 
                mDisableOnChange = false;
                onChange();
            } catch (KeyStoreException e) {
                onException(e);
            } catch (FileNotFoundException e) {
                onException(e);
            } catch (NoSuchAlgorithmException e) {
                onException(e);
            } catch (CertificateException e) {
                onException(e);
            } catch (IOException e) {
                onException(e);
            } finally {
                mDisableOnChange = false;
            }
        }
    }
    @Override
    public IWizardPage getPreviousPage() {
        return mWizard.getKeystoreSelectionPage();
    }
    @Override
    public IWizardPage getNextPage() {
        if (mWizard.getKeyCreationMode()) {
            return mWizard.getKeyCreationPage();
        }
        return mWizard.getKeyCheckPage();
    }
    private void onChange() {
        if (mDisableOnChange) {
            return;
        }
        setErrorMessage(null);
        setMessage(null);
        if (mWizard.getKeyCreationMode() == false) {
            if (mKeyAliases.getSelectionIndex() == -1) {
                setErrorMessage("Select a key alias.");
                setPageComplete(false);
                return;
            }
            if (mKeyPassword.getText().trim().length() == 0) {
                setErrorMessage("Enter key password.");
                setPageComplete(false);
                return;
            }
        }
        setPageComplete(true);
    }
    private void enableWidgets() {
        boolean useKey = !mWizard.getKeyCreationMode();
        mKeyAliasesLabel.setEnabled(useKey);
        mKeyAliases.setEnabled(useKey);
        mKeyPassword.setEnabled(useKey);
        mKeyPasswordLabel.setEnabled(useKey);
    }
}
