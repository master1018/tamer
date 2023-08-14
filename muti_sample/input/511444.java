public class LocalPackagesPage extends Composite implements ISdkListener {
    private final UpdaterData mUpdaterData;
    private Label mSdkLocLabel;
    private Text mSdkLocText;
    private Button mSdkLocBrowse;
    private TableViewer mTableViewerPackages;
    private Table mTablePackages;
    private TableColumn mColumnPackages;
    private Group mDescriptionContainer;
    private Composite mContainerButtons;
    private Button mUpdateButton;
    private Label mPlaceholder1;
    private Button mDeleteButton;
    private Label mPlaceholder2;
    private Button mRefreshButton;
    private Label mDescriptionLabel;
    public LocalPackagesPage(Composite parent, UpdaterData updaterData) {
        super(parent, SWT.BORDER);
        mUpdaterData = updaterData;
        createContents(this);
        postCreate();  
    }
    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(3, false));
        createSdkLocation(parent);
        mTableViewerPackages = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
        mTablePackages = mTableViewerPackages.getTable();
        mTablePackages.setHeaderVisible(true);
        mTablePackages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        mTablePackages.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onTreeSelected(); 
            }
        });
        mColumnPackages = new TableColumn(mTablePackages, SWT.NONE);
        mColumnPackages.setWidth(377);
        mColumnPackages.setText("Installed Packages");
        mDescriptionContainer = new Group(parent, SWT.NONE);
        mDescriptionContainer.setLayout(new GridLayout(1, false));
        mDescriptionContainer.setText("Description");
        mDescriptionContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        mDescriptionLabel = new Label(mDescriptionContainer, SWT.NONE);
        mDescriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
        mDescriptionLabel.setText("Line1\nLine2\nLine3");
        mContainerButtons = new Composite(parent, SWT.NONE);
        mContainerButtons.setLayout(new GridLayout(5, false));
        mContainerButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        mUpdateButton = new Button(mContainerButtons, SWT.NONE);
        mUpdateButton.setText("Update All...");
        mUpdateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUpdateSelected();  
            }
        });
        mPlaceholder1 = new Label(mContainerButtons, SWT.NONE);
        mPlaceholder1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        mDeleteButton = new Button(mContainerButtons, SWT.NONE);
        mDeleteButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        mDeleteButton.setText("Delete...");
        mDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onDeleteSelected();  
            }
        });
        mPlaceholder2 = new Label(mContainerButtons, SWT.NONE);
        mPlaceholder2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        mRefreshButton = new Button(mContainerButtons, SWT.NONE);
        mRefreshButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mRefreshButton.setText("Refresh");
        mRefreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onRefreshSelected();  
            }
        });
    }
    private void createSdkLocation(Composite parent) {
        mSdkLocLabel = new Label(parent, SWT.NONE);
        mSdkLocLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mSdkLocLabel.setText("SDK Location:");
        mSdkLocText = new Text(parent, SWT.BORDER);
        mSdkLocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        if (mUpdaterData.canUserChangeSdkRoot()) {
            mSdkLocBrowse = new Button(parent, SWT.NONE);
            mSdkLocBrowse.setText("Browse...");
        } else {
            mSdkLocText.setEditable(false);
            ((GridData)mSdkLocText.getLayoutData()).horizontalSpan++;
        }
        if (mUpdaterData.getOsSdkRoot() != null) {
            mSdkLocText.setText(mUpdaterData.getOsSdkRoot());
        }
    }
    @Override
    public void dispose() {
        mUpdaterData.removeListener(this);
        super.dispose();
    }
    @Override
    protected void checkSubclass() {
    }
    private void postCreate() {
        mUpdaterData.addListeners(this);
        adjustColumnsWidth();
        updateButtonsState();
    }
    private void adjustColumnsWidth() {
        ControlAdapter resizer = new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = mTablePackages.getClientArea();
                mColumnPackages.setWidth(r.width);
            }
        };
        mTablePackages.addControlListener(resizer);
        resizer.controlResized(null);
    }
    private void updateButtonsState() {
        ISelection sel = mTableViewerPackages.getSelection();
        boolean hasSelection = sel != null && !sel.isEmpty();
        mUpdateButton.setEnabled(mTablePackages.getItemCount() > 0);
        mDeleteButton.setEnabled(hasSelection);
        mRefreshButton.setEnabled(true);
    }
    private void onTreeSelected() {
        updateButtonsState();
        ISelection sel = mTableViewerPackages.getSelection();
        if (sel instanceof IStructuredSelection) {
            Object elem = ((IStructuredSelection) sel).getFirstElement();
            if (elem instanceof IDescription) {
                mDescriptionLabel.setText(((IDescription) elem).getLongDescription());
                mDescriptionContainer.layout(true);
                return;
            }
        }
        mDescriptionLabel.setText("");  
    }
    private void onUpdateSelected() {
        mUpdaterData.updateOrInstallAll(null );
    }
    private void onDeleteSelected() {
        ISelection sel = mTableViewerPackages.getSelection();
        if (sel instanceof IStructuredSelection) {
            Object elem = ((IStructuredSelection) sel).getFirstElement();
            if (elem instanceof Package) {
                String title = "Delete SDK Package";
                String error = null;
                Package p = (Package) elem;
                Archive[] archives = p.getArchives();
                if (archives.length == 1 && archives[0] != null && archives[0].isLocal()) {
                    Archive archive = archives[0];
                    String osPath = archive.getLocalOsPath();
                    File dir = new File(osPath);
                    if (dir.isDirectory()) {
                        String msg = String.format("Are you sure you want to delete '%1$s' at '%2$s'? This cannot be undone.",
                                p.getShortDescription(), osPath);
                        if (MessageDialog.openQuestion(getShell(), title, msg)) {
                            archive.deleteLocal();
                            onRefreshSelected();
                        }
                    } else {
                        error = "Directory not found for this package";
                    }
                } else {
                    error = "No local archive found for this package";
                }
                if (error != null) {
                    MessageDialog.openError(getShell(), title, error);
                }
                return;
            }
        }
    }
    private void onRefreshSelected() {
        mUpdaterData.reloadSdk();
        updateButtonsState();
    }
    public void onSdkChange(boolean init) {
        LocalSdkAdapter localSdkAdapter = mUpdaterData.getLocalSdkAdapter();
        mTableViewerPackages.setLabelProvider(  localSdkAdapter.getLabelProvider());
        mTableViewerPackages.setContentProvider(localSdkAdapter.getContentProvider());
        mTableViewerPackages.setInput(localSdkAdapter);
        onTreeSelected();
    }
}
