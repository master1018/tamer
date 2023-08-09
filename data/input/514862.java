public class NewTestProjectCreationPage extends WizardPage {
    static final String TEST_PAGE_NAME = "newAndroidTestProjectPage"; 
    private static final String INITIAL_NAME = "";  
    private static final boolean INITIAL_USE_DEFAULT_LOCATION = true;
    private static final boolean INITIAL_CREATE_TEST_PROJECT = false;
    private static final Pattern sProjectNamePattern = Pattern.compile("^[\\w][\\w. -]*$");  
    private static String sCustomLocationOsPath = "";  
    private final int MSG_NONE = 0;
    private final int MSG_WARNING = 1;
    private final int MSG_ERROR = 2;
    private final TestInfo mInfo = new TestInfo();
    private IMainInfo mMainInfo;
    private Text mProjectNameField;
    private Text mPackageNameField;
    private Text mApplicationNameField;
    private Button mUseDefaultLocation;
    private Label mLocationLabel;
    private Text mLocationPathField;
    private Button mBrowseButton;
    private Text mMinSdkVersionField;
    private SdkTargetSelector mSdkTargetSelector;
    private ITargetChangeListener mSdkTargetChangeListener;
    private Button mCreateTestProjectField;
    private Text mTestedProjectNameField;
    private Button mProjectBrowseButton;
    private ProjectChooserHelper mProjectChooserHelper;
    private Button mTestSelfProjectRadio;
    private Button mTestExistingProjectRadio;
    private ArrayList<Composite> mToggleComposites = new ArrayList<Composite>();
    private boolean mInternalProjectNameUpdate;
    private boolean mInternalLocationPathUpdate;
    private boolean mInternalPackageNameUpdate;
    private boolean mInternalApplicationNameUpdate;
    private boolean mInternalMinSdkVersionUpdate;
    private boolean mInternalSdkTargetUpdate;
    private IProject mExistingTestedProject;
    private boolean mProjectNameModifiedByUser;
    private boolean mApplicationNameModifiedByUser;
    private boolean mPackageNameModifiedByUser;
    private boolean mMinSdkVersionModifiedByUser;
    private boolean mSdkTargetModifiedByUser;
    private Label mTestTargetPackageLabel;
    private String mLastExistingPackageName;
    public NewTestProjectCreationPage() {
        super(TEST_PAGE_NAME);
        setPageComplete(false);
        setTitle("New Android Test Project");
        setDescription("Creates a new Android Test Project resource.");
    }
    public class TestInfo {
        public boolean getCreateTestProject() {
            return mCreateTestProjectField == null ? true : mCreateTestProjectField.getSelection();
        }
        public IPath getLocationPath() {
            return new Path(getProjectLocation());
        }
        public String getProjectName() {
            return mProjectNameField == null ? INITIAL_NAME : mProjectNameField.getText().trim();
        }
        public String getPackageName() {
            return mPackageNameField == null ? INITIAL_NAME : mPackageNameField.getText().trim();
        }
        public String getTargetPackageName() {
            return mTestTargetPackageLabel == null ? INITIAL_NAME
                                                   : mTestTargetPackageLabel.getText().trim();
        }
        public String getMinSdkVersion() {
            return mMinSdkVersionField == null ? "" : mMinSdkVersionField.getText().trim();  
        }
        public String getApplicationName() {
            return mApplicationNameField == null ? "" : mApplicationNameField.getText().trim();  
        }
        public boolean useDefaultLocation() {
            return mUseDefaultLocation == null ? INITIAL_USE_DEFAULT_LOCATION
                                               : mUseDefaultLocation.getSelection();
        }
        public String getSourceFolder() {
            return SdkConstants.FD_SOURCES;
        }
        public IAndroidTarget getSdkTarget() {
            return mSdkTargetSelector == null ? null : mSdkTargetSelector.getSelected();
        }
        public boolean isTestingSelf() {
            return mMainInfo == null &&
                (mTestSelfProjectRadio == null ? false : mTestSelfProjectRadio.getSelection());
        }
        public boolean isTestingMain() {
            return mMainInfo != null;
        }
        public boolean isTestingExisting() {
            return mMainInfo == null &&
                (mTestExistingProjectRadio == null ? false
                                                   : mTestExistingProjectRadio.getSelection());
        }
        public IProject getExistingTestedProject() {
            return mExistingTestedProject;
        }
    }
    public TestInfo getTestInfo() {
        return mInfo;
    }
    public void setMainInfo(IMainInfo mainInfo) {
        mMainInfo = mainInfo;
    }
    public void createControl(Composite parent) {
        final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolledComposite.setFont(parent.getFont());
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        initializeDialogUnits(parent);
        final Composite composite = new Composite(scrolledComposite, SWT.NULL);
        composite.setFont(parent.getFont());
        scrolledComposite.setContent(composite);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createToggleTestProject(composite);
        createTestProjectGroup(composite);
        createLocationGroup(composite);
        createTestTargetGroup(composite);
        createTargetGroup(composite);
        createPropertiesGroup(composite);
        enableLocationWidgets();
        scrolledComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = scrolledComposite.getClientArea();
                scrolledComposite.setMinSize(composite.computeSize(r.width, SWT.DEFAULT));
            }
        });
        setErrorMessage(null);
        setMessage(null);
        setControl(scrolledComposite);
        validatePageComplete();
    }
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            mProjectNameField.setFocus();
            validatePageComplete();
            onCreateTestProjectToggle();
            onExistingProjectChanged();
        }
    }
    @Override
    public void dispose() {
        if (mSdkTargetChangeListener != null) {
            AdtPlugin.getDefault().removeTargetListener(mSdkTargetChangeListener);
            mSdkTargetChangeListener = null;
        }
        super.dispose();
    }
    private final void createToggleTestProject(Composite parent) {
        if (mMainInfo != null) {
            mCreateTestProjectField = new Button(parent, SWT.CHECK);
            mCreateTestProjectField.setText("Create a Test Project");
            mCreateTestProjectField.setToolTipText("Select this if you also want to create a Test Project.");
            mCreateTestProjectField.setSelection(INITIAL_CREATE_TEST_PROJECT);
            mCreateTestProjectField.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    onCreateTestProjectToggle();
                }
            });
        }
    }
    private final void createTestProjectGroup(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mToggleComposites.add(group);
        String tooltip = "Name of the Eclipse test project to create. It cannot be empty.";
        Label label = new Label(group, SWT.NONE);
        label.setText("Test Project Name:");
        label.setFont(parent.getFont());
        label.setToolTipText(tooltip);
        mProjectNameField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        mProjectNameField.setToolTipText(tooltip);
        mProjectNameField.setLayoutData(data);
        mProjectNameField.setFont(parent.getFont());
        mProjectNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                if (!mInternalProjectNameUpdate) {
                    mProjectNameModifiedByUser = true;
                }
                updateLocationPathField(null);
            }
        });
    }
    private final void createLocationGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setLayout(new GridLayout(3, 
                false ));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(parent.getFont());
        group.setText("Content");
        mToggleComposites.add(group);
        mUseDefaultLocation = new Button(group, SWT.CHECK);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        mUseDefaultLocation.setLayoutData(gd);
        mUseDefaultLocation.setText("Use default location");
        mUseDefaultLocation.setSelection(INITIAL_USE_DEFAULT_LOCATION);
        mUseDefaultLocation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                enableLocationWidgets();
                validatePageComplete();
            }
        });
        mLocationLabel = new Label(group, SWT.NONE);
        mLocationLabel.setText("Location:");
        mLocationPathField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL, 
                GridData.BEGINNING, 
                true,  
                false, 
                1,     
                1);    
        mLocationPathField.setLayoutData(data);
        mLocationPathField.setFont(parent.getFont());
        mLocationPathField.addListener(SWT.Modify, new Listener() {
           public void handleEvent(Event event) {
               onLocationPathFieldModified();
            }
        });
        mBrowseButton = new Button(group, SWT.PUSH);
        mBrowseButton.setText("Browse...");
        setButtonLayoutData(mBrowseButton);
        mBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onOpenDirectoryBrowser();
            }
        });
    }
    private final void createTestTargetGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(parent.getFont());
        group.setText("Test Target");
        mToggleComposites.add(group);
        if (mMainInfo == null) {
            Label label = new Label(group, SWT.NONE);
            label.setText("Select the project to test:");
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 3;
            label.setLayoutData(gd);
            mTestSelfProjectRadio = new Button(group, SWT.RADIO);
            mTestSelfProjectRadio.setText("This project");
            mTestSelfProjectRadio.setSelection(false);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 3;
            mTestSelfProjectRadio.setLayoutData(gd);
            mTestExistingProjectRadio = new Button(group, SWT.RADIO);
            mTestExistingProjectRadio.setText("An existing Android project");
            mTestExistingProjectRadio.setSelection(mMainInfo == null);
            mTestExistingProjectRadio.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    onExistingProjectChanged();
                }
            });
            String tooltip = "The existing Android Project that is being tested.";
            mTestedProjectNameField = new Text(group, SWT.BORDER);
            mTestedProjectNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mTestedProjectNameField.setToolTipText(tooltip);
            mTestedProjectNameField.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onProjectFieldUpdated();
                }
            });
            mProjectBrowseButton = new Button(group, SWT.NONE);
            mProjectBrowseButton.setText("Browse...");
            mProjectBrowseButton.setToolTipText("Allows you to select the Android project to test.");
            mProjectBrowseButton.addSelectionListener(new SelectionAdapter() {
               @Override
                public void widgetSelected(SelectionEvent e) {
                   onProjectBrowse();
                }
            });
            mProjectChooserHelper = new ProjectChooserHelper(parent.getShell(), null );
        } else {
        }
        Label label = new Label(group, SWT.NONE);
        label.setText("Test Target Package:");
        mTestTargetPackageLabel = new Label(group, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mTestTargetPackageLabel.setLayoutData(gd);
    }
    private void createTargetGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(parent.getFont());
        group.setText("Build Target");
        mToggleComposites.add(group);
        mSdkTargetSelector = new SdkTargetSelector(group, null);
        mSdkTargetChangeListener = new ITargetChangeListener() {
            public void onSdkLoaded() {
                IAndroidTarget[] targets = null;
                if (Sdk.getCurrent() != null) {
                    targets = Sdk.getCurrent().getTargets();
                }
                mSdkTargetSelector.setTargets(targets);
                if (targets != null && targets.length == 1) {
                    mSdkTargetSelector.setSelection(targets[0]);
                }
            }
            public void onProjectTargetChange(IProject changedProject) {
            }
            public void onTargetLoaded(IAndroidTarget target) {
            }
        };
        AdtPlugin.getDefault().addTargetListener(mSdkTargetChangeListener);
        mSdkTargetChangeListener.onSdkLoaded();
        mSdkTargetSelector.setSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onSdkTargetModified();
                updateLocationPathField(null);
                validatePageComplete();
            }
        });
    }
    private final void createPropertiesGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(parent.getFont());
        group.setText("Properties");
        mToggleComposites.add(group);
        Label label = new Label(group, SWT.NONE);
        label.setText("Application name:");
        label.setFont(parent.getFont());
        label.setToolTipText("Name of the Application. This is a free string. It can be empty.");
        mApplicationNameField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        mApplicationNameField.setToolTipText("Name of the Application. This is a free string. It can be empty.");
        mApplicationNameField.setLayoutData(data);
        mApplicationNameField.setFont(parent.getFont());
        mApplicationNameField.addListener(SWT.Modify, new Listener() {
           public void handleEvent(Event event) {
               if (!mInternalApplicationNameUpdate) {
                   mApplicationNameModifiedByUser = true;
               }
           }
        });
        label = new Label(group, SWT.NONE);
        label.setText("Package name:");
        label.setFont(parent.getFont());
        label.setToolTipText("Namespace of the Package to create. This must be a Java namespace with at least two components.");
        mPackageNameField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        mPackageNameField.setToolTipText("Namespace of the Package to create. This must be a Java namespace with at least two components.");
        mPackageNameField.setLayoutData(data);
        mPackageNameField.setFont(parent.getFont());
        mPackageNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                if (!mInternalPackageNameUpdate) {
                    mPackageNameModifiedByUser = true;
                }
                onPackageNameFieldModified();
            }
        });
        label = new Label(group, SWT.NONE);
        label.setText("Min SDK Version:");
        label.setFont(parent.getFont());
        label.setToolTipText("The minimum SDK version number that the application requires. Must be an integer > 0. It can be empty.");
        mMinSdkVersionField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        label.setToolTipText("The minimum SDK version number that the application requires. Must be an integer > 0. It can be empty.");
        mMinSdkVersionField.setLayoutData(data);
        mMinSdkVersionField.setFont(parent.getFont());
        mMinSdkVersionField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                onMinSdkVersionFieldModified();
                validatePageComplete();
            }
        });
    }
    private String getLocationPathFieldValue() {
        return mLocationPathField == null ? "" : mLocationPathField.getText().trim();  
    }
    private String getProjectLocation() {
        if (mInfo.useDefaultLocation()) {
            return Platform.getLocation().toString();
        } else {
            return getLocationPathFieldValue();
        }
    }
    private IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(mInfo.getProjectName());
    }
    private void onExistingProjectChanged() {
        if (mInfo.isTestingExisting()) {
            boolean enabled = mTestExistingProjectRadio.getSelection();
            mTestedProjectNameField.setEnabled(enabled);
            mProjectBrowseButton.setEnabled(enabled);
            setExistingProject(mInfo.getExistingTestedProject());
            validatePageComplete();
        }
    }
    private void useMainProjectInformation() {
        if (mInfo.isTestingMain() && mMainInfo != null) {
            String projName = String.format("%1$sTest", mMainInfo.getProjectName());
            String appName = String.format("%1$sTest", mMainInfo.getApplicationName());
            String packageName = mMainInfo.getPackageName();
            if (packageName == null) {
                packageName = "";  
            }
            updateTestTargetPackageField(packageName);
            if (!mProjectNameModifiedByUser) {
                mInternalProjectNameUpdate = true;
                mProjectNameField.setText(projName);  
                mInternalProjectNameUpdate = false;
            }
            if (!mApplicationNameModifiedByUser) {
                mInternalApplicationNameUpdate = true;
                mApplicationNameField.setText(appName);
                mInternalApplicationNameUpdate = false;
            }
            if (!mPackageNameModifiedByUser) {
                mInternalPackageNameUpdate = true;
                packageName += ".test";  
                mPackageNameField.setText(packageName);
                mInternalPackageNameUpdate = false;
            }
            if (!mSdkTargetModifiedByUser) {
                mInternalSdkTargetUpdate = true;
                mSdkTargetSelector.setSelection(mMainInfo.getSdkTarget());
                mInternalSdkTargetUpdate = false;
            }
            if (!mMinSdkVersionModifiedByUser) {
                mInternalMinSdkVersionUpdate = true;
                mMinSdkVersionField.setText(mMainInfo.getMinSdkVersion());
                mInternalMinSdkVersionUpdate = false;
            }
        }
    }
    private void onProjectFieldUpdated() {
        String project = mTestedProjectNameField.getText();
        IJavaProject[] projects = mProjectChooserHelper.getAndroidProjects(null );
        for (IJavaProject p : projects) {
            if (p.getProject().getName().equals(project)) {
                setExistingProject(p.getProject());
                return;
            }
        }
    }
    private void onProjectBrowse() {
        IJavaProject p = mProjectChooserHelper.chooseJavaProject(mTestedProjectNameField.getText(),
                null );
        if (p != null) {
            setExistingProject(p.getProject());
            mTestedProjectNameField.setText(mExistingTestedProject.getName());
        }
    }
    private void setExistingProject(IProject project) {
        mExistingTestedProject = project;
        if (project != null &&
                (!mApplicationNameModifiedByUser ||
                 !mPackageNameModifiedByUser     ||
                 !mSdkTargetModifiedByUser       ||
                 !mMinSdkVersionModifiedByUser)) {
            IFile file = AndroidManifestParser.getManifest(project);
            AndroidManifestParser manifestData = null;
            if (file != null) {
                try {
                    manifestData = AndroidManifestParser.parseForData(file);
                } catch (CoreException e) {
                }
            }
            if (manifestData != null) {
                String appName = String.format("%1$sTest", project.getName());
                String packageName = manifestData.getPackage();
                String minSdkVersion = manifestData.getApiLevelRequirement();
                IAndroidTarget sdkTarget = null;
                if (Sdk.getCurrent() != null) {
                    sdkTarget = Sdk.getCurrent().getTarget(project);
                }
                if (packageName == null) {
                    packageName = "";  
                }
                mLastExistingPackageName = packageName;
                if (!mProjectNameModifiedByUser) {
                    mInternalProjectNameUpdate = true;
                    mProjectNameField.setText(appName);
                    mInternalProjectNameUpdate = false;
                }
                if (!mApplicationNameModifiedByUser) {
                    mInternalApplicationNameUpdate = true;
                    mApplicationNameField.setText(appName);
                    mInternalApplicationNameUpdate = false;
                }
                if (!mPackageNameModifiedByUser) {
                    mInternalPackageNameUpdate = true;
                    packageName += ".test";  
                    mPackageNameField.setText(packageName);  
                    mInternalPackageNameUpdate = false;
                }
                if (!mSdkTargetModifiedByUser && sdkTarget != null) {
                    mInternalSdkTargetUpdate = true;
                    mSdkTargetSelector.setSelection(sdkTarget);
                    mInternalSdkTargetUpdate = false;
                }
                if (!mMinSdkVersionModifiedByUser) {
                    mInternalMinSdkVersionUpdate = true;
                    if (minSdkVersion != null) {
                        mMinSdkVersionField.setText(minSdkVersion);
                    }
                    if (sdkTarget == null) {
                        updateSdkSelectorToMatchMinSdkVersion();
                    }
                    mInternalMinSdkVersionUpdate = false;
                }
            }
        }
        updateTestTargetPackageField(mLastExistingPackageName);
        validatePageComplete();
    }
    private void onOpenDirectoryBrowser() {
        String existing_dir = getLocationPathFieldValue();
        if (existing_dir.length() == 0) {
            existing_dir = null;
        } else {
            File f = new File(existing_dir);
            if (!f.exists()) {
                existing_dir = null;
            }
        }
        DirectoryDialog dd = new DirectoryDialog(mLocationPathField.getShell());
        dd.setMessage("Browse for folder");
        dd.setFilterPath(existing_dir);
        String abs_dir = dd.open();
        if (abs_dir != null) {
            updateLocationPathField(abs_dir);
            validatePageComplete();
        }
    }
    private void onCreateTestProjectToggle() {
        boolean enabled = mInfo.getCreateTestProject();
        for (Composite c : mToggleComposites) {
            enableControl(c, enabled);
        }
        mSdkTargetSelector.setEnabled(enabled);
        if (enabled) {
            useMainProjectInformation();
        }
        validatePageComplete();
    }
    private void enableControl(Control c, boolean enabled) {
        c.setEnabled(enabled);
        if (c instanceof Composite)
        for (Control c2 : ((Composite) c).getChildren()) {
            enableControl(c2, enabled);
        }
    }
    private void enableLocationWidgets() {
        boolean use_default = mInfo.useDefaultLocation();
        boolean location_enabled = !use_default;
        mLocationLabel.setEnabled(location_enabled);
        mLocationPathField.setEnabled(location_enabled);
        mBrowseButton.setEnabled(location_enabled);
        updateLocationPathField(null);
    }
    private void updateLocationPathField(String abs_dir) {
        boolean use_default = mInfo.useDefaultLocation();
        boolean custom_location = !use_default;
        if (!mInternalLocationPathUpdate) {
            mInternalLocationPathUpdate = true;
            if (custom_location) {
                if (abs_dir != null) {
                    sCustomLocationOsPath = TextProcessor.process(abs_dir);
                }
                if (!mLocationPathField.getText().equals(sCustomLocationOsPath)) {
                    mLocationPathField.setText(sCustomLocationOsPath);
                }
            } else {
                String value = Platform.getLocation().append(mInfo.getProjectName()).toString();
                value = TextProcessor.process(value);
                if (!mLocationPathField.getText().equals(value)) {
                    mLocationPathField.setText(value);
                }
            }
            validatePageComplete();
            mInternalLocationPathUpdate = false;
        }
    }
    private void onLocationPathFieldModified() {
        if (!mInternalLocationPathUpdate) {
            String newPath = getLocationPathFieldValue();
            sCustomLocationOsPath = newPath;
            validatePageComplete();
        }
    }
    private void onPackageNameFieldModified() {
        updateTestTargetPackageField(null);
        validatePageComplete();
    }
    private void updateTestTargetPackageField(String packageName) {
        if (mInfo.isTestingSelf()) {
            mTestTargetPackageLabel.setText(mInfo.getPackageName());
        } else if (packageName != null) {
            mTestTargetPackageLabel.setText(packageName);
        }
    }
    private void onMinSdkVersionFieldModified() {
        if (mInternalMinSdkVersionUpdate || mInternalSdkTargetUpdate) {
            return;
        }
        updateSdkSelectorToMatchMinSdkVersion();
        mMinSdkVersionModifiedByUser = true;
    }
    private void updateSdkSelectorToMatchMinSdkVersion() {
        String minSdkVersion = mInfo.getMinSdkVersion();
        IAndroidTarget curr_target = mInfo.getSdkTarget();
        if (curr_target != null && curr_target.getVersion().equals(minSdkVersion)) {
            return;
        }
        for (IAndroidTarget target : mSdkTargetSelector.getTargets()) {
            if (target.getVersion().equals(minSdkVersion)) {
                mSdkTargetSelector.setSelection(target);
                return;
            }
        }
    }
    private void onSdkTargetModified() {
        if (mInternalMinSdkVersionUpdate || mInternalSdkTargetUpdate) {
            return;
        }
        IAndroidTarget target = mInfo.getSdkTarget();
        if (target != null) {
            mInternalMinSdkVersionUpdate = true;
            mMinSdkVersionField.setText(target.getVersion().getApiString());
            mInternalMinSdkVersionUpdate = false;
        }
        mSdkTargetModifiedByUser = true;
    }
    private boolean validatePage() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        int status = MSG_NONE;
        if (mInfo.getCreateTestProject()) {
            status = validateProjectField(workspace);
            if ((status & MSG_ERROR) == 0) {
                status |= validateLocationPath(workspace);
            }
            if ((status & MSG_ERROR) == 0) {
                status |= validateTestTarget();
            }
            if ((status & MSG_ERROR) == 0) {
                status |= validateSdkTarget();
            }
            if ((status & MSG_ERROR) == 0) {
                status |= validatePackageField();
            }
            if ((status & MSG_ERROR) == 0) {
                status |= validateMinSdkVersionField();
            }
        }
        if (status == MSG_NONE)  {
            setStatus(null, MSG_NONE);
        }
        return (status & MSG_ERROR) == 0;
    }
    private void validatePageComplete() {
        setPageComplete(validatePage());
    }
    private int validateTestTarget() {
        if (mInfo.isTestingExisting() && mInfo.getExistingTestedProject() == null) {
            return setStatus("Please select an existing Android project as a test target.",
                    MSG_ERROR);
        }
        return MSG_NONE;
    }
    private int validateProjectField(IWorkspace workspace) {
        String projectName = mInfo.getProjectName();
        if (projectName.length() == 0) {
            return setStatus("Project name must be specified", MSG_ERROR);
        }
        if (!sProjectNamePattern.matcher(projectName).matches()) {
            return setStatus("The project name must start with an alphanumeric characters, followed by one or more alphanumerics, digits, dots, dashes, underscores or spaces.",
                    MSG_ERROR);
        }
        IStatus nameStatus = workspace.validateName(projectName, IResource.PROJECT);
        if (!nameStatus.isOK()) {
            return setStatus(nameStatus.getMessage(), MSG_ERROR);
        }
        if (mMainInfo != null && projectName.equals(mMainInfo.getProjectName())) {
            return setStatus("The main project name and the test project name must be different.",
                    MSG_ERROR);
        }
        if (getProjectHandle().exists()) {
            return setStatus("A project with that name already exists in the workspace",
                    MSG_ERROR);
        }
        return MSG_NONE;
    }
    private int validateLocationPath(IWorkspace workspace) {
        Path path = new Path(getProjectLocation());
        if (!mInfo.useDefaultLocation()) {
            URI uri = URIUtil.toURI(path.toOSString());
            IStatus locationStatus = workspace.validateProjectLocationURI(getProjectHandle(),
                    uri);
            if (!locationStatus.isOK()) {
                return setStatus(locationStatus.getMessage(), MSG_ERROR);
            } else {
                File f = path.toFile();
                if (f.exists() && !f.isDirectory()) {
                    return setStatus("A directory name must be specified.", MSG_ERROR);
                } else if (f.isDirectory()) {
                    String[] l = f.list();
                    if (l.length != 0) {
                        return setStatus("The selected output directory is not empty.",
                                MSG_WARNING);
                    }
                }
            }
        } else {
            if (getProjectLocation().length() == 0) {
                return setStatus("A directory name must be specified.", MSG_ERROR);
            }
            File dest = path.append(mInfo.getProjectName()).toFile();
            if (dest.exists()) {
                return setStatus(String.format("There is already a file or directory named \"%1$s\" in the selected location.",
                        mInfo.getProjectName()), MSG_ERROR);
            }
        }
        return MSG_NONE;
    }
    private int validateSdkTarget() {
        if (mInfo.getSdkTarget() == null) {
            return setStatus("An SDK Target must be specified.", MSG_ERROR);
        }
        return MSG_NONE;
    }
    private int validateMinSdkVersionField() {
        if (mInfo.getMinSdkVersion().length() == 0) {
            return MSG_NONE;
        }
        if (mInfo.getSdkTarget() != null &&
                mInfo.getSdkTarget().getVersion().equals(mInfo.getMinSdkVersion()) == false) {
            return setStatus("The API level for the selected SDK target does not match the Min SDK version.",
                    mInfo.getSdkTarget().getVersion().isPreview() ? MSG_ERROR : MSG_WARNING);
        }
        return MSG_NONE;
    }
    private int validatePackageField() {
        String packageName = mInfo.getPackageName();
        if (packageName.length() == 0) {
            return setStatus("Project package name must be specified.", MSG_ERROR);
        }
        int result = MSG_NONE;
        IStatus status = JavaConventions.validatePackageName(packageName, "1.5", "1.5"); 
        if (!status.isOK()) {
            result = setStatus(String.format("Project package: %s", status.getMessage()),
                        status.getSeverity() == IStatus.ERROR ? MSG_ERROR : MSG_WARNING);
        }
        if (result != MSG_ERROR && packageName.indexOf('.') == -1) {
            return setStatus("Project package name must have at least two identifiers.", MSG_ERROR);
        }
        packageName = mInfo.getTargetPackageName();
        if (packageName.length() == 0) {
            return setStatus("Target package name must be specified.", MSG_ERROR);
        }
        status = JavaConventions.validatePackageName(packageName, "1.5", "1.5"); 
        if (!status.isOK()) {
            result = setStatus(String.format("Target package: %s", status.getMessage()),
                        status.getSeverity() == IStatus.ERROR ? MSG_ERROR : MSG_WARNING);
        }
        if (result != MSG_ERROR && packageName.indexOf('.') == -1) {
            return setStatus("Target name must have at least two identifiers.", MSG_ERROR);
        }
        return result;
    }
    private int setStatus(String message, int messageType) {
        if (message == null) {
            setErrorMessage(null);
            setMessage(null);
        } else if (!message.equals(getMessage())) {
            setMessage(message, messageType == MSG_WARNING ? WizardPage.WARNING : WizardPage.ERROR);
        }
        return messageType;
    }
}
