public abstract class FileChannel extends AbstractInterruptibleChannel
        implements GatheringByteChannel, ScatteringByteChannel, ByteChannel {
    public static class MapMode {
        public static final MapMode PRIVATE = new MapMode("PRIVATE"); 
        public static final MapMode READ_ONLY = new MapMode("READ_ONLY"); 
        public static final MapMode READ_WRITE = new MapMode("READ_WRITE"); 
        private final String displayName;
        private MapMode(String displayName) {
            super();
            this.displayName = displayName;
        }
        @Override
        public String toString() {
            return displayName;
        }
    }
    protected FileChannel() {
        super();
    }
    public abstract void force(boolean metadata) throws IOException;
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }
    public abstract FileLock lock(long position, long size, boolean shared)
            throws IOException;
    public abstract MappedByteBuffer map(FileChannel.MapMode mode,
            long position, long size) throws IOException;
    public abstract long position() throws IOException;
    public abstract FileChannel position(long offset) throws IOException;
    public abstract int read(ByteBuffer buffer) throws IOException;
    public abstract int read(ByteBuffer buffer, long position)
            throws IOException;
    public final long read(ByteBuffer[] buffers) throws IOException {
        return read(buffers, 0, buffers.length);
    }
    public abstract long read(ByteBuffer[] buffers, int start, int number)
            throws IOException;
    public abstract long size() throws IOException;
    public abstract long transferFrom(ReadableByteChannel src, long position,
            long count) throws IOException;
    public abstract long transferTo(long position, long count,
            WritableByteChannel target) throws IOException;
    public abstract FileChannel truncate(long size) throws IOException;
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }
    public abstract FileLock tryLock(long position, long size, boolean shared)
            throws IOException;
    public abstract int write(ByteBuffer src) throws IOException;
    public abstract int write(ByteBuffer buffer, long position)
            throws IOException;
    public final long write(ByteBuffer[] buffers) throws IOException {
        return write(buffers, 0, buffers.length);
    }
    public abstract long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException;
}
