final class LibraryProperties {
    private Composite mTop;
    private Table mTable;
    private Image mMatchIcon;
    private Image mErrorIcon;
    private Button mAddButton;
    private Button mRemoveButton;
    private Button mUpButton;
    private Button mDownButton;
    private ProjectChooserHelper mProjectChooser;
    private ProjectState mState;
    private final List<ItemData> mItemDataList = new ArrayList<ItemData>();
    private boolean mMustSave = false;
    private final static class ItemData {
        String relativePath;
        IProject project;
    }
    IProjectChooserFilter mFilter = new IProjectChooserFilter() {
        public boolean accept(IProject project) {
            ProjectState state = Sdk.getProjectState(project);
            if (state != null) {
                if (state.isLibrary() == false || project == mState.getProject()) {
                    return false;
                }
                for (ItemData data : mItemDataList) {
                    if (data.project == project) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        public boolean useCache() {
            return false;
        }
    };
    LibraryProperties(Composite parent) {
        mMatchIcon = AdtPlugin.getImageDescriptor("/icons/match.png").createImage(); 
        mErrorIcon = AdtPlugin.getImageDescriptor("/icons/error.png").createImage(); 
        mTop = new Composite(parent, SWT.NONE);
        mTop.setLayout(new GridLayout(2, false));
        mTop.setLayoutData(new GridData(GridData.FILL_BOTH));
        mTop.setFont(parent.getFont());
        mTop.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                mMatchIcon.dispose();
                mErrorIcon.dispose();
            }
        });
        mTable = new Table(mTop, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        mTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mTable.setHeaderVisible(true);
        mTable.setLinesVisible(false);
        mTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetEnabled();
            }
        });
        final TableColumn column0 = new TableColumn(mTable, SWT.NONE);
        column0.setText("Reference");
        final TableColumn column1 = new TableColumn(mTable, SWT.NONE);
        column1.setText("Project");
        Composite buttons = new Composite(mTop, SWT.NONE);
        buttons.setLayout(new GridLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        mProjectChooser = new ProjectChooserHelper(parent.getShell(), mFilter);
        mAddButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mAddButton.setText("Add...");
        mAddButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IJavaProject javaProject = mProjectChooser.chooseJavaProject(null ,
                        "Please select a library project");
                if (javaProject != null) {
                    IProject iProject = javaProject.getProject();
                    IPath relativePath = Sdk.makeRelativeTo(
                            iProject.getLocation(), mState.getProject().getLocation());
                    addItem(relativePath.toString(), iProject, -1);
                    resetEnabled();
                    mMustSave = true;
                }
            }
        });
        mRemoveButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRemoveButton.setText("Remove");
        mRemoveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem selection = mTable.getSelection()[0];
                ItemData data = (ItemData) selection.getData();
                mItemDataList.remove(data);
                mTable.remove(mTable.getSelectionIndex());
                resetEnabled();
                mMustSave = true;
            }
        });
        Label l = new Label(buttons, SWT.SEPARATOR | SWT.HORIZONTAL);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mUpButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mUpButton.setText("Up");
        mUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mTable.getSelectionIndex();
                ItemData data = mItemDataList.remove(index);
                mTable.remove(index);
                addItem(data.relativePath, data.project, index - 1);
                mTable.select(index - 1);
                resetEnabled();
                mMustSave = true;
            }
        });
        mDownButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDownButton.setText("Down");
        mDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mTable.getSelectionIndex();
                ItemData data = mItemDataList.remove(index);
                mTable.remove(index);
                addItem(data.relativePath, data.project, index + 1);
                mTable.select(index + 1);
                resetEnabled();
                mMustSave = true;
            }
        });
        adjustColumnsWidth(mTable, column0, column1);
    }
    void setContent(ProjectState state) {
        mState = state;
        mTable.removeAll();
        mItemDataList.clear();
        List<LibraryState> libs = state.getLibraries();
        for (LibraryState lib : libs) {
            ProjectState libState = lib.getProjectState();
            addItem(lib.getRelativePath(), libState != null ? libState.getProject() : null, -1);
        }
        mMustSave = false;
        resetEnabled();
    }
    boolean save(boolean isLibrary) {
        boolean mustSave = mMustSave || (isLibrary && mState.getLibraries().size() > 0);
        if (mustSave) {
            ProjectProperties props = mState.getProperties();
            Set<String> keys = props.keySet();
            for (String key : keys) {
                if (key.startsWith(ProjectProperties.PROPERTY_LIB_REF)) {
                    props.removeProperty(key);
                }
            }
            if (isLibrary == false) {
                int index = 1;
                for (ItemData data : mItemDataList) {
                    props.setProperty(ProjectProperties.PROPERTY_LIB_REF + index++,
                            data.relativePath);
                }
            }
        }
        mMustSave = false;
        return mustSave;
    }
    void setEnabled(boolean enabled) {
        if (enabled == false) {
            mTable.setEnabled(false);
            mAddButton.setEnabled(false);
            mRemoveButton.setEnabled(false);
            mUpButton.setEnabled(false);
            mDownButton.setEnabled(false);
        } else {
            mTable.setEnabled(true);
            mAddButton.setEnabled(true);
            resetEnabled();
        }
    }
    private void resetEnabled() {
        int index = mTable.getSelectionIndex();
        mRemoveButton.setEnabled(index != -1);
        mUpButton.setEnabled(index > 0);
        mDownButton.setEnabled(index != -1 && index < mTable.getItemCount() - 1);
    }
    private void addItem(String relativePath, IProject project, int index) {
        ItemData data = new ItemData();
        data.relativePath = relativePath;
        data.project = project;
        TableItem item;
        if (index == -1) {
            mItemDataList.add(data);
            item = new TableItem(mTable, SWT.NONE);
        } else {
            mItemDataList.add(index, data);
            item = new TableItem(mTable, SWT.NONE, index);
        }
        item.setData(data);
        item.setText(0, data.relativePath);
        item.setImage( data.project != null ? mMatchIcon : mErrorIcon);
        item.setText(1, data.project != null ? data.project.getName() : "?");
    }
    private void adjustColumnsWidth(final Table table,
            final TableColumn column0,
            final TableColumn column1) {
        table.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = table.getClientArea();
                column0.setWidth(r.width * 50 / 100); 
                column1.setWidth(r.width * 50 / 100); 
            }
        });
    }
}
