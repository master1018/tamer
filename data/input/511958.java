class ArchiveInfo implements IDescription {
    private final Archive mNewArchive;
    private final Archive mReplaced;
    private final ArchiveInfo[] mDependsOn;
    private final ArrayList<ArchiveInfo> mDependencyFor = new ArrayList<ArchiveInfo>();
    private boolean mAccepted;
    private boolean mRejected;
    public ArchiveInfo(Archive newArchive, Archive replaced, ArchiveInfo[] dependsOn) {
        mNewArchive = newArchive;
        mReplaced = replaced;
        mDependsOn = dependsOn;
    }
    public Archive getNewArchive() {
        return mNewArchive;
    }
    public Archive getReplaced() {
        return mReplaced;
    }
    public ArchiveInfo[] getDependsOn() {
        return mDependsOn;
    }
    public boolean isDependencyFor() {
        return mDependencyFor.size() > 0;
    }
    public void addDependencyFor(ArchiveInfo dependencyFor) {
        if (!mDependencyFor.contains(dependencyFor)) {
            mDependencyFor.add(dependencyFor);
        }
    }
    public Collection<ArchiveInfo> getDependenciesFor() {
        return mDependencyFor;
    }
    public void setAccepted(boolean accepted) {
        mAccepted = accepted;
    }
    public boolean isAccepted() {
        return mAccepted;
    }
    public void setRejected(boolean rejected) {
        mRejected = rejected;
    }
    public boolean isRejected() {
        return mRejected;
    }
    public String getLongDescription() {
        if (mNewArchive != null) {
            Package p = mNewArchive.getParentPackage();
            if (p != null) {
                return p.getLongDescription();
            }
        }
        return "";
    }
    public String getShortDescription() {
        if (mNewArchive != null) {
            Package p = mNewArchive.getParentPackage();
            if (p != null) {
                return p.getShortDescription();
            }
        }
        return "";
    }
}
