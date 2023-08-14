class UnixFileStoreAttributes {
    private long f_frsize;          
    private long f_blocks;          
    private long f_bfree;           
    private long f_bavail;          
    private UnixFileStoreAttributes() {
    }
    static UnixFileStoreAttributes get(UnixPath path) throws UnixException {
        UnixFileStoreAttributes attrs = new UnixFileStoreAttributes();
        UnixNativeDispatcher.statvfs(path, attrs);
        return attrs;
    }
    long blockSize() {
        return f_frsize;
    }
    long totalBlocks() {
        return f_blocks;
    }
    long freeBlocks() {
        return f_bfree;
    }
    long availableBlocks() {
        return f_bavail;
    }
}
