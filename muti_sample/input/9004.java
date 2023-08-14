public class WindowsAsynchronousFileChannelImpl
    extends AsynchronousFileChannelImpl
    implements Iocp.OverlappedChannel, Groupable
{
    private static final JavaIOFileDescriptorAccess fdAccess =
        SharedSecrets.getJavaIOFileDescriptorAccess();
    private static final int ERROR_HANDLE_EOF = 38;
    private static class DefaultIocpHolder {
        static final Iocp defaultIocp = defaultIocp();
        private static Iocp defaultIocp() {
            try {
                return new Iocp(null, ThreadPool.createDefault()).start();
            } catch (IOException ioe) {
                InternalError e = new InternalError();
                e.initCause(ioe);
                throw e;
            }
        }
    }
    private static final FileDispatcher nd = new FileDispatcherImpl();
    private final long handle;
    private final int completionKey;
    private final Iocp iocp;
    private final boolean isDefaultIocp;
    private final PendingIoCache ioCache;
    private WindowsAsynchronousFileChannelImpl(FileDescriptor fdObj,
                                               boolean reading,
                                               boolean writing,
                                               Iocp iocp,
                                               boolean isDefaultIocp)
        throws IOException
    {
        super(fdObj, reading, writing, iocp.executor());
        this.handle = fdAccess.getHandle(fdObj);
        this.iocp = iocp;
        this.isDefaultIocp = isDefaultIocp;
        this.ioCache = new PendingIoCache();
        this.completionKey = iocp.associate(this, handle);
    }
    public static AsynchronousFileChannel open(FileDescriptor fdo,
                                               boolean reading,
                                               boolean writing,
                                               ThreadPool pool)
        throws IOException
    {
        Iocp iocp;
        boolean isDefaultIocp;
        if (pool == null) {
            iocp = DefaultIocpHolder.defaultIocp;
            isDefaultIocp = true;
        } else {
            iocp = new Iocp(null, pool).start();
            isDefaultIocp = false;
        }
        try {
            return new
                WindowsAsynchronousFileChannelImpl(fdo, reading, writing, iocp, isDefaultIocp);
        } catch (IOException x) {
            if (!isDefaultIocp)
                iocp.implClose();
            throw x;
        }
    }
    @Override
    public <V,A> PendingFuture<V,A> getByOverlapped(long overlapped) {
        return ioCache.remove(overlapped);
    }
    @Override
    public void close() throws IOException {
        closeLock.writeLock().lock();
        try {
            if (closed)
                return;     
            closed = true;
        } finally {
            closeLock.writeLock().unlock();
        }
        invalidateAllLocks();
        close0(handle);
        ioCache.close();
        iocp.disassociate(completionKey);
        if (!isDefaultIocp)
            iocp.detachFromThreadPool();
    }
    @Override
    public AsynchronousChannelGroupImpl group() {
        return iocp;
    }
    private static IOException toIOException(Throwable x) {
        if (x instanceof IOException) {
            if (x instanceof ClosedChannelException)
                x = new AsynchronousCloseException();
            return (IOException)x;
        }
        return new IOException(x);
    }
    @Override
    public long size() throws IOException {
        try {
            begin();
            return nd.size(fdObj);
        } finally {
            end();
        }
    }
    @Override
    public AsynchronousFileChannel truncate(long size) throws IOException {
        if (size < 0)
            throw new IllegalArgumentException("Negative size");
        if (!writing)
            throw new NonWritableChannelException();
        try {
            begin();
            if (size > nd.size(fdObj))
                return this;
            nd.truncate(fdObj, size);
        } finally {
            end();
        }
        return this;
    }
    @Override
    public void force(boolean metaData) throws IOException {
        try {
            begin();
            nd.force(fdObj, metaData);
        } finally {
            end();
        }
    }
    private class LockTask<A> implements Runnable, Iocp.ResultHandler {
        private final long position;
        private final FileLockImpl fli;
        private final PendingFuture<FileLock,A> result;
        LockTask(long position,
                 FileLockImpl fli,
                 PendingFuture<FileLock,A> result)
        {
            this.position = position;
            this.fli = fli;
            this.result = result;
        }
        @Override
        public void run() {
            long overlapped = 0L;
            try {
                begin();
                overlapped = ioCache.add(result);
                synchronized (result) {
                    int n = lockFile(handle, position, fli.size(), fli.isShared(),
                                     overlapped);
                    if (n == IOStatus.UNAVAILABLE) {
                        return;
                    }
                    result.setResult(fli);
                }
            } catch (Throwable x) {
                removeFromFileLockTable(fli);
                if (overlapped != 0L)
                    ioCache.remove(overlapped);
                result.setFailure(toIOException(x));
            } finally {
                end();
            }
            Invoker.invoke(result);
        }
        @Override
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            result.setResult(fli);
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            removeFromFileLockTable(fli);
            if (isOpen()) {
                result.setFailure(x);
            } else {
                result.setFailure(new AsynchronousCloseException());
            }
            Invoker.invoke(result);
        }
    }
    @Override
    <A> Future<FileLock> implLock(final long position,
                                  final long size,
                                  final boolean shared,
                                  A attachment,
                                  final CompletionHandler<FileLock,? super A> handler)
    {
        if (shared && !reading)
            throw new NonReadableChannelException();
        if (!shared && !writing)
            throw new NonWritableChannelException();
        FileLockImpl fli = addToFileLockTable(position, size, shared);
        if (fli == null) {
            Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invoke(this, handler, attachment, null, exc);
            return null;
        }
        PendingFuture<FileLock,A> result =
            new PendingFuture<FileLock,A>(this, handler, attachment);
        LockTask lockTask = new LockTask<A>(position, fli, result);
        result.setContext(lockTask);
        if (Iocp.supportsThreadAgnosticIo()) {
            lockTask.run();
        } else {
            boolean executed = false;
            try {
                Invoker.invokeOnThreadInThreadPool(this, lockTask);
                executed = true;
            } finally {
                if (!executed) {
                    removeFromFileLockTable(fli);
                }
            }
        }
        return result;
    }
    static final int NO_LOCK = -1;       
    static final int LOCKED = 0;         
    @Override
    public FileLock tryLock(long position, long size, boolean shared)
        throws IOException
    {
        if (shared && !reading)
            throw new NonReadableChannelException();
        if (!shared && !writing)
            throw new NonWritableChannelException();
        final FileLockImpl fli = addToFileLockTable(position, size, shared);
        if (fli == null)
            throw new ClosedChannelException();
        boolean gotLock = false;
        try {
            begin();
            int res = nd.lock(fdObj, false, position, size, shared);
            if (res == NO_LOCK)
                return null;
            gotLock = true;
            return fli;
        } finally {
            if (!gotLock)
                removeFromFileLockTable(fli);
            end();
        }
    }
    @Override
    protected void implRelease(FileLockImpl fli) throws IOException {
        nd.release(fdObj, fli.position(), fli.size());
    }
    private class ReadTask<A> implements Runnable, Iocp.ResultHandler {
        private final ByteBuffer dst;
        private final int pos, rem;     
        private final long position;    
        private final PendingFuture<Integer,A> result;
        private volatile ByteBuffer buf;
        ReadTask(ByteBuffer dst,
                 int pos,
                 int rem,
                 long position,
                 PendingFuture<Integer,A> result)
        {
            this.dst = dst;
            this.pos = pos;
            this.rem = rem;
            this.position = position;
            this.result = result;
        }
        void releaseBufferIfSubstituted() {
            if (buf != dst)
                Util.releaseTemporaryDirectBuffer(buf);
        }
        void updatePosition(int bytesTransferred) {
            if (bytesTransferred > 0) {
                if (buf == dst) {
                    try {
                        dst.position(pos + bytesTransferred);
                    } catch (IllegalArgumentException x) {
                    }
                } else {
                    buf.position(bytesTransferred).flip();
                    try {
                        dst.put(buf);
                    } catch (BufferOverflowException x) {
                    }
                }
            }
        }
        @Override
        public void run() {
            int n = -1;
            long overlapped = 0L;
            long address;
            if (dst instanceof DirectBuffer) {
                buf = dst;
                address = ((DirectBuffer)dst).address() + pos;
            } else {
                buf = Util.getTemporaryDirectBuffer(rem);
                address = ((DirectBuffer)buf).address();
            }
            boolean pending = false;
            try {
                begin();
                overlapped = ioCache.add(result);
                n = readFile(handle, address, rem, position, overlapped);
                if (n == IOStatus.UNAVAILABLE) {
                    pending = true;
                    return;
                } else if (n == IOStatus.EOF) {
                    result.setResult(n);
                } else {
                    throw new InternalError("Unexpected result: " + n);
                }
            } catch (Throwable x) {
                result.setFailure(toIOException(x));
            } finally {
                if (!pending) {
                    if (overlapped != 0L)
                        ioCache.remove(overlapped);
                    releaseBufferIfSubstituted();
                }
                end();
            }
            Invoker.invoke(result);
        }
        @Override
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            updatePosition(bytesTransferred);
            releaseBufferIfSubstituted();
            result.setResult(bytesTransferred);
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            if (error == ERROR_HANDLE_EOF) {
                completed(-1, false);
            } else {
                releaseBufferIfSubstituted();
                if (isOpen()) {
                    result.setFailure(x);
                } else {
                    result.setFailure(new AsynchronousCloseException());
                }
                Invoker.invoke(result);
            }
        }
    }
    @Override
    <A> Future<Integer> implRead(ByteBuffer dst,
                                 long position,
                                 A attachment,
                                 CompletionHandler<Integer,? super A> handler)
    {
        if (!reading)
            throw new NonReadableChannelException();
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (dst.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        if (!isOpen()) {
            Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invoke(this, handler, attachment, null, exc);
            return null;
        }
        int pos = dst.position();
        int lim = dst.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (rem == 0) {
            if (handler == null)
                return CompletedFuture.withResult(0);
            Invoker.invoke(this, handler, attachment, 0, null);
            return null;
        }
        PendingFuture<Integer,A> result =
            new PendingFuture<Integer,A>(this, handler, attachment);
        ReadTask readTask = new ReadTask<A>(dst, pos, rem, position, result);
        result.setContext(readTask);
        if (Iocp.supportsThreadAgnosticIo()) {
            readTask.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, readTask);
        }
        return result;
    }
    private class WriteTask<A> implements Runnable, Iocp.ResultHandler {
        private final ByteBuffer src;
        private final int pos, rem;     
        private final long position;    
        private final PendingFuture<Integer,A> result;
        private volatile ByteBuffer buf;
        WriteTask(ByteBuffer src,
                  int pos,
                  int rem,
                  long position,
                  PendingFuture<Integer,A> result)
        {
            this.src = src;
            this.pos = pos;
            this.rem = rem;
            this.position = position;
            this.result = result;
        }
        void releaseBufferIfSubstituted() {
            if (buf != src)
                Util.releaseTemporaryDirectBuffer(buf);
        }
        void updatePosition(int bytesTransferred) {
            if (bytesTransferred > 0) {
                try {
                    src.position(pos + bytesTransferred);
                } catch (IllegalArgumentException x) {
                }
            }
        }
        @Override
        public void run() {
            int n = -1;
            long overlapped = 0L;
            long address;
            if (src instanceof DirectBuffer) {
                buf = src;
                address = ((DirectBuffer)src).address() + pos;
            } else {
                buf = Util.getTemporaryDirectBuffer(rem);
                buf.put(src);
                buf.flip();
                src.position(pos);
                address = ((DirectBuffer)buf).address();
            }
            try {
                begin();
                overlapped = ioCache.add(result);
                n = writeFile(handle, address, rem, position, overlapped);
                if (n == IOStatus.UNAVAILABLE) {
                    return;
                } else {
                    throw new InternalError("Unexpected result: " + n);
                }
            } catch (Throwable x) {
                result.setFailure(toIOException(x));
                if (overlapped != 0L)
                    ioCache.remove(overlapped);
                releaseBufferIfSubstituted();
            } finally {
                end();
            }
            Invoker.invoke(result);
        }
        @Override
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            updatePosition(bytesTransferred);
            releaseBufferIfSubstituted();
            result.setResult(bytesTransferred);
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            releaseBufferIfSubstituted();
            if (isOpen()) {
                result.setFailure(x);
            } else {
                result.setFailure(new AsynchronousCloseException());
            }
            Invoker.invoke(result);
        }
    }
    <A> Future<Integer> implWrite(ByteBuffer src,
                                  long position,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler)
    {
        if (!writing)
            throw new NonWritableChannelException();
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (!isOpen()) {
           Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invoke(this, handler, attachment, null, exc);
            return null;
        }
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (rem == 0) {
            if (handler == null)
                return CompletedFuture.withResult(0);
            Invoker.invoke(this, handler, attachment, 0, null);
            return null;
        }
        PendingFuture<Integer,A> result =
            new PendingFuture<Integer,A>(this, handler, attachment);
        WriteTask writeTask = new WriteTask<A>(src, pos, rem, position, result);
        result.setContext(writeTask);
        if (Iocp.supportsThreadAgnosticIo()) {
            writeTask.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, writeTask);
        }
        return result;
    }
    private static native int readFile(long handle, long address, int len,
        long offset, long overlapped) throws IOException;
    private static native int writeFile(long handle, long address, int len,
        long offset, long overlapped) throws IOException;
    private static native int lockFile(long handle, long position, long size,
        boolean shared, long overlapped) throws IOException;
    private static native void close0(long handle);
    static {
        Util.load();
    }
}
