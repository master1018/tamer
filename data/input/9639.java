class WindowsFileStore
    extends FileStore
{
    private final String root;
    private final VolumeInformation volInfo;
    private final int volType;
    private final String displayName;   
    private WindowsFileStore(String root) throws WindowsException {
        assert root.charAt(root.length()-1) == '\\';
        this.root = root;
        this.volInfo = GetVolumeInformation(root);
        this.volType = GetDriveType(root);
        String vol = volInfo.volumeName();
        if (vol.length() > 0) {
            this.displayName = vol;
        } else {
            this.displayName = (volType == DRIVE_REMOVABLE) ? "Removable Disk" : "";
        }
    }
    static WindowsFileStore create(String root, boolean ignoreNotReady)
        throws IOException
    {
        try {
            return new WindowsFileStore(root);
        } catch (WindowsException x) {
            if (ignoreNotReady && x.lastError() == ERROR_NOT_READY)
                return null;
            x.rethrowAsIOException(root);
            return null; 
        }
    }
    static WindowsFileStore create(WindowsPath file) throws IOException {
        try {
            String target;
            if (file.getFileSystem().supportsLinks()) {
                target = WindowsLinkSupport.getFinalPath(file, true);
            } else {
                WindowsFileAttributes.get(file, true);
                target = file.getPathForWin32Calls();
            }
            String root = GetVolumePathName(target);
            return new WindowsFileStore(root);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null; 
        }
    }
    VolumeInformation volumeInformation() {
        return volInfo;
    }
    int volumeType() {
        return volType;
    }
    @Override
    public String name() {
        return volInfo.volumeName();   
    }
    @Override
    public String type() {
        return volInfo.fileSystemName();  
    }
    @Override
    public boolean isReadOnly() {
        return ((volInfo.flags() & FILE_READ_ONLY_VOLUME) != 0);
    }
    private DiskFreeSpace readDiskFreeSpace() throws IOException {
        try {
            return GetDiskFreeSpaceEx(root);
        } catch (WindowsException x) {
            x.rethrowAsIOException(root);
            return null;
        }
    }
    @Override
    public long getTotalSpace() throws IOException {
        return readDiskFreeSpace().totalNumberOfBytes();
    }
    @Override
    public long getUsableSpace() throws IOException {
        return readDiskFreeSpace().freeBytesAvailable();
    }
    @Override
    public long getUnallocatedSpace() throws IOException {
        return readDiskFreeSpace().freeBytesAvailable();
    }
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        if (type == null)
            throw new NullPointerException();
        return (V) null;
    }
    @Override
    public Object getAttribute(String attribute) throws IOException {
        if (attribute.equals("totalSpace"))
            return getTotalSpace();
        if (attribute.equals("usableSpace"))
            return getUsableSpace();
        if (attribute.equals("unallocatedSpace"))
            return getUnallocatedSpace();
        if (attribute.equals("volume:vsn"))
            return volInfo.volumeSerialNumber();
        if (attribute.equals("volume:isRemovable"))
            return volType == DRIVE_REMOVABLE;
        if (attribute.equals("volume:isCdrom"))
            return volType == DRIVE_CDROM;
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == null)
            throw new NullPointerException();
        if (type == BasicFileAttributeView.class || type == DosFileAttributeView.class)
            return true;
        if (type == AclFileAttributeView.class || type == FileOwnerAttributeView.class)
            return ((volInfo.flags() & FILE_PERSISTENT_ACLS) != 0);
        if (type == UserDefinedFileAttributeView.class)
            return ((volInfo.flags() & FILE_NAMED_STREAMS) != 0);
        return false;
    }
    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("basic") || name.equals("dos"))
            return true;
        if (name.equals("acl"))
            return supportsFileAttributeView(AclFileAttributeView.class);
        if (name.equals("owner"))
            return supportsFileAttributeView(FileOwnerAttributeView.class);
        if (name.equals("user"))
            return supportsFileAttributeView(UserDefinedFileAttributeView.class);
        return false;
    }
    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof WindowsFileStore))
            return false;
        WindowsFileStore other = (WindowsFileStore)ob;
        return root.equals(other.root);
    }
    @Override
    public int hashCode() {
        return root.hashCode();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(displayName);
        if (sb.length() > 0)
            sb.append(" ");
        sb.append("(");
        sb.append(root.subSequence(0, root.length()-1));
        sb.append(")");
        return sb.toString();
    }
 }
