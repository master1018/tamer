public class RemotePackagesPage extends Composite implements ISdkListener {
    private final UpdaterData mUpdaterData;
    private CheckboxTreeViewer mTreeViewerSources;
    private Tree mTreeSources;
    private TreeColumn mColumnSource;
    private Button mUpdateOnlyCheckBox;
    private Group mDescriptionContainer;
    private Button mAddSiteButton;
    private Button mDeleteSiteButton;
    private Button mRefreshButton;
    private Button mInstallSelectedButton;
    private Label mDescriptionLabel;
    RemotePackagesPage(Composite parent, UpdaterData updaterData) {
        super(parent, SWT.BORDER);
        mUpdaterData = updaterData;
        createContents(this);
        postCreate();  
    }
    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(5, false));
        mTreeViewerSources = new CheckboxTreeViewer(parent, SWT.BORDER);
        mTreeViewerSources.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                onTreeCheckStateChanged(event); 
            }
        });
        mTreeSources = mTreeViewerSources.getTree();
        mTreeSources.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onTreeSelected(); 
            }
        });
        mTreeSources.setHeaderVisible(true);
        mTreeSources.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
        mColumnSource = new TreeColumn(mTreeSources, SWT.NONE);
        mColumnSource.setWidth(289);
        mColumnSource.setText("Sites, Packages and Archives");
        mDescriptionContainer = new Group(parent, SWT.NONE);
        mDescriptionContainer.setLayout(new GridLayout(1, false));
        mDescriptionContainer.setText("Description");
        mDescriptionContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 5, 1));
        mDescriptionLabel = new Label(mDescriptionContainer, SWT.NONE);
        mDescriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        mDescriptionLabel.setText("Line1\nLine2\nLine3");  
        mAddSiteButton = new Button(parent, SWT.NONE);
        mAddSiteButton.setText("Add Add-on Site...");
        mAddSiteButton.setToolTipText("Allows you to enter a new add-on site. " +
                "Such site can only contribute add-ons and extra packages.");
        mAddSiteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onAddSiteSelected(); 
            }
        });
        mDeleteSiteButton = new Button(parent, SWT.NONE);
        mDeleteSiteButton.setText("Delete Add-on Site...");
        mDeleteSiteButton.setToolTipText("Allows you to remove an add-on site. " +
                "Built-in default sites cannot be removed.");
        mDeleteSiteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onRemoveSiteSelected(); 
            }
        });
        mUpdateOnlyCheckBox = new Button(parent, SWT.CHECK);
        mUpdateOnlyCheckBox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        mUpdateOnlyCheckBox.setText("Display updates only");
        mUpdateOnlyCheckBox.setToolTipText("When selected, only compatible update packages are shown in the list above.");
        mUpdateOnlyCheckBox.setSelection(mUpdaterData.getSettingsController().getShowUpdateOnly());
        mUpdateOnlyCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                onShowUpdateOnly(); 
            }
        });
        mRefreshButton = new Button(parent, SWT.NONE);
        mRefreshButton.setText("Refresh");
        mRefreshButton.setToolTipText("Refreshes the list of packages from open sites.");
        mRefreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onRefreshSelected(); 
            }
        });
        mInstallSelectedButton = new Button(parent, SWT.NONE);
        mInstallSelectedButton.setText("Install Selected");
        mInstallSelectedButton.setToolTipText("Allows you to review all selected packages " +
                "and install them.");
        mInstallSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onInstallSelectedArchives();  
            }
        });
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
                Rectangle r = mTreeSources.getClientArea();
                mColumnSource.setWidth(r.width);
            }
        };
        mTreeSources.addControlListener(resizer);
        resizer.controlResized(null);
    }
    private void onTreeSelected() {
        updateButtonsState();
        ISelection sel = mTreeViewerSources.getSelection();
        if (sel instanceof ITreeSelection) {
            Object elem = ((ITreeSelection) sel).getFirstElement();
            if (elem instanceof IDescription) {
                mDescriptionLabel.setText(((IDescription) elem).getLongDescription());
                mDescriptionContainer.layout(true);
                return;
            }
        }
        mDescriptionLabel.setText("");  
    }
    private void onTreeCheckStateChanged(CheckStateChangedEvent event) {
        updateButtonsState();
        boolean b = event.getChecked();
        Object elem = event.getElement(); 
        assert event.getSource() == mTreeViewerSources;
        if (b == false) {
            mTreeViewerSources.setSubtreeChecked(elem, b);
            return;
        }
        ITreeContentProvider provider =
            (ITreeContentProvider) mTreeViewerSources.getContentProvider();
        if (elem instanceof RepoSource) {
            mTreeViewerSources.setExpandedState(elem, true);
            for (Object pkg : provider.getChildren(elem)) {
                mTreeViewerSources.setChecked(pkg, true);
                selectCompatibleArchives(pkg, provider);
            }
        } else if (elem instanceof Package) {
            selectCompatibleArchives(elem, provider);
        }
    }
    private void selectCompatibleArchives(Object pkg, ITreeContentProvider provider) {
        for (Object archive : provider.getChildren(pkg)) {
            if (archive instanceof Archive) {
                mTreeViewerSources.setChecked(archive, ((Archive) archive).isCompatible());
            }
        }
    }
    private void onShowUpdateOnly() {
        SettingsController controller = mUpdaterData.getSettingsController();
        controller.setShowUpdateOnly(mUpdateOnlyCheckBox.getSelection());
        controller.saveSettings();
        ArrayList<Archive> archives = new ArrayList<Archive>();
        for (Object element : mTreeViewerSources.getCheckedElements()) {
            if (element instanceof Archive) {
                archives.add((Archive) element);
            }
            mTreeViewerSources.setChecked(element, false);
        }
        mTreeViewerSources.refresh();
        for (Archive a : archives) {
            if (a.isCompatible() && mTreeViewerSources.setChecked(a, true)) {
                mTreeViewerSources.setChecked(a.getParentPackage(), true);
            }
        }
        updateButtonsState();
    }
    private void onInstallSelectedArchives() {
        ArrayList<Archive> archives = new ArrayList<Archive>();
        for (Object element : mTreeViewerSources.getCheckedElements()) {
            if (element instanceof Archive) {
                archives.add((Archive) element);
            }
        }
        if (mUpdaterData != null) {
            mUpdaterData.updateOrInstallAll(archives);
        }
    }
    private void onAddSiteSelected() {
        final RepoSource[] knowSources = mUpdaterData.getSources().getSources();
        String title = "Add Add-on Site URL";
        String msg =
        "This dialog lets you add the URL of a new add-on site.\n" +
        "\n" +
        "An add-on site can only provide new add-ons or \"user\" packages.\n" +
        "Add-on sites cannot provide standard Android platforms, docs or samples packages.\n" +
        "Inserting a URL here will not allow you to clone an official Android repository.\n" +
        "\n" +
        "Please enter the URL of the repository.xml for the new add-on site:";
        InputDialog dlg = new InputDialog(getShell(), title, msg, null, new IInputValidator() {
            public String isValid(String newText) {
                if (newText == null || newText.length() == 0) {
                    return "Error: URL field is empty. Please enter a URL.";
                }
                if (!newText.startsWith("file:
                        !newText.startsWith("ftp:
                        !newText.startsWith("http:
                        !newText.startsWith("https:
                    return "Error: The URL must start by one of file:
                }
                for (RepoSource s : knowSources) {
                    if (newText.equalsIgnoreCase(s.getUrl())) {
                        return "Error : This site is already listed.";
                    }
                }
                return null;
            }
        });
        if (dlg.open() == Window.OK) {
            String url = dlg.getValue();
            mUpdaterData.getSources().add(new RepoSource(url, true ));
            onRefreshSelected();
        }
    }
    private void onRemoveSiteSelected() {
        boolean changed = false;
        ISelection sel = mTreeViewerSources.getSelection();
        if (mUpdaterData != null && sel instanceof ITreeSelection) {
            for (Object c : ((ITreeSelection) sel).toList()) {
                if (c instanceof RepoSource && ((RepoSource) c).isUserSource()) {
                    RepoSource source = (RepoSource) c;
                    String title = "Delete Add-on Site?";
                    String msg = String.format("Are you sure you want to delete the add-on site '%1$s'?",
                            source.getUrl());
                    if (MessageDialog.openQuestion(getShell(), title, msg)) {
                        mUpdaterData.getSources().remove(source);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            onRefreshSelected();
        }
    }
    private void onRefreshSelected() {
        if (mUpdaterData != null) {
            mUpdaterData.refreshSources(false );
        }
        mTreeViewerSources.refresh();
        updateButtonsState();
    }
    public void onSdkChange(boolean init) {
        RepoSourcesAdapter sources = mUpdaterData.getSourcesAdapter();
        mTreeViewerSources.setContentProvider(sources.getContentProvider());
        mTreeViewerSources.setLabelProvider(  sources.getLabelProvider());
        mTreeViewerSources.setInput(sources);
        onTreeSelected();
    }
    private void updateButtonsState() {
        boolean hasCheckedArchive = false;
        Object[] checked = mTreeViewerSources.getCheckedElements();
        if (checked != null) {
            for (Object c : checked) {
                if (c instanceof Archive) {
                    hasCheckedArchive = true;
                    break;
                }
            }
        }
        boolean hasSelectedUserSource = false;
        ISelection sel = mTreeViewerSources.getSelection();
        if (sel instanceof ITreeSelection) {
            for (Object c : ((ITreeSelection) sel).toList()) {
                if (c instanceof RepoSource &&
                        ((RepoSource) c).isUserSource()) {
                    hasSelectedUserSource = true;
                    break;
                }
            }
        }
        mAddSiteButton.setEnabled(true);
        mDeleteSiteButton.setEnabled(hasSelectedUserSource);
        mRefreshButton.setEnabled(true);
        mInstallSelectedButton.setEnabled(hasCheckedArchive);
        mUpdateOnlyCheckBox.setSelection(mUpdaterData.getSettingsController().getShowUpdateOnly());
    }
}
