public class AdtPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "com.android.ide.eclipse.adt"; 
    private static AdtPlugin sPlugin;
    private static Image sAndroidLogo;
    private static ImageDescriptor sAndroidLogoDesc;
    private MessageConsole mAndroidConsole;
    private MessageConsoleStream mAndroidConsoleStream;
    private MessageConsoleStream mAndroidConsoleErrorStream;
    private ImageLoader mLoader;
    private Color mRed;
    private LoadStatus mSdkIsLoaded = LoadStatus.LOADING;
    private final ArrayList<IJavaProject> mPostLoadProjectsToResolve =
            new ArrayList<IJavaProject>();
    private final ArrayList<IJavaProject> mPostLoadProjectsToCheck = new ArrayList<IJavaProject>();
    private GlobalProjectMonitor mResourceMonitor;
    private ArrayList<ITargetChangeListener> mTargetChangeListeners =
            new ArrayList<ITargetChangeListener>();
    protected boolean mSdkIsLoading;
    private static final class AndroidPrintStream extends PrintStream {
        private IProject mProject;
        private String mPrefix;
        public AndroidPrintStream(IProject project, String prefix, OutputStream stream) {
            super(stream);
            mProject = project;
        }
        @Override
        public void println(String message) {
            String tag = getMessageTag(mProject != null ? mProject.getName() : null);
            print(tag);
            print(' ');
            if (mPrefix != null) {
                print(mPrefix);
            }
            super.println(message);
        }
    }
    public static abstract class CheckSdkErrorHandler {
        public abstract boolean handleError(String message);
        public abstract boolean handleWarning(String message);
    }
    public AdtPlugin() {
        sPlugin = this;
    }
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Display display = getDisplay();
        mAndroidConsole = new MessageConsole("Android", null); 
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(
                new IConsole[] { mAndroidConsole });
        mAndroidConsoleStream = mAndroidConsole.newMessageStream();
        mAndroidConsoleErrorStream = mAndroidConsole.newMessageStream();
        mRed = new Color(display, 0xFF, 0x00, 0x00);
        display.asyncExec(new Runnable() {
            public void run() {
                mAndroidConsoleErrorStream.setColor(mRed);
            }
        });
        DdmConsole.setConsole(new IDdmConsole() {
            public void printErrorToConsole(String message) {
                AdtPlugin.printErrorToConsole((String)null, message);
            }
            public void printErrorToConsole(String[] messages) {
                AdtPlugin.printErrorToConsole((String)null, (Object[])messages);
            }
            public void printToConsole(String message) {
                AdtPlugin.printToConsole((String)null, message);
            }
            public void printToConsole(String[] messages) {
                AdtPlugin.printToConsole((String)null, (Object[])messages);
            }
        });
        AdtPrefs.init(getPreferenceStore());
        Preferences prefs = getPluginPreferences();
        prefs.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                AdtPrefs.getPrefs().loadValues(event);
                if (AdtPrefs.PREFS_SDK_DIR.equals(event.getProperty())) {
                    DdmsPlugin.setAdb(getOsAbsoluteAdb(), true );
                    if (checkSdkLocationAndId()) {
                        reparseSdk();
                    }
                }
            }
        });
        AdtPrefs.getPrefs().loadValues(null );
        final boolean isSdkLocationValid = checkSdkLocationAndId();
        mLoader = new ImageLoader(this);
        String osSdkLocation = AdtPrefs.getPrefs().getOsSdkFolder();
        if (osSdkLocation.length() > 0) {
            DdmsPlugin.setAdb(getOsAbsoluteAdb(), true);
        }
        DdmsPlugin.setRunningAppDebugLauncher(new DdmsPlugin.IDebugLauncher() {
            public boolean debug(String appName, int port) {
                IProject project = ProjectHelper.findAndroidProjectByAppName(appName);
                if (project != null) {
                    AndroidLaunchController.debugRunningApp(project, port);
                    return true;
                } else {
                    return false;
                }
            }
        });
        StackTracePanel.setSourceRevealer(new ISourceRevealer() {
            public void reveal(String applicationName, String className, int line) {
                IProject project = ProjectHelper.findAndroidProjectByAppName(applicationName);
                if (project != null) {
                    BaseProjectHelper.revealSource(project, className, line);
                }
            }
        });
        ExportHelper.setCallback(new IExportCallback() {
            public void startExportWizard(IProject project) {
                StructuredSelection selection = new StructuredSelection(project);
                ExportWizard wizard = new ExportWizard();
                wizard.init(PlatformUI.getWorkbench(), selection);
                WizardDialog dialog = new WizardDialog(getDisplay().getActiveShell(),
                        wizard);
                dialog.open();
            }
        });
        startEditors();
        Job pingJob = createPingUsageServerJob();
        pingJob.addJobChangeListener(new JobChangeAdapter() {
           @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                if (isSdkLocationValid) {
                    parseSdkContent();
                }
            }
        });
        pingJob.setPriority(Job.BUILD);
        pingJob.schedule(2000 );
    }
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        stopEditors();
        mRed.dispose();
        synchronized (AdtPlugin.class) {
            sPlugin = null;
        }
    }
    public static synchronized ImageLoader getImageLoader() {
        if (sPlugin != null) {
            return sPlugin.mLoader;
        }
        return null;
    }
    public static synchronized AdtPlugin getDefault() {
        return sPlugin;
    }
    public static Display getDisplay() {
        IWorkbench bench = null;
        synchronized (AdtPlugin.class) {
            bench = sPlugin.getWorkbench();
        }
        if (bench != null) {
            return bench.getDisplay();
        }
        return null;
    }
    public static String getOsRelativeAdb() {
        return SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_ADB;
    }
    public static String getOsRelativeZipAlign() {
        return SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_ZIPALIGN;
    }
    public static String getOsRelativeEmulator() {
        return SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_EMULATOR;
    }
    public static String getOsAbsoluteAdb() {
        return getOsSdkFolder() + getOsRelativeAdb();
    }
    public static String getOsAbsoluteZipAlign() {
        return getOsSdkFolder() + getOsRelativeZipAlign();
    }
    public static String getOsAbsoluteTraceview() {
        return getOsSdkFolder() + SdkConstants.OS_SDK_TOOLS_FOLDER +
                AndroidConstants.FN_TRACEVIEW;
    }
    public static String getOsAbsoluteEmulator() {
        return getOsSdkFolder() + getOsRelativeEmulator();
    }
    public static String getUrlDoc() {
        return ProjectHelper.getJavaDocPath(
                getOsSdkFolder() + AndroidConstants.WS_JAVADOC_FOLDER_LEAF);
    }
    public static synchronized String getOsSdkFolder() {
        if (sPlugin == null) {
            return null;
        }
        return AdtPrefs.getPrefs().getOsSdkFolder();
    }
    public static String getOsSdkToolsFolder() {
        return getOsSdkFolder() + SdkConstants.OS_SDK_TOOLS_FOLDER;
    }
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    public static String readEmbeddedTextFile(String filepath) {
        try {
            InputStream is = readEmbeddedFileAsStream(filepath);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder total = new StringBuilder(reader.readLine());
                while ((line = reader.readLine()) != null) {
                    total.append('\n');
                    total.append(line);
                }
                return total.toString();
            }
        } catch (IOException e) {
            AdtPlugin.log(e, "Failed to read text file '%s'", filepath);  
        }
        return null;
    }
    public static byte[] readEmbeddedFile(String filepath) {
        try {
            InputStream is = readEmbeddedFileAsStream(filepath);
            if (is != null) {
                BufferedInputStream stream = new BufferedInputStream(is);
                int avail = stream.available();
                byte[] buffer = new byte[avail];
                stream.read(buffer);
                return buffer;
            }
        } catch (IOException e) {
            AdtPlugin.log(e, "Failed to read binary file '%s'", filepath);  
        }
        return null;
    }
    public static InputStream readEmbeddedFileAsStream(String filepath) {
        try {
            URL url = getEmbeddedFileUrl(AndroidConstants.WS_SEP + filepath);
            if (url != null) {
                return url.openStream();
            }
        } catch (MalformedURLException e) {
            AdtPlugin.log(e, "Failed to read stream '%s'", filepath);  
        } catch (IOException e) {
            AdtPlugin.log(e, "Failed to read stream '%s'", filepath);  
        }
        return null;
    }
    public static URL getEmbeddedFileUrl(String filepath) {
        Bundle bundle = null;
        synchronized (AdtPlugin.class) {
            if (sPlugin != null) {
                bundle = sPlugin.getBundle();
            } else {
                AdtPlugin.log(IStatus.WARNING, "ADT Plugin is missing");    
                return null;
            }
        }
        String path = filepath;
        if (!path.startsWith(AndroidConstants.WS_SEP)) {
            path = AndroidConstants.WS_SEP + path;
        }
        URL url = bundle.getEntry(path);
        if (url == null) {
            AdtPlugin.log(IStatus.INFO, "Bundle file URL not found at path '%s'", path); 
        }
        return url;
    }
    public final static void displayError(final String title, final String message) {
        final Display display = getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                Shell shell = display.getActiveShell();
                MessageDialog.openError(shell, title, message);
            }
        });
    }
    public final static void displayWarning(final String title, final String message) {
        final Display display = getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                Shell shell = display.getActiveShell();
                MessageDialog.openWarning(shell, title, message);
            }
        });
    }
    public final static boolean displayPrompt(final String title, final String message) {
        final Display display = getDisplay();
        final boolean[] result = new boolean[1];
        display.syncExec(new Runnable() {
            public void run() {
                Shell shell = display.getActiveShell();
                result[0] = MessageDialog.openQuestion(shell, title, message);
            }
        });
        return result[0];
    }
    public static void log(int severity, String format, Object ... args) {
        String message = String.format(format, args);
        Status status = new Status(severity, PLUGIN_ID, message);
        getDefault().getLog().log(status);
    }
    public static void log(Throwable exception, String format, Object ... args) {
        String message = String.format(format, args);
        Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
        getDefault().getLog().log(status);
    }
    public static synchronized void logAndPrintError(Throwable exception, String tag,
            String format, Object ... args) {
        if (sPlugin != null) {
            String message = String.format(format, args);
            Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
            getDefault().getLog().log(status);
            printToStream(sPlugin.mAndroidConsoleErrorStream, tag, message);
            showAndroidConsole();
        }
    }
    public static synchronized void printErrorToConsole(String tag, Object... objects) {
        if (sPlugin != null) {
            printToStream(sPlugin.mAndroidConsoleErrorStream, tag, objects);
            showAndroidConsole();
        }
    }
    public static void printErrorToConsole(Object... objects) {
        printErrorToConsole((String)null, objects);
    }
    public static void printErrorToConsole(IProject project, Object... objects) {
        String tag = project != null ? project.getName() : null;
        printErrorToConsole(tag, objects);
    }
    public static synchronized void printBuildToConsole(BuildVerbosity level, IProject project,
            Object... objects) {
        if (sPlugin != null) {
            if (level.getLevel() <= AdtPrefs.getPrefs().getBuildVerbosity().getLevel()) {
                String tag = project != null ? project.getName() : null;
                printToStream(sPlugin.mAndroidConsoleStream, tag, objects);
            }
        }
    }
    public static synchronized void printToConsole(String tag, Object... objects) {
        if (sPlugin != null) {
            printToStream(sPlugin.mAndroidConsoleStream, tag, objects);
        }
    }
    public static void printToConsole(IProject project, Object... objects) {
        String tag = project != null ? project.getName() : null;
        printToConsole(tag, objects);
    }
    public static void showAndroidConsole() {
        EclipseUiHelper.showView(IConsoleConstants.ID_CONSOLE_VIEW, true);
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(
                AdtPlugin.getDefault().getAndroidConsole());
    }
    public static synchronized PrintStream getOutPrintStream(IProject project, String prefix) {
        if (sPlugin != null) {
            return new AndroidPrintStream(project, prefix, sPlugin.mAndroidConsoleStream);
        }
        return null;
    }
    public static synchronized PrintStream getErrPrintStream(IProject project, String prefix) {
        if (sPlugin != null) {
            return new AndroidPrintStream(project, prefix, sPlugin.mAndroidConsoleErrorStream);
        }
        return null;
    }
    public final LoadStatus getSdkLoadStatus() {
        synchronized (Sdk.getLock()) {
            return mSdkIsLoaded;
        }
    }
    public final void setProjectToResolve(IJavaProject javaProject) {
        synchronized (Sdk.getLock()) {
            mPostLoadProjectsToResolve.add(javaProject);
        }
    }
    public final void setProjectToCheck(IJavaProject javaProject) {
        synchronized (Sdk.getLock()) {
            mPostLoadProjectsToCheck.add(javaProject);
        }
    }
    private boolean checkSdkLocationAndId() {
        String sdkLocation = AdtPrefs.getPrefs().getOsSdkFolder();
        if (sdkLocation == null || sdkLocation.length() == 0) {
            displayError(Messages.Dialog_Title_SDK_Location, Messages.SDK_Not_Setup);
            return false;
        }
        return checkSdkLocationAndId(sdkLocation, new CheckSdkErrorHandler() {
            @Override
            public boolean handleError(String message) {
                AdtPlugin.displayError(Messages.Dialog_Title_SDK_Location,
                        String.format(Messages.Error_Check_Prefs, message));
                return false;
            }
            @Override
            public boolean handleWarning(String message) {
                AdtPlugin.displayWarning(Messages.Dialog_Title_SDK_Location, message);
                return true;
            }
        });
    }
    public boolean checkSdkLocationAndId(String osSdkLocation, CheckSdkErrorHandler errorHandler) {
        if (osSdkLocation.endsWith(File.separator) == false) {
            osSdkLocation = osSdkLocation + File.separator;
        }
        File osSdkFolder = new File(osSdkLocation);
        if (osSdkFolder.isDirectory() == false) {
            return errorHandler.handleError(
                    String.format(Messages.Could_Not_Find_Folder, osSdkLocation));
        }
        String osTools = osSdkLocation + SdkConstants.OS_SDK_TOOLS_FOLDER;
        File toolsFolder = new File(osTools);
        if (toolsFolder.isDirectory() == false) {
            return errorHandler.handleError(
                    String.format(Messages.Could_Not_Find_Folder_In_SDK,
                            SdkConstants.FD_TOOLS, osSdkLocation));
        }
        String[] filesToCheck = new String[] {
                osSdkLocation + getOsRelativeAdb(),
                osSdkLocation + getOsRelativeEmulator()
        };
        for (String file : filesToCheck) {
            if (checkFile(file) == false) {
                return errorHandler.handleError(String.format(Messages.Could_Not_Find, file));
            }
        }
        return VersionCheck.checkVersion(osSdkLocation, errorHandler);
    }
    private boolean checkFile(String osPath) {
        File file = new File(osPath);
        if (file.isFile() == false) {
            return false;
        }
        return true;
    }
    private Job createPingUsageServerJob() {
        Job job = new Job("Android SDK Ping") {  
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    pingUsageServer(); 
                    return Status.OK_STATUS;
                } catch (Throwable t) {
                    log(t, "pingUsageServer failed");       
                    return new Status(IStatus.ERROR, PLUGIN_ID,
                            "pingUsageServer failed", t);    
                }
            }
        };
        return job;
    }
    private void parseSdkContent() {
        Job job = new Job(Messages.AdtPlugin_Android_SDK_Content_Loader) {
            @SuppressWarnings("unchecked")
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (mSdkIsLoading) {
                        return new Status(IStatus.WARNING, PLUGIN_ID,
                                "An Android SDK is already being loaded. Please try again later.");
                    }
                    mSdkIsLoading = true;
                    SubMonitor progress = SubMonitor.convert(monitor,
                            "Initialize SDK Manager", 100);
                    Sdk sdk = Sdk.loadSdk(AdtPrefs.getPrefs().getOsSdkFolder());
                    if (sdk != null) {
                        ArrayList<IJavaProject> list = new ArrayList<IJavaProject>();
                        synchronized (Sdk.getLock()) {
                            mSdkIsLoaded = LoadStatus.LOADED;
                            progress.setTaskName("Check Projects");
                            for (IJavaProject javaProject : mPostLoadProjectsToResolve) {
                                IProject iProject = javaProject.getProject();
                                if (iProject.isOpen()) {
                                    sdk.loadTarget(Sdk.getProjectState(iProject));
                                    list.add(javaProject);
                                }
                            }
                            mPostLoadProjectsToResolve.clear();
                        }
                        AndroidClasspathContainerInitializer.checkProjectsCache(
                                mPostLoadProjectsToCheck);
                        list.addAll(mPostLoadProjectsToCheck);
                        if (list.size() > 0) {
                            IJavaProject[] array = list.toArray(
                                    new IJavaProject[list.size()]);
                            AndroidClasspathContainerInitializer.updateProjects(array);
                        }
                        progress.worked(10);
                    } else {
                        synchronized (Sdk.getLock()) {
                            mSdkIsLoaded = LoadStatus.FAILED;
                        }
                    }
                    progress.setTaskName("Refresh UI");
                    progress.setWorkRemaining(mTargetChangeListeners.size());
                    final List<ITargetChangeListener> listeners =
                        (List<ITargetChangeListener>)mTargetChangeListeners.clone();
                    final SubMonitor progress2 = progress;
                    AdtPlugin.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            for (ITargetChangeListener listener : listeners) {
                                try {
                                    listener.onSdkLoaded();
                                } catch (Exception e) {
                                    AdtPlugin.log(e, "Failed to update a TargetChangeListener."); 
                                } finally {
                                    progress2.worked(1);
                                }
                            }
                        }
                    });
                } catch (Throwable t) {
                    log(t, "Unknown exception in parseSdkContent.");    
                    return new Status(IStatus.ERROR, PLUGIN_ID,
                            "parseSdkContent failed", t);               
                } finally {
                    mSdkIsLoading = false;
                    if (monitor != null) {
                        monitor.done();
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.BUILD); 
        job.schedule();
    }
    public MessageConsole getAndroidConsole() {
        return mAndroidConsole;
    }
    public void startEditors() {
        sAndroidLogoDesc = imageDescriptorFromPlugin(AdtPlugin.PLUGIN_ID,
                "/icons/android.png"); 
        sAndroidLogo = sAndroidLogoDesc.createImage();
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        mResourceMonitor = GlobalProjectMonitor.startMonitoring(ws);
        if (mResourceMonitor != null) {
            try {
                setupDefaultEditor(mResourceMonitor);
                ResourceManager.setup(mResourceMonitor);
            } catch (Throwable t) {
                log(t, "ResourceManager.setup failed"); 
            }
        }
    }
    public void stopEditors() {
        sAndroidLogo.dispose();
        IconFactory.getInstance().Dispose();
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        GlobalProjectMonitor.stopMonitoring(ws);
        mRed.dispose();
    }
    public static Image getAndroidLogo() {
        return sAndroidLogo;
    }
    public static ImageDescriptor getAndroidLogoDesc() {
        return sAndroidLogoDesc;
    }
    public GlobalProjectMonitor getResourceMonitor() {
        return mResourceMonitor;
    }
    public void setupDefaultEditor(GlobalProjectMonitor monitor) {
        monitor.addFileListener(new IFileListener() {
            private static final String UNKNOWN_EDITOR = "unknown-editor"; 
            public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind) {
                if (AndroidConstants.EXT_XML.equals(file.getFileExtension())) {
                    if (file.getFullPath().segmentCount() == 4) {
                        String segment = file.getFullPath().segment(1);
                        if (segment.equalsIgnoreCase(SdkConstants.FD_RESOURCES)) {
                            ProjectResources resources = ResourceManager.getInstance().
                                getProjectResources(file.getProject());
                            if (resources == null) {
                                log(IStatus.INFO,
                                        "getProjectResources failed for path %1$s in project %2$s", 
                                        file.getFullPath().toOSString(),
                                        file.getProject().getName());
                                return;
                            }
                            ResourceFolder resFolder = resources.getResourceFolder(
                                (IFolder)file.getParent());
                            if (resFolder != null) {
                                if (kind == IResourceDelta.ADDED) {
                                    resourceAdded(file, resFolder.getType());
                                } else if (kind == IResourceDelta.CHANGED) {
                                    resourceChanged(file, resFolder.getType());
                                }
                            } else {
                                IEditorDescriptor desc = IDE.getDefaultEditor(file);
                                String editorId = desc.getId();
                                if (editorId.startsWith(AndroidConstants.EDITORS_NAMESPACE)) {
                                    IDE.setDefaultEditor(file, null);
                                }
                            }
                        }
                    }
                }
            }
            private void resourceAdded(IFile file, ResourceFolderType type) {
                if (type == ResourceFolderType.LAYOUT) {
                    IDE.setDefaultEditor(file, LayoutEditor.ID);
                } else if (type == ResourceFolderType.DRAWABLE
                        || type == ResourceFolderType.VALUES) {
                    IDE.setDefaultEditor(file, ResourcesEditor.ID);
                } else if (type == ResourceFolderType.MENU) {
                    IDE.setDefaultEditor(file, MenuEditor.ID);
                } else if (type == ResourceFolderType.XML) {
                    if (XmlEditor.canHandleFile(file)) {
                        IDE.setDefaultEditor(file, XmlEditor.ID);
                    } else {
                        QualifiedName qname = new QualifiedName(
                                AdtPlugin.PLUGIN_ID,
                                UNKNOWN_EDITOR);
                        try {
                            file.setPersistentProperty(qname, "1"); 
                        } catch (CoreException e) {
                        }
                    }
                }
            }
            private void resourceChanged(IFile file, ResourceFolderType type) {
                if (type == ResourceFolderType.XML) {
                    IEditorDescriptor ed = IDE.getDefaultEditor(file);
                    if (ed == null || ed.getId() != XmlEditor.ID) {
                        QualifiedName qname = new QualifiedName(
                                AdtPlugin.PLUGIN_ID,
                                UNKNOWN_EDITOR);
                        String prop = null;
                        try {
                            prop = file.getPersistentProperty(qname);
                        } catch (CoreException e) {
                        }
                        if (prop != null && XmlEditor.canHandleFile(file)) {
                            try {
                                file.setPersistentProperty(qname, null);
                                IWorkbench wb = PlatformUI.getWorkbench();
                                IWorkbenchWindow win = wb == null ? null :
                                                       wb.getActiveWorkbenchWindow();
                                IWorkbenchPage page = win == null ? null :
                                                      win.getActivePage();
                                IEditorPart oldEditor = page == null ? null :
                                                        page.findEditor(new FileEditorInput(file));
                                if (page != null &&
                                        oldEditor != null &&
                                        AdtPlugin.displayPrompt("Android XML Editor",
                                            String.format("The file you just saved as been recognized as a file that could be better handled using the Android XML Editor. Do you want to edit '%1$s' using the Android XML editor instead?",
                                                    file.getFullPath()))) {
                                    IDE.setDefaultEditor(file, XmlEditor.ID);
                                    IEditorPart newEditor = page.openEditor(
                                            new FileEditorInput(file),
                                            XmlEditor.ID,
                                            true, 
                                            IWorkbenchPage.MATCH_NONE);
                                    if (newEditor != null) {
                                        page.closeEditor(oldEditor, true );
                                    }
                                }
                            } catch (CoreException e) {
                            }
                        }
                    }
                }
            }
        }, IResourceDelta.ADDED | IResourceDelta.CHANGED);
    }
    public void addTargetListener(ITargetChangeListener listener) {
        mTargetChangeListeners.add(listener);
    }
    public void removeTargetListener(ITargetChangeListener listener) {
        mTargetChangeListeners.remove(listener);
    }
    @SuppressWarnings("unchecked")
    public void updateTargetListeners(final IProject project) {
        final List<ITargetChangeListener> listeners =
            (List<ITargetChangeListener>)mTargetChangeListeners.clone();
        AdtPlugin.getDisplay().asyncExec(new Runnable() {
            public void run() {
                for (ITargetChangeListener listener : listeners) {
                    try {
                        listener.onProjectTargetChange(project);
                    } catch (Exception e) {
                        AdtPlugin.log(e, "Failed to update a TargetChangeListener.");  
                    }
                }
            }
        });
    }
    @SuppressWarnings("unchecked")
    public void updateTargetListeners(final IAndroidTarget target) {
        final List<ITargetChangeListener> listeners =
            (List<ITargetChangeListener>)mTargetChangeListeners.clone();
        AdtPlugin.getDisplay().asyncExec(new Runnable() {
            public void run() {
                for (ITargetChangeListener listener : listeners) {
                    try {
                        listener.onTargetLoaded(target);
                    } catch (Exception e) {
                        AdtPlugin.log(e, "Failed to update a TargetChangeListener.");  
                    }
                }
            }
        });
    }
    public static synchronized OutputStream getErrorStream() {
        return sPlugin.mAndroidConsoleErrorStream;
    }
    private void pingUsageServer() {
        String versionString = (String) getBundle().getHeaders().get(
                Constants.BUNDLE_VERSION);
        Version version = new Version(versionString);
        versionString = String.format("%1$d.%2$d.%3$d", version.getMajor(), 
                version.getMinor(), version.getMicro());
        SdkStatsService.ping("adt", versionString, getDisplay()); 
    }
    public void reparseSdk() {
        synchronized (Sdk.getLock()) {
            IJavaProject[] androidProjects = BaseProjectHelper.getAndroidProjects(null );
            mPostLoadProjectsToResolve.addAll(Arrays.asList(androidProjects));
        }
        parseSdkContent();
    }
    public static synchronized void printToStream(MessageConsoleStream stream, String tag,
            Object... objects) {
        String dateTag = getMessageTag(tag);
        for (Object obj : objects) {
            stream.print(dateTag);
            stream.print(" "); 
            if (obj instanceof String) {
                stream.println((String)obj);
            } else if (obj == null) {
                stream.println("(null)");  
            } else {
                stream.println(obj.toString());
            }
        }
    }
    public static String getMessageTag(String tag) {
        Calendar c = Calendar.getInstance();
        if (tag == null) {
            return String.format(Messages.Console_Date_Tag, c);
        }
        return String.format(Messages.Console_Data_Project_Tag, c, tag);
    }
}
