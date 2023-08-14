abstract class UnixFileStore
    extends FileStore
{
    private final UnixPath file;
    private final long dev;
    private final UnixMountEntry entry;
    private static long devFor(UnixPath file) throws IOException {
        try {
            return UnixFileAttributes.get(file, true).dev();
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return 0L;  
        }
    }
    UnixFileStore(UnixPath file) throws IOException {
        this.file = file;
        this.dev = devFor(file);
        this.entry = findMountEntry();
    }
    UnixFileStore(UnixFileSystem fs, UnixMountEntry entry) throws IOException {
        this.file = new UnixPath(fs, entry.dir());
        this.dev = (entry.dev() == 0L) ? devFor(this.file) : entry.dev();
        this.entry = entry;
    }
    abstract UnixMountEntry findMountEntry() throws IOException;
    UnixPath file() {
        return file;
    }
    long dev() {
        return dev;
    }
    UnixMountEntry entry() {
        return entry;
    }
    @Override
    public String name() {
        return entry.name();
    }
    @Override
    public String type() {
        return entry.fstype();
    }
    @Override
    public boolean isReadOnly() {
        return entry.isReadOnly();
    }
    private UnixFileStoreAttributes readAttributes() throws IOException {
        try {
            return UnixFileStoreAttributes.get(file);
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null;    
        }
    }
    @Override
    public long getTotalSpace() throws IOException {
        UnixFileStoreAttributes attrs = readAttributes();
        return attrs.blockSize() * attrs.totalBlocks();
    }
    @Override
    public long getUsableSpace() throws IOException {
       UnixFileStoreAttributes attrs = readAttributes();
       return attrs.blockSize() * attrs.availableBlocks();
    }
    @Override
    public long getUnallocatedSpace() throws IOException {
        UnixFileStoreAttributes attrs = readAttributes();
        return attrs.blockSize() * attrs.freeBlocks();
    }
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> view)
    {
        if (view == null)
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
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == null)
            throw new NullPointerException();
        if (type == BasicFileAttributeView.class)
            return true;
        if (type == PosixFileAttributeView.class ||
            type == FileOwnerAttributeView.class)
        {
            FeatureStatus status = checkIfFeaturePresent("posix");
            return (status != FeatureStatus.NOT_PRESENT);
        }
        return false;
    }
    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("basic") || name.equals("unix"))
            return true;
        if (name.equals("posix"))
            return supportsFileAttributeView(PosixFileAttributeView.class);
        if (name.equals("owner"))
            return supportsFileAttributeView(FileOwnerAttributeView.class);
        return false;
    }
    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof UnixFileStore))
            return false;
        UnixFileStore other = (UnixFileStore)ob;
        return (this.dev == other.dev) &&
               Arrays.equals(this.entry.dir(), other.entry.dir());
    }
    @Override
    public int hashCode() {
        return (int)(dev ^ (dev >>> 32)) ^ Arrays.hashCode(entry.dir());
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(new String(entry.dir()));
        sb.append(" (");
        sb.append(entry.name());
        sb.append(")");
        return sb.toString();
    }
    private static final Object loadLock = new Object();
    private static volatile Properties props;
    enum FeatureStatus {
        PRESENT,
        NOT_PRESENT,
        UNKNOWN;
    }
    FeatureStatus checkIfFeaturePresent(String feature) {
        if (props == null) {
            synchronized (loadLock) {
                if (props == null) {
                    props = AccessController.doPrivileged(
                        new PrivilegedAction<Properties>() {
                            @Override
                            public Properties run() {
                                return loadProperties();
                            }});
                }
            }
        }
        String value = props.getProperty(type());
        if (value != null) {
            String[] values = value.split("\\s");
            for (String s: values) {
                s = s.trim().toLowerCase();
                if (s.equals(feature)) {
                    return FeatureStatus.PRESENT;
                }
                if (s.startsWith("no")) {
                    s = s.substring(2);
                    if (s.equals(feature)) {
                        return FeatureStatus.NOT_PRESENT;
                    }
                }
            }
        }
        return FeatureStatus.UNKNOWN;
    }
    private static Properties loadProperties() {
        Properties result = new Properties();
        String fstypes = System.getProperty("java.home") + "/lib/fstypes.properties";
        Path file = Paths.get(fstypes);
        try {
            try (ReadableByteChannel rbc = Files.newByteChannel(file)) {
                result.load(Channels.newReader(rbc, "UTF-8"));
            }
        } catch (IOException x) {
        }
        return result;
    }
}
