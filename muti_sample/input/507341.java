public class ClasspathEntryMock implements IClasspathEntry {
    private int mKind;
    private IPath mPath;
    public ClasspathEntryMock(IPath path, int kind) {
        mPath = path;
        mKind = kind;
    }
    public int getEntryKind() {
        return mKind;
    }
    public IPath getPath() {
        return mPath;
    }
    public boolean combineAccessRules() {
        throw new NotImplementedException();
    }
    public IAccessRule[] getAccessRules() {
        throw new NotImplementedException();
    }
    public int getContentKind() {
        throw new NotImplementedException();
    }
    public IPath[] getExclusionPatterns() {
        throw new NotImplementedException();
    }
    public IClasspathAttribute[] getExtraAttributes() {
        throw new NotImplementedException();
    }
    public IPath[] getInclusionPatterns() {
        throw new NotImplementedException();
    }
    public IPath getOutputLocation() {
        throw new NotImplementedException();
    }
    public IClasspathEntry getResolvedEntry() {
        throw new NotImplementedException();
    }
    public IPath getSourceAttachmentPath() {
        throw new NotImplementedException();
    }
    public IPath getSourceAttachmentRootPath() {
        throw new NotImplementedException();
    }
    public boolean isExported() {
        throw new NotImplementedException();
    }
}
