public abstract class AsynchronousFileChannel
    implements AsynchronousChannel
{
    protected AsynchronousFileChannel() {
    }
    public static AsynchronousFileChannel open(Path file,
                                               Set<? extends OpenOption> options,
                                               ExecutorService executor,
                                               FileAttribute<?>... attrs)
        throws IOException
    {
        FileSystemProvider provider = file.getFileSystem().provider();
        return provider.newAsynchronousFileChannel(file, options, executor, attrs);
    }
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];
    public static AsynchronousFileChannel open(Path file, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return open(file, set, null, NO_ATTRIBUTES);
    }
    public abstract long size() throws IOException;
    public abstract AsynchronousFileChannel truncate(long size) throws IOException;
    public abstract void force(boolean metaData) throws IOException;
    public abstract <A> void lock(long position,
                                  long size,
                                  boolean shared,
                                  A attachment,
                                  CompletionHandler<FileLock,? super A> handler);
    public final <A> void lock(A attachment,
                               CompletionHandler<FileLock,? super A> handler)
    {
        lock(0L, Long.MAX_VALUE, false, attachment, handler);
    }
    public abstract Future<FileLock> lock(long position, long size, boolean shared);
    public final Future<FileLock> lock() {
        return lock(0L, Long.MAX_VALUE, false);
    }
    public abstract FileLock tryLock(long position, long size, boolean shared)
        throws IOException;
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }
    public abstract <A> void read(ByteBuffer dst,
                                  long position,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);
    public abstract Future<Integer> read(ByteBuffer dst, long position);
    public abstract <A> void write(ByteBuffer src,
                                   long position,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);
    public abstract Future<Integer> write(ByteBuffer src, long position);
}
