class LinuxFileStore
    extends UnixFileStore
{
    private volatile boolean xattrChecked;
    private volatile boolean xattrEnabled;
    LinuxFileStore(UnixPath file) throws IOException {
        super(file);
    }
    LinuxFileStore(UnixFileSystem fs, UnixMountEntry entry) throws IOException {
        super(fs, entry);
    }
    @Override
    UnixMountEntry findMountEntry() throws IOException {
        LinuxFileSystem fs = (LinuxFileSystem)file().getFileSystem();
        UnixPath path = null;
        try {
            byte[] rp = UnixNativeDispatcher.realpath(file());
            path = new UnixPath(fs, rp);
        } catch (UnixException x) {
            x.rethrowAsIOException(file());
        }
        UnixPath parent = path.getParent();
        while (parent != null) {
            UnixFileAttributes attrs = null;
            try {
                attrs = UnixFileAttributes.get(parent, true);
            } catch (UnixException x) {
                x.rethrowAsIOException(parent);
            }
            if (attrs.dev() != dev())
                break;
            path = parent;
            parent = parent.getParent();
        }
        byte[] dir = path.asByteArray();
        for (UnixMountEntry entry: fs.getMountEntries("/proc/mounts")) {
            if (Arrays.equals(dir, entry.dir()))
                return entry;
        }
        throw new IOException("Mount point not found");
    }
    private boolean isExtendedAttributesEnabled(UnixPath path) {
        try {
            int fd = path.openForAttributeAccess(false);
            try {
                LinuxNativeDispatcher.fgetxattr(fd, "user.java".getBytes(), 0L, 0);
                return true;
            } catch (UnixException e) {
                if (e.errno() == UnixConstants.ENODATA)
                    return true;
            } finally {
                UnixNativeDispatcher.close(fd);
            }
        } catch (IOException ignore) {
        }
        return false;
    }
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == DosFileAttributeView.class ||
            type == UserDefinedFileAttributeView.class)
        {
            FeatureStatus status = checkIfFeaturePresent("user_xattr");
            if (status == FeatureStatus.PRESENT)
                return true;
            if (status == FeatureStatus.NOT_PRESENT)
                return false;
            if ((entry().hasOption("user_xattr")))
                return true;
            if (entry().fstype().equals("ext3") || entry().fstype().equals("ext4"))
                return false;
            if (!xattrChecked) {
                UnixPath dir = new UnixPath(file().getFileSystem(), entry().dir());
                xattrEnabled = isExtendedAttributesEnabled(dir);
                xattrChecked = true;
            }
            return xattrEnabled;
        }
        return super.supportsFileAttributeView(type);
    }
    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("dos"))
            return supportsFileAttributeView(DosFileAttributeView.class);
        if (name.equals("user"))
            return supportsFileAttributeView(UserDefinedFileAttributeView.class);
        return super.supportsFileAttributeView(name);
    }
}
