public class SimpleAsynchronousFileChannelImpl
    extends AsynchronousFileChannelImpl
{
    private static class DefaultExecutorHolder {
        static final ExecutorService defaultExecutor =
            ThreadPool.createDefault().executor();
    }
    private static final FileDispatcher nd = new FileDispatcherImpl();
    private final NativeThreadSet threads = new NativeThreadSet(2);
    SimpleAsynchronousFileChannelImpl(FileDescriptor fdObj,
                                      boolean reading,
                                      boolean writing,
                                      ExecutorService executor)
    {
        super(fdObj, reading, writing, executor);
    }
    public static AsynchronousFileChannel open(FileDescriptor fdo,
                                               boolean reading,
                                               boolean writing,
                                               ThreadPool pool)
    {
        ExecutorService executor = (pool == null) ?
            DefaultExecutorHolder.defaultExecutor : pool.executor();
        return new SimpleAsynchronousFileChannelImpl(fdo, reading, writing, executor);
    }
    @Override
    public void close() throws IOException {
        synchronized (fdObj) {
            if (closed)
                return;     
            closed = true;
        }
        invalidateAllLocks();
        nd.preClose(fdObj);
        threads.signalAndWait();
        closeLock.writeLock().lock();
        try {
        } finally {
            closeLock.writeLock().unlock();
        }
        nd.close(fdObj);
    }
    @Override
    public long size() throws IOException {
        int ti = threads.add();
        try {
            long n = 0L;
            try {
                begin();
                do {
                    n = nd.size(fdObj);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return n;
            } finally {
                end(n >= 0L);
            }
        } finally {
            threads.remove(ti);
        }
    }
    @Override
    public AsynchronousFileChannel truncate(long size) throws IOException {
        if (size < 0L)
            throw new IllegalArgumentException("Negative size");
        if (!writing)
            throw new NonWritableChannelException();
        int ti = threads.add();
        try {
            long n = 0L;
            try {
                begin();
                do {
                    n = nd.size(fdObj);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                if (size < n && isOpen()) {
                    do {
                        n = nd.truncate(fdObj, size);
                    } while ((n == IOStatus.INTERRUPTED) && isOpen());
                }
                return this;
            } finally {
                end(n > 0);
            }
        } finally {
            threads.remove(ti);
        }
    }
    @Override
    public void force(boolean metaData) throws IOException {
        int ti = threads.add();
        try {
            int n = 0;
            try {
                begin();
                do {
                    n = nd.force(fdObj, metaData);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
            } finally {
                end(n >= 0);
            }
        } finally {
            threads.remove(ti);
        }
    }
    @Override
    <A> Future<FileLock> implLock(final long position,
                                  final long size,
                                  final boolean shared,
                                  final A attachment,
                                  final CompletionHandler<FileLock,? super A> handler)
    {
        if (shared && !reading)
            throw new NonReadableChannelException();
        if (!shared && !writing)
            throw new NonWritableChannelException();
        final FileLockImpl fli = addToFileLockTable(position, size, shared);
        if (fli == null) {
            Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invokeIndirectly(handler, attachment, null, exc, executor);
            return null;
        }
        final PendingFuture<FileLock,A> result = (handler == null) ?
            new PendingFuture<FileLock,A>(this) : null;
        Runnable task = new Runnable() {
            public void run() {
                Throwable exc = null;
                int ti = threads.add();
                try {
                    int n;
                    try {
                        begin();
                        do {
                            n = nd.lock(fdObj, true, position, size, shared);
                        } while ((n == FileDispatcher.INTERRUPTED) && isOpen());
                        if (n != FileDispatcher.LOCKED || !isOpen()) {
                            throw new AsynchronousCloseException();
                        }
                    } catch (IOException x) {
                        removeFromFileLockTable(fli);
                        if (!isOpen())
                            x = new AsynchronousCloseException();
                        exc = x;
                    } finally {
                        end();
                    }
                } finally {
                    threads.remove(ti);
                }
                if (handler == null) {
                    result.setResult(fli, exc);
                } else {
                    Invoker.invokeUnchecked(handler, attachment, fli, exc);
                }
            }
        };
        boolean executed = false;
        try {
            executor.execute(task);
            executed = true;
        } finally {
            if (!executed) {
                removeFromFileLockTable(fli);
            }
        }
        return result;
    }
    @Override
    public FileLock tryLock(long position, long size, boolean shared)
        throws IOException
    {
        if (shared && !reading)
            throw new NonReadableChannelException();
        if (!shared && !writing)
            throw new NonWritableChannelException();
        FileLockImpl fli = addToFileLockTable(position, size, shared);
        if (fli == null)
            throw new ClosedChannelException();
        int ti = threads.add();
        boolean gotLock = false;
        try {
            begin();
            int n;
            do {
                n = nd.lock(fdObj, false, position, size, shared);
            } while ((n == FileDispatcher.INTERRUPTED) && isOpen());
            if (n == FileDispatcher.LOCKED && isOpen()) {
                gotLock = true;
                return fli;    
            }
            if (n == FileDispatcher.NO_LOCK)
                return null;    
            if (n == FileDispatcher.INTERRUPTED)
                throw new AsynchronousCloseException();
            throw new AssertionError();
        } finally {
            if (!gotLock)
                removeFromFileLockTable(fli);
            end();
            threads.remove(ti);
        }
    }
    @Override
    protected void implRelease(FileLockImpl fli) throws IOException {
        nd.release(fdObj, fli.position(), fli.size());
    }
    @Override
    <A> Future<Integer> implRead(final ByteBuffer dst,
                                 final long position,
                                 final A attachment,
                                 final CompletionHandler<Integer,? super A> handler)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (!reading)
            throw new NonReadableChannelException();
        if (dst.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        if (!isOpen() || (dst.remaining() == 0)) {
            Throwable exc = (isOpen()) ? null : new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withResult(0, exc);
            Invoker.invokeIndirectly(handler, attachment, 0, exc, executor);
            return null;
        }
        final PendingFuture<Integer,A> result = (handler == null) ?
            new PendingFuture<Integer,A>(this) : null;
        Runnable task = new Runnable() {
            public void run() {
                int n = 0;
                Throwable exc = null;
                int ti = threads.add();
                try {
                    begin();
                    do {
                        n = IOUtil.read(fdObj, dst, position, nd, null);
                    } while ((n == IOStatus.INTERRUPTED) && isOpen());
                    if (n < 0 && !isOpen())
                        throw new AsynchronousCloseException();
                } catch (IOException x) {
                    if (!isOpen())
                        x = new AsynchronousCloseException();
                    exc = x;
                } finally {
                    end();
                    threads.remove(ti);
                }
                if (handler == null) {
                    result.setResult(n, exc);
                } else {
                    Invoker.invokeUnchecked(handler, attachment, n, exc);
                }
            }
        };
        executor.execute(task);
        return result;
    }
    @Override
    <A> Future<Integer> implWrite(final ByteBuffer src,
                                  final long position,
                                  final A attachment,
                                  final CompletionHandler<Integer,? super A> handler)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (!writing)
            throw new NonWritableChannelException();
        if (!isOpen() || (src.remaining() == 0)) {
            Throwable exc = (isOpen()) ? null : new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withResult(0, exc);
            Invoker.invokeIndirectly(handler, attachment, 0, exc, executor);
            return null;
        }
        final PendingFuture<Integer,A> result = (handler == null) ?
            new PendingFuture<Integer,A>(this) : null;
        Runnable task = new Runnable() {
            public void run() {
                int n = 0;
                Throwable exc = null;
                int ti = threads.add();
                try {
                    begin();
                    do {
                        n = IOUtil.write(fdObj, src, position, nd, null);
                    } while ((n == IOStatus.INTERRUPTED) && isOpen());
                    if (n < 0 && !isOpen())
                        throw new AsynchronousCloseException();
                } catch (IOException x) {
                    if (!isOpen())
                        x = new AsynchronousCloseException();
                    exc = x;
                } finally {
                    end();
                    threads.remove(ti);
                }
                if (handler == null) {
                    result.setResult(n, exc);
                } else {
                    Invoker.invokeUnchecked(handler, attachment, n, exc);
                }
            }
        };
        executor.execute(task);
        return result;
    }
}
