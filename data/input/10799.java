class WindowsAsynchronousSocketChannelImpl
    extends AsynchronousSocketChannelImpl implements Iocp.OverlappedChannel
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static int addressSize = unsafe.addressSize();
    private static int dependsArch(int value32, int value64) {
        return (addressSize == 4) ? value32 : value64;
    }
    private static final int SIZEOF_WSABUF  = dependsArch(8, 16);
    private static final int OFFSETOF_LEN   = 0;
    private static final int OFFSETOF_BUF   = dependsArch(4, 8);
    private static final int MAX_WSABUF     = 16;
    private static final int SIZEOF_WSABUFARRAY = MAX_WSABUF * SIZEOF_WSABUF;
    final long handle;
    private final Iocp iocp;
    private final int completionKey;
    private final PendingIoCache ioCache;
    private final long readBufferArray;
    private final long writeBufferArray;
    WindowsAsynchronousSocketChannelImpl(Iocp iocp, boolean failIfGroupShutdown)
        throws IOException
    {
        super(iocp);
        long h = IOUtil.fdVal(fd);
        int key = 0;
        try {
            key = iocp.associate(this, h);
        } catch (ShutdownChannelGroupException x) {
            if (failIfGroupShutdown) {
                closesocket0(h);
                throw x;
            }
        } catch (IOException x) {
            closesocket0(h);
            throw x;
        }
        this.handle = h;
        this.iocp = iocp;
        this.completionKey = key;
        this.ioCache = new PendingIoCache();
        this.readBufferArray = unsafe.allocateMemory(SIZEOF_WSABUFARRAY);
        this.writeBufferArray = unsafe.allocateMemory(SIZEOF_WSABUFARRAY);
    }
    WindowsAsynchronousSocketChannelImpl(Iocp iocp) throws IOException {
        this(iocp, true);
    }
    @Override
    public AsynchronousChannelGroupImpl group() {
        return iocp;
    }
    @Override
    public <V,A> PendingFuture<V,A> getByOverlapped(long overlapped) {
        return ioCache.remove(overlapped);
    }
    long handle() {
        return handle;
    }
    void setConnected(SocketAddress localAddress, SocketAddress remoteAddress) {
        synchronized (stateLock) {
            state = ST_CONNECTED;
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
        }
    }
    @Override
    void implClose() throws IOException {
        closesocket0(handle);
        ioCache.close();
        unsafe.freeMemory(readBufferArray);
        unsafe.freeMemory(writeBufferArray);
        if (completionKey != 0)
            iocp.disassociate(completionKey);
    }
    @Override
    public void onCancel(PendingFuture<?,?> task) {
        if (task.getContext() instanceof ConnectTask)
            killConnect();
        if (task.getContext() instanceof ReadTask)
            killReading();
        if (task.getContext() instanceof WriteTask)
            killWriting();
    }
    private class ConnectTask<A> implements Runnable, Iocp.ResultHandler {
        private final InetSocketAddress remote;
        private final PendingFuture<Void,A> result;
        ConnectTask(InetSocketAddress remote, PendingFuture<Void,A> result) {
            this.remote = remote;
            this.result = result;
        }
        private void closeChannel() {
            try {
                close();
            } catch (IOException ignore) { }
        }
        private IOException toIOException(Throwable x) {
            if (x instanceof IOException) {
                if (x instanceof ClosedChannelException)
                    x = new AsynchronousCloseException();
                return (IOException)x;
            }
            return new IOException(x);
        }
        private void afterConnect() throws IOException {
            updateConnectContext(handle);
            synchronized (stateLock) {
                state = ST_CONNECTED;
                remoteAddress = remote;
            }
        }
        @Override
        public void run() {
            long overlapped = 0L;
            Throwable exc = null;
            try {
                begin();
                synchronized (result) {
                    overlapped = ioCache.add(result);
                    int n = connect0(handle, Net.isIPv6Available(), remote.getAddress(),
                                     remote.getPort(), overlapped);
                    if (n == IOStatus.UNAVAILABLE) {
                        return;
                    }
                    afterConnect();
                    result.setResult(null);
                }
            } catch (Throwable x) {
                if (overlapped != 0L)
                    ioCache.remove(overlapped);
                exc = x;
            } finally {
                end();
            }
            if (exc != null) {
                closeChannel();
                result.setFailure(toIOException(exc));
            }
            Invoker.invoke(result);
        }
        @Override
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            Throwable exc = null;
            try {
                begin();
                afterConnect();
                result.setResult(null);
            } catch (Throwable x) {
                exc = x;
            } finally {
                end();
            }
            if (exc != null) {
                closeChannel();
                result.setFailure(toIOException(exc));
            }
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            if (isOpen()) {
                closeChannel();
                result.setFailure(x);
            } else {
                result.setFailure(new AsynchronousCloseException());
            }
            Invoker.invoke(result);
        }
    }
    @Override
    <A> Future<Void> implConnect(SocketAddress remote,
                                 A attachment,
                                 CompletionHandler<Void,? super A> handler)
    {
        if (!isOpen()) {
            Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invoke(this, handler, attachment, null, exc);
            return null;
        }
        InetSocketAddress isa = Net.checkAddress(remote);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
        IOException bindException = null;
        synchronized (stateLock) {
            if (state == ST_CONNECTED)
                throw new AlreadyConnectedException();
            if (state == ST_PENDING)
                throw new ConnectionPendingException();
            if (localAddress == null) {
                try {
                    bind(new InetSocketAddress(0));
                } catch (IOException x) {
                    bindException = x;
                }
            }
            if (bindException == null)
                state = ST_PENDING;
        }
        if (bindException != null) {
            try {
                close();
            } catch (IOException ignore) { }
            if (handler == null)
                return CompletedFuture.withFailure(bindException);
            Invoker.invoke(this, handler, attachment, null, bindException);
            return null;
        }
        PendingFuture<Void,A> result =
            new PendingFuture<Void,A>(this, handler, attachment);
        ConnectTask task = new ConnectTask<A>(isa, result);
        result.setContext(task);
        if (Iocp.supportsThreadAgnosticIo()) {
            task.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, task);
        }
        return result;
    }
    private class ReadTask<V,A> implements Runnable, Iocp.ResultHandler {
        private final ByteBuffer[] bufs;
        private final int numBufs;
        private final boolean scatteringRead;
        private final PendingFuture<V,A> result;
        private ByteBuffer[] shadow;
        ReadTask(ByteBuffer[] bufs,
                 boolean scatteringRead,
                 PendingFuture<V,A> result)
        {
            this.bufs = bufs;
            this.numBufs = (bufs.length > MAX_WSABUF) ? MAX_WSABUF : bufs.length;
            this.scatteringRead = scatteringRead;
            this.result = result;
        }
        void prepareBuffers() {
            shadow = new ByteBuffer[numBufs];
            long address = readBufferArray;
            for (int i=0; i<numBufs; i++) {
                ByteBuffer dst = bufs[i];
                int pos = dst.position();
                int lim = dst.limit();
                assert (pos <= lim);
                int rem = (pos <= lim ? lim - pos : 0);
                long a;
                if (!(dst instanceof DirectBuffer)) {
                    ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
                    shadow[i] = bb;
                    a = ((DirectBuffer)bb).address();
                } else {
                    shadow[i] = dst;
                    a = ((DirectBuffer)dst).address() + pos;
                }
                unsafe.putAddress(address + OFFSETOF_BUF, a);
                unsafe.putInt(address + OFFSETOF_LEN, rem);
                address += SIZEOF_WSABUF;
            }
        }
        void updateBuffers(int bytesRead) {
            for (int i=0; i<numBufs; i++) {
                ByteBuffer nextBuffer = shadow[i];
                int pos = nextBuffer.position();
                int len = nextBuffer.remaining();
                if (bytesRead >= len) {
                    bytesRead -= len;
                    int newPosition = pos + len;
                    try {
                        nextBuffer.position(newPosition);
                    } catch (IllegalArgumentException x) {
                    }
                } else { 
                    if (bytesRead > 0) {
                        assert(pos + bytesRead < (long)Integer.MAX_VALUE);
                        int newPosition = pos + bytesRead;
                        try {
                            nextBuffer.position(newPosition);
                        } catch (IllegalArgumentException x) {
                        }
                    }
                    break;
                }
            }
            for (int i=0; i<numBufs; i++) {
                if (!(bufs[i] instanceof DirectBuffer)) {
                    shadow[i].flip();
                    try {
                        bufs[i].put(shadow[i]);
                    } catch (BufferOverflowException x) {
                    }
                }
            }
        }
        void releaseBuffers() {
            for (int i=0; i<numBufs; i++) {
                if (!(bufs[i] instanceof DirectBuffer)) {
                    Util.releaseTemporaryDirectBuffer(shadow[i]);
                }
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            long overlapped = 0L;
            boolean prepared = false;
            boolean pending = false;
            try {
                begin();
                prepareBuffers();
                prepared = true;
                overlapped = ioCache.add(result);
                int n = read0(handle, numBufs, readBufferArray, overlapped);
                if (n == IOStatus.UNAVAILABLE) {
                    pending = true;
                    return;
                }
                if (n == IOStatus.EOF) {
                    enableReading();
                    if (scatteringRead) {
                        result.setResult((V)Long.valueOf(-1L));
                    } else {
                        result.setResult((V)Integer.valueOf(-1));
                    }
                } else {
                    throw new InternalError("Read completed immediately");
                }
            } catch (Throwable x) {
                enableReading();
                if (x instanceof ClosedChannelException)
                    x = new AsynchronousCloseException();
                if (!(x instanceof IOException))
                    x = new IOException(x);
                result.setFailure(x);
            } finally {
                if (!pending) {
                    if (overlapped != 0L)
                        ioCache.remove(overlapped);
                    if (prepared)
                        releaseBuffers();
                }
                end();
            }
            Invoker.invoke(result);
        }
        @Override
        @SuppressWarnings("unchecked")
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            if (bytesTransferred == 0) {
                bytesTransferred = -1;  
            } else {
                updateBuffers(bytesTransferred);
            }
            releaseBuffers();
            synchronized (result) {
                if (result.isDone())
                    return;
                enableReading();
                if (scatteringRead) {
                    result.setResult((V)Long.valueOf(bytesTransferred));
                } else {
                    result.setResult((V)Integer.valueOf(bytesTransferred));
                }
            }
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            releaseBuffers();
            if (!isOpen())
                x = new AsynchronousCloseException();
            synchronized (result) {
                if (result.isDone())
                    return;
                enableReading();
                result.setFailure(x);
            }
            Invoker.invoke(result);
        }
        void timeout() {
            synchronized (result) {
                if (result.isDone())
                    return;
                enableReading(true);
                result.setFailure(new InterruptedByTimeoutException());
            }
            Invoker.invoke(result);
        }
    }
    @Override
    <V extends Number,A> Future<V> implRead(boolean isScatteringRead,
                                            ByteBuffer dst,
                                            ByteBuffer[] dsts,
                                            long timeout,
                                            TimeUnit unit,
                                            A attachment,
                                            CompletionHandler<V,? super A> handler)
    {
        PendingFuture<V,A> result =
            new PendingFuture<V,A>(this, handler, attachment);
        ByteBuffer[] bufs;
        if (isScatteringRead) {
            bufs = dsts;
        } else {
            bufs = new ByteBuffer[1];
            bufs[0] = dst;
        }
        final ReadTask readTask = new ReadTask<V,A>(bufs, isScatteringRead, result);
        result.setContext(readTask);
        if (timeout > 0L) {
            Future<?> timeoutTask = iocp.schedule(new Runnable() {
                public void run() {
                    readTask.timeout();
                }
            }, timeout, unit);
            result.setTimeoutTask(timeoutTask);
        }
        if (Iocp.supportsThreadAgnosticIo()) {
            readTask.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, readTask);
        }
        return result;
    }
    private class WriteTask<V,A> implements Runnable, Iocp.ResultHandler {
        private final ByteBuffer[] bufs;
        private final int numBufs;
        private final boolean gatheringWrite;
        private final PendingFuture<V,A> result;
        private ByteBuffer[] shadow;
        WriteTask(ByteBuffer[] bufs,
                  boolean gatheringWrite,
                  PendingFuture<V,A> result)
        {
            this.bufs = bufs;
            this.numBufs = (bufs.length > MAX_WSABUF) ? MAX_WSABUF : bufs.length;
            this.gatheringWrite = gatheringWrite;
            this.result = result;
        }
        void prepareBuffers() {
            shadow = new ByteBuffer[numBufs];
            long address = writeBufferArray;
            for (int i=0; i<numBufs; i++) {
                ByteBuffer src = bufs[i];
                int pos = src.position();
                int lim = src.limit();
                assert (pos <= lim);
                int rem = (pos <= lim ? lim - pos : 0);
                long a;
                if (!(src instanceof DirectBuffer)) {
                    ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
                    bb.put(src);
                    bb.flip();
                    src.position(pos);  
                    shadow[i] = bb;
                    a = ((DirectBuffer)bb).address();
                } else {
                    shadow[i] = src;
                    a = ((DirectBuffer)src).address() + pos;
                }
                unsafe.putAddress(address + OFFSETOF_BUF, a);
                unsafe.putInt(address + OFFSETOF_LEN, rem);
                address += SIZEOF_WSABUF;
            }
        }
        void updateBuffers(int bytesWritten) {
            for (int i=0; i<numBufs; i++) {
                ByteBuffer nextBuffer = bufs[i];
                int pos = nextBuffer.position();
                int lim = nextBuffer.limit();
                int len = (pos <= lim ? lim - pos : lim);
                if (bytesWritten >= len) {
                    bytesWritten -= len;
                    int newPosition = pos + len;
                    try {
                        nextBuffer.position(newPosition);
                    } catch (IllegalArgumentException x) {
                    }
                } else { 
                    if (bytesWritten > 0) {
                        assert(pos + bytesWritten < (long)Integer.MAX_VALUE);
                        int newPosition = pos + bytesWritten;
                        try {
                            nextBuffer.position(newPosition);
                        } catch (IllegalArgumentException x) {
                        }
                    }
                    break;
                }
            }
        }
        void releaseBuffers() {
            for (int i=0; i<numBufs; i++) {
                if (!(bufs[i] instanceof DirectBuffer)) {
                    Util.releaseTemporaryDirectBuffer(shadow[i]);
                }
            }
        }
        @Override
        public void run() {
            long overlapped = 0L;
            boolean prepared = false;
            boolean pending = false;
            boolean shutdown = false;
            try {
                begin();
                prepareBuffers();
                prepared = true;
                overlapped = ioCache.add(result);
                int n = write0(handle, numBufs, writeBufferArray, overlapped);
                if (n == IOStatus.UNAVAILABLE) {
                    pending = true;
                    return;
                }
                if (n == IOStatus.EOF) {
                    shutdown = true;
                    throw new ClosedChannelException();
                }
                throw new InternalError("Write completed immediately");
            } catch (Throwable x) {
                enableWriting();
                if (!shutdown && (x instanceof ClosedChannelException))
                    x = new AsynchronousCloseException();
                if (!(x instanceof IOException))
                    x = new IOException(x);
                result.setFailure(x);
            } finally {
                if (!pending) {
                    if (overlapped != 0L)
                        ioCache.remove(overlapped);
                    if (prepared)
                        releaseBuffers();
                }
                end();
            }
            Invoker.invoke(result);
        }
        @Override
        @SuppressWarnings("unchecked")
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            updateBuffers(bytesTransferred);
            releaseBuffers();
            synchronized (result) {
                if (result.isDone())
                    return;
                enableWriting();
                if (gatheringWrite) {
                    result.setResult((V)Long.valueOf(bytesTransferred));
                } else {
                    result.setResult((V)Integer.valueOf(bytesTransferred));
                }
            }
            if (canInvokeDirect) {
                Invoker.invokeUnchecked(result);
            } else {
                Invoker.invoke(result);
            }
        }
        @Override
        public void failed(int error, IOException x) {
            releaseBuffers();
            if (!isOpen())
                x = new AsynchronousCloseException();
            synchronized (result) {
                if (result.isDone())
                    return;
                enableWriting();
                result.setFailure(x);
            }
            Invoker.invoke(result);
        }
        void timeout() {
            synchronized (result) {
                if (result.isDone())
                    return;
                enableWriting(true);
                result.setFailure(new InterruptedByTimeoutException());
            }
            Invoker.invoke(result);
        }
    }
    @Override
    <V extends Number,A> Future<V> implWrite(boolean gatheringWrite,
                                             ByteBuffer src,
                                             ByteBuffer[] srcs,
                                             long timeout,
                                             TimeUnit unit,
                                             A attachment,
                                             CompletionHandler<V,? super A> handler)
    {
        PendingFuture<V,A> result =
            new PendingFuture<V,A>(this, handler, attachment);
        ByteBuffer[] bufs;
        if (gatheringWrite) {
            bufs = srcs;
        } else {
            bufs = new ByteBuffer[1];
            bufs[0] = src;
        }
        final WriteTask writeTask = new WriteTask<V,A>(bufs, gatheringWrite, result);
        result.setContext(writeTask);
        if (timeout > 0L) {
            Future<?> timeoutTask = iocp.schedule(new Runnable() {
                public void run() {
                    writeTask.timeout();
                }
            }, timeout, unit);
            result.setTimeoutTask(timeoutTask);
        }
        if (Iocp.supportsThreadAgnosticIo()) {
            writeTask.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, writeTask);
        }
        return result;
    }
    private static native void initIDs();
    private static native int connect0(long socket, boolean preferIPv6,
        InetAddress remote, int remotePort, long overlapped) throws IOException;
    private static native void updateConnectContext(long socket) throws IOException;
    private static native int read0(long socket, int count, long addres, long overlapped)
        throws IOException;
    private static native int write0(long socket, int count, long address,
        long overlapped) throws IOException;
    private static native void shutdown0(long socket, int how) throws IOException;
    private static native void closesocket0(long socket) throws IOException;
    static {
        Util.load();
        initIDs();
    }
}
