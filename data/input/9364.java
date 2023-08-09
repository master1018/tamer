class WinNTFileSystem extends Win32FileSystem {
    protected native String canonicalize0(String path)
                                                throws IOException;
    protected native String canonicalizeWithPrefix0(String canonicalPrefix,
                                                    String pathWithCanonicalPrefix)
                                                throws IOException;
    public native int getBooleanAttributes(File f);
    public native boolean checkAccess(File f, int access);
    public native long getLastModifiedTime(File f);
    public native long getLength(File f);
    public native boolean setPermission(File f, int access, boolean enable, boolean owneronly);
    public long getSpace(File f, int t) {
        if (f.exists()) {
            return getSpace0(f, t);
        }
        return 0;
    }
    private native long getSpace0(File f, int t);
    public native boolean createFileExclusively(String path)
                                               throws IOException;
    protected native boolean delete0(File f);
    public native String[] list(File f);
    public native boolean createDirectory(File f);
    protected native boolean rename0(File f1, File f2);
    public native boolean setLastModifiedTime(File f, long time);
    public native boolean setReadOnly(File f);
    protected native String getDriveDirectory(int drive);
    private static native void initIDs();
    static {
            initIDs();
    }
}
