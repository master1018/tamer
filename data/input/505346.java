public class NewProjectWizard extends Wizard implements INewWizard {
    protected enum AvailablePages {
        ANDROID_AND_TEST_PROJECT,
        TEST_PROJECT_ONLY
    }
    private static final String PARAM_SDK_TOOLS_DIR = "ANDROID_SDK_TOOLS";          
    private static final String PARAM_ACTIVITY = "ACTIVITY_NAME";                   
    private static final String PARAM_APPLICATION = "APPLICATION_NAME";             
    private static final String PARAM_PACKAGE = "PACKAGE";                          
    private static final String PARAM_PROJECT = "PROJECT_NAME";                     
    private static final String PARAM_STRING_NAME = "STRING_NAME";                  
    private static final String PARAM_STRING_CONTENT = "STRING_CONTENT";            
    private static final String PARAM_IS_NEW_PROJECT = "IS_NEW_PROJECT";            
    private static final String PARAM_SRC_FOLDER = "SRC_FOLDER";                    
    private static final String PARAM_SDK_TARGET = "SDK_TARGET";                    
    private static final String PARAM_MIN_SDK_VERSION = "MIN_SDK_VERSION";          
    private static final String PARAM_TEST_TARGET_PACKAGE = "TEST_TARGET_PCKG";     
    private static final String PARAM_TARGET_SELF = "TARGET_SELF";                  
    private static final String PARAM_TARGET_MAIN = "TARGET_MAIN";                  
    private static final String PARAM_TARGET_EXISTING = "TARGET_EXISTING";          
    private static final String PARAM_REFERENCE_PROJECT = "REFERENCE_PROJECT";      
    private static final String PH_ACTIVITIES = "ACTIVITIES";                       
    private static final String PH_USES_SDK = "USES-SDK";                           
    private static final String PH_INTENT_FILTERS = "INTENT_FILTERS";               
    private static final String PH_STRINGS = "STRINGS";                             
    private static final String PH_TEST_USES_LIBRARY = "TEST-USES-LIBRARY";         
    private static final String PH_TEST_INSTRUMENTATION = "TEST-INSTRUMENTATION";   
    private static final String BIN_DIRECTORY =
        SdkConstants.FD_OUTPUT + AndroidConstants.WS_SEP;
    private static final String RES_DIRECTORY =
        SdkConstants.FD_RESOURCES + AndroidConstants.WS_SEP;
    private static final String ASSETS_DIRECTORY =
        SdkConstants.FD_ASSETS + AndroidConstants.WS_SEP;
    private static final String DRAWABLE_DIRECTORY =
        SdkConstants.FD_DRAWABLE + AndroidConstants.WS_SEP;
    private static final String DRAWABLE_HDPI_DIRECTORY =
        SdkConstants.FD_DRAWABLE + "-" + Density.HIGH.getValue() + AndroidConstants.WS_SEP;   
    private static final String DRAWABLE_MDPI_DIRECTORY =
        SdkConstants.FD_DRAWABLE + "-" + Density.MEDIUM.getValue() + AndroidConstants.WS_SEP; 
    private static final String DRAWABLE_LDPI_DIRECTORY =
        SdkConstants.FD_DRAWABLE + "-" + Density.LOW.getValue() + AndroidConstants.WS_SEP;    
    private static final String LAYOUT_DIRECTORY =
        SdkConstants.FD_LAYOUT + AndroidConstants.WS_SEP;
    private static final String VALUES_DIRECTORY =
        SdkConstants.FD_VALUES + AndroidConstants.WS_SEP;
    private static final String GEN_SRC_DIRECTORY =
        SdkConstants.FD_GEN_SOURCES + AndroidConstants.WS_SEP;
    private static final String TEMPLATES_DIRECTORY = "templates/"; 
    private static final String TEMPLATE_MANIFEST = TEMPLATES_DIRECTORY
            + "AndroidManifest.template"; 
    private static final String TEMPLATE_ACTIVITIES = TEMPLATES_DIRECTORY
            + "activity.template"; 
    private static final String TEMPLATE_USES_SDK = TEMPLATES_DIRECTORY
            + "uses-sdk.template"; 
    private static final String TEMPLATE_INTENT_LAUNCHER = TEMPLATES_DIRECTORY
            + "launcher_intent_filter.template"; 
    private static final String TEMPLATE_TEST_USES_LIBRARY = TEMPLATES_DIRECTORY
            + "test_uses-library.template"; 
    private static final String TEMPLATE_TEST_INSTRUMENTATION = TEMPLATES_DIRECTORY
            + "test_instrumentation.template"; 
    private static final String TEMPLATE_STRINGS = TEMPLATES_DIRECTORY
            + "strings.template"; 
    private static final String TEMPLATE_STRING = TEMPLATES_DIRECTORY
            + "string.template"; 
    private static final String PROJECT_ICON = "icon.png"; 
    private static final String ICON_HDPI = "icon_hdpi.png"; 
    private static final String ICON_MDPI = "icon_mdpi.png"; 
    private static final String ICON_LDPI = "icon_ldpi.png"; 
    private static final String STRINGS_FILE = "strings.xml";       
    private static final String STRING_RSRC_PREFIX = "@string/";    
    private static final String STRING_APP_NAME = "app_name";       
    private static final String STRING_HELLO_WORLD = "hello";       
    private static final String[] DEFAULT_DIRECTORIES = new String[] {
            BIN_DIRECTORY, RES_DIRECTORY, ASSETS_DIRECTORY };
    private static final String[] RES_DIRECTORIES = new String[] {
            DRAWABLE_DIRECTORY, LAYOUT_DIRECTORY, VALUES_DIRECTORY };
    private static final String[] RES_DENSITY_ENABLED_DIRECTORIES = new String[] {
            DRAWABLE_HDPI_DIRECTORY, DRAWABLE_MDPI_DIRECTORY, DRAWABLE_LDPI_DIRECTORY,
            LAYOUT_DIRECTORY, VALUES_DIRECTORY };
    private static final String PROJECT_LOGO_LARGE = "icons/android_large.png"; 
    private static final String JAVA_ACTIVITY_TEMPLATE = "java_file.template";  
    private static final String LAYOUT_TEMPLATE = "layout.template";            
    private static final String MAIN_LAYOUT_XML = "main.xml";                   
    private NewProjectCreationPage mMainPage;
    private NewTestProjectCreationPage mTestPage;
    private String mPackageName;
    private final AvailablePages mAvailablePages;
    public NewProjectWizard() {
        this(AvailablePages.ANDROID_AND_TEST_PROJECT);
    }
    protected NewProjectWizard(AvailablePages availablePages) {
        mAvailablePages = availablePages;
    }
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setHelpAvailable(false); 
        setImageDescriptor();
        if (mAvailablePages == AvailablePages.ANDROID_AND_TEST_PROJECT) {
            mMainPage = createMainPage();
            setWindowTitle("New Android Project");
        } else {
            setWindowTitle("New Android Test Project");
        }
        mTestPage = createTestPage();
    }
    protected NewProjectCreationPage createMainPage() {
        return new NewProjectCreationPage();
    }
    protected NewTestProjectCreationPage createTestPage() {
        return new NewTestProjectCreationPage();
    }
    @Override
    public void addPages() {
        if (mAvailablePages == AvailablePages.ANDROID_AND_TEST_PROJECT) {
            addPage(mMainPage);
        }
        addPage(mTestPage);
        if (mMainPage != null && mTestPage != null) {
            mTestPage.setMainInfo(mMainPage.getMainInfo());
            mMainPage.setTestInfo(mTestPage.getTestInfo());
        }
    }
    @Override
    public boolean performFinish() {
        if (!createAndroidProjects()) {
            return false;
        }
        OpenJavaPerspectiveAction action = new OpenJavaPerspectiveAction();
        action.run();
        return true;
    }
    public String getPackageName() {
        return mPackageName;
    }
    private boolean validateNewProjectLocationIsEmpty(IPath destination) {
        File f = new File(destination.toOSString());
        if (f.isDirectory() && f.list().length > 0) {
            return AdtPlugin.displayPrompt("New Android Project",
                    "You are going to create a new Android Project in an existing, non-empty, directory. Are you sure you want to proceed?");
        }
        return true;
    }
    private static class ProjectInfo {
        private final IProject mProject;
        private final IProjectDescription mDescription;
        private final Map<String, Object> mParameters;
        private final HashMap<String, String> mDictionary;
        public ProjectInfo(IProject project,
                IProjectDescription description,
                Map<String, Object> parameters,
                HashMap<String, String> dictionary) {
                    mProject = project;
                    mDescription = description;
                    mParameters = parameters;
                    mDictionary = dictionary;
        }
        public IProject getProject() {
            return mProject;
        }
        public IProjectDescription getDescription() {
            return mDescription;
        }
        public Map<String, Object> getParameters() {
            return mParameters;
        }
        public HashMap<String, String> getDictionary() {
            return mDictionary;
        }
    }
    private boolean createAndroidProjects() {
        final ProjectInfo mainData = collectMainPageInfo();
        if (mMainPage != null && mainData == null) {
            return false;
        }
        final ProjectInfo testData = collectTestPageInfo();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
                createProjectAsync(monitor, mainData, testData);
            }
        };
        runAsyncOperation(op);
        return true;
    }
    private ProjectInfo collectMainPageInfo() {
        if (mMainPage == null) {
            return null;
        }
        IMainInfo info = mMainPage.getMainInfo();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(info.getProjectName());
        final IProjectDescription description = workspace.newProjectDescription(project.getName());
        mPackageName = info.getPackageName();
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_PROJECT, info.getProjectName());
        parameters.put(PARAM_PACKAGE, mPackageName);
        parameters.put(PARAM_APPLICATION, STRING_RSRC_PREFIX + STRING_APP_NAME);
        parameters.put(PARAM_SDK_TOOLS_DIR, AdtPlugin.getOsSdkToolsFolder());
        parameters.put(PARAM_IS_NEW_PROJECT, info.isNewProject());
        parameters.put(PARAM_SRC_FOLDER, info.getSourceFolder());
        parameters.put(PARAM_SDK_TARGET, info.getSdkTarget());
        parameters.put(PARAM_MIN_SDK_VERSION, info.getMinSdkVersion());
        if (info.isCreateActivity()) {
            String activityName = info.getActivityName();
            if (activityName.startsWith(".")) { 
                activityName = activityName.substring(1);
            }
            parameters.put(PARAM_ACTIVITY, activityName);
        }
        final HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put(STRING_APP_NAME, info.getApplicationName());
        IPath path = info.getLocationPath();
        IPath defaultLocation = Platform.getLocation();
        if (!path.equals(defaultLocation)) {
            description.setLocation(path);
        }
        if (info.isNewProject() && !info.useDefaultLocation() &&
                !validateNewProjectLocationIsEmpty(path)) {
            return null;
        }
        return new ProjectInfo(project, description, parameters, dictionary);
    }
    private ProjectInfo collectTestPageInfo() {
        if (mTestPage == null) {
            return null;
        }
        TestInfo info = mTestPage.getTestInfo();
        if (!info.getCreateTestProject()) {
            return null;
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(info.getProjectName());
        final IProjectDescription description = workspace.newProjectDescription(project.getName());
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_PROJECT, info.getProjectName());
        parameters.put(PARAM_PACKAGE, info.getPackageName());
        parameters.put(PARAM_APPLICATION, STRING_RSRC_PREFIX + STRING_APP_NAME);
        parameters.put(PARAM_SDK_TOOLS_DIR, AdtPlugin.getOsSdkToolsFolder());
        parameters.put(PARAM_IS_NEW_PROJECT, true);
        parameters.put(PARAM_SRC_FOLDER, info.getSourceFolder());
        parameters.put(PARAM_SDK_TARGET, info.getSdkTarget());
        parameters.put(PARAM_MIN_SDK_VERSION, info.getMinSdkVersion());
        parameters.put(PARAM_TEST_TARGET_PACKAGE, info.getTargetPackageName());
        if (info.isTestingSelf()) {
            parameters.put(PARAM_TARGET_SELF, true);
        }
        if (info.isTestingMain()) {
            parameters.put(PARAM_TARGET_MAIN, true);
        }
        if (info.isTestingExisting()) {
            parameters.put(PARAM_TARGET_EXISTING, true);
            parameters.put(PARAM_REFERENCE_PROJECT, info.getExistingTestedProject());
        }
        final HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put(STRING_APP_NAME, info.getApplicationName());
        IPath path = info.getLocationPath();
        IPath defaultLocation = Platform.getLocation();
        if (!path.equals(defaultLocation)) {
            description.setLocation(path);
        }
        if (!info.useDefaultLocation() && !validateNewProjectLocationIsEmpty(path)) {
            return null;
        }
        return new ProjectInfo(project, description, parameters, dictionary);
    }
    private void runAsyncOperation(WorkspaceModifyOperation op) {
        try {
            getContainer().run(true , true , op);
        } catch (InvocationTargetException e) {
            AdtPlugin.log(e, "New Project Wizard failed");
            Throwable t = e.getTargetException();
            if (t instanceof CoreException) {
                CoreException core = (CoreException) t;
                if (core.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    MessageDialog.openError(getShell(), "Error", "Error: Case Variant Exists");
                } else {
                    ErrorDialog.openError(getShell(), "Error", core.getMessage(), core.getStatus());
                }
            } else {
                String msg = t.getMessage();
                Throwable t1 = t;
                while (msg == null && t1.getCause() != null) {
                    msg = t1.getMessage();
                    t1 = t1.getCause();
                }
                if (msg == null) {
                    msg = t.toString();
                }
                MessageDialog.openError(getShell(), "Error", msg);
            }
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void createProjectAsync(IProgressMonitor monitor,
            ProjectInfo mainData,
            ProjectInfo testData)
                throws InvocationTargetException {
        monitor.beginTask("Create Android Project", 100);
        try {
            IProject mainProject = null;
            if (mainData != null) {
                mainProject = createEclipseProject(
                        new SubProgressMonitor(monitor, 50),
                        mainData.getProject(),
                        mainData.getDescription(),
                        mainData.getParameters(),
                        mainData.getDictionary());
            }
            if (testData != null) {
                Map<String, Object> parameters = testData.getParameters();
                if (parameters.containsKey(PARAM_TARGET_MAIN) && mainProject != null) {
                    parameters.put(PARAM_REFERENCE_PROJECT, mainProject);
                }
                createEclipseProject(
                        new SubProgressMonitor(monitor, 50),
                        testData.getProject(),
                        testData.getDescription(),
                        parameters,
                        testData.getDictionary());
            }
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            monitor.done();
        }
    }
    private IProject createEclipseProject(IProgressMonitor monitor,
            IProject project,
            IProjectDescription description,
            Map<String, Object> parameters,
            Map<String, String> dictionary)
                throws CoreException, IOException {
        IAndroidTarget target = (IAndroidTarget) parameters.get(PARAM_SDK_TARGET);
        boolean legacy = target.getVersion().getApiLevel() < 4;
        project.create(description, new SubProgressMonitor(monitor, 10));
        if (monitor.isCanceled()) throw new OperationCanceledException();
        project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 10));
        AndroidNature.setupProjectNatures(project, monitor);
        addDefaultDirectories(project, AndroidConstants.WS_ROOT, DEFAULT_DIRECTORIES, monitor);
        String[] sourceFolders = new String[] {
                    (String) parameters.get(PARAM_SRC_FOLDER),
                    GEN_SRC_DIRECTORY
                };
        addDefaultDirectories(project, AndroidConstants.WS_ROOT, sourceFolders, monitor);
        if (legacy) {
            addDefaultDirectories(project, RES_DIRECTORY, RES_DIRECTORIES, monitor);
        } else {
            addDefaultDirectories(project, RES_DIRECTORY, RES_DENSITY_ENABLED_DIRECTORIES, monitor);
        }
        IJavaProject javaProject = JavaCore.create(project);
        for (String sourceFolder : sourceFolders) {
            setupSourceFolder(javaProject, sourceFolder, monitor);
        }
        IFolder genSrcFolder = project.getFolder(AndroidConstants.WS_ROOT + GEN_SRC_DIRECTORY);
        if (genSrcFolder.exists()) {
            genSrcFolder.setDerived(true);
        }
        if (((Boolean) parameters.get(PARAM_IS_NEW_PROJECT)).booleanValue()) {
            addManifest(project, parameters, dictionary, monitor);
            addIcon(project, legacy, monitor);
            addSampleCode(project, sourceFolders[0], parameters, dictionary, monitor);
            if (dictionary.size() > 0) {
                addStringDictionaryFile(project, dictionary, monitor);
            }
            javaProject.setOutputLocation(project.getFolder(BIN_DIRECTORY).getFullPath(),
                    monitor);
        }
        if (parameters.containsKey(PARAM_REFERENCE_PROJECT)) {
            IProject refProject = (IProject) parameters.get(PARAM_REFERENCE_PROJECT);
            if (refProject != null) {
                IProjectDescription desc = project.getDescription();
                desc.setReferencedProjects(new IProject[] { refProject });
                project.setDescription(desc, IResource.KEEP_HISTORY,
                        new SubProgressMonitor(monitor, 10));
                IClasspathEntry entry = JavaCore.newProjectEntry(
                        refProject.getFullPath(), 
                        new IAccessRule[0], 
                        false, 
                        new IClasspathAttribute[0], 
                        false 
                );
                ProjectHelper.addEntryToClasspath(javaProject, entry);
            }
        }
        Sdk.getCurrent().initProject(project, target);
        ProjectHelper.fixProject(project);
        return project;
    }
    private void addDefaultDirectories(IProject project, String parentFolder,
            String[] folders, IProgressMonitor monitor) throws CoreException {
        for (String name : folders) {
            if (name.length() > 0) {
                IFolder folder = project.getFolder(parentFolder + name);
                if (!folder.exists()) {
                    folder.create(true , true ,
                            new SubProgressMonitor(monitor, 10));
                }
            }
        }
    }
    private void addManifest(IProject project, Map<String, Object> parameters,
            Map<String, String> dictionary, IProgressMonitor monitor)
            throws CoreException, IOException {
        IFile file = project.getFile(AndroidConstants.FN_ANDROID_MANIFEST);
        if (!file.exists()) {
            String manifestTemplate = AdtPlugin.readEmbeddedTextFile(TEMPLATE_MANIFEST);
            manifestTemplate = replaceParameters(manifestTemplate, parameters);
            if (manifestTemplate == null) {
                AdtPlugin.logAndPrintError(null, getWindowTitle() ,
                        "Failed to generate the Android manifest. Missing template %s",
                        TEMPLATE_MANIFEST);
                return;
            }
            if (parameters.containsKey(PARAM_ACTIVITY)) {
                String activityTemplate = AdtPlugin.readEmbeddedTextFile(TEMPLATE_ACTIVITIES);
                String activities = replaceParameters(activityTemplate, parameters);
                String intent = AdtPlugin.readEmbeddedTextFile(TEMPLATE_INTENT_LAUNCHER);
                if (activities != null) {
                    if (intent != null) {
                        activities = activities.replaceAll(PH_INTENT_FILTERS, intent);
                    }
                    manifestTemplate = manifestTemplate.replaceAll(PH_ACTIVITIES, activities);
                }
            } else {
                manifestTemplate = manifestTemplate.replaceAll(PH_ACTIVITIES, "");  
            }
            if (parameters.containsKey(PARAM_TEST_TARGET_PACKAGE)) {
                String usesLibrary = AdtPlugin.readEmbeddedTextFile(TEMPLATE_TEST_USES_LIBRARY);
                if (usesLibrary != null) {
                    manifestTemplate = manifestTemplate.replaceAll(
                            PH_TEST_USES_LIBRARY, usesLibrary);
                }
                String instru = AdtPlugin.readEmbeddedTextFile(TEMPLATE_TEST_INSTRUMENTATION);
                if (instru != null) {
                    manifestTemplate = manifestTemplate.replaceAll(
                            PH_TEST_INSTRUMENTATION, instru);
                }
                manifestTemplate = replaceParameters(manifestTemplate, parameters);
            } else {
                manifestTemplate = manifestTemplate.replaceAll(PH_TEST_USES_LIBRARY, "");     
                manifestTemplate = manifestTemplate.replaceAll(PH_TEST_INSTRUMENTATION, "");  
            }
            String minSdkVersion = (String) parameters.get(PARAM_MIN_SDK_VERSION);
            if (minSdkVersion != null && minSdkVersion.length() > 0) {
                String usesSdkTemplate = AdtPlugin.readEmbeddedTextFile(TEMPLATE_USES_SDK);
                if (usesSdkTemplate != null) {
                    String usesSdk = replaceParameters(usesSdkTemplate, parameters);
                    manifestTemplate = manifestTemplate.replaceAll(PH_USES_SDK, usesSdk);
                }
            } else {
                manifestTemplate = manifestTemplate.replaceAll(PH_USES_SDK, "");
            }
            InputStream stream = new ByteArrayInputStream(
                    manifestTemplate.getBytes("UTF-8")); 
            file.create(stream, false , new SubProgressMonitor(monitor, 10));
        }
    }
    private void addStringDictionaryFile(IProject project,
            Map<String, String> strings, IProgressMonitor monitor)
            throws CoreException, IOException {
        IFile file = project.getFile(RES_DIRECTORY + AndroidConstants.WS_SEP
                                     + VALUES_DIRECTORY + AndroidConstants.WS_SEP + STRINGS_FILE);
        if (!file.exists()) {
            String stringDefinitionTemplate = AdtPlugin.readEmbeddedTextFile(TEMPLATE_STRINGS);
            String stringTemplate = AdtPlugin.readEmbeddedTextFile(TEMPLATE_STRING);
            Set<String> stringNames = strings.keySet();
            StringBuilder stringNodes = new StringBuilder();
            for (String key : stringNames) {
                String value = strings.get(key);
                String stringDef = stringTemplate.replace(PARAM_STRING_NAME, key);
                stringDef = stringDef.replace(PARAM_STRING_CONTENT, value);
                if (stringNodes.length() > 0) {
                    stringNodes.append("\n");
                }
                stringNodes.append(stringDef);
            }
            stringDefinitionTemplate = stringDefinitionTemplate.replace(PH_STRINGS,
                                                                        stringNodes.toString());
            InputStream stream = new ByteArrayInputStream(
                    stringDefinitionTemplate.getBytes("UTF-8")); 
            file.create(stream, false , new SubProgressMonitor(monitor, 10));
        }
    }
    private void addIcon(IProject project, boolean legacy, IProgressMonitor monitor)
            throws CoreException {
        if (legacy) { 
            IFile file = project.getFile(RES_DIRECTORY + AndroidConstants.WS_SEP
                    + DRAWABLE_DIRECTORY + AndroidConstants.WS_SEP + PROJECT_ICON);
            if (!file.exists()) {
                addFile(file, AdtPlugin.readEmbeddedFile(TEMPLATES_DIRECTORY + ICON_MDPI), monitor);
            }
        } else {
            IFile file;
            file = project.getFile(RES_DIRECTORY + AndroidConstants.WS_SEP
                    + DRAWABLE_HDPI_DIRECTORY + AndroidConstants.WS_SEP + PROJECT_ICON);
            if (!file.exists()) {
                addFile(file, AdtPlugin.readEmbeddedFile(TEMPLATES_DIRECTORY + ICON_HDPI), monitor);
            }
            file = project.getFile(RES_DIRECTORY + AndroidConstants.WS_SEP
                    + DRAWABLE_MDPI_DIRECTORY + AndroidConstants.WS_SEP + PROJECT_ICON);
            if (!file.exists()) {
                addFile(file, AdtPlugin.readEmbeddedFile(TEMPLATES_DIRECTORY + ICON_MDPI), monitor);
            }
            file = project.getFile(RES_DIRECTORY + AndroidConstants.WS_SEP
                    + DRAWABLE_LDPI_DIRECTORY + AndroidConstants.WS_SEP + PROJECT_ICON);
            if (!file.exists()) {
                addFile(file, AdtPlugin.readEmbeddedFile(TEMPLATES_DIRECTORY + ICON_LDPI), monitor);
            }
        }
    }
    private void addFile(IFile dest, byte[] source, IProgressMonitor monitor) throws CoreException {
        if (source != null) {
            InputStream stream = new ByteArrayInputStream(source);
            dest.create(stream, false , new SubProgressMonitor(monitor, 10));
        }
    }
    private void addSampleCode(IProject project, String sourceFolder,
            Map<String, Object> parameters, Map<String, String> dictionary,
            IProgressMonitor monitor) throws CoreException, IOException {
        IFolder pkgFolder = project.getFolder(sourceFolder);
        String packageName = (String) parameters.get(PARAM_PACKAGE);
        String activityName = (String) parameters.get(PARAM_ACTIVITY);
        Map<String, Object> java_activity_parameters = parameters;
        if (activityName != null) {
            if (activityName.indexOf('.') >= 0) {
                packageName += "." + activityName; 
                int pos = packageName.lastIndexOf('.');
                activityName = packageName.substring(pos + 1);
                packageName = packageName.substring(0, pos);
                java_activity_parameters = new HashMap<String, Object>(parameters);
                java_activity_parameters.put(PARAM_PACKAGE, packageName);
                java_activity_parameters.put(PARAM_ACTIVITY, activityName);
            }
        }
        String[] components = packageName.split(AndroidConstants.RE_DOT);
        for (String component : components) {
            pkgFolder = pkgFolder.getFolder(component);
            if (!pkgFolder.exists()) {
                pkgFolder.create(true , true ,
                        new SubProgressMonitor(monitor, 10));
            }
        }
        if (activityName != null) {
            String activityJava = activityName + AndroidConstants.DOT_JAVA;
            IFile file = pkgFolder.getFile(activityJava);
            if (!file.exists()) {
                copyFile(JAVA_ACTIVITY_TEMPLATE, file, java_activity_parameters, monitor);
            }
        }
        IFolder layoutfolder = project.getFolder(RES_DIRECTORY).getFolder(LAYOUT_DIRECTORY);
        IFile file = layoutfolder.getFile(MAIN_LAYOUT_XML);
        if (!file.exists()) {
            copyFile(LAYOUT_TEMPLATE, file, parameters, monitor);
            if (activityName != null) {
                dictionary.put(STRING_HELLO_WORLD, "Hello World, " + activityName + "!");
            } else {
                dictionary.put(STRING_HELLO_WORLD, "Hello World!");
            }
        }
    }
    private void setupSourceFolder(IJavaProject javaProject, String sourceFolder,
            IProgressMonitor monitor) throws JavaModelException {
        IProject project = javaProject.getProject();
        IFolder srcFolder = project.getFolder(sourceFolder);
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        entries = removeSourceClasspath(entries, srcFolder);
        entries = removeSourceClasspath(entries, srcFolder.getParent());
        entries = ProjectHelper.addEntryToClasspath(entries,
                JavaCore.newSourceEntry(srcFolder.getFullPath()));
        javaProject.setRawClasspath(entries, new SubProgressMonitor(monitor, 10));
    }
    private IClasspathEntry[] removeSourceClasspath(IClasspathEntry[] entries, IContainer folder) {
        if (folder == null) {
            return entries;
        }
        IClasspathEntry source = JavaCore.newSourceEntry(folder.getFullPath());
        int n = entries.length;
        for (int i = n - 1; i >= 0; i--) {
            if (entries[i].equals(source)) {
                IClasspathEntry[] newEntries = new IClasspathEntry[n - 1];
                if (i > 0) System.arraycopy(entries, 0, newEntries, 0, i);
                if (i < n - 1) System.arraycopy(entries, i + 1, newEntries, i, n - i - 1);
                n--;
                entries = newEntries;
            }
        }
        return entries;
    }
    private void copyFile(String resourceFilename, IFile destFile,
            Map<String, Object> parameters, IProgressMonitor monitor)
            throws CoreException, IOException {
        String template = AdtPlugin.readEmbeddedTextFile(
                TEMPLATES_DIRECTORY + resourceFilename);
        template = replaceParameters(template, parameters);
        InputStream stream = new ByteArrayInputStream(template.getBytes("UTF-8")); 
        destFile.create(stream, false , new SubProgressMonitor(monitor, 10));
    }
    private void setImageDescriptor() {
        ImageDescriptor desc = AdtPlugin.getImageDescriptor(PROJECT_LOGO_LARGE);
        setDefaultPageImageDescriptor(desc);
    }
    private String replaceParameters(String str, Map<String, Object> parameters) {
        if (parameters == null) {
            AdtPlugin.log(IStatus.ERROR,
                    "NPW replace parameters: null parameter map. String: '%s'", str);  
            return str;
        } else if (str == null) {
            AdtPlugin.log(IStatus.ERROR,
                    "NPW replace parameters: null template string");  
            return str;
        }
        for (Entry<String, Object> entry : parameters.entrySet()) {
            if (entry != null && entry.getValue() instanceof String) {
                Object value = entry.getValue();
                if (value == null) {
                    AdtPlugin.log(IStatus.ERROR,
                    "NPW replace parameters: null value for key '%s' in template '%s'",  
                    entry.getKey(),
                    str);
                } else {
                    str = str.replaceAll(entry.getKey(), (String) value);
                }
            }
        }
        return str;
    }
}
