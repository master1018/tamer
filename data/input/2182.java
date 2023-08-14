abstract class AsynchronousFileChannelImpl
    extends AsynchronousFileChannel
{
    protected final ReadWriteLock closeLock = new ReentrantReadWriteLock();
    protected volatile boolean closed;
    protected final FileDescriptor fdObj;
    protected final boolean reading;
    protected final boolean writing;
    protected final ExecutorService executor;
    protected AsynchronousFileChannelImpl(FileDescriptor fdObj,
                                          boolean reading,
                                          boolean writing,
                                          ExecutorService executor)
    {
        this.fdObj = fdObj;
        this.reading = reading;
        this.writing = writing;
        this.executor = executor;
    }
    final ExecutorService executor() {
        return executor;
    }
    @Override
    public final boolean isOpen() {
        return !closed;
    }
    protected final void begin() throws IOException {
        closeLock.readLock().lock();
        if (closed)
            throw new ClosedChannelException();
    }
    protected final void end() {
        closeLock.readLock().unlock();
    }
    protected final void end(boolean completed) throws IOException {
        end();
        if (!completed && !isOpen())
            throw new AsynchronousCloseException();
    }
    abstract <A> Future<FileLock> implLock(long position,
                                           long size,
                                           boolean shared,
                                           A attachment,
                                           CompletionHandler<FileLock,? super A> handler);
    @Override
    public final Future<FileLock> lock(long position,
                                       long size,
                                       boolean shared)
    {
        return implLock(position, size, shared, null, null);
    }
    @Override
    public final <A> void lock(long position,
                               long size,
                               boolean shared,
                               A attachment,
                               CompletionHandler<FileLock,? super A> handler)
    {
        if (handler == null)
            throw new NullPointerException("'handler' is null");
        implLock(position, size, shared, attachment, handler);
    }
    private volatile FileLockTable fileLockTable;
    final void ensureFileLockTableInitialized() throws IOException {
        if (fileLockTable == null) {
            synchronized (this) {
                if (fileLockTable == null) {
                    fileLockTable = FileLockTable.newSharedFileLockTable(this, fdObj);
                }
            }
        }
    }
    final void invalidateAllLocks() throws IOException {
        if (fileLockTable != null) {
            for (FileLock fl: fileLockTable.removeAll()) {
                synchronized (fl) {
                    if (fl.isValid()) {
                        FileLockImpl fli = (FileLockImpl)fl;
                        implRelease(fli);
                        fli.invalidate();
                    }
                }
            }
        }
    }
    protected final FileLockImpl addToFileLockTable(long position, long size, boolean shared) {
        final FileLockImpl fli;
        try {
            closeLock.readLock().lock();
            if (closed)
                return null;
            try {
                ensureFileLockTableInitialized();
            } catch (IOException x) {
                throw new AssertionError(x);
            }
            fli = new FileLockImpl(this, position, size, shared);
            fileLockTable.add(fli);
        } finally {
            end();
        }
        return fli;
    }
    protected final void removeFromFileLockTable(FileLockImpl fli) {
        fileLockTable.remove(fli);
    }
    protected abstract void implRelease(FileLockImpl fli) throws IOException;
    final void release(FileLockImpl fli) throws IOException {
        try {
            begin();
            implRelease(fli);
            removeFromFileLockTable(fli);
        } finally {
            end();
        }
    }
    abstract <A> Future<Integer> implRead(ByteBuffer dst,
                                         long position,
                                         A attachment,
                                         CompletionHandler<Integer,? super A> handler);
    @Override
    public final Future<Integer> read(ByteBuffer dst, long position) {
        return implRead(dst, position, null, null);
    }
    @Override
    public final <A> void read(ByteBuffer dst,
                               long position,
                               A attachment,
                               CompletionHandler<Integer,? super A> handler)
    {
        if (handler == null)
            throw new NullPointerException("'handler' is null");
        implRead(dst, position, attachment, handler);
    }
    abstract <A> Future<Integer> implWrite(ByteBuffer src,
                                           long position,
                                           A attachment,
                                           CompletionHandler<Integer,? super A> handler);
    @Override
    public final Future<Integer> write(ByteBuffer src, long position) {
        return implWrite(src, position, null, null);
    }
    @Override
    public final <A> void write(ByteBuffer src,
                                long position,
                                A attachment,
                                CompletionHandler<Integer,? super A> handler)
    {
        if (handler == null)
            throw new NullPointerException("'handler' is null");
        implWrite(src, position, attachment, handler);
    }
}
