public class LibraryDeltaVisitor implements IResourceDeltaVisitor {
    private boolean mResChange = false;
    private boolean mLibChange = false;
    public boolean getResChange() {
        return mResChange;
    }
    public boolean getLibChange() {
        return mLibChange;
    }
    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        IPath path = resource.getFullPath();
        String[] segments = path.segments();
        if (segments.length == 1) {
            return true;
        } else if (segments.length == 2) {
            if (SdkConstants.FD_RESOURCES.equalsIgnoreCase(segments[1])) {
                mResChange = true;
            } else if (SdkConstants.FD_NATIVE_LIBS.equalsIgnoreCase(segments[1])) {
                mLibChange = true;
            }
        }
        return false;
    }
}
