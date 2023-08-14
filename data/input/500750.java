class DeviceContentProvider implements ITreeContentProvider {
    private TreeViewer mViewer;
    private FileListingService mFileListingService;
    private FileEntry mRootEntry;
    private IListingReceiver sListingReceiver = new IListingReceiver() {
        public void setChildren(final FileEntry entry, FileEntry[] children) {
            final Tree t = mViewer.getTree();
            if (t != null && t.isDisposed() == false) {
                Display display = t.getDisplay();
                if (display.isDisposed() == false) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (t.isDisposed() == false) {
                                mViewer.refresh(entry);
                                mViewer.setExpandedState(entry, true);
                            }
                        }
                    });
                }
            }
        }
        public void refreshEntry(final FileEntry entry) {
            final Tree t = mViewer.getTree();
            if (t != null && t.isDisposed() == false) {
                Display display = t.getDisplay();
                if (display.isDisposed() == false) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (t.isDisposed() == false) {
                                mViewer.refresh(entry);
                            }
                        }
                    });
                }
            }
        }
    };
    public DeviceContentProvider() {
    }
    public void setListingService(FileListingService fls) {
        mFileListingService = fls;
    }
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof FileEntry) {
            FileEntry parentEntry = (FileEntry)parentElement;
            Object[] oldEntries = parentEntry.getCachedChildren();
            Object[] newEntries = mFileListingService.getChildren(parentEntry,
                    true, sListingReceiver);
            if (newEntries != null) {
                return newEntries;
            } else {
                return oldEntries;
            }
        }
        return new Object[0];
    }
    public Object getParent(Object element) {
        if (element instanceof FileEntry) {
            FileEntry entry = (FileEntry)element;
            return entry.getParent();
        }
        return null;
    }
    public boolean hasChildren(Object element) {
        if (element instanceof FileEntry) {
            FileEntry entry = (FileEntry)element;
            return entry.getType() == FileListingService.TYPE_DIRECTORY;
        }
        return false;
    }
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof FileEntry) {
            FileEntry entry = (FileEntry)inputElement;
            if (entry.isRoot()) {
                return getChildren(mRootEntry);
            }
        }
        return null;
    }
    public void dispose() {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (viewer instanceof TreeViewer) {
            mViewer = (TreeViewer)viewer;
        }
        if (newInput instanceof FileEntry) {
            mRootEntry = (FileEntry)newInput;
        }
    }
}
