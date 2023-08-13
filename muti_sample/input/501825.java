final class UpdateChooserDialog extends GridDialog {
    private static Point sLastSize;
    private boolean mLicenseAcceptAll;
    private boolean mInternalLicenseRadioUpdate;
    private SashForm mSashForm;
    private Composite mPackageRootComposite;
    private TableViewer mTableViewPackage;
    private Table mTablePackage;
    private TableColumn mTableColum;
    private StyledText mPackageText;
    private Button mLicenseRadioAccept;
    private Button mLicenseRadioReject;
    private Button mLicenseRadioAcceptAll;
    private Group mPackageTextGroup;
    private final UpdaterData mUpdaterData;
    private Group mTableGroup;
    private Label mErrorLabel;
    private final ArrayList<ArchiveInfo> mArchives;
    public UpdateChooserDialog(Shell parentShell,
            UpdaterData updaterData,
            ArrayList<ArchiveInfo> archives) {
        super(parentShell, 3, false);
        mUpdaterData = updaterData;
        mArchives = archives;
    }
    @Override
    protected boolean isResizable() {
        return true;
    }
    public ArrayList<ArchiveInfo> getResult() {
        ArrayList<ArchiveInfo> ais = new ArrayList<ArchiveInfo>();
        if (getReturnCode() == Window.OK) {
            for (ArchiveInfo ai : mArchives) {
                if (ai.isAccepted()) {
                    ais.add(ai);
                }
            }
        }
        return ais;
    }
    @Override
    public void createDialogContent(Composite parent) {
        mSashForm = new SashForm(parent, SWT.NONE);
        mSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        mTableGroup = new Group(mSashForm, SWT.NONE);
        mTableGroup.setText("Packages");
        mTableGroup.setLayout(new GridLayout(1, false));
        mTableViewPackage = new TableViewer(mTableGroup, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
        mTablePackage = mTableViewPackage.getTable();
        mTablePackage.setHeaderVisible(false);
        mTablePackage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        mTablePackage.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onPackageSelected();  
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                onPackageDoubleClick();
            }
        });
        mTableColum = new TableColumn(mTablePackage, SWT.NONE);
        mTableColum.setWidth(100);
        mTableColum.setText("Packages");
        mPackageRootComposite = new Composite(mSashForm, SWT.NONE);
        mPackageRootComposite.setLayout(new GridLayout(4, false));
        mPackageRootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mPackageTextGroup = new Group(mPackageRootComposite, SWT.NONE);
        mPackageTextGroup.setText("Package Description && License");
        mPackageTextGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        mPackageTextGroup.setLayout(new GridLayout(1, false));
        mPackageText = new StyledText(mPackageTextGroup,
                        SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        mPackageText.setBackground(
                getParentShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        mPackageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        mLicenseRadioAccept = new Button(mPackageRootComposite, SWT.RADIO);
        mLicenseRadioAccept.setText("Accept");
        mLicenseRadioAccept.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onLicenseRadioSelected();
            }
        });
        mLicenseRadioReject = new Button(mPackageRootComposite, SWT.RADIO);
        mLicenseRadioReject.setText("Reject");
        mLicenseRadioReject.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onLicenseRadioSelected();
            }
        });
        Label placeholder = new Label(mPackageRootComposite, SWT.NONE);
        placeholder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        mLicenseRadioAcceptAll = new Button(mPackageRootComposite, SWT.RADIO);
        mLicenseRadioAcceptAll.setText("Accept All");
        mLicenseRadioAcceptAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onLicenseRadioSelected();
            }
        });
        mSashForm.setWeights(new int[] {200, 300});
    }
    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; 
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());
        mErrorLabel = new Label(composite, SWT.NONE);
        mErrorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        label.setText("[*] Something depends on this package");
        label.setEnabled(false);
        layout.numColumns++;
        createButtonsForButtonBar(composite);
        Button button = getButton(IDialogConstants.OK_ID);
        button.setText("Install");
        return composite;
    }
    @Override
    public void create() {
        super.create();
        getShell().setText("Choose Packages to Install");
        setWindowImage();
        for (ArchiveInfo ai : mArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                String license = a.getParentPackage().getLicense();
                ai.setAccepted(license == null || license.trim().length() == 0);
            }
        }
        mTableViewPackage.setLabelProvider(new NewArchivesLabelProvider());
        mTableViewPackage.setContentProvider(new NewArchivesContentProvider());
        mTableViewPackage.setInput(mArchives);
        adjustColumnsWidth();
        mTablePackage.select(0);
        onPackageSelected();
    }
    private void setWindowImage() {
        String imageName = "android_icon_16.png"; 
        if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_DARWIN) {
            imageName = "android_icon_128.png"; 
        }
        if (mUpdaterData != null) {
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                getShell().setImage(imgFactory.getImageByName(imageName));
            }
        }
    }
    private void adjustColumnsWidth() {
        ControlAdapter resizer = new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = mTablePackage.getClientArea();
                mTableColum.setWidth(r.width);
            }
        };
        mTablePackage.addControlListener(resizer);
        resizer.controlResized(null);
    }
    @Override
    public boolean close() {
        sLastSize = getShell().getSize();
        return super.close();
    }
    @Override
    protected Point getInitialSize() {
        if (sLastSize != null) {
            return sLastSize;
        } else {
            return new Point(740, 370);
        }
    }
    private void onPackageSelected() {
        ArchiveInfo ai = getSelectedArchive();
        displayInformation(ai);
        displayMissingDependency(ai);
        updateLicenceRadios(ai);
    }
    private ArchiveInfo getSelectedArchive() {
        ISelection sel = mTableViewPackage.getSelection();
        if (sel instanceof IStructuredSelection) {
            Object elem = ((IStructuredSelection) sel).getFirstElement();
            if (elem instanceof ArchiveInfo) {
                return (ArchiveInfo) elem;
            }
        }
        return null;
    }
    private void displayInformation(ArchiveInfo ai) {
        if (ai == null) {
            mPackageText.setText("Please select a package.");
            return;
        }
        Archive aNew = ai.getNewArchive();
        if (aNew == null) {
            return;
        }
        Package pNew = aNew.getParentPackage();
        mPackageText.setText("");                   
        addSectionTitle("Package Description\n");
        addText(pNew.getLongDescription(), "\n\n"); 
        Archive aOld = ai.getReplaced();
        if (aOld != null) {
            Package pOld = aOld.getParentPackage();
            int rOld = pOld.getRevision();
            int rNew = pNew.getRevision();
            boolean showRev = true;
            if (pNew instanceof IPackageVersion && pOld instanceof IPackageVersion) {
                AndroidVersion vOld = ((IPackageVersion) pOld).getVersion();
                AndroidVersion vNew = ((IPackageVersion) pNew).getVersion();
                if (!vOld.equals(vNew)) {
                    addText(String.format("This update will replace API %1$s revision %2$d with API %3$s revision %4$d.\n\n",
                            vOld.getApiString(), rOld,
                            vNew.getApiString(), rNew));
                    showRev = false;
                }
            }
            if (showRev) {
                addText(String.format("This update will replace revision %1$d with revision %2$d.\n\n",
                        rOld,
                        rNew));
            }
        }
        ArchiveInfo[] aDeps = ai.getDependsOn();
        if ((aDeps != null && aDeps.length > 0) || ai.isDependencyFor()) {
            addSectionTitle("Dependencies\n");
            if (aDeps != null && aDeps.length > 0) {
                addText("Installing this package also requires installing:");
                for (ArchiveInfo aDep : aDeps) {
                    addText(String.format("\n- %1$s",
                            aDep.getShortDescription()));
                }
                addText("\n\n");
            }
            if (ai.isDependencyFor()) {
                addText("This package is a dependency for:");
                for (ArchiveInfo ai2 : ai.getDependenciesFor()) {
                    addText(String.format("\n- %1$s",
                            ai2.getShortDescription()));
                }
                addText("\n\n");
            }
        }
        addSectionTitle("Archive Description\n");
        addText(aNew.getLongDescription(), "\n\n");                             
        String license = pNew.getLicense();
        if (license != null) {
            addSectionTitle("License\n");
            addText(license.trim(), "\n\n");                                       
        }
        addSectionTitle("Site\n");
        addText(pNew.getParentSource().getShortDescription());
    }
    private void displayMissingDependency(ArchiveInfo ai) {
        String error = null;
        try {
            if (ai != null) {
                if (!ai.isAccepted()) {
                    for (ArchiveInfo ai2 : ai.getDependenciesFor()) {
                        if (ai2.isAccepted()) {
                            error = String.format("Package '%1$s' depends on this one.",
                                    ai2.getShortDescription());
                            return;
                        }
                    }
                } else {
                    ArchiveInfo[] adeps = ai.getDependsOn();
                    if (adeps != null) {
                        for (ArchiveInfo adep : adeps) {
                            if (!adep.isAccepted()) {
                                error = String.format("This package depends on '%1$s'.",
                                        adep.getShortDescription());
                                return;
                            }
                        }
                    }
                }
            }
            for (ArchiveInfo ai2 : mArchives) {
                if (ai2.isAccepted()) {
                    ArchiveInfo[] adeps = ai2.getDependsOn();
                    if (adeps != null) {
                        for (ArchiveInfo adep : adeps) {
                            if (!adep.isAccepted()) {
                                error = String.format("Package '%1$s' depends on '%2$s'",
                                        ai2.getShortDescription(),
                                        adep.getShortDescription());
                                return;
                            }
                        }
                    }
                }
            }
        } finally {
            mErrorLabel.setText(error == null ? "" : error);        
        }
    }
    private void addText(String...string) {
        for (String s : string) {
            mPackageText.append(s);
        }
    }
    private void addSectionTitle(String string) {
        String s = mPackageText.getText();
        int start = (s == null ? 0 : s.length());
        mPackageText.append(string);
        StyleRange sr = new StyleRange();
        sr.start = start;
        sr.length = string.length();
        sr.fontStyle = SWT.BOLD;
        sr.underline = true;
        mPackageText.setStyleRange(sr);
    }
    private void updateLicenceRadios(ArchiveInfo ai) {
        if (mInternalLicenseRadioUpdate) {
            return;
        }
        mInternalLicenseRadioUpdate = true;
        boolean oneAccepted = false;
        if (mLicenseAcceptAll) {
            mLicenseRadioAcceptAll.setSelection(true);
            mLicenseRadioAccept.setEnabled(true);
            mLicenseRadioReject.setEnabled(true);
            mLicenseRadioAccept.setSelection(false);
            mLicenseRadioReject.setSelection(false);
        } else {
            mLicenseRadioAcceptAll.setSelection(false);
            oneAccepted = ai != null && ai.isAccepted();
            mLicenseRadioAccept.setEnabled(ai != null);
            mLicenseRadioReject.setEnabled(ai != null);
            mLicenseRadioAccept.setSelection(oneAccepted);
            mLicenseRadioReject.setSelection(ai != null && ai.isRejected());
        }
        boolean missing = mErrorLabel.getText() != null && mErrorLabel.getText().length() > 0;
        if (!missing && !oneAccepted) {
            for(ArchiveInfo ai2 : mArchives) {
                if (ai2.isAccepted()) {
                    oneAccepted = true;
                    break;
                }
            }
        }
        getButton(IDialogConstants.OK_ID).setEnabled(!missing && oneAccepted);
        mInternalLicenseRadioUpdate = false;
    }
    private void onLicenseRadioSelected() {
        if (mInternalLicenseRadioUpdate) {
            return;
        }
        mInternalLicenseRadioUpdate = true;
        ArchiveInfo ai = getSelectedArchive();
        if (ai == null) {
            return;
        }
        boolean needUpdate = true;
        if (!mLicenseAcceptAll && mLicenseRadioAcceptAll.getSelection()) {
            mLicenseAcceptAll = true;
            for(ArchiveInfo ai2 : mArchives) {
                ai2.setAccepted(true);
                ai2.setRejected(false);
            }
        } else if (mLicenseRadioAccept.getSelection()) {
            mLicenseAcceptAll = false;
            ai.setAccepted(true);
            ai.setRejected(false);
        } else if (mLicenseRadioReject.getSelection()) {
            mLicenseAcceptAll = false;
            ai.setAccepted(false);
            ai.setRejected(true);
        } else {
            needUpdate = false;
        }
        mInternalLicenseRadioUpdate = false;
        if (needUpdate) {
            if (mLicenseAcceptAll) {
                mTableViewPackage.refresh();
            } else {
               mTableViewPackage.refresh(ai);
            }
            displayMissingDependency(ai);
            updateLicenceRadios(ai);
        }
    }
    private void onPackageDoubleClick() {
        ArchiveInfo ai = getSelectedArchive();
        if (ai == null) {
            return;
        }
        boolean wasAccepted = ai.isAccepted();
        ai.setAccepted(!wasAccepted);
        ai.setRejected(wasAccepted);
        mLicenseAcceptAll = false;
        mTableViewPackage.refresh(ai);
        updateLicenceRadios(ai);
    }
    private class NewArchivesLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            assert element instanceof ArchiveInfo;
            ArchiveInfo ai = (ArchiveInfo) element;
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                if (ai.isAccepted()) {
                    return imgFactory.getImageByName("accept_icon16.png");
                } else if (ai.isRejected()) {
                    return imgFactory.getImageByName("reject_icon16.png");
                }
                return imgFactory.getImageByName("unknown_icon16.png");
            }
            return super.getImage(element);
        }
        @Override
        public String getText(Object element) {
            assert element instanceof ArchiveInfo;
            ArchiveInfo ai = (ArchiveInfo) element;
            String desc = ai.getShortDescription();
            if (ai.isDependencyFor()) {
                desc += " [*]";
            }
            return desc;
        }
    }
    private class NewArchivesContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public Object[] getElements(Object inputElement) {
            return mArchives.toArray();
        }
    }
}
