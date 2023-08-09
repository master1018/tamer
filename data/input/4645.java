class SolarisFileStore
    extends UnixFileStore
{
    private final boolean xattrEnabled;
    SolarisFileStore(UnixPath file) throws IOException {
        super(file);
        this.xattrEnabled = xattrEnabled();
    }
    SolarisFileStore(UnixFileSystem fs, UnixMountEntry entry) throws IOException {
        super(fs, entry);
        this.xattrEnabled = xattrEnabled();
    }
    private boolean xattrEnabled() {
        long res = 0L;
        try {
            res = pathconf(file(), _PC_XATTR_ENABLED);
        } catch (UnixException x) {
        }
        return (res != 0L);
    }
    @Override
    UnixMountEntry findMountEntry() throws IOException {
        for (UnixMountEntry entry: file().getFileSystem().getMountEntries()) {
            if (entry.dev() == dev()) {
                return entry;
            }
        }
        throw new IOException("Device not found in mnttab");
    }
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == AclFileAttributeView.class) {
            FeatureStatus status = checkIfFeaturePresent("nfsv4acl");
            switch (status) {
                case PRESENT     : return true;
                case NOT_PRESENT : return false;
                default :
                    return (type().equals("zfs"));
            }
        }
        if (type == UserDefinedFileAttributeView.class) {
            FeatureStatus status = checkIfFeaturePresent("xattr");
            switch (status) {
                case PRESENT     : return true;
                case NOT_PRESENT : return false;
                default :
                    return xattrEnabled;
            }
        }
        return super.supportsFileAttributeView(type);
    }
    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("acl"))
            return supportsFileAttributeView(AclFileAttributeView.class);
        if (name.equals("user"))
            return supportsFileAttributeView(UserDefinedFileAttributeView.class);
        return super.supportsFileAttributeView(name);
    }
}
