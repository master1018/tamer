public abstract class ResourceFile extends Resource {
    private final IAbstractFile mFile;
    private final ResourceFolder mFolder;
    protected ResourceFile(IAbstractFile file, ResourceFolder folder) {
        mFile = file;
        mFolder = folder;
    }
    @Override
    public FolderConfiguration getConfiguration() {
        return mFolder.getConfiguration();
    }
    public final IAbstractFile getFile() {
        return mFile;
    }
    public final ResourceFolder getFolder() {
        return mFolder;
    }
    public final boolean isFramework() {
        return mFolder.isFramework();
    }
    public abstract ResourceType[] getResourceTypes();
    public abstract boolean hasResources(ResourceType type);
    public abstract Collection<ProjectResourceItem> getResources(ResourceType type,
            ProjectResources projectResources);
    public abstract IResourceValue getValue(ResourceType type, String name);
}
