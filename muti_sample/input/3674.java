class WindowsFileAttributes
    implements DosFileAttributes
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final short SIZEOF_FILE_INFORMATION  = 52;
    private static final short OFFSETOF_FILE_INFORMATION_ATTRIBUTES      = 0;
    private static final short OFFSETOF_FILE_INFORMATION_CREATETIME      = 4;
    private static final short OFFSETOF_FILE_INFORMATION_LASTACCESSTIME  = 12;
    private static final short OFFSETOF_FILE_INFORMATION_LASTWRITETIME   = 20;
    private static final short OFFSETOF_FILE_INFORMATION_VOLSERIALNUM    = 28;
    private static final short OFFSETOF_FILE_INFORMATION_SIZEHIGH        = 32;
    private static final short OFFSETOF_FILE_INFORMATION_SIZELOW         = 36;
    private static final short OFFSETOF_FILE_INFORMATION_INDEXHIGH       = 44;
    private static final short OFFSETOF_FILE_INFORMATION_INDEXLOW        = 48;
    private static final short SIZEOF_FILE_ATTRIBUTE_DATA = 36;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_ATTRIBUTES      = 0;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_CREATETIME      = 4;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_LASTACCESSTIME  = 12;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_LASTWRITETIME   = 20;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_SIZEHIGH        = 28;
    private static final short OFFSETOF_FILE_ATTRIBUTE_DATA_SIZELOW         = 32;
    private static final short SIZEOF_FIND_DATA = 592;
    private static final short OFFSETOF_FIND_DATA_ATTRIBUTES = 0;
    private static final short OFFSETOF_FIND_DATA_CREATETIME = 4;
    private static final short OFFSETOF_FIND_DATA_LASTACCESSTIME = 12;
    private static final short OFFSETOF_FIND_DATA_LASTWRITETIME = 20;
    private static final short OFFSETOF_FIND_DATA_SIZEHIGH = 28;
    private static final short OFFSETOF_FIND_DATA_SIZELOW = 32;
    private static final short OFFSETOF_FIND_DATA_RESERVED0 = 36;
    private static final long WINDOWS_EPOCH_IN_MICROSECONDS = -11644473600000000L;
    private static final boolean ensureAccurateMetadata;
    static {
        String propValue = AccessController.doPrivileged(
            new GetPropertyAction("sun.nio.fs.ensureAccurateMetadata", "false"));
        ensureAccurateMetadata = (propValue.length() == 0) ?
            true : Boolean.valueOf(propValue);
    }
    private final int fileAttrs;
    private final long creationTime;
    private final long lastAccessTime;
    private final long lastWriteTime;
    private final long size;
    private final int reparseTag;
    private final int volSerialNumber;
    private final int fileIndexHigh;
    private final int fileIndexLow;
    static FileTime toFileTime(long time) {
        time /= 10L;
        time += WINDOWS_EPOCH_IN_MICROSECONDS;
        return FileTime.from(time, TimeUnit.MICROSECONDS);
    }
    static long toWindowsTime(FileTime time) {
        long value = time.to(TimeUnit.MICROSECONDS);
        value -= WINDOWS_EPOCH_IN_MICROSECONDS;
        value *= 10L;
        return value;
    }
    private WindowsFileAttributes(int fileAttrs,
                                  long creationTime,
                                  long lastAccessTime,
                                  long lastWriteTime,
                                  long size,
                                  int reparseTag,
                                  int volSerialNumber,
                                  int fileIndexHigh,
                                  int fileIndexLow)
    {
        this.fileAttrs = fileAttrs;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastWriteTime = lastWriteTime;
        this.size = size;
        this.reparseTag = reparseTag;
        this.volSerialNumber = volSerialNumber;
        this.fileIndexHigh = fileIndexHigh;
        this.fileIndexLow = fileIndexLow;
    }
    private static WindowsFileAttributes fromFileInformation(long address, int reparseTag) {
        int fileAttrs = unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_ATTRIBUTES);
        long creationTime = unsafe.getLong(address + OFFSETOF_FILE_INFORMATION_CREATETIME);
        long lastAccessTime = unsafe.getLong(address + OFFSETOF_FILE_INFORMATION_LASTACCESSTIME);
        long lastWriteTime = unsafe.getLong(address + OFFSETOF_FILE_INFORMATION_LASTWRITETIME);
        long size = ((long)(unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_SIZEHIGH)) << 32)
            + (unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_SIZELOW) & 0xFFFFFFFFL);
        int volSerialNumber = unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_VOLSERIALNUM);
        int fileIndexHigh = unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_INDEXHIGH);
        int fileIndexLow = unsafe.getInt(address + OFFSETOF_FILE_INFORMATION_INDEXLOW);
        return new WindowsFileAttributes(fileAttrs,
                                         creationTime,
                                         lastAccessTime,
                                         lastWriteTime,
                                         size,
                                         reparseTag,
                                         volSerialNumber,
                                         fileIndexHigh,
                                         fileIndexLow);
    }
    private static WindowsFileAttributes fromFileAttributeData(long address, int reparseTag) {
        int fileAttrs = unsafe.getInt(address + OFFSETOF_FILE_ATTRIBUTE_DATA_ATTRIBUTES);
        long creationTime = unsafe.getLong(address + OFFSETOF_FILE_ATTRIBUTE_DATA_CREATETIME);
        long lastAccessTime = unsafe.getLong(address + OFFSETOF_FILE_ATTRIBUTE_DATA_LASTACCESSTIME);
        long lastWriteTime = unsafe.getLong(address + OFFSETOF_FILE_ATTRIBUTE_DATA_LASTWRITETIME);
        long size = ((long)(unsafe.getInt(address + OFFSETOF_FILE_ATTRIBUTE_DATA_SIZEHIGH)) << 32)
            + (unsafe.getInt(address + OFFSETOF_FILE_ATTRIBUTE_DATA_SIZELOW) & 0xFFFFFFFFL);
        return new WindowsFileAttributes(fileAttrs,
                                         creationTime,
                                         lastAccessTime,
                                         lastWriteTime,
                                         size,
                                         reparseTag,
                                         0,  
                                         0,  
                                         0); 
    }
    static NativeBuffer getBufferForFindData() {
        return NativeBuffers.getNativeBuffer(SIZEOF_FIND_DATA);
    }
    static WindowsFileAttributes fromFindData(long address) {
        int fileAttrs = unsafe.getInt(address + OFFSETOF_FIND_DATA_ATTRIBUTES);
        long creationTime = unsafe.getLong(address + OFFSETOF_FIND_DATA_CREATETIME);
        long lastAccessTime = unsafe.getLong(address + OFFSETOF_FIND_DATA_LASTACCESSTIME);
        long lastWriteTime = unsafe.getLong(address + OFFSETOF_FIND_DATA_LASTWRITETIME);
        long size = ((long)(unsafe.getInt(address + OFFSETOF_FIND_DATA_SIZEHIGH)) << 32)
            + (unsafe.getInt(address + OFFSETOF_FIND_DATA_SIZELOW) & 0xFFFFFFFFL);
        int reparseTag = isReparsePoint(fileAttrs) ?
            unsafe.getInt(address + OFFSETOF_FIND_DATA_RESERVED0) : 0;
        return new WindowsFileAttributes(fileAttrs,
                                         creationTime,
                                         lastAccessTime,
                                         lastWriteTime,
                                         size,
                                         reparseTag,
                                         0,  
                                         0,  
                                         0); 
    }
    static WindowsFileAttributes readAttributes(long handle)
        throws WindowsException
    {
        NativeBuffer buffer = NativeBuffers
            .getNativeBuffer(SIZEOF_FILE_INFORMATION);
        try {
            long address = buffer.address();
            GetFileInformationByHandle(handle, address);
            int reparseTag = 0;
            int fileAttrs = unsafe
                .getInt(address + OFFSETOF_FILE_INFORMATION_ATTRIBUTES);
            if (isReparsePoint(fileAttrs)) {
                int size = MAXIMUM_REPARSE_DATA_BUFFER_SIZE;
                NativeBuffer reparseBuffer = NativeBuffers.getNativeBuffer(size);
                try {
                    DeviceIoControlGetReparsePoint(handle, reparseBuffer.address(), size);
                    reparseTag = (int)unsafe.getLong(reparseBuffer.address());
                } finally {
                    reparseBuffer.release();
                }
            }
            return fromFileInformation(address, reparseTag);
        } finally {
            buffer.release();
        }
    }
    static WindowsFileAttributes get(WindowsPath path, boolean followLinks)
        throws WindowsException
    {
        if (!ensureAccurateMetadata) {
            WindowsException firstException = null;
            NativeBuffer buffer =
                NativeBuffers.getNativeBuffer(SIZEOF_FILE_ATTRIBUTE_DATA);
            try {
                long address = buffer.address();
                GetFileAttributesEx(path.getPathForWin32Calls(), address);
                int fileAttrs = unsafe
                    .getInt(address + OFFSETOF_FILE_ATTRIBUTE_DATA_ATTRIBUTES);
                if (!isReparsePoint(fileAttrs))
                    return fromFileAttributeData(address, 0);
            } catch (WindowsException x) {
                if (x.lastError() != ERROR_SHARING_VIOLATION)
                    throw x;
                firstException = x;
            } finally {
                buffer.release();
            }
            if (firstException != null) {
                String search = path.getPathForWin32Calls();
                char last = search.charAt(search.length() -1);
                if (last == ':' || last == '\\')
                    throw firstException;
                buffer = getBufferForFindData();
                try {
                    long handle = FindFirstFile(search, buffer.address());
                    FindClose(handle);
                    WindowsFileAttributes attrs = fromFindData(buffer.address());
                    if (attrs.isReparsePoint())
                        throw firstException;
                    return attrs;
                } catch (WindowsException ignore) {
                    throw firstException;
                } finally {
                    buffer.release();
                }
            }
        }
        long handle = path.openForReadAttributeAccess(followLinks);
        try {
            return readAttributes(handle);
        } finally {
            CloseHandle(handle);
        }
    }
    static boolean isSameFile(WindowsFileAttributes attrs1,
                              WindowsFileAttributes attrs2)
    {
        return (attrs1.volSerialNumber == attrs2.volSerialNumber) &&
               (attrs1.fileIndexHigh == attrs2.fileIndexHigh) &&
               (attrs1.fileIndexLow == attrs2.fileIndexLow);
    }
    static boolean isReparsePoint(int attributes) {
        return (attributes & FILE_ATTRIBUTE_REPARSE_POINT) != 0;
    }
    int attributes() {
        return fileAttrs;
    }
    int volSerialNumber() {
        if (volSerialNumber == 0)
            throw new AssertionError("Should not get here");
        return volSerialNumber;
    }
    int fileIndexHigh() {
        if (volSerialNumber == 0)
            throw new AssertionError("Should not get here");
        return fileIndexHigh;
    }
    int fileIndexLow() {
        if (volSerialNumber == 0)
            throw new AssertionError("Should not get here");
        return fileIndexLow;
    }
    @Override
    public long size() {
        return size;
    }
    @Override
    public FileTime lastModifiedTime() {
        return toFileTime(lastWriteTime);
    }
    @Override
    public FileTime lastAccessTime() {
        return toFileTime(lastAccessTime);
    }
    @Override
    public FileTime creationTime() {
        return toFileTime(creationTime);
    }
    @Override
    public Object fileKey() {
        return null;
    }
    boolean isReparsePoint() {
        return isReparsePoint(fileAttrs);
    }
    boolean isDirectoryLink() {
        return isSymbolicLink() && ((fileAttrs & FILE_ATTRIBUTE_DIRECTORY) != 0);
    }
    @Override
    public boolean isSymbolicLink() {
        return reparseTag == IO_REPARSE_TAG_SYMLINK;
    }
    @Override
    public boolean isDirectory() {
        if (isSymbolicLink())
            return false;
        return ((fileAttrs & FILE_ATTRIBUTE_DIRECTORY) != 0);
    }
    @Override
    public boolean isOther() {
        if (isSymbolicLink())
            return false;
        return ((fileAttrs & (FILE_ATTRIBUTE_DEVICE | FILE_ATTRIBUTE_REPARSE_POINT)) != 0);
    }
    @Override
    public boolean isRegularFile() {
        return !isSymbolicLink() && !isDirectory() && !isOther();
    }
    @Override
    public boolean isReadOnly() {
        return (fileAttrs & FILE_ATTRIBUTE_READONLY) != 0;
    }
    @Override
    public boolean isHidden() {
        return (fileAttrs & FILE_ATTRIBUTE_HIDDEN) != 0;
    }
    @Override
    public boolean isArchive() {
        return (fileAttrs & FILE_ATTRIBUTE_ARCHIVE) != 0;
    }
    @Override
    public boolean isSystem() {
        return (fileAttrs & FILE_ATTRIBUTE_SYSTEM) != 0;
    }
}
