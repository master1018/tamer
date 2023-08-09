public abstract class FileTypeDetector {
    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileTypeDetector"));
        return null;
    }
    private FileTypeDetector(Void ignore) { }
    protected FileTypeDetector() {
        this(checkPermission());
    }
    public abstract String probeContentType(Path path)
        throws IOException;
}
