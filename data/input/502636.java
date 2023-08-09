public class NewProjectCreationPage extends WizardPage {
    private static final String MAIN_PAGE_NAME = "newAndroidProjectPage"; 
    private static final String INITIAL_NAME = "";  
    private static final boolean INITIAL_CREATE_NEW_PROJECT = true;
    private static final boolean INITIAL_CREATE_FROM_SAMPLE = false;
    private static final boolean INITIAL_CREATE_FROM_SOURCE = false;
    private static final boolean INITIAL_USE_DEFAULT_LOCATION = true;
    private static final boolean INITIAL_CREATE_ACTIVITY = true;
    private static final Pattern sProjectNamePattern = Pattern.compile("^[\\w][\\w. -]*$");  
    private static String sCustomLocationOsPath = "";  
    private static boolean sAutoComputeCustomLocation = true;
    private final int MSG_NONE = 0;
    private final int MSG_WARNING = 1;
    private final int MSG_ERROR = 2;
    private final MainInfo mInfo = new MainInfo();
    private TestInfo mTestInfo;
    private String mUserPackageName = "";       
    private String mUserActivityName = "";      
    private boolean mUserCreateActivityCheck = INITIAL_CREATE_ACTIVITY;
    private String mSourceFolder = "";          
    private Text mProjectNameField;
    private Text mPackageNameField;
    private Text mActivityNameField;
    private Text mApplicationNameField;
    private Button mCreateNewProjectRadio;
    private Button mCreateFromSampleRadio;
    private Button mUseDefaultLocation;
    private Label mLocationLabel;
    private Text mLocationPathField;
    private Button mBrowseButton;
    private Button mCreateActivityCheck;
    private Text mMinSdkVersionField;
    private SdkTargetSelector mSdkTargetSelector;
    private ITargetChangeListener mSdkTargetChangeListener;
    private boolean mInternalLocationPathUpdate;
    private boolean mInternalProjectNameUpdate;
    private boolean mInternalApplicationNameUpdate;
    private boolean mInternalCreateActivityUpdate;
    private boolean mInternalActivityNameUpdate;
    private boolean mProjectNameModifiedByUser;
    private boolean mApplicationNameModifiedByUser;
    private final ArrayList<String> mSamplesPaths = new ArrayList<String>();
    private Combo mSamplesCombo;
    public NewProjectCreationPage() {
        super(MAIN_PAGE_NAME);
        setPageComplete(false);
        setTitle("New Android Project");
        setDescription("Creates a new Android Project resource.");
    }
    public interface IMainInfo {
        public IPath getLocationPath();
        public String getProjectName();
        public String getPackageName();
        public String getActivityName();
        public String getMinSdkVersion();
        public String getApplicationName();
        public boolean isNewProject();
        public boolean isCreateActivity();
        public boolean useDefaultLocation();
        public String getSourceFolder();
        public IAndroidTarget getSdkTarget();
    }
    public class MainInfo implements IMainInfo {
        public IPath getLocationPath() {
            return new Path(getProjectLocation());
        }
        public String getProjectName() {
            return mProjectNameField == null ? INITIAL_NAME : mProjectNameField.getText().trim();
        }
        public String getPackageName() {
            return mPackageNameField == null ? INITIAL_NAME : mPackageNameField.getText().trim();
        }
        public String getActivityName() {
            return mActivityNameField == null ? INITIAL_NAME : mActivityNameField.getText().trim();
        }
        public String getMinSdkVersion() {
            return mMinSdkVersionField == null ? "" : mMinSdkVersionField.getText().trim();  
        }
        public String getApplicationName() {
            return mApplicationNameField == null ? getActivityName()
                                                 : mApplicationNameField.getText().trim();
        }
        public boolean isNewProject() {
            return mCreateNewProjectRadio == null ? INITIAL_CREATE_NEW_PROJECT
                                                  : mCreateNewProjectRadio.getSelection();
        }
        public boolean isCreateFromSample() {
            return mCreateFromSampleRadio == null ? INITIAL_CREATE_FROM_SAMPLE
                                                  : mCreateFromSampleRadio.getSelection();
        }
        public boolean isCreateActivity() {
            return mCreateActivityCheck == null ? INITIAL_CREATE_ACTIVITY
                                                  : mCreateActivityCheck.getSelection();
        }
        public boolean useDefaultLocation() {
            return mUseDefaultLocation == null ? INITIAL_USE_DEFAULT_LOCATION
                                               : mUseDefaultLocation.getSelection();
        }
        public String getSourceFolder() {
            if (isNewProject() || mSourceFolder == null || mSourceFolder.length() == 0) {
                return SdkConstants.FD_SOURCES;
            } else {
                return mSourceFolder;
            }
        }
        public IAndroidTarget getSdkTarget() {
            return mSdkTargetSelector == null ? null : mSdkTargetSelector.getSelected();
        }
    }
    public IMainInfo getMainInfo() {
        return mInfo;
    }
    public void setTestInfo(TestInfo testInfo) {
        mTestInfo = testInfo;
    }
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            mProjectNameField.setFocus();
            validatePageComplete();
        }
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
        createProjectNameGroup(composite);
        createLocationGroup(composite);
        createTargetGroup(composite);
        createPropertiesGroup(composite);
        enableLocationWidgets();
        loadSamplesForTarget(null );
        mSdkTargetChangeListener.onSdkLoaded();
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
    public void dispose() {
        if (mSdkTargetChangeListener != null) {
            AdtPlugin.getDefault().removeTargetListener(mSdkTargetChangeListener);
            mSdkTargetChangeListener = null;
        }
        super.dispose();
    }
    private final void createProjectNameGroup(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label = new Label(group, SWT.NONE);
        label.setText("Project name:");
        label.setFont(parent.getFont());
        label.setToolTipText("Name of the Eclipse project to create. It cannot be empty.");
        mProjectNameField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        mProjectNameField.setToolTipText("Name of the Eclipse project to create. It cannot be empty.");
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
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(parent.getFont());
        group.setText("Contents");
        mCreateNewProjectRadio = new Button(group, SWT.RADIO);
        mCreateNewProjectRadio.setText("Create new project in workspace");
        mCreateNewProjectRadio.setSelection(INITIAL_CREATE_NEW_PROJECT);
        Button existing_project_radio = new Button(group, SWT.RADIO);
        existing_project_radio.setText("Create project from existing source");
        existing_project_radio.setSelection(INITIAL_CREATE_FROM_SOURCE);
        mUseDefaultLocation = new Button(group, SWT.CHECK);
        mUseDefaultLocation.setText("Use default location");
        mUseDefaultLocation.setSelection(INITIAL_USE_DEFAULT_LOCATION);
        SelectionListener location_listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                enableLocationWidgets();
                extractNamesFromAndroidManifest();
                validatePageComplete();
            }
        };
        mCreateNewProjectRadio.addSelectionListener(location_listener);
        existing_project_radio.addSelectionListener(location_listener);
        mUseDefaultLocation.addSelectionListener(location_listener);
        Composite location_group = new Composite(group, SWT.NONE);
        location_group.setLayout(new GridLayout(3, 
                false ));
        location_group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        location_group.setFont(parent.getFont());
        mLocationLabel = new Label(location_group, SWT.NONE);
        mLocationLabel.setText("Location:");
        mLocationPathField = new Text(location_group, SWT.BORDER);
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
        mBrowseButton = new Button(location_group, SWT.PUSH);
        mBrowseButton.setText("Browse...");
        setButtonLayoutData(mBrowseButton);
        mBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onOpenDirectoryBrowser();
            }
        });
        mCreateFromSampleRadio = new Button(group, SWT.RADIO);
        mCreateFromSampleRadio.setText("Create project from existing sample");
        mCreateFromSampleRadio.setSelection(INITIAL_CREATE_FROM_SAMPLE);
        mCreateFromSampleRadio.addSelectionListener(location_listener);
        Composite samples_group = new Composite(group, SWT.NONE);
        samples_group.setLayout(new GridLayout(2, 
                false ));
        samples_group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        samples_group.setFont(parent.getFont());
        new Label(samples_group, SWT.NONE).setText("Samples:");
        mSamplesCombo = new Combo(samples_group, SWT.DROP_DOWN | SWT.READ_ONLY);
        mSamplesCombo.setEnabled(false);
        mSamplesCombo.select(0);
        mSamplesCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSamplesCombo.setToolTipText("Select a sample");
        mSamplesCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onSampleSelected();
            }
        });
    }
    private void createTargetGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(parent.getFont());
        group.setText("Build Target");
        mSdkTargetSelector = new SdkTargetSelector(group, null);
        mSdkTargetSelector.setSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onSdkTargetModified();
                updateLocationPathField(null);
                validatePageComplete();
            }
        });
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
    }
    private final void createPropertiesGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(parent.getFont());
        group.setText("Properties");
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
                onPackageNameFieldModified();
            }
        });
        mCreateActivityCheck = new Button(group, SWT.CHECK);
        mCreateActivityCheck.setText("Create Activity:");
        mCreateActivityCheck.setToolTipText("Specifies if you want to create a default Activity.");
        mCreateActivityCheck.setFont(parent.getFont());
        mCreateActivityCheck.setSelection(INITIAL_CREATE_ACTIVITY);
        mCreateActivityCheck.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                onCreateActivityCheckModified();
                enableLocationWidgets();
            }
        });
        mActivityNameField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        mActivityNameField.setToolTipText("Name of the Activity class to create. Must be a valid Java identifier.");
        mActivityNameField.setLayoutData(data);
        mActivityNameField.setFont(parent.getFont());
        mActivityNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                onActivityNameFieldModified();
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
                validatePageComplete();
            }
        });
    }
    private String getLocationPathFieldValue() {
        return mLocationPathField == null ? "" : mLocationPathField.getText().trim();  
    }
    private String getSelectedSamplePath() {
        int selIndex = mSamplesCombo.getSelectionIndex();
        if (selIndex >= 0 && selIndex < mSamplesPaths.size()) {
            return mSamplesPaths.get(selIndex);
        }
        return "";
    }
    private String getProjectLocation() {
        if (mInfo.isCreateFromSample()) {
            return getSelectedSamplePath();
        } else if (mInfo.isNewProject() && mInfo.useDefaultLocation()) {
            return Platform.getLocation().toString();
        } else {
            return getLocationPathFieldValue();
        }
    }
    private IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(mInfo.getProjectName());
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
            extractNamesFromAndroidManifest();
            validatePageComplete();
        }
    }
    private void onSampleSelected() {
        if (mInfo.isCreateFromSample()) {
            extractNamesFromAndroidManifest();
            validatePageComplete();
        }
    }
    private void enableLocationWidgets() {
        boolean is_new_project = mInfo.isNewProject();
        boolean is_create_from_sample = mInfo.isCreateFromSample();
        boolean use_default = mInfo.useDefaultLocation() && !is_create_from_sample;
        boolean location_enabled = (!is_new_project || !use_default) && !is_create_from_sample;
        boolean create_activity = mInfo.isCreateActivity();
        mUseDefaultLocation.setEnabled(is_new_project);
        mLocationLabel.setEnabled(location_enabled);
        mLocationPathField.setEnabled(location_enabled);
        mBrowseButton.setEnabled(location_enabled);
        mSamplesCombo.setEnabled(is_create_from_sample && mSamplesPaths.size() > 0);
        mApplicationNameField.setEnabled(is_new_project);
        mMinSdkVersionField.setEnabled(is_new_project);
        mPackageNameField.setEnabled(is_new_project);
        mCreateActivityCheck.setEnabled(is_new_project);
        mActivityNameField.setEnabled(is_new_project & create_activity);
        updateLocationPathField(null);
        updatePackageAndActivityFields();
    }
    private void updateLocationPathField(String abs_dir) {
        if (mInfo.isCreateFromSample()) {
            return;
        }
        boolean is_new_project = mInfo.isNewProject();
        boolean use_default = mInfo.useDefaultLocation();
        boolean custom_location = !is_new_project || !use_default;
        if (!mInternalLocationPathUpdate) {
            mInternalLocationPathUpdate = true;
            if (custom_location) {
                if (abs_dir != null) {
                    sAutoComputeCustomLocation = sAutoComputeCustomLocation &&
                                                 abs_dir.equals(sCustomLocationOsPath);
                    sCustomLocationOsPath = TextProcessor.process(abs_dir);
                } else  if (sAutoComputeCustomLocation ||
                            (!is_new_project && !new File(sCustomLocationOsPath).isDirectory())) {
                    IAndroidTarget target = mInfo.getSdkTarget();
                    if (target != null) {
                        sCustomLocationOsPath = target.getPath(IAndroidTarget.SAMPLES);
                    }
                    if (sCustomLocationOsPath == null || sCustomLocationOsPath.length() == 0) {
                        if (Sdk.getCurrent() != null) {
                            sCustomLocationOsPath = Sdk.getCurrent().getSdkLocation();
                        } else {
                            sCustomLocationOsPath = File.listRoots()[0].getAbsolutePath();
                        }
                    }
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
            sAutoComputeCustomLocation = sAutoComputeCustomLocation &&
                                         newPath.equals(sCustomLocationOsPath);
            sCustomLocationOsPath = newPath;
            extractNamesFromAndroidManifest();
            validatePageComplete();
        }
    }
    private void onPackageNameFieldModified() {
        if (mInfo.isNewProject()) {
            mUserPackageName = mInfo.getPackageName();
            validatePageComplete();
        }
    }
    private void onCreateActivityCheckModified() {
        if (mInfo.isNewProject() && !mInternalCreateActivityUpdate) {
            mUserCreateActivityCheck = mInfo.isCreateActivity();
        }
        validatePageComplete();
    }
    private void onActivityNameFieldModified() {
        if (mInfo.isNewProject() && !mInternalActivityNameUpdate) {
            mUserActivityName = mInfo.getActivityName();
            validatePageComplete();
        }
    }
    private void onSdkTargetModified() {
        IAndroidTarget target = mInfo.getSdkTarget();
        loadSamplesForTarget(target);
        enableLocationWidgets();
        onSampleSelected();
    }
    private void updatePackageAndActivityFields() {
        if (mInfo.isNewProject()) {
            if (mUserPackageName.length() > 0 &&
                    !mPackageNameField.getText().equals(mUserPackageName)) {
                mPackageNameField.setText(mUserPackageName);
            }
            if (mUserActivityName.length() > 0 &&
                    !mActivityNameField.getText().equals(mUserActivityName)) {
                mInternalActivityNameUpdate = true;
                mActivityNameField.setText(mUserActivityName);
                mInternalActivityNameUpdate = false;
            }
            if (mUserCreateActivityCheck != mCreateActivityCheck.getSelection()) {
                mInternalCreateActivityUpdate = true;
                mCreateActivityCheck.setSelection(mUserCreateActivityCheck);
                mInternalCreateActivityUpdate = false;
            }
        }
    }
    private void extractNamesFromAndroidManifest() {
        if (mInfo.isNewProject()) {
            return;
        }
        String projectLocation = getProjectLocation();
        File f = new File(projectLocation);
        if (!f.isDirectory()) {
            return;
        }
        Path path = new Path(f.getPath());
        String osPath = path.append(AndroidConstants.FN_ANDROID_MANIFEST).toOSString();
        AndroidManifestParser manifestData = AndroidManifestParser.parseForData(osPath);
        if (manifestData == null) {
            return;
        }
        String packageName = null;
        Activity activity = null;
        String activityName = null;
        String minSdkVersion = null;
        try {
            packageName = manifestData.getPackage();
            minSdkVersion = manifestData.getApiLevelRequirement();
            activity = manifestData.getLauncherActivity();
            if (activity == null) {
                Activity[] activities = manifestData.getActivities();
                if (activities != null && activities.length > 0) {
                    activity = activities[0];
                }
            }
        } catch (Exception e) {
        }
        if (packageName != null && packageName.length() > 0) {
            mPackageNameField.setText(packageName);
        }
        if (activity != null) {
            activityName = AndroidManifest.extractActivityName(activity.getName(), packageName);
        }
        if (activityName != null && activityName.length() > 0) {
            mInternalActivityNameUpdate = true;
            mInternalCreateActivityUpdate = true;
            mActivityNameField.setText(activityName);
            mCreateActivityCheck.setSelection(false);
            mInternalCreateActivityUpdate = false;
            mInternalActivityNameUpdate = false;
            if (activityName.indexOf('.') != -1) {
                String[] ids = activityName.split(AndroidConstants.RE_DOT);
                activityName = ids[ids.length - 1];
            }
            if (mProjectNameField.getText().length() == 0 || !mProjectNameModifiedByUser) {
                mInternalProjectNameUpdate = true;
                mProjectNameModifiedByUser = false;
                mProjectNameField.setText(activityName);
                mInternalProjectNameUpdate = false;
            }
            if (mApplicationNameField.getText().length() == 0 || !mApplicationNameModifiedByUser) {
                mInternalApplicationNameUpdate = true;
                mApplicationNameModifiedByUser = false;
                mApplicationNameField.setText(activityName);
                mInternalApplicationNameUpdate = false;
            }
        } else {
            mInternalActivityNameUpdate = true;
            mInternalCreateActivityUpdate = true;
            mActivityNameField.setText("");  
            mCreateActivityCheck.setSelection(false);
            mInternalCreateActivityUpdate = false;
            mInternalActivityNameUpdate = false;
            if (packageName != null && packageName.length() > 0) {
                if (mApplicationNameField.getText().length() == 0 ||
                        !mApplicationNameModifiedByUser) {
                    mInternalApplicationNameUpdate = true;
                    mApplicationNameField.setText(packageName);
                    mInternalApplicationNameUpdate = false;
                }
                packageName = packageName.replace('.', '_');
                if (mProjectNameField.getText().length() == 0 || !mProjectNameModifiedByUser) {
                    mInternalProjectNameUpdate = true;
                    mProjectNameField.setText(packageName);
                    mInternalProjectNameUpdate = false;
                }
            }
        }
        IAndroidTarget foundTarget = null;
        IAndroidTarget currentTarget = mInfo.getSdkTarget();
        if (currentTarget == null || !mInfo.isCreateFromSample()) {
            ProjectProperties p = ProjectProperties.create(projectLocation, null);
            if (p != null) {
                p.merge(PropertyType.BUILD).merge(PropertyType.DEFAULT);
                String v = p.getProperty(ProjectProperties.PROPERTY_TARGET);
                IAndroidTarget desiredTarget = Sdk.getCurrent().getTargetFromHashString(v);
                if (desiredTarget != null &&
                        (currentTarget == null || !desiredTarget.canRunOn(currentTarget))) {
                    foundTarget = desiredTarget;
                }
            }
            if (foundTarget == null && minSdkVersion != null) {
                for (IAndroidTarget existingTarget : mSdkTargetSelector.getTargets()) {
                    if (existingTarget != null &&
                            existingTarget.getVersion().equals(minSdkVersion)) {
                        foundTarget = existingTarget;
                        break;
                    }
                }
            }
            if (foundTarget == null) {
                for (IAndroidTarget existingTarget : mSdkTargetSelector.getTargets()) {
                    if (existingTarget != null &&
                            projectLocation.startsWith(existingTarget.getLocation())) {
                        foundTarget = existingTarget;
                        break;
                    }
                }
            }
        }
        if (foundTarget != null) {
            mSdkTargetSelector.setSelection(foundTarget);
        }
        mMinSdkVersionField.setText(minSdkVersion == null ? "" : minSdkVersion);  
    }
    private void loadSamplesForTarget(IAndroidTarget target) {
        String oldChoice = null;
        if (mSamplesPaths.size() > 0) {
            int selIndex = mSamplesCombo.getSelectionIndex();
            if (selIndex > -1) {
                oldChoice = mSamplesCombo.getItem(selIndex);
            }
        }
        mSamplesCombo.removeAll();
        mSamplesPaths.clear();
        if (target != null) {
            String samplesRootPath = target.getPath(IAndroidTarget.SAMPLES);
            File samplesDir = new File(samplesRootPath);
            findSamplesManifests(samplesDir, mSamplesPaths);
            if (mSamplesPaths.size() == 0) {
                mSamplesCombo.add("This target has no samples. Please select another target.");
                mSamplesCombo.select(0);
                return;
            }
            int selIndex = 0;
            int i = 0;
            int n = samplesRootPath.length();
            for (String path : mSamplesPaths) {
                if (path.length() > n) {
                    path = path.substring(n);
                    if (path.charAt(0) == File.separatorChar) {
                        path = path.substring(1);
                    }
                    if (path.endsWith(File.separator)) {
                        path = path.substring(0, path.length() - 1);
                    }
                    path = path.replaceAll(Pattern.quote(File.separator), " > ");
                }
                if (oldChoice != null && oldChoice.equals(path)) {
                    selIndex = i;
                }
                mSamplesCombo.add(path);
                i++;
            }
            mSamplesCombo.select(selIndex);
        } else {
            mSamplesCombo.add("Please select a target.");
            mSamplesCombo.select(0);
        }
    }
    private void findSamplesManifests(File samplesDir, ArrayList<String> samplesPaths) {
        if (!samplesDir.isDirectory()) {
            return;
        }
        for (File f : samplesDir.listFiles()) {
            if (f.isDirectory()) {
                File manifestFile = new File(f, SdkConstants.FN_ANDROID_MANIFEST_XML);
                if (manifestFile.isFile()) {
                    samplesPaths.add(f.getPath());
                }
                String leaf = f.getName();
                if (!SdkConstants.FD_SOURCES.equals(leaf) &&
                        !SdkConstants.FD_ASSETS.equals(leaf) &&
                        !SdkConstants.FD_RES.equals(leaf)) {
                    findSamplesManifests(f, samplesPaths);
                }
            }
        }
    }
    private boolean validatePage() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        int status = validateProjectField(workspace);
        if ((status & MSG_ERROR) == 0) {
            status |= validateSdkTarget();
        }
        if ((status & MSG_ERROR) == 0) {
            status |= validateLocationPath(workspace);
        }
        if ((status & MSG_ERROR) == 0) {
            status |= validatePackageField();
        }
        if ((status & MSG_ERROR) == 0) {
            status |= validateActivityField();
        }
        if ((status & MSG_ERROR) == 0) {
            status |= validateMinSdkVersionField();
        }
        if ((status & MSG_ERROR) == 0) {
            status |= validateSourceFolder();
        }
        if (status == MSG_NONE)  {
            setStatus(null, MSG_NONE);
        }
        return (status & MSG_ERROR) == 0;
    }
    private void validatePageComplete() {
        setPageComplete(validatePage());
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
        if (getProjectHandle().exists()) {
            return setStatus("A project with that name already exists in the workspace",
                    MSG_ERROR);
        }
        if (mTestInfo != null &&
                mTestInfo.getCreateTestProject() &&
                projectName.equals(mTestInfo.getProjectName())) {
            return setStatus("The main project name and the test project name must be different.",
                    MSG_WARNING);
        }
        return MSG_NONE;
    }
    private int validateLocationPath(IWorkspace workspace) {
        Path path = new Path(getProjectLocation());
        if (mInfo.isNewProject()) {
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
        } else {
            File f = path.toFile();
            if (!f.isDirectory()) {
                return setStatus("An existing directory name must be specified.", MSG_ERROR);
            }
            String osPath = path.append(AndroidConstants.FN_ANDROID_MANIFEST).toOSString();
            File manifestFile = new File(osPath);
            if (!manifestFile.isFile()) {
                return setStatus(
                        String.format("File %1$s not found in %2$s.",
                                AndroidConstants.FN_ANDROID_MANIFEST, f.getName()),
                                MSG_ERROR);
            }
            AndroidManifestParser manifestData = AndroidManifestParser.parseForData(osPath);
            if (manifestData == null) {
                return setStatus(
                        String.format("File %1$s could not be parsed.", osPath),
                        MSG_ERROR);
            }
            String packageName = manifestData.getPackage();
            if (packageName == null || packageName.length() == 0) {
                return setStatus(
                        String.format("No package name defined in %1$s.", osPath),
                        MSG_ERROR);
            }
            Activity[] activities = manifestData.getActivities();
            if (activities == null || activities.length == 0) {
                if (mInfo.isCreateActivity()) {
                    return setStatus(
                            String.format("No activity name defined in %1$s.", osPath),
                            MSG_ERROR);
                }
            }
            if (path.append(".project").toFile().exists()) {  
                return setStatus("An Eclipse project already exists in this directory. Consider using File > Import > Existing Project instead.",
                        MSG_WARNING);
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
        if (mInfo.isNewProject() &&
                mInfo.getSdkTarget() != null &&
                mInfo.getSdkTarget().getVersion().isPreview() &&
                mInfo.getSdkTarget().getVersion().equals(mInfo.getMinSdkVersion()) == false) {
            return setStatus(
                    String.format("The SDK target is a preview. Min SDK Version must be set to '%s'.",
                            mInfo.getSdkTarget().getVersion().getCodename()),
                    MSG_ERROR);
        }
        if (mInfo.getMinSdkVersion().length() == 0) {
            return MSG_NONE;
        }
        if (mInfo.getSdkTarget() != null &&
                mInfo.getSdkTarget().getVersion().equals(mInfo.getMinSdkVersion()) == false) {
            return setStatus("The API level for the selected SDK target does not match the Min SDK Version.",
                    mInfo.getSdkTarget().getVersion().isPreview() ? MSG_ERROR : MSG_WARNING);
        }
        return MSG_NONE;
    }
    private int validateActivityField() {
        if (!mInfo.isCreateActivity()) {
            return MSG_NONE;
        }
        String activityFieldContents = mInfo.getActivityName();
        if (activityFieldContents.length() == 0) {
            return setStatus("Activity name must be specified.", MSG_ERROR);
        }
        String packageName = "";  
        int pos = activityFieldContents.lastIndexOf('.');
        if (pos >= 0) {
            packageName = activityFieldContents.substring(0, pos);
            if (packageName.startsWith(".")) { 
                packageName = packageName.substring(1);
            }
            activityFieldContents = activityFieldContents.substring(pos + 1);
        }
        if (activityFieldContents.charAt(0) == '.') {
            activityFieldContents = activityFieldContents.substring(1);
        }
        int result = MSG_NONE;
        IStatus status = JavaConventions.validateTypeVariableName(activityFieldContents,
                                                            "1.5", "1.5"); 
        if (!status.isOK()) {
            result = setStatus(status.getMessage(),
                        status.getSeverity() == IStatus.ERROR ? MSG_ERROR : MSG_WARNING);
        }
        if (result != MSG_ERROR && packageName.length() > 0) {
            status = JavaConventions.validatePackageName(packageName,
                                                            "1.5", "1.5"); 
            if (!status.isOK()) {
                result = setStatus(status.getMessage() + " (in the activity name)",
                            status.getSeverity() == IStatus.ERROR ? MSG_ERROR : MSG_WARNING);
            }
        }
        return result;
    }
    private int validatePackageField() {
        String packageFieldContents = mInfo.getPackageName();
        if (packageFieldContents.length() == 0) {
            return setStatus("Package name must be specified.", MSG_ERROR);
        }
        int result = MSG_NONE;
        IStatus status = JavaConventions.validatePackageName(packageFieldContents, "1.5", "1.5"); 
        if (!status.isOK()) {
            result = setStatus(status.getMessage(),
                        status.getSeverity() == IStatus.ERROR ? MSG_ERROR : MSG_WARNING);
        }
        if (result != MSG_ERROR && packageFieldContents.indexOf('.') == -1) {
            return setStatus("Package name must have at least two identifiers.", MSG_ERROR);
        }
        return result;
    }
    private int validateSourceFolder() {
        if (mInfo.isNewProject() || !mInfo.isCreateActivity()) {
            return MSG_NONE;
        }
        String osTarget = mInfo.getActivityName();
        if (osTarget.indexOf('.') == -1) {
            osTarget = mInfo.getPackageName() + File.separator + osTarget;
        } else if (osTarget.indexOf('.') == 0) {
            osTarget = mInfo.getPackageName() + osTarget;
        }
        osTarget = osTarget.replace('.', File.separatorChar) + AndroidConstants.DOT_JAVA;
        String projectPath = getProjectLocation();
        File projectDir = new File(projectPath);
        File[] all_dirs = projectDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File f : all_dirs) {
            Path path = new Path(f.getAbsolutePath());
            File java_activity = path.append(osTarget).toFile();
            if (java_activity.isFile()) {
                mSourceFolder = f.getName();
                return MSG_NONE;
            }
        }
        if (all_dirs.length > 0) {
            return setStatus(
                    String.format("%1$s can not be found under %2$s.", osTarget, projectPath),
                    MSG_ERROR);
        } else {
            return setStatus(
                    String.format("No source folders can be found in %1$s.", projectPath),
                    MSG_ERROR);
        }
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
