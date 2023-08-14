public class IFolderWrapper implements IAbstractFolder {
    private final IFolder mFolder; 
    private final IContainer mContainer; 
    public IFolderWrapper(IFolder folder) {
        mContainer = mFolder = folder;
    }
    public IFolderWrapper(IContainer container) {
        mFolder = container instanceof IFolder ? (IFolder)container : null;
        mContainer = container;
    }
    public String getName() {
        return mContainer.getName();
    }
    public boolean exists() {
        return mContainer.exists();
    }
    public IAbstractResource[] listMembers() {
        try {
            IResource[] members = mContainer.members();
            final int count = members.length;
            IAbstractResource[] afiles = new IAbstractResource[count];
            for (int i = 0 ; i < count ; i++) {
                IResource f = members[i];
                if (f instanceof IFile) {
                    afiles[i] = new IFileWrapper((IFile) f);
                } else {
                    afiles[i] = new IFolderWrapper((IContainer) f);
                }
            }
            return afiles;
        } catch (CoreException e) {
        }
        return new IAbstractResource[0];
    }
    public boolean hasFile(String name) {
        try {
            IResource[] files = mContainer.members();
            for (IResource file : files) {
                if (name.equals(file.getName())) {
                    return true;
                }
            }
        } catch (CoreException e) {
        }
        return false;
    }
    public IAbstractFile getFile(String name) {
        if (mFolder != null) {
            IFile file = mFolder.getFile(name);
            return new IFileWrapper(file);
        }
        IFile file = mContainer.getFile(new Path(name));
        return new IFileWrapper(file);
    }
    public IFolder getIFolder() {
        return mFolder;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IFolderWrapper) {
            return mFolder.equals(((IFolderWrapper)obj).mFolder);
        }
        if (obj instanceof IFolder) {
            return mFolder.equals(obj);
        }
        return super.equals(obj);
    }
    @Override
    public int hashCode() {
        return mContainer.hashCode();
    }
}
