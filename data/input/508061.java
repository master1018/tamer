final class KeyCheckPage extends ExportWizardPage {
    private final ExportWizard mWizard;
    private PrivateKey mPrivateKey;
    private X509Certificate mCertificate;
    private Text mDestination;
    private boolean mFatalSigningError;
    private FormText mDetailText;
    private ScrolledComposite mScrolledComposite;
    private ApkSettings mApkSettings;
    private String mKeyDetails;
    private String mDestinationDetails;
    protected KeyCheckPage(ExportWizard wizard, String pageName) {
        super(pageName);
        mWizard = wizard;
        setTitle("Destination and key/certificate checks");
        setDescription(""); 
    }
    public void createControl(Composite parent) {
        setErrorMessage(null);
        setMessage(null);
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout(3, false);
        gl.verticalSpacing *= 3;
        composite.setLayout(gl);
        GridData gd;
        new Label(composite, SWT.NONE).setText("Destination APK file:");
        mDestination = new Text(composite, SWT.BORDER);
        mDestination.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        mDestination.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onDestinationChange(false );
            }
        });
        final Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(browseButton.getShell(), SWT.SAVE);
                fileDialog.setText("Destination file name");
                String filename = ProjectHelper.getApkFilename(mWizard.getProject(),
                        null );
                fileDialog.setFileName(filename);
                String saveLocation = fileDialog.open();
                if (saveLocation != null) {
                    mDestination.setText(saveLocation);
                }
            }
        });
        mScrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
        mScrolledComposite.setLayoutData(gd = new GridData(GridData.FILL_BOTH));
        gd.horizontalSpan = 3;
        mScrolledComposite.setExpandHorizontal(true);
        mScrolledComposite.setExpandVertical(true);
        mDetailText = new FormText(mScrolledComposite, SWT.NONE);
        mScrolledComposite.setContent(mDetailText);
        mScrolledComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                updateScrolling();
            }
        });
        setControl(composite);
    }
    @Override
    void onShow() {
        if ((mProjectDataChanged & DATA_PROJECT) != 0) {
            IProject project = mWizard.getProject();
            ProjectState state = Sdk.getProjectState(project);
            if (state != null) {
                mApkSettings = state.getApkSettings();
            }
            String destination = ProjectHelper.loadStringProperty(project,
                    ExportWizard.PROPERTY_DESTINATION);
            String filename = ProjectHelper.loadStringProperty(project,
                    ExportWizard.PROPERTY_FILENAME);
            if (destination != null && filename != null) {
                mDestination.setText(destination + File.separator + filename);
            }
        }
        if (mProjectDataChanged != 0) {
            mFatalSigningError = false;
            mWizard.setSigningInfo(null, null);
            mPrivateKey = null;
            mCertificate = null;
            mKeyDetails = null;
            if (mWizard.getKeystoreCreationMode() || mWizard.getKeyCreationMode()) {
                int validity = mWizard.getValidity();
                StringBuilder sb = new StringBuilder(
                        String.format("<p>Certificate expires in %d years.</p>",
                        validity));
                if (validity < 25) {
                    sb.append("<p>Make sure the certificate is valid for the planned lifetime of the product.</p>");
                    sb.append("<p>If the certificate expires, you will be forced to sign your application with a different one.</p>");
                    sb.append("<p>Applications cannot be upgraded if their certificate changes from one version to another, ");
                    sb.append("forcing a full uninstall/install, which will make the user lose his/her data.</p>");
                    sb.append("<p>Android Market currently requires certificates to be valid until 2033.</p>");
                }
                mKeyDetails = sb.toString();
            } else {
                try {
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream fis = new FileInputStream(mWizard.getKeystore());
                    keyStore.load(fis, mWizard.getKeystorePassword().toCharArray());
                    fis.close();
                    PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(
                            mWizard.getKeyAlias(),
                            new KeyStore.PasswordProtection(
                                    mWizard.getKeyPassword().toCharArray()));
                    if (entry != null) {
                        mPrivateKey = entry.getPrivateKey();
                        mCertificate = (X509Certificate)entry.getCertificate();
                    } else {
                        setErrorMessage("Unable to find key.");
                        setPageComplete(false);
                    }
                } catch (FileNotFoundException e) {
                    onException(e);
                } catch (KeyStoreException e) {
                    onException(e);
                } catch (NoSuchAlgorithmException e) {
                    onException(e);
                } catch (UnrecoverableEntryException e) {
                    onException(e);
                } catch (CertificateException e) {
                    onException(e);
                } catch (IOException e) {
                    onException(e);
                }
                if (mPrivateKey != null && mCertificate != null) {
                    Calendar expirationCalendar = Calendar.getInstance();
                    expirationCalendar.setTime(mCertificate.getNotAfter());
                    Calendar today = Calendar.getInstance();
                    if (expirationCalendar.before(today)) {
                        mKeyDetails = String.format(
                                "<p>Certificate expired on %s</p>",
                                mCertificate.getNotAfter().toString());
                        mFatalSigningError = true;
                        setErrorMessage("Certificate is expired.");
                        setPageComplete(false);
                    } else {
                        mWizard.setSigningInfo(mPrivateKey, mCertificate);
                        StringBuilder sb = new StringBuilder(String.format(
                                "<p>Certificate expires on %s.</p>",
                                mCertificate.getNotAfter().toString()));
                        int expirationYear = expirationCalendar.get(Calendar.YEAR);
                        int thisYear = today.get(Calendar.YEAR);
                        if (thisYear + 25 < expirationYear) {
                        } else {
                            if (expirationYear == thisYear) {
                                sb.append("<p>The certificate expires this year.</p>");
                            } else {
                                int count = expirationYear-thisYear;
                                sb.append(String.format(
                                        "<p>The Certificate expires in %1$s %2$s.</p>",
                                        count, count == 1 ? "year" : "years"));
                            }
                            sb.append("<p>Make sure the certificate is valid for the planned lifetime of the product.</p>");
                            sb.append("<p>If the certificate expires, you will be forced to sign your application with a different one.</p>");
                            sb.append("<p>Applications cannot be upgraded if their certificate changes from one version to another, ");
                            sb.append("forcing a full uninstall/install, which will make the user lose his/her data.</p>");
                            sb.append("<p>Android Market currently requires certificates to be valid until 2033.</p>");
                        }
                        mKeyDetails = sb.toString();
                    }
                } else {
                    mFatalSigningError = true;
                }
            }
        }
        onDestinationChange(true );
    }
    private void onDestinationChange(boolean forceDetailUpdate) {
        if (mFatalSigningError == false) {
            setErrorMessage(null);
            setMessage(null);
            String path = mDestination.getText().trim();
            if (path.length() == 0) {
                setErrorMessage("Enter destination for the APK file.");
                mWizard.resetDestination();
                setPageComplete(false);
                return;
            }
            File file = new File(path);
            if (file.isDirectory()) {
                setErrorMessage("Destination is a directory.");
                mWizard.resetDestination();
                setPageComplete(false);
                return;
            }
            File parentFolder = file.getParentFile();
            if (parentFolder == null || parentFolder.isDirectory() == false) {
                setErrorMessage("Not a valid directory.");
                mWizard.resetDestination();
                setPageComplete(false);
                return;
            }
            Map<String, String[]> apkFileMap = getApkFileMap(file);
            boolean fileExists = false;
            StringBuilder sb = new StringBuilder(String.format(
                    "<p>This will create the following files:</p>"));
            Set<Entry<String, String[]>> set = apkFileMap.entrySet();
            for (Entry<String, String[]> entry : set) {
                String[] apkArray = entry.getValue();
                String filename = apkArray[ExportWizard.APK_FILE_DEST];
                File f = new File(parentFolder, filename);
                if (f.isFile()) {
                    fileExists = true;
                    sb.append(String.format("<li>%1$s (WARNING: already exists)</li>", filename));
                } else if (f.isDirectory()) {
                    setErrorMessage(String.format("%1$s is a directory.", filename));
                    mWizard.resetDestination();
                    setPageComplete(false);
                    return;
                } else {
                    sb.append(String.format("<li>%1$s</li>", filename));
                }
            }
            mDestinationDetails = sb.toString();
            mWizard.setDestination(parentFolder, apkFileMap);
            setPageComplete(true);
            if (fileExists) {
                setMessage("A destination file already exists.", WARNING);
            }
            updateDetailText();
        } else if (forceDetailUpdate) {
            updateDetailText();
        }
    }
    private void updateScrolling() {
        if (mDetailText != null) {
            Rectangle r = mScrolledComposite.getClientArea();
            mScrolledComposite.setMinSize(mDetailText.computeSize(r.width, SWT.DEFAULT));
            mScrolledComposite.layout();
        }
    }
    private void updateDetailText() {
        StringBuilder sb = new StringBuilder("<form>");
        if (mKeyDetails != null) {
            sb.append(mKeyDetails);
        }
        if (mDestinationDetails != null && mFatalSigningError == false) {
            sb.append(mDestinationDetails);
        }
        sb.append("</form>");
        mDetailText.setText(sb.toString(), true ,
                true );
        mDetailText.getParent().layout();
        updateScrolling();
    }
    private Map<String, String[]> getApkFileMap(File file) {
        String filename = file.getName();
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        String[] apkArray = new String[ExportWizard.APK_COUNT];
        apkArray[ExportWizard.APK_FILE_SOURCE] = ProjectHelper.getApkFilename(
                mWizard.getProject(), null );
        apkArray[ExportWizard.APK_FILE_DEST] = filename;
        map.put(null, apkArray);
        if (mApkSettings != null) {
            Map<String, String> apkFilters = mApkSettings.getResourceFilters();
            if (apkFilters.size() > 0) {
                int index = filename.lastIndexOf('.');
                String base = filename.substring(0, index);
                String extension = filename.substring(index);
                for (Entry<String, String> entry : apkFilters.entrySet()) {
                    apkArray = new String[ExportWizard.APK_COUNT];
                    apkArray[ExportWizard.APK_FILE_SOURCE] = ProjectHelper.getApkFilename(
                            mWizard.getProject(), entry.getKey());
                    apkArray[ExportWizard.APK_FILE_DEST] = base + "-" + 
                            entry.getKey() + extension;
                    map.put(entry.getKey(), apkArray);
                }
            }
        }
        return map;
    }
    @Override
    protected void onException(Throwable t) {
        super.onException(t);
        mKeyDetails = String.format("ERROR: %1$s", ExportWizard.getExceptionMessage(t));
    }
}
