public class AndroidClasspathContainerInitializer extends ClasspathContainerInitializer {
    private final static String CONTAINER_ID =
        "com.android.ide.eclipse.adt.ANDROID_FRAMEWORK"; 
    private final static String PATH_SEPARATOR = "\u001C"; 
    private final static String PROPERTY_CONTAINER_CACHE = "androidContainerCache"; 
    private final static String PROPERTY_TARGET_NAME = "androidTargetCache"; 
    private final static String CACHE_VERSION = "01"; 
    private final static String CACHE_VERSION_SEP = CACHE_VERSION + PATH_SEPARATOR;
    private final static int CACHE_INDEX_JAR = 0;
    private final static int CACHE_INDEX_SRC = 1;
    private final static int CACHE_INDEX_DOCS_URI = 2;
    private final static int CACHE_INDEX_OPT_DOCS_URI = 3;
    private final static int CACHE_INDEX_ADD_ON_START = CACHE_INDEX_OPT_DOCS_URI;
    public AndroidClasspathContainerInitializer() {
    }
    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        if (CONTAINER_ID.equals(containerPath.toString())) {
            JavaCore.setClasspathContainer(new Path(CONTAINER_ID),
                    new IJavaProject[] { project },
                    new IClasspathContainer[] { allocateAndroidContainer(project) },
                    new NullProgressMonitor());
        }
    }
    public static IClasspathEntry getContainerEntry() {
        return JavaCore.newContainerEntry(new Path(CONTAINER_ID));
    }
    public static boolean checkPath(IPath path) {
        return CONTAINER_ID.equals(path.toString());
    }
    public static boolean updateProjects(IJavaProject[] androidProjects) {
        try {
            int projectCount = androidProjects.length;
            IClasspathContainer[] containers = new IClasspathContainer[projectCount];
            for (int i = 0 ; i < projectCount; i++) {
                containers[i] = allocateAndroidContainer(androidProjects[i]);
            }
            JavaCore.setClasspathContainer(
                    new Path(CONTAINER_ID),
                    androidProjects, containers, new NullProgressMonitor());
            return true;
        } catch (JavaModelException e) {
            return false;
        }
    }
    private static IClasspathContainer allocateAndroidContainer(IJavaProject javaProject) {
        final IProject iProject = javaProject.getProject();
        String markerMessage = null;
        boolean outputToConsole = true;
        try {
            AdtPlugin plugin = AdtPlugin.getDefault();
            synchronized (Sdk.getLock()) {
                boolean sdkIsLoaded = plugin.getSdkLoadStatus() == LoadStatus.LOADED;
                ProjectState state = Sdk.getProjectState(iProject);
                if (state == null) {
                    markerMessage = String.format(
                            "Project has no default.properties file! Edit the project properties to set one.");
                } else {
                    IAndroidTarget target = state.getTarget();
                    if (sdkIsLoaded && target != null) {
                        Sdk.getCurrent().checkAndLoadTargetData(target, null );
                        if (state.hasLibraries() &&
                                target.getProperty(
                                        SdkConstants.PROP_SDK_SUPPORT_LIBRARY, false) == false) {
                            AdtPlugin.printErrorToConsole(iProject, String.format(
                                    "Target '%1$s' does not support building project with libraries.",
                                    target.getFullName()));
                        }
                        String targetName = target.getClasspathName();
                        return new AndroidClasspathContainer(
                                createClasspathEntries(iProject, target, targetName),
                                new Path(CONTAINER_ID), targetName);
                    }
                    String hashString = state.getTargetHashString();
                    if (hashString == null || hashString.length() == 0) {
                        if (sdkIsLoaded) {
                            markerMessage = String.format(
                                    "Project has no target set. Edit the project properties to set one.");
                        }
                    } else if (sdkIsLoaded) {
                        markerMessage = String.format(
                                "Unable to resolve target '%s'", hashString);
                    } else {
                        AndroidClasspathContainer container = getContainerFromCache(iProject);
                        if (container == null) {
                            plugin.setProjectToResolve(javaProject);
                            markerMessage = String.format(
                                    "Unable to resolve target '%s' until the SDK is loaded.",
                                    hashString);
                            outputToConsole = false;
                        } else {
                            plugin.setProjectToCheck(javaProject);
                            return container;
                        }
                    }
                }
                return new IClasspathContainer() {
                    public IClasspathEntry[] getClasspathEntries() {
                        return new IClasspathEntry[0];
                    }
                    public String getDescription() {
                        return "Unable to get system library for the project";
                    }
                    public int getKind() {
                        return IClasspathContainer.K_DEFAULT_SYSTEM;
                    }
                    public IPath getPath() {
                        return null;
                    }
                };
            }
        } finally {
            if (markerMessage != null) {
                if (outputToConsole) {
                    AdtPlugin.printErrorToConsole(iProject, markerMessage);
                }
                try {
                    BaseProjectHelper.markProject(iProject, AndroidConstants.MARKER_TARGET,
                            markerMessage, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_HIGH);
                } catch (CoreException e) {
                    final String fmessage = markerMessage;
                    Job markerJob = new Job("Android SDK: Resolving error markers") {
                        @Override
                        protected IStatus run(IProgressMonitor monitor) {
                            try {
                                BaseProjectHelper.markProject(iProject,
                                        AndroidConstants.MARKER_TARGET,
                                        fmessage, IMarker.SEVERITY_ERROR,
                                        IMarker.PRIORITY_HIGH);
                            } catch (CoreException e2) {
                                return e2.getStatus();
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    markerJob.setPriority(Job.BUILD);
                    markerJob.schedule();
                }
            } else {
                try {
                    if (iProject.exists()) {
                        iProject.deleteMarkers(AndroidConstants.MARKER_TARGET, true,
                                IResource.DEPTH_INFINITE);
                    }
                } catch (CoreException ce) {
                    Job markerJob = new Job("Android SDK: Resolving error markers") {
                        @Override
                        protected IStatus run(IProgressMonitor monitor) {
                            try {
                                iProject.deleteMarkers(AndroidConstants.MARKER_TARGET, true,
                                        IResource.DEPTH_INFINITE);
                            } catch (CoreException e2) {
                                return e2.getStatus();
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    markerJob.setPriority(Job.BUILD);
                    markerJob.schedule();
                }
            }
        }
    }
    private static IClasspathEntry[] createClasspathEntries(IProject project,
            IAndroidTarget target, String targetName) {
        String[] paths = getTargetPaths(target);
        IClasspathEntry[] entries = createClasspathEntriesFromPaths(paths);
        StringBuilder sb = new StringBuilder(CACHE_VERSION);
        for (String p : paths) {
            sb.append(PATH_SEPARATOR);
            sb.append(p);
        }
        ProjectHelper.saveStringProperty(project, PROPERTY_CONTAINER_CACHE, sb.toString());
        ProjectHelper.saveStringProperty(project, PROPERTY_TARGET_NAME, targetName);
        return entries;
    }
    private static AndroidClasspathContainer getContainerFromCache(IProject project) {
        String cache = ProjectHelper.loadStringProperty(project, PROPERTY_CONTAINER_CACHE);
        String targetNameCache = ProjectHelper.loadStringProperty(project, PROPERTY_TARGET_NAME);
        if (cache == null || targetNameCache == null) {
            return null;
        }
        if (cache.startsWith(CACHE_VERSION_SEP) == false) {
            return null;
        }
        cache = cache.substring(CACHE_VERSION_SEP.length());
        String[] paths = cache.split(Pattern.quote(PATH_SEPARATOR));
        if (paths.length < 3 || paths.length == 4) {
            return null;
        }
        try {
            if (new File(paths[CACHE_INDEX_JAR]).exists() == false ||
                    new File(new URI(paths[CACHE_INDEX_DOCS_URI])).exists() == false) {
                return null;
            }
            if (paths.length > CACHE_INDEX_ADD_ON_START) {
                if (new File(new URI(paths[CACHE_INDEX_OPT_DOCS_URI])).exists() == false) {
                    return null;
                }
                for (int i = CACHE_INDEX_ADD_ON_START + 1; i < paths.length; i++) {
                    String path = paths[i];
                    if (path.length() > 0) {
                        File f = new File(path);
                        if (f.exists() == false) {
                            return null;
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            return null;
        }
        IClasspathEntry[] entries = createClasspathEntriesFromPaths(paths);
        return new AndroidClasspathContainer(entries,
                new Path(CONTAINER_ID), targetNameCache);
    }
    private static IClasspathEntry[] createClasspathEntriesFromPaths(String[] paths) {
        ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        IPath android_lib = new Path(paths[CACHE_INDEX_JAR]);
        IPath android_src = new Path(paths[CACHE_INDEX_SRC]);
        IClasspathAttribute cpAttribute = JavaCore.newClasspathAttribute(
                IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
                paths[CACHE_INDEX_DOCS_URI]);
        IAccessRule accessRule = JavaCore.newAccessRule(
                new Path("com/android/internal
    public static void checkProjectsCache(ArrayList<IJavaProject> projects) {
        Sdk currentSdk = Sdk.getCurrent();
        int i = 0;
        projectLoop: while (i < projects.size()) {
            IJavaProject javaProject = projects.get(i);
            IProject iProject = javaProject.getProject();
            if (iProject.isOpen() == false) {
                projects.remove(i);
                continue;
            }
            IAndroidTarget target = currentSdk.loadTarget(Sdk.getProjectState(iProject));
            if (target == null) {
                i++;
                continue;
            }
            String[] targetPaths = getTargetPaths(target);
            String cache = ProjectHelper.loadStringProperty(iProject, PROPERTY_CONTAINER_CACHE);
            if (cache == null) {
                i++;
                continue;
            }
            String[] cachedPaths = cache.split(Pattern.quote(PATH_SEPARATOR));
            if (cachedPaths.length < 3 || cachedPaths.length == 4) {
                i++;
                continue;
            }
            if (targetPaths.length != cachedPaths.length) {
                i++;
                continue;
            }
            if (new File(targetPaths[CACHE_INDEX_JAR]).equals(
                            new File(cachedPaths[CACHE_INDEX_JAR])) == false ||
                    new File(targetPaths[CACHE_INDEX_SRC]).equals(
                            new File(cachedPaths[CACHE_INDEX_SRC])) == false ||
                    new File(targetPaths[CACHE_INDEX_DOCS_URI]).equals(
                            new File(cachedPaths[CACHE_INDEX_DOCS_URI])) == false) {
                i++;
                continue;
            }
            if (cachedPaths.length > CACHE_INDEX_OPT_DOCS_URI) {
                if (new File(targetPaths[CACHE_INDEX_OPT_DOCS_URI]).equals(
                        new File(cachedPaths[CACHE_INDEX_OPT_DOCS_URI])) == false) {
                    i++;
                    continue;
                }
                targetLoop: for (int tpi = 4 ; tpi < targetPaths.length; tpi++) {
                    String targetPath = targetPaths[tpi];
                    for (int cpi = 4 ; cpi < cachedPaths.length; cpi++) {
                        if (new File(targetPath).equals(new File(cachedPaths[cpi]))) {
                            continue targetLoop;
                        }
                    }
                    i++;
                    continue projectLoop;
                }
            }
            projects.remove(i);
        }
    }
    private static String[] getTargetPaths(IAndroidTarget target) {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(target.getPath(IAndroidTarget.ANDROID_JAR));
        paths.add(target.getPath(IAndroidTarget.SOURCES));
        paths.add(AdtPlugin.getUrlDoc());
        IOptionalLibrary[] libraries = target.getOptionalLibraries();
        if (libraries != null) {
            String targetDocPath = target.getPath(IAndroidTarget.DOCS);
            if (targetDocPath != null) {
                paths.add(ProjectHelper.getJavaDocPath(targetDocPath));
            } else {
                paths.add("");
            }
            HashSet<String> visitedJars = new HashSet<String>();
            for (IOptionalLibrary library : libraries) {
                String jarPath = library.getJarPath();
                if (visitedJars.contains(jarPath) == false) {
                    visitedJars.add(jarPath);
                    paths.add(jarPath);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }
}
