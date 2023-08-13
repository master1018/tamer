public final class ProjectState {
    public final class LibraryState {
        private String mRelativePath;
        private ProjectState mProjectState;
        private String mPath;
        private LibraryState(String relativePath) {
            mRelativePath = relativePath;
        }
        public ProjectState getMainProjectState() {
            return ProjectState.this;
        }
        public void close() {
            mProjectState = null;
            mPath = null;
            updateLibraries();
        }
        private void setRelativePath(String relativePath) {
            mRelativePath = relativePath;
        }
        private void setProject(ProjectState project) {
            mProjectState = project;
            mPath = project.getProject().getLocation().toOSString();
            updateLibraries();
        }
        public String getRelativePath() {
            return mRelativePath;
        }
        public ProjectState getProjectState() {
            return mProjectState;
        }
        public String getProjectLocation() {
            return mPath;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LibraryState) {
                LibraryState objState = (LibraryState)obj;
                return mRelativePath.equals(objState.mRelativePath) &&
                        getMainProjectState().equals(objState.getMainProjectState());
            } else if (obj instanceof ProjectState || obj instanceof IProject) {
                return mProjectState != null && mProjectState.equals(obj);
            } else if (obj instanceof String) {
                return normalizePath(mRelativePath).equals(normalizePath((String) obj));
            }
            return false;
        }
        @Override
        public int hashCode() {
            return mRelativePath.hashCode();
        }
    }
    private final IProject mProject;
    private final ProjectProperties mProperties;
    private final ArrayList<LibraryState> mLibraries = new ArrayList<LibraryState>();
    private IAndroidTarget mTarget;
    private ApkSettings mApkSettings;
    private IProject[] mLibraryProjects;
    public ProjectState(IProject project, ProjectProperties properties) {
        mProject = project;
        mProperties = properties;
        mApkSettings = new ApkSettings(properties);
        synchronized (mLibraries) {
            int index = 1;
            while (true) {
                String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index++);
                String rootPath = mProperties.getProperty(propName);
                if (rootPath == null) {
                    break;
                }
                mLibraries.add(new LibraryState(convertPath(rootPath)));
            }
        }
    }
    public IProject getProject() {
        return mProject;
    }
    public ProjectProperties getProperties() {
        return mProperties;
    }
    public void setTarget(IAndroidTarget target) {
        mTarget = target;
    }
    public String getTargetHashString() {
        if (mTarget != null) {
            return mTarget.hashString();
        }
        if (mProperties != null) {
            return mProperties.getProperty(ProjectProperties.PROPERTY_TARGET);
        }
        return null;
    }
    public IAndroidTarget getTarget() {
        return mTarget;
    }
    public static class LibraryDifference {
        public List<LibraryState> removed = new ArrayList<LibraryState>();
        public boolean added = false;
        public boolean hasDiff() {
            return removed.size() > 0 || added;
        }
    }
    public LibraryDifference reloadProperties() {
        mTarget = null;
        mProperties.reload();
        LibraryDifference diff = new LibraryDifference();
        synchronized (mLibraries) {
            List<LibraryState> oldLibraries = new ArrayList<LibraryState>(mLibraries);
            mLibraries.clear();
            int index = 1;
            while (true) {
                String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index++);
                String rootPath = mProperties.getProperty(propName);
                if (rootPath == null) {
                    break;
                }
                String convertedPath = convertPath(rootPath);
                boolean found = false;
                for (int i = 0 ; i < oldLibraries.size(); i++) {
                    LibraryState libState = oldLibraries.get(i);
                    if (libState.equals(convertedPath)) {
                        found = true;
                        mLibraries.add(libState);
                        oldLibraries.remove(i);
                        break;
                    }
                }
                if (found == false) {
                    diff.added = true;
                    mLibraries.add(new LibraryState(convertedPath));
                }
            }
            diff.removed.addAll(oldLibraries);
            updateLibraries();
        }
        return diff;
    }
    public void setApkSettings(ApkSettings apkSettings) {
        mApkSettings = apkSettings;
    }
    public ApkSettings getApkSettings() {
        return mApkSettings;
    }
    public List<LibraryState> getLibraries() {
        synchronized (mLibraries) {
            return Collections.unmodifiableList(mLibraries);
        }
    }
    public IProject[] getLibraryProjects() {
        return mLibraryProjects;
    }
    public boolean isLibrary() {
        String value = mProperties.getProperty(ProjectProperties.PROPERTY_LIBRARY);
        return value != null && Boolean.valueOf(value);
    }
    public boolean hasLibraries() {
        synchronized (mLibraries) {
            return mLibraries.size() > 0;
        }
    }
    public boolean isMissingLibraries() {
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                if (state.getProjectState() == null) {
                    return true;
                }
            }
        }
        return false;
    }
    public LibraryState getLibrary(IProject library) {
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                ProjectState ps = state.getProjectState();
                if (ps != null && ps.equals(library)) {
                    return state;
                }
            }
        }
        return null;
    }
    public LibraryState getLibrary(String name) {
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                ProjectState ps = state.getProjectState();
                if (ps != null && ps.getProject().getName().equals(name)) {
                    return state;
                }
            }
        }
        return null;
    }
    public LibraryState needs(ProjectState libraryProject) {
        File projectFile = mProject.getLocation().toFile();
        File libraryFile = libraryProject.getProject().getLocation().toFile();
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                if (state.getProjectState() == null) {
                    File library = new File(projectFile, state.getRelativePath());
                    try {
                        File absPath = library.getCanonicalFile();
                        if (absPath.equals(libraryFile)) {
                            state.setProject(libraryProject);
                            return state;
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }
    public LibraryState updateLibrary(String oldRelativePath, String newRelativePath,
            ProjectState newLibraryState) {
        File projectFile = mProject.getLocation().toFile();
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                if (state.getProjectState() == null) {
                    try {
                        File library1 = new File(projectFile, oldRelativePath);
                        File library2 = new File(projectFile, state.getRelativePath());
                        if (library1.getCanonicalPath().equals(library2.getCanonicalPath())) {
                            String oldProperty = state.getRelativePath();
                            state.setRelativePath(newRelativePath);
                            state.setProject(newLibraryState);
                            IStatus status = replaceLibraryProperty(oldProperty, newRelativePath);
                            if (status != null) {
                                if (status.getSeverity() != IStatus.OK) {
                                }
                            } else {
                            }
                            return state;
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }
    public void saveProperties() {
        try {
            mProperties.save();
            IResource defaultProp = mProject.findMember(SdkConstants.FN_DEFAULT_PROPERTIES);
            defaultProp.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    private IStatus replaceLibraryProperty(String oldValue, String newValue) {
        int index = 1;
        while (true) {
            String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index++);
            String rootPath = mProperties.getProperty(propName);
            if (rootPath == null) {
                break;
            }
            if (rootPath.equals(oldValue)) {
                mProperties.setProperty(propName, newValue);
                try {
                    mProperties.save();
                } catch (IOException e) {
                    return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                            String.format("Failed to save %1$s for project %2$s",
                                    mProperties.getType().getFilename(), mProject.getName()),
                            e);
                }
                return Status.OK_STATUS;
            }
        }
        return null;
    }
    private void updateLibraries() {
        ArrayList<IProject> list = new ArrayList<IProject>();
        synchronized (mLibraries) {
            for (LibraryState state : mLibraries) {
                if (state.getProjectState() != null) {
                    list.add(state.getProjectState().getProject());
                }
            }
        }
        mLibraryProjects = list.toArray(new IProject[list.size()]);
    }
    private String convertPath(String path) {
        return path.replaceAll("/", Matcher.quoteReplacement(File.separator)); 
    }
    private String normalizePath(String path) {
        path = convertPath(path);
        if (path.endsWith("/")) { 
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectState) {
            return mProject.equals(((ProjectState) obj).mProject);
        } else if (obj instanceof IProject) {
            return mProject.equals(obj);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mProject.hashCode();
    }
}
