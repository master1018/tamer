public class IFileWrapper implements IAbstractFile {
    private final IFile mFile;
    public IFileWrapper(IFile file) {
        mFile = file;
    }
    public InputStream getContents() throws StreamException {
        try {
            return mFile.getContents();
        } catch (CoreException e) {
            throw new StreamException(e);
        }
    }
    public void setContents(InputStream source) throws StreamException {
        try {
            mFile.setContents(source, IResource.FORCE, null);
        } catch (CoreException e) {
            throw new StreamException(e);
        }
    }
    public String getOsLocation() {
        return mFile.getLocation().toOSString();
    }
    public String getName() {
        return mFile.getName();
    }
    public boolean exists() {
        return mFile.exists();
    }
    public IFile getIFile() {
        return mFile;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IFileWrapper) {
            return mFile.equals(((IFileWrapper)obj).mFile);
        }
        if (obj instanceof IFile) {
            return mFile.equals(obj);
        }
        return super.equals(obj);
    }
    @Override
    public int hashCode() {
        return mFile.hashCode();
    }
}
