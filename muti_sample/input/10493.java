public class ZipFileStore extends FileStore {
    private final ZipFileSystem zfs;
    ZipFileStore(ZipPath zpath) {
        this.zfs = (ZipFileSystem)zpath.getFileSystem();
    }
    @Override
    public String name() {
        return zfs.toString() + "/";
    }
    @Override
    public String type() {
        return "zipfs";
    }
    @Override
    public boolean isReadOnly() {
        return zfs.isReadOnly();
    }
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return (type == BasicFileAttributeView.class ||
                type == ZipFileAttributeView.class);
    }
    @Override
    public boolean supportsFileAttributeView(String name) {
        return name.equals("basic") || name.equals("zip");
    }
    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        if (type == null)
            throw new NullPointerException();
        return (V)null;
    }
    @Override
    public long getTotalSpace() throws IOException {
         return new ZipFileStoreAttributes(this).totalSpace();
    }
    @Override
    public long getUsableSpace() throws IOException {
         return new ZipFileStoreAttributes(this).usableSpace();
    }
    @Override
    public long getUnallocatedSpace() throws IOException {
         return new ZipFileStoreAttributes(this).unallocatedSpace();
    }
    @Override
    public Object getAttribute(String attribute) throws IOException {
         if (attribute.equals("totalSpace"))
               return getTotalSpace();
         if (attribute.equals("usableSpace"))
               return getUsableSpace();
         if (attribute.equals("unallocatedSpace"))
               return getUnallocatedSpace();
         throw new UnsupportedOperationException("does not support the given attribute");
    }
    private static class ZipFileStoreAttributes {
        final FileStore fstore;
        final long size;
        public ZipFileStoreAttributes(ZipFileStore fileStore)
            throws IOException
        {
            Path path = FileSystems.getDefault().getPath(fileStore.name());
            this.size = Files.size(path);
            this.fstore = Files.getFileStore(path);
        }
        public long totalSpace() {
            return size;
        }
        public long usableSpace() throws IOException {
            if (!fstore.isReadOnly())
                return fstore.getUsableSpace();
            return 0;
        }
        public long unallocatedSpace()  throws IOException {
            if (!fstore.isReadOnly())
                return fstore.getUnallocatedSpace();
            return 0;
        }
    }
}
