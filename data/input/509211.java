public final class LayoutReloadMonitor {
    private final static LayoutReloadMonitor sThis = new LayoutReloadMonitor();
    private final Map<IProject, List<ILayoutReloadListener>> mListenerMap =
        new HashMap<IProject, List<ILayoutReloadListener>>();
    public final static class ChangeFlags {
        public boolean code = false;
        public boolean resources = false;
        public boolean layout = false;
        public boolean rClass = false;
        public boolean localeList = false;
        boolean isAllTrue() {
            return code && resources && rClass && localeList;
        }
    }
    private final Map<IProject, ChangeFlags> mProjectFlags = new HashMap<IProject, ChangeFlags>();
    public interface ILayoutReloadListener {
        void reloadLayout(ChangeFlags flags, boolean libraryModified);
    }
    public static LayoutReloadMonitor getMonitor() {
        return sThis;
    }
    private LayoutReloadMonitor() {
        ResourceManager.getInstance().addListener(mResourceListener);
        GlobalProjectMonitor monitor = GlobalProjectMonitor.getMonitor();
        monitor.addFileListener(mFileListener,
                IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED);
        monitor.addResourceEventListener(mResourceEventListener);
    }
    public void addListener(IProject project, ILayoutReloadListener listener) {
        synchronized (mListenerMap) {
            List<ILayoutReloadListener> list = mListenerMap.get(project);
            if (list == null) {
                list = new ArrayList<ILayoutReloadListener>();
                mListenerMap.put(project, list);
            }
            list.add(listener);
        }
    }
    public void removeListener(IProject project, ILayoutReloadListener listener) {
        synchronized (mListenerMap) {
            List<ILayoutReloadListener> list = mListenerMap.get(project);
            if (list != null) {
                list.remove(listener);
            }
        }
    }
    public void removeListener(ILayoutReloadListener listener) {
        synchronized (mListenerMap) {
            for (List<ILayoutReloadListener> list : mListenerMap.values()) {
                Iterator<ILayoutReloadListener> it = list.iterator();
                while (it.hasNext()) {
                    ILayoutReloadListener i = it.next();
                    if (i == listener) {
                        it.remove();
                    }
                }
            }
        }
    }
    private IFileListener mFileListener = new IFileListener() {
        public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind) {
            IProject project = file.getProject();
            ChangeFlags changeFlags = mProjectFlags.get(project);
            if (changeFlags != null && changeFlags.isAllTrue()) {
                return;
            }
            if (AndroidConstants.EXT_CLASS.equals(file.getFileExtension())) {
                if (file.getName().matches("R[\\$\\.](.*)")) {
                    if (changeFlags == null) {
                        changeFlags = new ChangeFlags();
                        mProjectFlags.put(project, changeFlags);
                    }
                    changeFlags.rClass = true;
                } else {
                    if (changeFlags == null) {
                        changeFlags = new ChangeFlags();
                        mProjectFlags.put(project, changeFlags);
                    }
                    changeFlags.code = true;
                }
            }
        }
    };
    private IResourceEventListener mResourceEventListener = new IResourceEventListener() {
        public void resourceChangeEventStart() {
        }
        public void resourceChangeEventEnd() {
            for (Entry<IProject, ChangeFlags> entry : mProjectFlags.entrySet()) {
                IProject project = entry.getKey();
                notifyForProject(project, entry.getValue(), false);
                ProjectState state = Sdk.getProjectState(project);
                if (state != null && state.isLibrary()) {
                    Set<ProjectState> mainProjects = Sdk.getMainProjectsFor(project);
                    for (ProjectState mainProject : mainProjects) {
                        notifyForProject(mainProject.getProject(), entry.getValue(), true);
                    }
                }
            }
            mProjectFlags.clear();
        }
        private void notifyForProject(IProject project, ChangeFlags flags,
                boolean libraryChanged) {
            synchronized (mListenerMap) {
                List<ILayoutReloadListener> listeners = mListenerMap.get(project);
                if (listeners != null) {
                    for (ILayoutReloadListener listener : listeners) {
                        try {
                            listener.reloadLayout(flags, libraryChanged);
                        } catch (Throwable t) {
                            AdtPlugin.log(t, "Failed to call ILayoutReloadListener.reloadLayout");
                        }
                    }
                }
            }
        }
    };
    private IResourceListener mResourceListener = new IResourceListener() {
        public void folderChanged(IProject project, ResourceFolder folder, int eventType) {
            ChangeFlags changeFlags = mProjectFlags.get(project);
            if (changeFlags != null && changeFlags.isAllTrue()) {
                return;
            }
            if (changeFlags == null) {
                changeFlags = new ChangeFlags();
                mProjectFlags.put(project, changeFlags);
            }
            changeFlags.localeList = true;
        }
        public void fileChanged(IProject project, ResourceFile file, int eventType) {
            ChangeFlags changeFlags = mProjectFlags.get(project);
            if (changeFlags != null && changeFlags.isAllTrue()) {
                return;
            }
            ResourceType[] resTypes = file.getResourceTypes();
            if (resTypes.length > 0) {
                if (changeFlags == null) {
                    changeFlags = new ChangeFlags();
                    mProjectFlags.put(project, changeFlags);
                }
                if (resTypes[0] != ResourceType.LAYOUT) {
                    changeFlags.resources = true;
                } else {
                    changeFlags.layout = true;
                }
            }
        }
    };
}
