class NewXmlFileCreationPage extends WizardPage {
    static class TypeInfo {
        private final String mUiName;
        private final ResourceFolderType mResFolderType;
        private final String mTooltip;
        private final Object mRootSeed;
        private Button mWidget;
        private ArrayList<String> mRoots = new ArrayList<String>();
        private final String mXmlns;
        private final String mDefaultAttrs;
        private final String mDefaultRoot;
        private final int mTargetApiLevel;
        public TypeInfo(String uiName,
                        String tooltip,
                        ResourceFolderType resFolderType,
                        Object rootSeed,
                        String defaultRoot,
                        String xmlns,
                        String defaultAttrs,
                        int targetApiLevel) {
            mUiName = uiName;
            mResFolderType = resFolderType;
            mTooltip = tooltip;
            mRootSeed = rootSeed;
            mDefaultRoot = defaultRoot;
            mXmlns = xmlns;
            mDefaultAttrs = defaultAttrs;
            mTargetApiLevel = targetApiLevel;
        }
        String getUiName() {
            return mUiName;
        }
        String getTooltip() {
            return mTooltip;
        }
        String getResFolderName() {
            return mResFolderType.getName();
        }
        ResourceFolderType getResFolderType() {
            return mResFolderType;
        }
        void setWidget(Button widget) {
            mWidget = widget;
        }
        Button getWidget() {
            return mWidget;
        }
        Object getRootSeed() {
            return mRootSeed;
        }
        String getDefaultRoot() {
            return mDefaultRoot;
        }
        ArrayList<String> getRoots() {
            return mRoots;
        }
        String getXmlns() {
            return mXmlns;
        }
        String getDefaultAttrs() {
            return mDefaultAttrs;
        }
        public int getTargetApiLevel() {
            return mTargetApiLevel;
        }
    }
    private static final TypeInfo[] sTypes = {
        new TypeInfo(
                "Layout",                                           
                "An XML file that describes a screen layout.",      
                ResourceFolderType.LAYOUT,                          
                AndroidTargetData.DESCRIPTOR_LAYOUT,                
                "LinearLayout",                                     
                SdkConstants.NS_RESOURCES,                          
                "android:layout_width=\"wrap_content\"\n" +         
                "android:layout_height=\"wrap_content\"",
                1                                                   
                ),
        new TypeInfo("Values",                                      
                "An XML file with simple values: colors, strings, dimensions, etc.", 
                ResourceFolderType.VALUES,                          
                ResourcesDescriptors.ROOT_ELEMENT,                  
                null,                                               
                null,                                               
                null,                                               
                1                                                   
                ),
        new TypeInfo("Menu",                                        
                "An XML file that describes an menu.",              
                ResourceFolderType.MENU,                            
                MenuDescriptors.MENU_ROOT_ELEMENT,                  
                null,                                               
                SdkConstants.NS_RESOURCES,                          
                null,                                               
                1                                                   
                ),
        new TypeInfo("AppWidget Provider",                          
                "An XML file that describes a widget provider.",    
                ResourceFolderType.XML,                             
                AndroidTargetData.DESCRIPTOR_APPWIDGET_PROVIDER,    
                null,                                               
                SdkConstants.NS_RESOURCES,                          
                null,                                               
                3                                                   
                ),
        new TypeInfo("Preference",                                  
                "An XML file that describes preferences.",          
                ResourceFolderType.XML,                             
                AndroidTargetData.DESCRIPTOR_PREFERENCES,           
                AndroidConstants.CLASS_NAME_PREFERENCE_SCREEN,      
                SdkConstants.NS_RESOURCES,                          
                null,                                               
                1                                                   
                ),
        new TypeInfo("Searchable",                                  
                "An XML file that describes a searchable.",         
                ResourceFolderType.XML,                             
                AndroidTargetData.DESCRIPTOR_SEARCHABLE,            
                null,                                               
                SdkConstants.NS_RESOURCES,                          
                null,                                               
                1                                                   
                ),
        new TypeInfo("Animation",                                   
                "An XML file that describes an animation.",         
                ResourceFolderType.ANIM,                            
                new String[] {                                      
                    "set",          
                    "alpha",        
                    "scale",        
                    "translate",    
                    "rotate"        
                    },
                "set",              
                null,                                               
                null,                                               
                1                                                   
                ),
    };
    final static int NUM_COL = 4;
    private static final String RES_FOLDER_ABS = AndroidConstants.WS_RESOURCES + AndroidConstants.WS_SEP;
    private static final String RES_FOLDER_REL = SdkConstants.FD_RESOURCES + AndroidConstants.WS_SEP;
    private IProject mProject;
    private Text mProjectTextField;
    private Button mProjectBrowseButton;
    private Text mFileNameTextField;
    private Text mWsFolderPathTextField;
    private Combo mRootElementCombo;
    private IStructuredSelection mInitialSelection;
    private ConfigurationSelector mConfigSelector;
    private FolderConfiguration mTempConfig = new FolderConfiguration();
    private boolean mInternalWsFolderPathUpdate;
    private boolean mInternalTypeUpdate;
    private boolean mInternalConfigSelectorUpdate;
    private ProjectChooserHelper mProjectChooserHelper;
    private TargetChangeListener mSdkTargetChangeListener;
    private TypeInfo mCurrentTypeInfo;
    protected NewXmlFileCreationPage(String pageName) {
        super(pageName);
        setPageComplete(false);
    }
    public void setInitialSelection(IStructuredSelection initialSelection) {
        mInitialSelection = initialSelection;
    }
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        initializeDialogUnits(parent);
        composite.setLayout(new GridLayout(NUM_COL, false ));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectGroup(composite);
        createTypeGroup(composite);
        createRootGroup(composite);
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
        initializeFromSelection(mInitialSelection);
        initializeRootValues();
        enableTypesBasedOnApi();
        if (mCurrentTypeInfo != null) {
            updateRootCombo(mCurrentTypeInfo);
        }
        installTargetChangeListener();
        validatePage();
    }
    private void installTargetChangeListener() {
        mSdkTargetChangeListener = new TargetChangeListener() {
            @Override
            public IProject getProject() {
                return mProject;
            }
            @Override
            public void reload() {
                if (mProject != null) {
                    changeProject(mProject);
                }
            }
        };
        AdtPlugin.getDefault().addTargetListener(mSdkTargetChangeListener);
    }
    @Override
    public void dispose() {
        if (mSdkTargetChangeListener != null) {
            AdtPlugin.getDefault().removeTargetListener(mSdkTargetChangeListener);
            mSdkTargetChangeListener = null;
        }
        super.dispose();
    }
    public IProject getProject() {
        return mProject;
    }
    public String getFileName() {
        return mFileNameTextField == null ? "" : mFileNameTextField.getText();         
    }
    public String getWsFolderPath() {
        return mWsFolderPathTextField == null ? "" : mWsFolderPathTextField.getText(); 
    }
    public IFile getDestinationFile() {
        IProject project = getProject();
        String wsFolderPath = getWsFolderPath();
        String fileName = getFileName();
        if (project != null && wsFolderPath.length() > 0 && fileName.length() > 0) {
            IPath dest = new Path(wsFolderPath).append(fileName);
            IFile file = project.getFile(dest);
            return file;
        }
        return null;
    }
    public TypeInfo getSelectedType() {
        TypeInfo type = null;
        for (TypeInfo ti : sTypes) {
            if (ti.getWidget().getSelection()) {
                type = ti;
                break;
            }
        }
        return type;
    }
    public String getRootElement() {
        int index = mRootElementCombo.getSelectionIndex();
        if (index >= 0) {
            return mRootElementCombo.getItem(index);
        }
        return null;
    }
    private GridData newGridData(int horizSpan) {
        GridData gd = new GridData();
        gd.horizontalSpan = horizSpan;
        return gd;
    }
    private GridData newGridData(int horizSpan, int style) {
        GridData gd = new GridData(style);
        gd.horizontalSpan = horizSpan;
        return gd;
    }
    private void emptyCell(Composite parent) {
        new Label(parent, SWT.NONE);
    }
    private int padWithEmptyCells(Composite parent, int col) {
        for (; col < NUM_COL; ++col) {
            emptyCell(parent);
        }
        col = 0;
        return col;
    }
    private void createProjectGroup(Composite parent) {
        int col = 0;
        String tooltip = "The Android Project where the new resource file will be created.";
        Label label = new Label(parent, SWT.NONE);
        label.setText("Project");
        label.setToolTipText(tooltip);
        ++col;
        mProjectTextField = new Text(parent, SWT.BORDER);
        mProjectTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mProjectTextField.setToolTipText(tooltip);
        mProjectTextField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onProjectFieldUpdated();
            }
        });
        ++col;
        mProjectBrowseButton = new Button(parent, SWT.NONE);
        mProjectBrowseButton.setText("Browse...");
        mProjectBrowseButton.setToolTipText("Allows you to select the Android project to modify.");
        mProjectBrowseButton.addSelectionListener(new SelectionAdapter() {
           @Override
            public void widgetSelected(SelectionEvent e) {
               onProjectBrowse();
            }
        });
        mProjectChooserHelper = new ProjectChooserHelper(parent.getShell(), null );
        ++col;
        col = padWithEmptyCells(parent, col);
        tooltip = "The name of the resource file to create.";
        label = new Label(parent, SWT.NONE);
        label.setText("File");
        label.setToolTipText(tooltip);
        ++col;
        mFileNameTextField = new Text(parent, SWT.BORDER);
        mFileNameTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mFileNameTextField.setToolTipText(tooltip);
        mFileNameTextField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validatePage();
            }
        });
        ++col;
        padWithEmptyCells(parent, col);
    }
    private void createTypeGroup(Composite parent) {
        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(newGridData(NUM_COL, GridData.GRAB_HORIZONTAL));
        label = new Label(parent, SWT.NONE);
        label.setText("What type of resource would you like to create?");
        label.setLayoutData(newGridData(NUM_COL));
        emptyCell(parent);
        Composite grid = new Composite(parent, SWT.NONE);
        padWithEmptyCells(parent, 2);
        grid.setLayout(new GridLayout(NUM_COL, true ));
        SelectionListener radioListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof Button) {
                    onRadioTypeUpdated((Button) e.getSource());
                }
            }
        };
        int n = sTypes.length;
        int num_lines = (n + NUM_COL/2) / NUM_COL;
        for (int line = 0, k = 0; line < num_lines; line++) {
            for (int i = 0; i < NUM_COL; i++, k++) {
                if (k < n) {
                    TypeInfo type = sTypes[k];
                    Button radio = new Button(grid, SWT.RADIO);
                    type.setWidget(radio);
                    radio.setSelection(false);
                    radio.setText(type.getUiName());
                    radio.setToolTipText(type.getTooltip());
                    radio.addSelectionListener(radioListener);
                } else {
                    emptyCell(grid);
                }
            }
        }
        label = new Label(parent, SWT.NONE);
        label.setText("What type of resource configuration would you like?");
        label.setLayoutData(newGridData(NUM_COL));
        emptyCell(parent);
        mConfigSelector = new ConfigurationSelector(parent, false );
        GridData gd = newGridData(2, GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gd.widthHint = ConfigurationSelector.WIDTH_HINT;
        gd.heightHint = ConfigurationSelector.HEIGHT_HINT;
        mConfigSelector.setLayoutData(gd);
        mConfigSelector.setOnChangeListener(new onConfigSelectorUpdated());
        emptyCell(parent);
        String tooltip = "The folder where the file will be generated, relative to the project.";
        label = new Label(parent, SWT.NONE);
        label.setText("Folder");
        label.setToolTipText(tooltip);
        mWsFolderPathTextField = new Text(parent, SWT.BORDER);
        mWsFolderPathTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mWsFolderPathTextField.setToolTipText(tooltip);
        mWsFolderPathTextField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onWsFolderPathUpdated();
            }
        });
    }
    private void createRootGroup(Composite parent) {
        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(newGridData(NUM_COL, GridData.GRAB_HORIZONTAL));
        String tooltip = "The root element to create in the XML file.";
        label = new Label(parent, SWT.NONE);
        label.setText("Select the root element for the XML file:");
        label.setLayoutData(newGridData(NUM_COL));
        label.setToolTipText(tooltip);
        emptyCell(parent);
        mRootElementCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        mRootElementCombo.setEnabled(false);
        mRootElementCombo.select(0);
        mRootElementCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRootElementCombo.setToolTipText(tooltip);
        padWithEmptyCells(parent, 2);
    }
    private void initializeFromSelection(IStructuredSelection selection) {
        if (selection == null) {
            return;
        }
        IProject targetProject = null;
        String targetWsFolderPath = null;
        String targetFileName = null;
        int targetScore = 0;
        for (Object element : selection.toList()) {
            if (element instanceof IAdaptable) {
                IResource res = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
                IProject project = res != null ? res.getProject() : null;
                try {
                    if (project == null || !project.hasNature(AndroidConstants.NATURE)) {
                        continue;
                    }
                } catch (CoreException e) {
                    continue;
                }
                int score = 1; 
                IPath wsFolderPath = null;
                String fileName = null;
                if (res.getType() == IResource.FOLDER) {
                    wsFolderPath = res.getProjectRelativePath();
                } else if (res.getType() == IResource.FILE) {
                    fileName = res.getName();
                    wsFolderPath = res.getParent().getProjectRelativePath();
                }
                if (wsFolderPath != null &&
                        wsFolderPath.segmentCount() > 1 &&
                        SdkConstants.FD_RESOURCES.equals(wsFolderPath.segment(0))) {
                    score += 2;
                } else {
                    wsFolderPath = null;
                    fileName = null;
                }
                score += fileName != null ? 4 : 0;
                if (score > targetScore) {
                    targetScore = score;
                    targetProject = project;
                    targetWsFolderPath = wsFolderPath != null ? wsFolderPath.toString() : null;
                    targetFileName = fileName;
                }
            }
        }
        if (targetScore > 0) {
            mProject = targetProject;
            mProjectTextField.setText(targetProject != null ? targetProject.getName() : ""); 
            mFileNameTextField.setText(targetFileName != null ? targetFileName : ""); 
            mWsFolderPathTextField.setText(targetWsFolderPath != null ? targetWsFolderPath : ""); 
        }
    }
    private void initializeRootValues() {
        for (TypeInfo type : sTypes) {
            ArrayList<String> roots = type.getRoots();
            if (roots.size() > 0) {
                roots.clear();
            }
            Object rootSeed = type.getRootSeed();
            if (rootSeed instanceof String) {
                roots.add((String) rootSeed);
            } else if (rootSeed instanceof String[]) {
                for (String value : (String[]) rootSeed) {
                    roots.add(value);
                }
            } else if (rootSeed instanceof Integer && mProject != null) {
                IAndroidTarget target = null;
                AndroidTargetData data = null;
                target = Sdk.getCurrent().getTarget(mProject);
                if (target == null) {
                    AdtPlugin.log(IStatus.INFO,
                            "NewXmlFile wizard: no platform target for project %s",  
                            mProject.getName());
                    continue;
                } else {
                    data = Sdk.getCurrent().getTargetData(target);
                    if (data == null) {
                        AdtPlugin.log(IStatus.INFO,
                              "NewXmlFile wizard: no data for target %s, project %s",  
                              target.getName(), mProject.getName());
                        continue;
                    }
                }
                IDescriptorProvider provider = data.getDescriptorProvider((Integer)rootSeed);
                ElementDescriptor descriptor = provider.getDescriptor();
                if (descriptor != null) {
                    HashSet<ElementDescriptor> visited = new HashSet<ElementDescriptor>();
                    initRootElementDescriptor(roots, descriptor, visited);
                }
                Collections.sort(roots);
            }
        }
    }
    private void initRootElementDescriptor(ArrayList<String> roots,
            ElementDescriptor desc, HashSet<ElementDescriptor> visited) {
        if (!(desc instanceof DocumentDescriptor)) {
            String xmlName = desc.getXmlName();
            if (xmlName != null && xmlName.length() > 0) {
                roots.add(xmlName);
            }
        }
        visited.add(desc);
        for (ElementDescriptor child : desc.getChildren()) {
            if (!visited.contains(child)) {
                initRootElementDescriptor(roots, child, visited);
            }
        }
    }
    private void onProjectFieldUpdated() {
        String project = mProjectTextField.getText();
        IJavaProject[] projects = mProjectChooserHelper.getAndroidProjects(null );
        IProject found = null;
        for (IJavaProject p : projects) {
            if (p.getProject().getName().equals(project)) {
                found = p.getProject();
                break;
            }
        }
        if (found != mProject) {
            changeProject(found);
        }
    }
    private void onProjectBrowse() {
        IJavaProject p = mProjectChooserHelper.chooseJavaProject(mProjectTextField.getText(),
                "Please select the target project");
        if (p != null) {
            changeProject(p.getProject());
            mProjectTextField.setText(mProject.getName());
        }
    }
    private void changeProject(IProject newProject) {
        mProject = newProject;
        enableTypesBasedOnApi();
        resetFolderPath(false );
        initializeRootValues();
        updateRootCombo(getSelectedType());
        validatePage();
    }
    private void onWsFolderPathUpdated() {
        if (mInternalWsFolderPathUpdate) {
            return;
        }
        String wsFolderPath = mWsFolderPathTextField.getText();
        wsFolderPath = wsFolderPath.replaceAll("/+\\.\\./+|/+\\./+|
        wsFolderPath = wsFolderPath.replaceAll("^\\.\\./+|^\\./+", "");                   
        wsFolderPath = wsFolderPath.replaceAll("/+\\.\\.$|/+\\.$|/+$", "");               
        ArrayList<TypeInfo> matches = new ArrayList<TypeInfo>();
        if (wsFolderPath.startsWith(RES_FOLDER_REL)) {
            wsFolderPath = RES_FOLDER_ABS + wsFolderPath.substring(RES_FOLDER_REL.length());
            mInternalWsFolderPathUpdate = true;
            mWsFolderPathTextField.setText(wsFolderPath);
            mInternalWsFolderPathUpdate = false;
        }
        if (wsFolderPath.startsWith(RES_FOLDER_ABS)) {
            wsFolderPath = wsFolderPath.substring(RES_FOLDER_ABS.length());
            int pos = wsFolderPath.indexOf(AndroidConstants.WS_SEP_CHAR);
            if (pos >= 0) {
                wsFolderPath = wsFolderPath.substring(0, pos);
            }
            String[] folderSegments = wsFolderPath.split(FolderConfiguration.QUALIFIER_SEP);
            if (folderSegments.length > 0) {
                String folderName = folderSegments[0];
                mInternalConfigSelectorUpdate = true;
                mConfigSelector.setConfiguration(folderSegments);
                mInternalConfigSelectorUpdate = false;
                boolean selected = false;
                for (TypeInfo type : sTypes) {
                    if (type.getResFolderName().equals(folderName)) {
                        matches.add(type);
                        selected |= type.getWidget().getSelection();
                    }
                }
                if (matches.size() == 1) {
                    if (!selected) {
                        selectType(matches.get(0));
                    }
                } else if (matches.size() > 1) {
                    if (!selected) {
                        selectType(null);
                    }
                } else {
                    selectType(null);
                }
            }
        }
        validatePage();
    }
    private void onRadioTypeUpdated(Button typeWidget) {
        if (mInternalTypeUpdate || !typeWidget.getSelection()) {
            return;
        }
        TypeInfo type = null;
        for (TypeInfo ti : sTypes) {
            if (ti.getWidget() == typeWidget) {
                type = ti;
                break;
            }
        }
        if (type == null) {
            return;
        }
        updateRootCombo(type);
        String wsFolderPath = mWsFolderPathTextField.getText();
        String newPath = null;
        mConfigSelector.getConfiguration(mTempConfig);
        ResourceQualifier qual = mTempConfig.getInvalidQualifier();
        if (qual == null) {
            newPath = RES_FOLDER_ABS + mTempConfig.getFolderName(type.getResFolderType(), mProject);
        } else {
            if (wsFolderPath.startsWith(RES_FOLDER_ABS)) {
                wsFolderPath.replaceFirst(
                        "^(" + RES_FOLDER_ABS +")[^-]*(.*)",         
                        "\\1" + type.getResFolderName() + "\\2");   
            } else {
                newPath = RES_FOLDER_ABS + mTempConfig.getFolderName(type.getResFolderType(),
                        mProject);
            }
        }
        if (newPath != null && !newPath.equals(wsFolderPath)) {
            mInternalWsFolderPathUpdate = true;
            mWsFolderPathTextField.setText(newPath);
            mInternalWsFolderPathUpdate = false;
        }
        validatePage();
    }
    private void updateRootCombo(TypeInfo type) {
        mRootElementCombo.removeAll();
        if (type != null) {
            ArrayList<String> roots = type.getRoots();
            mRootElementCombo.setEnabled(roots != null && roots.size() > 1);
            for (String root : roots) {
                mRootElementCombo.add(root);
            }
            int index = 0; 
            String defaultRoot = type.getDefaultRoot();
            if (defaultRoot != null) {
                index = roots.indexOf(defaultRoot);
            }
            mRootElementCombo.select(index < 0 ? 0 : index);
        }
    }
    private class onConfigSelectorUpdated implements Runnable {
        public void run() {
            if (mInternalConfigSelectorUpdate) {
                return;
            }
            resetFolderPath(true );
        }
    }
    private void selectType(TypeInfo type) {
        if (type == null || !type.getWidget().getSelection()) {
            mInternalTypeUpdate = true;
            mCurrentTypeInfo = type;
            for (TypeInfo type2 : sTypes) {
                type2.getWidget().setSelection(type2 == type);
            }
            updateRootCombo(type);
            mInternalTypeUpdate = false;
        }
    }
    private void enableTypesBasedOnApi() {
        IAndroidTarget target = mProject != null ? Sdk.getCurrent().getTarget(mProject) : null;
        int currentApiLevel = 1;
        if (target != null) {
            currentApiLevel = target.getVersion().getApiLevel();
        }
        for (TypeInfo type : sTypes) {
            type.getWidget().setEnabled(type.getTargetApiLevel() <= currentApiLevel);
        }
    }
    private void resetFolderPath(boolean validate) {
        TypeInfo type = getSelectedType();
        if (type != null) {
            mConfigSelector.getConfiguration(mTempConfig);
            StringBuffer sb = new StringBuffer(RES_FOLDER_ABS);
            sb.append(mTempConfig.getFolderName(type.getResFolderType(), mProject));
            mInternalWsFolderPathUpdate = true;
            mWsFolderPathTextField.setText(sb.toString());
            mInternalWsFolderPathUpdate = false;
            if (validate) {
                validatePage();
            }
        }
    }
    private void validatePage() {
        String error = null;
        String warning = null;
        if (getProject() == null) {
            error = "Please select an Android project.";
        }
        if (error == null) {
            String fileName = getFileName();
            if (fileName == null || fileName.length() == 0) {
                error = "A destination file name is required.";
            } else if (!fileName.endsWith(AndroidConstants.DOT_XML)) {
                error = String.format("The filename must end with %1$s.", AndroidConstants.DOT_XML);
            }
        }
        if (error == null) {
            TypeInfo type = getSelectedType();
            if (type == null) {
                error = "One of the types must be selected (e.g. layout, values, etc.)";
            }
        }
        if (error == null) {
            IAndroidTarget target = Sdk.getCurrent().getTarget(mProject);
            int currentApiLevel = 1;
            if (target != null) {
                currentApiLevel = target.getVersion().getApiLevel();
            }
            TypeInfo type = getSelectedType();
            if (type.getTargetApiLevel() > currentApiLevel) {
                error = "The API level of the selected type (e.g. AppWidget, etc.) is not " +
                        "compatible with the API level of the project.";
            }
        }
        if (error == null) {
            ConfigurationState state = mConfigSelector.getState();
            if (state == ConfigurationState.INVALID_CONFIG) {
                ResourceQualifier qual = mConfigSelector.getInvalidQualifier();
                if (qual != null) {
                    error = String.format("The qualifier '%1$s' is invalid in the folder configuration.",
                            qual.getName());
                }
            } else if (state == ConfigurationState.REGION_WITHOUT_LANGUAGE) {
                error = "The Region qualifier requires the Language qualifier.";
            }
        }
        if (error == null) {
            String wsFolderPath = getWsFolderPath();
            if (!wsFolderPath.startsWith(RES_FOLDER_ABS)) {
                error = String.format("Target folder must start with %1$s.", RES_FOLDER_ABS);
            }
        }
        if (error == null) {
            IFile file = getDestinationFile();
            if (file != null && file.exists()) {
                warning = "The destination file already exists";
            }
        }
        setPageComplete(error == null);
        if (error != null) {
            setMessage(error, WizardPage.ERROR);
        } else if (warning != null) {
            setMessage(warning, WizardPage.WARNING);
        } else {
            setErrorMessage(null);
            setMessage(null);
        }
    }
}
