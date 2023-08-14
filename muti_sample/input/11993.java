class UnixFileAttributes
    implements PosixFileAttributes
{
    private int     st_mode;
    private long    st_ino;
    private long    st_dev;
    private long    st_rdev;
    private int     st_nlink;
    private int     st_uid;
    private int     st_gid;
    private long    st_size;
    private long    st_atime;
    private long    st_mtime;
    private long    st_ctime;
    private volatile UserPrincipal owner;
    private volatile GroupPrincipal group;
    private volatile UnixFileKey key;
    private UnixFileAttributes() {
    }
    static UnixFileAttributes get(UnixPath path, boolean followLinks)
        throws UnixException
    {
        UnixFileAttributes attrs = new UnixFileAttributes();
        if (followLinks) {
            UnixNativeDispatcher.stat(path, attrs);
        } else {
            UnixNativeDispatcher.lstat(path, attrs);
        }
        return attrs;
    }
    static UnixFileAttributes get(int fd) throws UnixException {
        UnixFileAttributes attrs = new UnixFileAttributes();
        UnixNativeDispatcher.fstat(fd, attrs);
        return attrs;
    }
    static UnixFileAttributes get(int dfd, UnixPath path, boolean followLinks)
        throws UnixException
    {
        UnixFileAttributes attrs = new UnixFileAttributes();
        int flag = (followLinks) ? 0 : UnixConstants.AT_SYMLINK_NOFOLLOW;
        UnixNativeDispatcher.fstatat(dfd, path.asByteArray(), flag, attrs);
        return attrs;
    }
    boolean isSameFile(UnixFileAttributes attrs) {
        return ((st_ino == attrs.st_ino) && (st_dev == attrs.st_dev));
    }
    int mode()  { return st_mode; }
    long ino()  { return st_ino; }
    long dev()  { return st_dev; }
    long rdev() { return st_rdev; }
    int nlink() { return st_nlink; }
    int uid()   { return st_uid; }
    int gid()   { return st_gid; }
    FileTime ctime() {
        return FileTime.from(st_ctime, TimeUnit.SECONDS);
    }
    boolean isDevice() {
        int type = st_mode & UnixConstants.S_IFMT;
        return (type == UnixConstants.S_IFCHR ||
                type == UnixConstants.S_IFBLK  ||
                type == UnixConstants.S_IFIFO);
    }
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(st_mtime, TimeUnit.SECONDS);
    }
    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(st_atime, TimeUnit.SECONDS);
    }
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }
    @Override
    public boolean isRegularFile() {
       return ((st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFREG);
    }
    @Override
    public boolean isDirectory() {
        return ((st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFDIR);
    }
    @Override
    public boolean isSymbolicLink() {
        return ((st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFLNK);
    }
    @Override
    public boolean isOther() {
        int type = st_mode & UnixConstants.S_IFMT;
        return (type != UnixConstants.S_IFREG &&
                type != UnixConstants.S_IFDIR &&
                type != UnixConstants.S_IFLNK);
    }
    @Override
    public long size() {
        return st_size;
    }
    @Override
    public UnixFileKey fileKey() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    key = new UnixFileKey(st_dev, st_ino);
                }
            }
        }
        return key;
    }
    @Override
    public UserPrincipal owner() {
        if (owner == null) {
            synchronized (this) {
                if (owner == null) {
                    owner = UnixUserPrincipals.fromUid(st_uid);
                }
            }
        }
        return owner;
    }
    @Override
    public GroupPrincipal group() {
        if (group == null) {
            synchronized (this) {
                if (group == null) {
                    group = UnixUserPrincipals.fromGid(st_gid);
                }
            }
        }
        return group;
    }
    @Override
    public Set<PosixFilePermission> permissions() {
        int bits = (st_mode & UnixConstants.S_IAMB);
        HashSet<PosixFilePermission> perms = new HashSet<>();
        if ((bits & UnixConstants.S_IRUSR) > 0)
            perms.add(PosixFilePermission.OWNER_READ);
        if ((bits & UnixConstants.S_IWUSR) > 0)
            perms.add(PosixFilePermission.OWNER_WRITE);
        if ((bits & UnixConstants.S_IXUSR) > 0)
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        if ((bits & UnixConstants.S_IRGRP) > 0)
            perms.add(PosixFilePermission.GROUP_READ);
        if ((bits & UnixConstants.S_IWGRP) > 0)
            perms.add(PosixFilePermission.GROUP_WRITE);
        if ((bits & UnixConstants.S_IXGRP) > 0)
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        if ((bits & UnixConstants.S_IROTH) > 0)
            perms.add(PosixFilePermission.OTHERS_READ);
        if ((bits & UnixConstants.S_IWOTH) > 0)
            perms.add(PosixFilePermission.OTHERS_WRITE);
        if ((bits & UnixConstants.S_IXOTH) > 0)
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        return perms;
    }
    BasicFileAttributes asBasicFileAttributes() {
        return UnixAsBasicFileAttributes.wrap(this);
    }
    static UnixFileAttributes toUnixFileAttributes(BasicFileAttributes attrs) {
        if (attrs instanceof UnixFileAttributes)
            return (UnixFileAttributes)attrs;
        if (attrs instanceof UnixAsBasicFileAttributes) {
            return ((UnixAsBasicFileAttributes)attrs).unwrap();
        }
        return null;
    }
    private static class UnixAsBasicFileAttributes implements BasicFileAttributes {
        private final UnixFileAttributes attrs;
        private UnixAsBasicFileAttributes(UnixFileAttributes attrs) {
            this.attrs = attrs;
        }
        static UnixAsBasicFileAttributes wrap(UnixFileAttributes attrs) {
            return new UnixAsBasicFileAttributes(attrs);
        }
        UnixFileAttributes unwrap() {
            return attrs;
        }
        @Override
        public FileTime lastModifiedTime() {
            return attrs.lastModifiedTime();
        }
        @Override
        public FileTime lastAccessTime() {
            return attrs.lastAccessTime();
        }
        @Override
        public FileTime creationTime() {
            return attrs.creationTime();
        }
        @Override
        public boolean isRegularFile() {
            return attrs.isRegularFile();
        }
        @Override
        public boolean isDirectory() {
            return attrs.isDirectory();
        }
        @Override
        public boolean isSymbolicLink() {
            return attrs.isSymbolicLink();
        }
        @Override
        public boolean isOther() {
            return attrs.isOther();
        }
        @Override
        public long size() {
            return attrs.size();
        }
        @Override
        public Object fileKey() {
            return attrs.fileKey();
        }
    }
}
