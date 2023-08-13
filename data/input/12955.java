class WindowsAsynchronousServerSocketChannelImpl
    extends AsynchronousServerSocketChannelImpl implements Iocp.OverlappedChannel
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int DATA_BUFFER_SIZE = 88;
    private final long handle;
    private final int completionKey;
    private final Iocp iocp;
    private final PendingIoCache ioCache;
    private final long dataBuffer;
    private AtomicBoolean accepting = new AtomicBoolean();
    WindowsAsynchronousServerSocketChannelImpl(Iocp iocp) throws IOException {
        super(iocp);
        long h = IOUtil.fdVal(fd);
        int key;
        try {
            key = iocp.associate(this, h);
        } catch (IOException x) {
            closesocket0(h);   
            throw x;
        }
        this.handle = h;
        this.completionKey = key;
        this.iocp = iocp;
        this.ioCache = new PendingIoCache();
        this.dataBuffer = unsafe.allocateMemory(DATA_BUFFER_SIZE);
    }
    @Override
    public <V,A> PendingFuture<V,A> getByOverlapped(long overlapped) {
        return ioCache.remove(overlapped);
    }
    @Override
    void implClose() throws IOException {
        closesocket0(handle);
        ioCache.close();
        iocp.disassociate(completionKey);
        unsafe.freeMemory(dataBuffer);
    }
    @Override
    public AsynchronousChannelGroupImpl group() {
        return iocp;
    }
    private class AcceptTask implements Runnable, Iocp.ResultHandler {
        private final WindowsAsynchronousSocketChannelImpl channel;
        private final AccessControlContext acc;
        private final PendingFuture<AsynchronousSocketChannel,Object> result;
        AcceptTask(WindowsAsynchronousSocketChannelImpl channel,
                   AccessControlContext acc,
                   PendingFuture<AsynchronousSocketChannel,Object> result)
        {
            this.channel = channel;
            this.acc = acc;
            this.result = result;
        }
        void enableAccept() {
            accepting.set(false);
        }
        void closeChildChannel() {
            try {
                channel.close();
            } catch (IOException ignore) { }
        }
        void finishAccept() throws IOException {
            updateAcceptContext(handle, channel.handle());
            InetSocketAddress local = Net.localAddress(channel.fd);
            final InetSocketAddress remote = Net.remoteAddress(channel.fd);
            channel.setConnected(local, remote);
            if (acc != null) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        SecurityManager sm = System.getSecurityManager();
                        sm.checkAccept(remote.getAddress().getHostAddress(),
                                       remote.getPort());
                        return null;
                    }
                }, acc);
            }
        }
        @Override
        public void run() {
            long overlapped = 0L;
            try {
                begin();
                try {
                    channel.begin();
                    synchronized (result) {
                        overlapped = ioCache.add(result);
                        int n = accept0(handle, channel.handle(), overlapped, dataBuffer);
                        if (n == IOStatus.UNAVAILABLE) {
                            return;
                        }
                        finishAccept();
                        enableAccept();
                        result.setResult(channel);
                    }
                } finally {
                    channel.end();
                }
            } catch (Throwable x) {
                if (overlapped != 0L)
                    ioCache.remove(overlapped);
                closeChildChannel();
                if (x instanceof ClosedChannelException)
                    x = new AsynchronousCloseException();
                if (!(x instanceof IOException) && !(x instanceof SecurityException))
                    x = new IOException(x);
                enableAccept();
                result.setFailure(x);
            } finally {
                end();
            }
            if (result.isCancelled()) {
                closeChildChannel();
            }
            Invoker.invokeIndirectly(result);
        }
        @Override
        public void completed(int bytesTransferred, boolean canInvokeDirect) {
            try {
                if (iocp.isShutdown()) {
                    throw new IOException(new ShutdownChannelGroupException());
                }
                try {
                    begin();
                    try {
                        channel.begin();
                        finishAccept();
                    } finally {
                        channel.end();
                    }
                } finally {
                    end();
                }
                enableAccept();
                result.setResult(channel);
            } catch (Throwable x) {
                enableAccept();
                closeChildChannel();
                if (x instanceof ClosedChannelException)
                    x = new AsynchronousCloseException();
                if (!(x instanceof IOException) && !(x instanceof SecurityException))
                    x = new IOException(x);
                result.setFailure(x);
            }
            if (result.isCancelled()) {
                closeChildChannel();
            }
            Invoker.invokeIndirectly(result);
        }
        @Override
        public void failed(int error, IOException x) {
            enableAccept();
            closeChildChannel();
            if (isOpen()) {
                result.setFailure(x);
            } else {
                result.setFailure(new AsynchronousCloseException());
            }
            Invoker.invokeIndirectly(result);
        }
    }
    @Override
    Future<AsynchronousSocketChannel> implAccept(Object attachment,
        final CompletionHandler<AsynchronousSocketChannel,Object> handler)
    {
        if (!isOpen()) {
            Throwable exc = new ClosedChannelException();
            if (handler == null)
                return CompletedFuture.withFailure(exc);
            Invoker.invokeIndirectly(this, handler, attachment, null, exc);
            return null;
        }
        if (isAcceptKilled())
            throw new RuntimeException("Accept not allowed due to cancellation");
        if (localAddress == null)
            throw new NotYetBoundException();
        WindowsAsynchronousSocketChannelImpl ch = null;
        IOException ioe = null;
        try {
            begin();
            ch = new WindowsAsynchronousSocketChannelImpl(iocp, false);
        } catch (IOException x) {
            ioe = x;
        } finally {
            end();
        }
        if (ioe != null) {
            if (handler == null)
                return CompletedFuture.withFailure(ioe);
            Invoker.invokeIndirectly(this, handler, attachment, null, ioe);
            return null;
        }
        AccessControlContext acc = (System.getSecurityManager() == null) ?
            null : AccessController.getContext();
        PendingFuture<AsynchronousSocketChannel,Object> result =
            new PendingFuture<AsynchronousSocketChannel,Object>(this, handler, attachment);
        AcceptTask task = new AcceptTask(ch, acc, result);
        result.setContext(task);
        if (!accepting.compareAndSet(false, true))
            throw new AcceptPendingException();
        if (Iocp.supportsThreadAgnosticIo()) {
            task.run();
        } else {
            Invoker.invokeOnThreadInThreadPool(this, task);
        }
        return result;
    }
    private static native void initIDs();
    private static native int accept0(long listenSocket, long acceptSocket,
        long overlapped, long dataBuffer) throws IOException;
    private static native void updateAcceptContext(long listenSocket,
        long acceptSocket) throws IOException;
    private static native void closesocket0(long socket) throws IOException;
    static {
        Util.load();
        initIDs();
    }
}
