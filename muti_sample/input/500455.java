public final class ResourceManager {
    private final static ResourceManager sThis = new ResourceManager();
    private final ResourceQualifier[] mQualifiers;
    private final HashMap<IProject, ProjectResources> mMap =
        new HashMap<IProject, ProjectResources>();
    public interface IResourceListener {
        void fileChanged(IProject project, ResourceFile file, int eventType);
        void folderChanged(IProject project, ResourceFolder folder, int eventType);
    }
    private final ArrayList<IResourceListener> mListeners = new ArrayList<IResourceListener>();
    public static void setup(GlobalProjectMonitor monitor) {
        monitor.addProjectListener(sThis.mProjectListener);
        int mask = IResourceDelta.ADDED | IResourceDelta.REMOVED | IResourceDelta.CHANGED;
        monitor.addFolderListener(sThis.mFolderListener, mask);
        monitor.addFileListener(sThis.mFileListener, mask);
        CompiledResourcesMonitor.setupMonitor(monitor);
    }
    public static ResourceManager getInstance() {
        return sThis;
    }
    public void addListener(IResourceListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }
    public void removeListener(IResource listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }
    public ProjectResources getProjectResources(IProject project) {
        synchronized (mMap) {
            return mMap.get(project);
        }
    }
    private IFolderListener mFolderListener = new IFolderListener() {
        public void folderChanged(IFolder folder, int kind) {
            ProjectResources resources;
            final IProject project = folder.getProject();
            try {
                if (project.hasNature(AndroidConstants.NATURE) == false) {
                    return;
                }
            } catch (CoreException e) {
                return;
            }
            switch (kind) {
                case IResourceDelta.ADDED:
                    IPath path = folder.getFullPath();
                    if (path.segmentCount() == 3) {
                        if (isInResFolder(path)) {
                            synchronized (mMap) {
                                resources = mMap.get(project);
                                if (resources == null) {
                                    resources = new ProjectResources(project);
                                    mMap.put(project, resources);
                                }
                            }
                            ResourceFolder newFolder = processFolder(new IFolderWrapper(folder),
                                    resources);
                            if (newFolder != null) {
                                notifyListenerOnFolderChange(project, newFolder, kind);
                            }
                        }
                    }
                    break;
                case IResourceDelta.CHANGED:
                    synchronized (mMap) {
                        resources = mMap.get(folder.getProject());
                    }
                    if (resources != null) {
                        ResourceFolder resFolder = resources.getResourceFolder(folder);
                        if (resFolder != null) {
                            resFolder.touch();
                            notifyListenerOnFolderChange(project, resFolder, kind);
                        }
                    }
                    break;
                case IResourceDelta.REMOVED:
                    synchronized (mMap) {
                        resources = mMap.get(folder.getProject());
                    }
                    if (resources != null) {
                        ResourceFolderType type = ResourceFolderType.getFolderType(
                                folder.getName());
                        ResourceFolder removedFolder = resources.removeFolder(type, folder);
                        if (removedFolder != null) {
                            notifyListenerOnFolderChange(project, removedFolder, kind);
                        }
                    }
                    break;
            }
        }
    };
    private IFileListener mFileListener = new IFileListener() {
        public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind) {
            ProjectResources resources;
            final IProject project = file.getProject();
            try {
                if (project.hasNature(AndroidConstants.NATURE) == false) {
                    return;
                }
            } catch (CoreException e) {
                return;
            }
            switch (kind) {
                case IResourceDelta.ADDED:
                    IPath path = file.getFullPath();
                    if (path.segmentCount() == 4) {
                        if (isInResFolder(path)) {
                            synchronized (mMap) {
                                resources = mMap.get(project);
                            }
                            IContainer container = file.getParent();
                            if (container instanceof IFolder && resources != null) {
                                ResourceFolder folder = resources.getResourceFolder(
                                        (IFolder)container);
                                if (folder != null) {
                                    ResourceFile resFile = processFile(
                                            new IFileWrapper(file), folder);
                                    notifyListenerOnFileChange(project, resFile, kind);
                                }
                            }
                        }
                    }
                    break;
                case IResourceDelta.CHANGED:
                    synchronized (mMap) {
                        resources = mMap.get(project);
                    }
                    if (resources != null) {
                        IContainer container = file.getParent();
                        if (container instanceof IFolder) {
                            ResourceFolder resFolder = resources.getResourceFolder(
                                    (IFolder)container);
                            if (resFolder != null) {
                                ResourceFile resFile = resFolder.getFile(file);
                                if (resFile != null) {
                                    resFile.touch();
                                    notifyListenerOnFileChange(project, resFile, kind);
                                }
                            }
                        }
                    }
                    break;
                case IResourceDelta.REMOVED:
                    synchronized (mMap) {
                        resources = mMap.get(project);
                    }
                    if (resources != null) {
                        IContainer container = file.getParent();
                        if (container instanceof IFolder) {
                            ResourceFolder resFolder = resources.getResourceFolder(
                                    (IFolder)container);
                            if (resFolder != null) {
                                ResourceFile resFile = resFolder.removeFile(file);
                                if (resFile != null) {
                                    notifyListenerOnFileChange(project, resFile, kind);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };
    private IProjectListener mProjectListener = new IProjectListener() {
        public void projectClosed(IProject project) {
            synchronized (mMap) {
                mMap.remove(project);
            }
        }
        public void projectDeleted(IProject project) {
            synchronized (mMap) {
                mMap.remove(project);
            }
        }
        public void projectOpened(IProject project) {
            createProject(project);
        }
        public void projectOpenedWithWorkspace(IProject project) {
            createProject(project);
        }
        public void projectRenamed(IProject project, IPath from) {
        }
    };
    public ResourceFolder getResourceFolder(IFile file) {
        IContainer container = file.getParent();
        if (container.getType() == IResource.FOLDER) {
            IFolder parent = (IFolder)container;
            IProject project = file.getProject();
            ProjectResources resources = getProjectResources(project);
            if (resources != null) {
                return resources.getResourceFolder(parent);
            }
        }
        return null;
    }
    public ResourceFolder getResourceFolder(IFolder folder) {
        IProject project = folder.getProject();
        ProjectResources resources = getProjectResources(project);
        if (resources != null) {
            return resources.getResourceFolder(folder);
        }
        return null;
    }
    public ProjectResources loadFrameworkResources(IAndroidTarget androidTarget) {
        String osResourcesPath = androidTarget.getPath(IAndroidTarget.RESOURCES);
        FolderWrapper frameworkRes = new FolderWrapper(osResourcesPath);
        if (frameworkRes.exists()) {
            ProjectResources resources = new ProjectResources();
            try {
                loadResources(resources, frameworkRes);
                return resources;
            } catch (IOException e) {
            }
        }
        return null;
    }
    public void loadResources(ProjectResources resources, IAbstractFolder rootFolder)
            throws IOException {
        IAbstractResource[] files = rootFolder.listMembers();
        for (IAbstractResource file : files) {
            if (file instanceof IAbstractFolder) {
                IAbstractFolder folder = (IAbstractFolder) file;
                ResourceFolder resFolder = processFolder(folder, resources);
                if (resFolder != null) {
                    IAbstractResource[] children = folder.listMembers();
                    for (IAbstractResource childRes : children) {
                        if (childRes instanceof IAbstractFile) {
                            processFile((IAbstractFile) childRes, resFolder);
                        }
                    }
                }
            }
        }
        resources.loadAll();
    }
    private void createProject(IProject project) {
        if (project.isOpen()) {
            try {
                if (project.hasNature(AndroidConstants.NATURE) == false) {
                    return;
                }
            } catch (CoreException e1) {
                return;
            }
            IFolder resourceFolder = project.getFolder(SdkConstants.FD_RESOURCES);
            ProjectResources projectResources;
            synchronized (mMap) {
                projectResources = mMap.get(project);
                if (projectResources == null) {
                    projectResources = new ProjectResources(project);
                    mMap.put(project, projectResources);
                }
            }
            if (resourceFolder != null && resourceFolder.exists()) {
                try {
                    IResource[] resources = resourceFolder.members();
                    for (IResource res : resources) {
                        if (res.getType() == IResource.FOLDER) {
                            IFolder folder = (IFolder)res;
                            ResourceFolder resFolder = processFolder(new IFolderWrapper(folder),
                                    projectResources);
                            if (resFolder != null) {
                                IResource[] files = folder.members();
                                for (IResource fileRes : files) {
                                    if (fileRes.getType() == IResource.FILE) {
                                        IFile file = (IFile)fileRes;
                                        processFile(new IFileWrapper(file), resFolder);
                                    }
                                }
                            }
                        }
                    }
                } catch (CoreException e) {
                }
            }
        }
    }
    public FolderConfiguration getConfig(String[] folderSegments) {
        FolderConfiguration config = new FolderConfiguration();
        int qualifierIndex = 0;
        int qualifierCount = mQualifiers.length;
        for (int i = 1 ; i < folderSegments.length; i++) {
            String seg = folderSegments[i];
            if (seg.length() > 0) {
                while (qualifierIndex < qualifierCount &&
                        mQualifiers[qualifierIndex].checkAndSet(seg, config) == false) {
                    qualifierIndex++;
                }
                if (qualifierIndex == qualifierCount) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return config;
    }
    private ResourceFolder processFolder(IAbstractFolder folder, ProjectResources project) {
        String[] folderSegments = folder.getName().split(FolderConfiguration.QUALIFIER_SEP);
        ResourceFolderType type = ResourceFolderType.getTypeByName(folderSegments[0]);
        if (type != null) {
            FolderConfiguration config = getConfig(folderSegments);
            if (config != null) {
                ResourceFolder configuredFolder = project.add(type, config, folder);
                return configuredFolder;
            }
        }
        return null;
    }
    private ResourceFile processFile(IAbstractFile file, ResourceFolder folder) {
        ResourceFolderType type = folder.getType();
        ResourceFile resFile = folder.getFile(file);
        if (resFile != null) {
            resFile.touch();
        } else {
            ResourceType[] types = FolderTypeRelationship.getRelatedResourceTypes(type);
            if (types.length == 1) {
                resFile = new SingleResourceFile(file, folder);
            } else {
                resFile = new MultiResourceFile(file, folder);
            }
            folder.addFile(resFile);
        }
        return resFile;
    }
    private boolean isInResFolder(IPath path) {
        return SdkConstants.FD_RESOURCES.equalsIgnoreCase(path.segment(1));
    }
    private void notifyListenerOnFolderChange(IProject project, ResourceFolder folder,
            int eventType) {
        synchronized (mListeners) {
            for (IResourceListener listener : mListeners) {
                try {
                    listener.folderChanged(project, folder, eventType);
                } catch (Throwable t) {
                    AdtPlugin.log(t,
                            "Failed to execute ResourceManager.IResouceListener.folderChanged()"); 
                }
            }
        }
    }
    private void notifyListenerOnFileChange(IProject project, ResourceFile file, int eventType) {
        synchronized (mListeners) {
            for (IResourceListener listener : mListeners) {
                try {
                    listener.fileChanged(project, file, eventType);
                } catch (Throwable t) {
                    AdtPlugin.log(t,
                            "Failed to execute ResourceManager.IResouceListener.fileChanged()"); 
                }
            }
        }
    }
    private ResourceManager() {
        FolderConfiguration defaultConfig = new FolderConfiguration();
        defaultConfig.createDefault();
        mQualifiers = defaultConfig.getQualifiers();
    }
}
