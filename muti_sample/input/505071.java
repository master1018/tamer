public final class Sdk  {
    private final static Object sLock = new Object();
    private static Sdk sCurrentSdk = null;
    private final static HashMap<IProject, ProjectState> sProjectStateMap =
            new HashMap<IProject, ProjectState>();
    private final static class TargetLoadBundle {
        LoadStatus status;
        final HashSet<IJavaProject> projecsToReload = new HashSet<IJavaProject>();
    }
    private final SdkManager mManager;
    private final AvdManager mAvdManager;
    private final HashMap<IAndroidTarget, AndroidTargetData> mTargetDataMap =
        new HashMap<IAndroidTarget, AndroidTargetData>();
    private final HashMap<IAndroidTarget, TargetLoadBundle> mTargetDataStatusMap =
        new HashMap<IAndroidTarget, TargetLoadBundle>();
    private final String mDocBaseUrl;
    private final LayoutDeviceManager mLayoutDeviceManager = new LayoutDeviceManager();
    public interface ITargetChangeListener {
        void onProjectTargetChange(IProject changedProject);
        void onTargetLoaded(IAndroidTarget target);
        void onSdkLoaded();
    }
    public static abstract class TargetChangeListener implements ITargetChangeListener {
        public abstract IProject getProject();
        public abstract void reload();
        public void onProjectTargetChange(IProject changedProject) {
            if (changedProject != null && changedProject.equals(getProject())) {
                reload();
            }
        }
        public void onTargetLoaded(IAndroidTarget target) {
            IProject project = getProject();
            if (target != null && target.equals(Sdk.getCurrent().getTarget(project))) {
                reload();
            }
        }
        public void onSdkLoaded() {
        }
    }
    public static final Object getLock() {
        return sLock;
    }
    public static Sdk loadSdk(String sdkLocation) {
        synchronized (sLock) {
            if (sCurrentSdk != null) {
                sCurrentSdk.dispose();
                sCurrentSdk = null;
            }
            final ArrayList<String> logMessages = new ArrayList<String>();
            ISdkLog log = new ISdkLog() {
                public void error(Throwable throwable, String errorFormat, Object... arg) {
                    if (errorFormat != null) {
                        logMessages.add(String.format("Error: " + errorFormat, arg));
                    }
                    if (throwable != null) {
                        logMessages.add(throwable.getMessage());
                    }
                }
                public void warning(String warningFormat, Object... arg) {
                    logMessages.add(String.format("Warning: " + warningFormat, arg));
                }
                public void printf(String msgFormat, Object... arg) {
                    logMessages.add(String.format(msgFormat, arg));
                }
            };
            SdkManager manager = SdkManager.createManager(sdkLocation, log);
            if (manager != null) {
                AvdManager avdManager = null;
                try {
                    avdManager = new AvdManager(manager, log);
                } catch (AndroidLocationException e) {
                    log.error(e, "Error parsing the AVDs");
                }
                sCurrentSdk = new Sdk(manager, avdManager);
                return sCurrentSdk;
            } else {
                StringBuilder sb = new StringBuilder("Error Loading the SDK:\n");
                for (String msg : logMessages) {
                    sb.append('\n');
                    sb.append(msg);
                }
                AdtPlugin.displayError("Android SDK", sb.toString());
            }
            return null;
        }
    }
    public static Sdk getCurrent() {
        synchronized (sLock) {
            return sCurrentSdk;
        }
    }
    public String getSdkLocation() {
        return mManager.getLocation();
    }
    public String getDocumentationBaseUrl() {
        return mDocBaseUrl;
    }
    public IAndroidTarget[] getTargets() {
        return mManager.getTargets();
    }
    public IAndroidTarget getTargetFromHashString(String hash) {
        return mManager.getTargetFromHashString(hash);
    }
    public void initProject(IProject project, IAndroidTarget target) throws IOException {
        if (project == null || target == null) {
            return;
        }
        synchronized (sLock) {
            ProjectState state = getProjectState(project);
            ProjectProperties properties = null;
            if (state != null) {
                properties = state.getProperties();
            }
            if (properties == null) {
                IPath location = project.getLocation();
                if (location == null) {  
                    return;
                }
                properties = ProjectProperties.create(location.toOSString(), PropertyType.DEFAULT);
            }
            properties.setProperty(ProjectProperties.PROPERTY_TARGET, target.hashString());
            properties.save();
        }
    }
    public static ProjectState getProjectState(IProject project) {
        if (project == null) {
            return null;
        }
        synchronized (sLock) {
            ProjectState state = sProjectStateMap.get(project);
            if (state == null) {
                IPath location = project.getLocation();
                if (location == null) {  
                    return null;
                }
                ProjectProperties properties = ProjectProperties.load(location.toOSString(),
                        PropertyType.DEFAULT);
                if (properties == null) {
                    AdtPlugin.log(IStatus.ERROR, "Failed to load properties file for project '%s'",
                            project.getName());
                    return null;
                }
                state = new ProjectState(project, properties);
                sProjectStateMap.put(project, state);
                if (AdtPlugin.getDefault().getSdkLoadStatus() == LoadStatus.LOADED) {
                    sCurrentSdk.loadTarget(state);
                }
            }
            return state;
        }
    }
    public IAndroidTarget getTarget(IProject project) {
        if (project == null) {
            return null;
        }
        ProjectState state = getProjectState(project);
        if (state != null) {
            return state.getTarget();
        }
        return null;
    }
    public IAndroidTarget loadTarget(ProjectState state) {
        IAndroidTarget target = null;
        String hash = state.getTargetHashString();
        if (hash != null) {
            state.setTarget(target = getTargetFromHashString(hash));
        }
        return target;
    }
    public LoadStatus checkAndLoadTargetData(final IAndroidTarget target, IJavaProject project) {
        boolean loadData = false;
        synchronized (sLock) {
            TargetLoadBundle bundle = mTargetDataStatusMap.get(target);
            if (bundle == null) {
                bundle = new TargetLoadBundle();
                mTargetDataStatusMap.put(target,bundle);
                bundle.status = LoadStatus.LOADING;
                if (project != null) {
                    bundle.projecsToReload.add(project);
                }
                loadData = true;
            } else if (bundle.status == LoadStatus.LOADING) {
                if (project != null) {
                    bundle.projecsToReload.add(project);
                }
                return bundle.status;
            } else if (bundle.status == LoadStatus.LOADED || bundle.status == LoadStatus.FAILED) {
                return bundle.status;
            }
        }
        if (loadData) {
            Job job = new Job(String.format("Loading data for %1$s", target.getFullName())) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    AdtPlugin plugin = AdtPlugin.getDefault();
                    try {
                        IStatus status = new AndroidTargetParser(target).run(monitor);
                        IJavaProject[] javaProjectArray = null;
                        synchronized (sLock) {
                            TargetLoadBundle bundle = mTargetDataStatusMap.get(target);
                            if (status.getCode() != IStatus.OK) {
                                bundle.status = LoadStatus.FAILED;
                                bundle.projecsToReload.clear();
                            } else {
                                bundle.status = LoadStatus.LOADED;
                                javaProjectArray = bundle.projecsToReload.toArray(
                                        new IJavaProject[bundle.projecsToReload.size()]);
                                plugin.updateTargetListeners(target);
                            }
                        }
                        if (javaProjectArray != null) {
                            AndroidClasspathContainerInitializer.updateProjects(javaProjectArray);
                        }
                        return status;
                    } catch (Throwable t) {
                        synchronized (sLock) {
                            TargetLoadBundle bundle = mTargetDataStatusMap.get(target);
                            bundle.status = LoadStatus.FAILED;
                        }
                        AdtPlugin.log(t, "Exception in checkAndLoadTargetData.");    
                        return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                                String.format(
                                        "Parsing Data for %1$s failed", 
                                        target.hashString()),
                                t);
                    }
                }
            };
            job.setPriority(Job.BUILD); 
            job.schedule();
        }
        return LoadStatus.LOADING;
    }
    public AndroidTargetData getTargetData(IAndroidTarget target) {
        synchronized (sLock) {
            return mTargetDataMap.get(target);
        }
    }
    public AndroidTargetData getTargetData(IProject project) {
        synchronized (sLock) {
            IAndroidTarget target = getTarget(project);
            if (target != null) {
                return getTargetData(target);
            }
        }
        return null;
    }
    public AvdManager getAvdManager() {
        return mAvdManager;
    }
    public static AndroidVersion getDeviceVersion(IDevice device) {
        try {
            Map<String, String> props = device.getProperties();
            String apiLevel = props.get(IDevice.PROP_BUILD_API_LEVEL);
            if (apiLevel == null) {
                return null;
            }
            return new AndroidVersion(Integer.parseInt(apiLevel),
                    props.get((IDevice.PROP_BUILD_CODENAME)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public LayoutDeviceManager getLayoutDeviceManager() {
        return mLayoutDeviceManager;
    }
    public static Set<ProjectState> getMainProjectsFor(IProject project) {
        synchronized (sLock) {
            HashSet<ProjectState> list = new HashSet<ProjectState>();
            for (Entry<IProject, ProjectState> entry : sProjectStateMap.entrySet()) {
                if (project != entry.getKey()) {
                    LibraryState library = entry.getValue().getLibrary(project);
                    if (library != null) {
                        list.add(entry.getValue());
                    }
                }
            }
            HashSet<ProjectState> result = new HashSet<ProjectState>(list);
            for (ProjectState p : list) {
                if (p.isLibrary()) {
                    Set<ProjectState> set = getMainProjectsFor(p.getProject());
                    result.addAll(set);
                }
            }
            return result;
        }
    }
    private Sdk(SdkManager manager, AvdManager avdManager) {
        mManager = manager;
        mAvdManager = avdManager;
        GlobalProjectMonitor monitor = GlobalProjectMonitor.getMonitor();
        monitor.addProjectListener(mProjectListener);
        monitor.addFileListener(mFileListener, IResourceDelta.CHANGED | IResourceDelta.ADDED);
        mDocBaseUrl = getDocumentationBaseUrl(mManager.getLocation() +
                SdkConstants.OS_SDK_DOCS_FOLDER);
        mLayoutDeviceManager.loadDefaultAndUserDevices(mManager.getLocation());
        loadLayoutDevices();
        synchronized (sLock) {
            for (Entry<IProject, ProjectState> entry: sProjectStateMap.entrySet()) {
                entry.getValue().setTarget(
                        getTargetFromHashString(entry.getValue().getTargetHashString()));
            }
        }
    }
    private void dispose() {
        GlobalProjectMonitor monitor = GlobalProjectMonitor.getMonitor();
        monitor.removeProjectListener(mProjectListener);
        monitor.removeFileListener(mFileListener);
        synchronized (sLock) {
            for (Entry<IProject, ProjectState> entry: sProjectStateMap.entrySet()) {
                entry.getValue().setTarget(null);
            }
        }
    }
    void setTargetData(IAndroidTarget target, AndroidTargetData data) {
        synchronized (sLock) {
            mTargetDataMap.put(target, data);
        }
    }
    private String getDocumentationBaseUrl(String osDocsPath) {
        File f = new File(osDocsPath);
        if (f.isDirectory()) {
            try {
                String path = f.getAbsolutePath();
                if (File.separatorChar != '/') {
                    path = path.replace(File.separatorChar, '/');
                }
                URL url = new URL("file", null, "
                String result = url.toString();
                return result;
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }
    private void loadLayoutDevices() {
        IAndroidTarget[] targets = mManager.getTargets();
        for (IAndroidTarget target : targets) {
            if (target.isPlatform() == false) {
                File deviceXml = new File(target.getLocation(), SdkConstants.FN_DEVICES_XML);
                if (deviceXml.isFile()) {
                    mLayoutDeviceManager.parseAddOnLayoutDevice(deviceXml);
                }
            }
        }
        mLayoutDeviceManager.sealAddonLayoutDevices();
    }
    private IProjectListener mProjectListener = new IProjectListener() {
        public void projectClosed(IProject project) {
            onProjectRemoved(project, false );
        }
        public void projectDeleted(IProject project) {
            onProjectRemoved(project, true );
        }
        private void onProjectRemoved(IProject project, boolean deleted) {
            synchronized (sLock) {
                ProjectState state = sProjectStateMap.get(project);
                if (state != null) {
                    IAndroidTarget target = state.getTarget();
                    if (target != null) {
                        AndroidTargetData data = mTargetDataMap.get(target);
                        if (data != null) {
                            LayoutBridge bridge = data.getLayoutBridge();
                            if (bridge != null && bridge.status == LoadStatus.LOADED) {
                                bridge.bridge.clearCaches(project);
                            }
                        }
                    }
                    for (ProjectState projectState : sProjectStateMap.values()) {
                        LibraryState libState = projectState.getLibrary(project);
                        if (libState != null) {
                            libState.close();
                            unlinkLibrary(projectState, project, true );
                        }
                    }
                    if (deleted) {
                        disposeLibraryProject(project);
                    }
                    sProjectStateMap.remove(project);
                }
            }
        }
        public void projectOpened(IProject project) {
            onProjectOpened(project, true );
        }
        public void projectOpenedWithWorkspace(IProject project) {
            onProjectOpened(project, false );
        }
        private void onProjectOpened(IProject openedProject, boolean recompile) {
            ProjectState openedState = getProjectState(openedProject);
            if (openedState != null) {
                if (openedState.isMissingLibraries()) {
                    boolean foundLibrary = false;
                    synchronized (sLock) {
                        for (ProjectState projectState : sProjectStateMap.values()) {
                            if (projectState != openedState) {
                                LibraryState libState = openedState.needs(projectState);
                                if (libState != null) {
                                    foundLibrary = true;
                                    linkProjectAndLibrary(openedState, libState, null,
                                            true );
                                }
                            }
                        }
                    }
                    if (recompile && foundLibrary) {
                        recompile(openedState.getProject());
                    }
                }
                if (openedState.isLibrary()) {
                    setupLibraryProject(openedProject);
                    synchronized (sLock) {
                        for (ProjectState projectState : sProjectStateMap.values()) {
                            if (projectState != openedState && projectState.isMissingLibraries()) {
                                LibraryState libState = projectState.needs(openedState);
                                if (libState != null) {
                                    linkProjectAndLibrary(projectState, libState, null,
                                            true );
                                    if (recompile) {
                                        recompile(projectState.getProject());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        public void projectRenamed(IProject project, IPath from) {
            ProjectState renamedState = getProjectState(project);
            if (renamedState.isLibrary()) {
                disposeLibraryProject(from.lastSegment());
                synchronized (sLock) {
                    for (ProjectState projectState : sProjectStateMap.values()) {
                        if (projectState != renamedState && projectState.isMissingLibraries()) {
                            IPath oldRelativePath = makeRelativeTo(from,
                                    projectState.getProject().getFullPath());
                            IPath newRelativePath = makeRelativeTo(project.getFullPath(),
                                    projectState.getProject().getFullPath());
                            LibraryState libState = projectState.updateLibrary(
                                    oldRelativePath.toString(), newRelativePath.toString(),
                                    renamedState);
                            if (libState != null) {
                                linkProjectAndLibrary(projectState, libState, from,
                                        true );
                                recompile(projectState.getProject());
                            }
                        }
                    }
                }
            }
        }
    };
    private IFileListener mFileListener = new IFileListener() {
        public void fileChanged(final IFile file, IMarkerDelta[] markerDeltas, int kind) {
            if (SdkConstants.FN_DEFAULT_PROPERTIES.equals(file.getName()) &&
                    file.getParent() == file.getProject()) {
                Job job = new Job("Project Update") {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            IProject iProject = file.getProject();
                            ProjectState state = Sdk.getProjectState(iProject);
                            IAndroidTarget oldTarget = state.getTarget();
                            LibraryDifference diff = state.reloadProperties();
                            IAndroidTarget newTarget = loadTarget(state);
                            if (diff.hasDiff()) {
                                for (LibraryState removedState : diff.removed) {
                                    ProjectState removePState = removedState.getProjectState();
                                    if (removePState != null) {
                                        unlinkLibrary(state, removePState.getProject(),
                                                false );
                                    }
                                }
                                if (diff.added) {
                                    synchronized (sLock) {
                                        for (ProjectState projectState : sProjectStateMap.values()) {
                                            if (projectState != state) {
                                                LibraryState libState = state.needs(projectState);
                                                if (libState != null) {
                                                    linkProjectAndLibrary(state, libState, null,
                                                            false );
                                                }
                                            }
                                        }
                                    }
                                }
                                iProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                            }
                            if (newTarget != oldTarget) {
                                IJavaProject javaProject = BaseProjectHelper.getJavaProject(
                                        file.getProject());
                                if (javaProject != null) {
                                    AndroidClasspathContainerInitializer.updateProjects(
                                            new IJavaProject[] { javaProject });
                                }
                                AdtPlugin.getDefault().updateTargetListeners(iProject);
                            }
                        } catch (CoreException e) {
                        }
                        return Status.OK_STATUS;
                    }
                };
                job.setPriority(Job.BUILD); 
                job.schedule();
            }
        }
    };
    private void setupLibraryProject(IProject libProject) {
        IPathVariableManager pathVarMgr =
            ResourcesPlugin.getWorkspace().getPathVariableManager();
        IPath libPath = libProject.getLocation();
        final String libName = libProject.getName();
        final String varName = "_android_" + libName; 
        if (libPath.equals(pathVarMgr.getValue(varName)) == false) {
            try {
                pathVarMgr.setValue(varName, libPath);
            } catch (CoreException e) {
                String message = String.format(
                        "Unable to set linked path var '%1$s' for library %2$s", 
                        varName, libPath.toOSString());
                AdtPlugin.log(e, message);
            }
        }
    }
    private void disposeLibraryProject(IProject project) {
        disposeLibraryProject(project.getName());
    }
    private void disposeLibraryProject(String libName) {
        IPathVariableManager pathVarMgr =
            ResourcesPlugin.getWorkspace().getPathVariableManager();
        final String varName = "_android_" + libName; 
        try {
            pathVarMgr.setValue(varName, null );
        } catch (CoreException e) {
            String message = String.format("Unable to remove linked path var '%1$s'", 
                    varName);
            AdtPlugin.log(e, message);
        }
    }
    private void linkProjectAndLibrary(
            final ProjectState projectState,
            final LibraryState libraryState,
            final IPath previousLibraryPath,
            boolean doInJob) {
        final IJobRunnable jobRunnable = new IJobRunnable() {
            public IStatus run(IProgressMonitor monitor) {
                try {
                    IProject project = projectState.getProject();
                    IProject library = libraryState.getProjectState().getProject();
                    IProjectDescription projectDescription = project.getDescription();
                    IProject[] refs = projectDescription.getDynamicReferences();
                    if (refs.length > 0) {
                        ArrayList<IProject> list = new ArrayList<IProject>(Arrays.asList(refs));
                        if (previousLibraryPath != null) {
                            final int count = list.size();
                            for (int i = 0 ; i < count ; i++) {
                                if (list.get(i).getName().equals(
                                        previousLibraryPath.lastSegment())) {
                                    list.remove(i);
                                    break;
                                }
                            }
                        }
                        list.add(library);
                        projectDescription.setDynamicReferences(
                                list.toArray(new IProject[list.size()]));
                    } else {
                        projectDescription.setDynamicReferences(new IProject[] { library });
                    }
                    final String libName = library.getName();
                    final String varName = "_android_" + libName; 
                    IFolder libSrc = project.getFolder(libName);
                    String libSrcFolder = "src"; 
                    libSrc.createLink(new Path(varName + "/" + libSrcFolder), 
                            IResource.REPLACE, monitor);
                    IJavaProject javaProject = JavaCore.create(project);
                    IClasspathEntry[] entries = javaProject.getRawClasspath();
                    ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>(
                            Arrays.asList(entries));
                    IPath path = libSrc.getFullPath();
                    int count = list.size();
                    boolean foundMatch = false;
                    for (int i = 0 ; i < count ; i++) {
                        if (list.get(i).getPath().equals(path)) {
                            foundMatch = true;
                            break;
                        }
                    }
                    if (foundMatch == false) {
                        list.add(JavaCore.newSourceEntry(path));
                    }
                    if (previousLibraryPath != null) {
                        String oldLibName = previousLibraryPath.lastSegment();
                        IPath oldEntryPath = new Path("/").append(project.getName()).append(oldLibName);
                        count = list.size();
                        for (int i = 0 ; i < count ; i++) {
                            if (list.get(i).getPath().equals(oldEntryPath)) {
                                list.remove(i);
                                break;
                            }
                        }
                        IFolder oldLinkedFolder = project.getFolder(oldLibName);
                        oldLinkedFolder.delete(true, monitor);
                    }
                    javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]),
                            monitor);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    return e.getStatus();
                }
            }
        };
        if (doInJob) {
            Job job = new Job("Android Library link creation") { 
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    return jobRunnable.run(monitor);
                }
            };
            job.setPriority(Job.BUILD);
            job.schedule();
        } else {
            jobRunnable.run(new NullProgressMonitor());
        }
    }
    private void unlinkLibrary(final ProjectState projectState, final IProject libraryProject,
            boolean doInJob) {
        final IJobRunnable jobRunnable = new IJobRunnable() {
            public IStatus run(IProgressMonitor monitor) {
                try {
                    IProject project = projectState.getProject();
                    IProjectDescription projectDescription = project.getDescription();
                    IProject[] refs = projectDescription.getDynamicReferences();
                    if (refs.length > 0) {
                        ArrayList<IProject> list = new ArrayList<IProject>(Arrays.asList(refs));
                        final int count = list.size();
                        for (int i = 0 ; i < count ; i++) {
                            if (list.get(i).equals(libraryProject)) {
                                list.remove(i);
                                break;
                            }
                        }
                        projectDescription.setDynamicReferences(
                                list.toArray(new IProject[list.size()]));
                    }
                    IJavaProject javaProject = JavaCore.create(project);
                    IClasspathEntry[] entries = javaProject.getRawClasspath();
                    ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>(
                            Arrays.asList(entries));
                    String libName = libraryProject.getName();
                    IPath oldEntryPath = new Path("/").append(project.getName()).append(libName);
                    final int count = list.size();
                    for (int i = 0 ; i < count ; i++) {
                        if (list.get(i).getPath().equals(oldEntryPath)) {
                            list.remove(i);
                            break;
                        }
                    }
                    IFolder oldLinkedFolder = project.getFolder(libName);
                    oldLinkedFolder.delete(true, monitor);
                    javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]),
                            monitor);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    return e.getStatus();
                }
            }
        };
        if (doInJob) {
            Job job = new Job("Android Library unlinking") { 
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    return jobRunnable.run(monitor);
                }
            };
            job.setPriority(Job.BUILD);
            job.schedule();
        } else {
            jobRunnable.run(new NullProgressMonitor());
        }
    }
    private void recompile(final IProject project) {
        Job job = new Job("Project recompilation trigger") { 
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    project.build( IncrementalProjectBuilder.FULL_BUILD, null);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    return e.getStatus();
                }
            }
        };
        job.setPriority(Job.BUILD);
        job.schedule();
    }
    public static IPath makeRelativeTo(IPath target, IPath base) {
        if (target.getDevice() != base.getDevice() && (target.getDevice() == null ||
                !target.getDevice().equalsIgnoreCase(base.getDevice())))
            return target;
        int commonLength = target.matchingFirstSegments(base);
        final int differenceLength = base.segmentCount() - commonLength;
        final int newSegmentLength = differenceLength + target.segmentCount() - commonLength;
        if (newSegmentLength == 0)
            return Path.EMPTY;
        String[] newSegments = new String[newSegmentLength];
        Arrays.fill(newSegments, 0, differenceLength, ".."); 
        System.arraycopy(target.segments(), commonLength, newSegments,
                differenceLength, newSegmentLength - differenceLength);
        StringBuilder sb = new StringBuilder();
        for (String s : newSegments) {
            sb.append(s).append('/');
        }
        return new Path(null, sb.toString());
    }
}
