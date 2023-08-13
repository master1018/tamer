public final class ResourceFolder extends Resource {
    ResourceFolderType mType;
    FolderConfiguration mConfiguration;
    IAbstractFolder mFolder;
    ArrayList<ResourceFile> mFiles = null;
    private final boolean mIsFramework;
    public ResourceFolder(ResourceFolderType type, FolderConfiguration config,
            IAbstractFolder folder, boolean isFrameworkRepository) {
        mType = type;
        mConfiguration = config;
        mFolder = folder;
        mIsFramework = isFrameworkRepository;
    }
    public void addFile(ResourceFile file) {
        if (mFiles == null) {
            mFiles = new ArrayList<ResourceFile>();
        }
        mFiles.add(file);
    }
    public ResourceFile removeFile(IFile file) {
        if (mFiles != null) {
            int count = mFiles.size();
            for (int i = 0 ; i < count ; i++) {
                ResourceFile resFile = mFiles.get(i);
                if (resFile != null) {
                    IAbstractFile abstractFile = resFile.getFile();
                    if (abstractFile instanceof IFileWrapper) {
                        IFile iFile = ((IFileWrapper)resFile.getFile()).getIFile();
                        if (iFile != null && iFile.equals(file)) {
                            mFiles.remove(i);
                            touch();
                            return resFile;
                        }
                    }
                }
            }
        }
        return null;
    }
    public IAbstractFolder getFolder() {
        return mFolder;
    }
    public ResourceFolderType getType() {
        return mType;
    }
    public boolean isFramework() {
        return mIsFramework;
    }
    public Collection<ResourceType> getResourceTypes() {
        ArrayList<ResourceType> list = new ArrayList<ResourceType>();
        if (mFiles != null) {
            for (ResourceFile file : mFiles) {
                ResourceType[] types = file.getResourceTypes();
                if (types != null) {
                    for (ResourceType resType : types) {
                        if (list.indexOf(resType) == -1) {
                            list.add(resType);
                        }
                    }
                }
            }
        }
        return list;
    }
    @Override
    public FolderConfiguration getConfiguration() {
        return mConfiguration;
    }
    public boolean hasFile(String name) {
        return mFolder.hasFile(name);
    }
    public ResourceFile getFile(IAbstractFile file) {
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                if (f.getFile().equals(file)) {
                    return f;
                }
            }
        }
        return null;
    }
    public ResourceFile getFile(IFile file) {
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                IAbstractFile abstractFile = f.getFile();
                if (abstractFile instanceof IFileWrapper) {
                    IFile iFile = ((IFileWrapper)f.getFile()).getIFile();
                    if (iFile != null && iFile.equals(file)) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
    public ResourceFile getFile(String filename) {
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                if (f.getFile().getName().equals(filename)) {
                    return f;
                }
            }
        }
        return null;
    }
    public boolean hasResources(ResourceType type) {
        ResourceFolderType[] folderTypes = FolderTypeRelationship.getRelatedFolders(type);
        boolean valid = false;
        for (ResourceFolderType rft : folderTypes) {
            if (rft == mType) {
                valid = true;
                break;
            }
        }
        if (valid) {
            if (mFiles != null) {
                for (ResourceFile f : mFiles) {
                    if (f.hasResources(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public Collection<ProjectResourceItem> getResources(ResourceType type,
            ProjectResources projectResources) {
        Collection<ProjectResourceItem> list = new ArrayList<ProjectResourceItem>();
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                list.addAll(f.getResources(type, projectResources));
            }
        }
        return list;
    }
}
