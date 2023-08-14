class AndroidClasspathContainer implements IClasspathContainer {
    private IClasspathEntry[] mClasspathEntry;
    private IPath mContainerPath;
    private String mName;
    AndroidClasspathContainer(IClasspathEntry[] entries, IPath path, String name) {
        mClasspathEntry = entries;
        mContainerPath = path;
        mName = name;
    }
    public IClasspathEntry[] getClasspathEntries() {
        return mClasspathEntry;
    }
    public String getDescription() {
        return mName;
    }
    public int getKind() {
        return IClasspathContainer.K_DEFAULT_SYSTEM;
    }
    public IPath getPath() {
        return mContainerPath;
    }
}
