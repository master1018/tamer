public class AndroidPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
    private IProject mProject;
    private SdkTargetSelector mSelector;
    private Button mIsLibrary;
    private LibraryProperties mLibraryDependencies;
    public AndroidPropertyPage() {
    }
    @Override
    protected Control createContents(Composite parent) {
        mProject = (IProject)getElement();
        IAndroidTarget[] targets = null;
        if (Sdk.getCurrent() != null) {
            targets = Sdk.getCurrent().getTargets();
        }
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setLayout(new GridLayout(1, false));
        Group targetGroup = new Group(top, SWT.NONE);
        targetGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        targetGroup.setLayout(new GridLayout(1, false));
        targetGroup.setText("Project Build Target");
        mSelector = new SdkTargetSelector(targetGroup, targets);
        Group libraryGroup = new Group(top, SWT.NONE);
        libraryGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        libraryGroup.setLayout(new GridLayout(1, false));
        libraryGroup.setText("Library");
        mIsLibrary = new Button(libraryGroup, SWT.CHECK);
        mIsLibrary.setText("Is Library");
        mIsLibrary.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mLibraryDependencies.setEnabled(!mIsLibrary.getSelection());
            }
        });
        mLibraryDependencies = new LibraryProperties(libraryGroup);
        fillUi();
        mSelector.setSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateValidity();
            }
        });
        if (mProject.isOpen() == false) {
        }
        return top;
    }
    @Override
    public boolean performOk() {
        Sdk currentSdk = Sdk.getCurrent();
        if (currentSdk != null && mProject.isOpen()) {
            ProjectState state = Sdk.getProjectState(mProject);
            ProjectProperties properties = null;
            boolean mustSaveProp = false;
            IAndroidTarget newTarget = mSelector.getSelected();
            if (newTarget != state.getTarget()) {
                properties = state.getProperties();
                properties.setProperty(ProjectProperties.PROPERTY_TARGET, newTarget.hashString());
                mustSaveProp = true;
            }
            if (mIsLibrary.getSelection() != state.isLibrary()) {
                properties = state.getProperties();
                properties.setProperty(ProjectProperties.PROPERTY_LIBRARY,
                        Boolean.toString(mIsLibrary.getSelection()));
                mustSaveProp = true;
            }
            if (mLibraryDependencies.save(mIsLibrary.getSelection())) {
                mustSaveProp = true;
            }
            if (mustSaveProp) {
                state.saveProperties();
            }
        }
        return true;
    }
    @Override
    protected void performDefaults() {
        fillUi();
        updateValidity();
    }
    private void fillUi() {
        if (Sdk.getCurrent() != null && mProject.isOpen()) {
            ProjectState state = Sdk.getProjectState(mProject);
            IAndroidTarget target = state.getTarget();;
            if (target != null) {
                mSelector.setSelection(target);
            }
            mIsLibrary.setSelection(state.isLibrary());
            mLibraryDependencies.setContent(state);
            mLibraryDependencies.setEnabled(!state.isLibrary());
        }
    }
    private void updateValidity() {
        IAndroidTarget target = mSelector.getSelected();
        setValid(target != null);
    }
}
