public class FileSystemImpl extends FileSystem {
    public boolean supportsFileSecurity(File f) throws IOException {
        return true;
    }
    public boolean isAccessUserOnly(File f) throws IOException {
        return isAccessUserOnly0(f.getPath());
    }
    static native boolean isAccessUserOnly0(String path) throws IOException;
    static {
        java.security.AccessController
            .doPrivileged(new sun.security.action.LoadLibraryAction("management"));
    }
}
