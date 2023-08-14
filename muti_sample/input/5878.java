public abstract class FileChannel
    extends AbstractInterruptibleChannel
    implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel
{
    protected FileChannel() { }
    public static FileChannel open(Path path,
                                   Set<? extends OpenOption> options,
                                   FileAttribute<?>... attrs)
        throws IOException
    {
        FileSystemProvider provider = path.getFileSystem().provider();
        return provider.newFileChannel(path, options, attrs);
    }
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];
    public static FileChannel open(Path path, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return open(path, set, NO_ATTRIBUTES);
    }
    public abstract int read(ByteBuffer dst) throws IOException;
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }
    public abstract int write(ByteBuffer src) throws IOException;
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }
    public abstract long position() throws IOException;
    public abstract FileChannel position(long newPosition) throws IOException;
    public abstract long size() throws IOException;
    public abstract FileChannel truncate(long size) throws IOException;
    public abstract void force(boolean metaData) throws IOException;
    public abstract long transferTo(long position, long count,
                                    WritableByteChannel target)
        throws IOException;
    public abstract long transferFrom(ReadableByteChannel src,
                                      long position, long count)
        throws IOException;
    public abstract int read(ByteBuffer dst, long position) throws IOException;
    public abstract int write(ByteBuffer src, long position) throws IOException;
    public static class MapMode {
        public static final MapMode READ_ONLY
            = new MapMode("READ_ONLY");
        public static final MapMode READ_WRITE
            = new MapMode("READ_WRITE");
        public static final MapMode PRIVATE
            = new MapMode("PRIVATE");
        private final String name;
        private MapMode(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
    public abstract MappedByteBuffer map(MapMode mode,
                                         long position, long size)
        throws IOException;
    public abstract FileLock lock(long position, long size, boolean shared)
        throws IOException;
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }
    public abstract FileLock tryLock(long position, long size, boolean shared)
        throws IOException;
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }
}
