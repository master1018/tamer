public final class GlobalProjectMonitor {
    private final static GlobalProjectMonitor sThis = new GlobalProjectMonitor();
    public interface IFileListener {
        public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind);
    }
    public interface IProjectListener {
        public void projectOpenedWithWorkspace(IProject project);
        public void projectOpened(IProject project);
        public void projectClosed(IProject project);
        public void projectDeleted(IProject project);
        public void projectRenamed(IProject project, IPath from);
    }
    public interface IFolderListener {
        public void folderChanged(IFolder folder, int kind);
    }
    public interface IResourceEventListener {
        public void resourceChangeEventStart();
        public void resourceChangeEventEnd();
    }
    private static class ListenerBundle {
        public final static int MASK_NONE = -1;
        int kindMask;
    }
    private static class FileListenerBundle extends ListenerBundle {
        IFileListener listener;
    }
    private static class FolderListenerBundle extends ListenerBundle {
        IFolderListener listener;
    }
    private final ArrayList<FileListenerBundle> mFileListeners =
        new ArrayList<FileListenerBundle>();
    private final ArrayList<FolderListenerBundle> mFolderListeners =
        new ArrayList<FolderListenerBundle>();
    private final ArrayList<IProjectListener> mProjectListeners = new ArrayList<IProjectListener>();
    private final ArrayList<IResourceEventListener> mEventListeners =
        new ArrayList<IResourceEventListener>();
    private IWorkspace mWorkspace;
    private final class DeltaVisitor implements IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) {
            IResource r = delta.getResource();
            int type = r.getType();
            if (type == IResource.FILE) {
                int kind = delta.getKind();
                for (FileListenerBundle bundle : mFileListeners) {
                    if (bundle.kindMask == ListenerBundle.MASK_NONE
                            || (bundle.kindMask & kind) != 0) {
                        try {
                            bundle.listener.fileChanged((IFile)r, delta.getMarkerDeltas(), kind);
                        } catch (Throwable t) {
                            AdtPlugin.log(t,"Failed to call IFileListener.fileChanged");
                        }
                    }
                }
                return false;
            } else if (type == IResource.FOLDER) {
                int kind = delta.getKind();
                for (FolderListenerBundle bundle : mFolderListeners) {
                    if (bundle.kindMask == ListenerBundle.MASK_NONE
                            || (bundle.kindMask & kind) != 0) {
                        try {
                            bundle.listener.folderChanged((IFolder)r, kind);
                        } catch (Throwable t) {
                            AdtPlugin.log(t,"Failed to call IFileListener.folderChanged");
                        }
                    }
                }
                return true;
            } else if (type == IResource.PROJECT) {
                int flags = delta.getFlags();
                if ((flags & IResourceDelta.OPEN) != 0) {
                    IProject project = (IProject)r;
                    if (project.isOpen()) {
                        for (IProjectListener pl : mProjectListeners) {
                            try {
                                pl.projectOpened(project);
                            } catch (Throwable t) {
                                AdtPlugin.log(t,"Failed to call IProjectListener.projectOpened");
                            }
                        }
                    } else {
                        for (IProjectListener pl : mProjectListeners) {
                            try {
                                pl.projectClosed(project);
                            } catch (Throwable t) {
                                AdtPlugin.log(t,"Failed to call IProjectListener.projectClosed");
                            }
                        }
                    }
                    if ((flags & IResourceDelta.MOVED_FROM) != 0) {
                        IPath from = delta.getMovedFromPath();
                        for (IProjectListener pl : mProjectListeners) {
                            try {
                                pl.projectRenamed(project, from);
                            } catch (Throwable t) {
                                AdtPlugin.log(t,"Failed to call IProjectListener.projectRenamed");
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
    public static GlobalProjectMonitor getMonitor() {
        return sThis;
    }
    public static GlobalProjectMonitor startMonitoring(IWorkspace ws) {
        if (sThis != null) {
            ws.addResourceChangeListener(sThis.mResourceChangeListener,
                    IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE);
            sThis.mWorkspace = ws;
        }
        return sThis;
    }
    public static void stopMonitoring(IWorkspace ws) {
        if (sThis != null) {
            ws.removeResourceChangeListener(sThis.mResourceChangeListener);
            synchronized (sThis) {
                sThis.mFileListeners.clear();
                sThis.mProjectListeners.clear();
            }
        }
    }
    public synchronized void addFileListener(IFileListener listener, int kindMask) {
        FileListenerBundle bundle = new FileListenerBundle();
        bundle.listener = listener;
        bundle.kindMask = kindMask;
        mFileListeners.add(bundle);
    }
    public synchronized void removeFileListener(IFileListener listener) {
        for (int i = 0 ; i < mFileListeners.size() ; i++) {
            FileListenerBundle bundle = mFileListeners.get(i);
            if (bundle.listener == listener) {
                mFileListeners.remove(i);
                return;
            }
        }
    }
    public synchronized void addFolderListener(IFolderListener listener, int kindMask) {
        FolderListenerBundle bundle = new FolderListenerBundle();
        bundle.listener = listener;
        bundle.kindMask = kindMask;
        mFolderListeners.add(bundle);
    }
    public synchronized void removeFolderListener(IFolderListener listener) {
        for (int i = 0 ; i < mFolderListeners.size() ; i++) {
            FolderListenerBundle bundle = mFolderListeners.get(i);
            if (bundle.listener == listener) {
                mFolderListeners.remove(i);
                return;
            }
        }
    }
    public synchronized void addProjectListener(IProjectListener listener) {
        mProjectListeners.add(listener);
        IWorkspaceRoot workspaceRoot = mWorkspace.getRoot();
        IJavaModel javaModel = JavaCore.create(workspaceRoot);
        IJavaProject[] androidProjects = BaseProjectHelper.getAndroidProjects(javaModel,
                null );
        for (IJavaProject androidProject : androidProjects) {
            listener.projectOpenedWithWorkspace(androidProject.getProject());
        }
    }
    public synchronized void removeProjectListener(IProjectListener listener) {
        mProjectListeners.remove(listener);
    }
    public synchronized void addResourceEventListener(IResourceEventListener listener) {
        mEventListeners.add(listener);
    }
    public synchronized void removeResourceEventListener(IResourceEventListener listener) {
        mEventListeners.remove(listener);
    }
    private IResourceChangeListener mResourceChangeListener = new IResourceChangeListener() {
        public synchronized void resourceChanged(IResourceChangeEvent event) {
            for (IResourceEventListener listener : mEventListeners) {
                try {
                    listener.resourceChangeEventStart();
                } catch (Throwable t) {
                    AdtPlugin.log(t,"Failed to call IResourceEventListener.resourceChangeEventStart");
                }
            }
            if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                IResource r = event.getResource();
                IProject project = r.getProject();
                for (IProjectListener pl : mProjectListeners) {
                    try {
                        pl.projectDeleted(project);
                    } catch (Throwable t) {
                        AdtPlugin.log(t,"Failed to call IProjectListener.projectDeleted");
                    }
                }
            } else {
                IResourceDelta delta = event.getDelta();
                DeltaVisitor visitor = new DeltaVisitor();
                try {
                    delta.accept(visitor);
                } catch (CoreException e) {
                }
            }
            for (IResourceEventListener listener : mEventListeners) {
                try {
                    listener.resourceChangeEventEnd();
                } catch (Throwable t) {
                    AdtPlugin.log(t,"Failed to call IResourceEventListener.resourceChangeEventEnd");
                }
            }
        }
    };
}
