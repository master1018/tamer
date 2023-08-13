class UnixAsynchronousServerSocketChannelImpl
    extends AsynchronousServerSocketChannelImpl
    implements Port.PollableChannel
{
    private final static NativeDispatcher nd = new SocketDispatcher();
    private final Port port;
    private final int fdVal;
    private final AtomicBoolean accepting = new AtomicBoolean();
    private void enableAccept() {
        accepting.set(false);
    }
    private final Object updateLock = new Object();
    private boolean acceptPending;
    private CompletionHandler<AsynchronousSocketChannel,Object> acceptHandler;
    private Object acceptAttachment;
    private PendingFuture<AsynchronousSocketChannel,Object> acceptFuture;
    private AccessControlContext acceptAcc;
    UnixAsynchronousServerSocketChannelImpl(Port port)
        throws IOException
    {
        super(port);
        try {
            IOUtil.configureBlocking(fd, false);
        } catch (IOException x) {
            nd.close(fd);  
            throw x;
        }
        this.port = port;
        this.fdVal = IOUtil.fdVal(fd);
        port.register(fdVal, this);
    }
    @Override
    void implClose() throws IOException {
        port.unregister(fdVal);
        nd.close(fd);
        CompletionHandler<AsynchronousSocketChannel,Object> handler;
        Object att;
        PendingFuture<AsynchronousSocketChannel,Object> future;
        synchronized (updateLock) {
            if (!acceptPending)
                return;  
            acceptPending = false;
            handler = acceptHandler;
            att = acceptAttachment;
            future = acceptFuture;
        }
        AsynchronousCloseException x = new AsynchronousCloseException();
        x.setStackTrace(new StackTraceElement[0]);
        if (handler == null) {
            future.setFailure(x);
        } else {
            Invoker.invokeIndirectly(this, handler, att, null, x);
        }
    }
    @Override
    public AsynchronousChannelGroupImpl group() {
        return port;
    }
    @Override
    public void onEvent(int events, boolean mayInvokeDirect) {
        synchronized (updateLock) {
            if (!acceptPending)
                return;  
            acceptPending = false;
        }
        FileDescriptor newfd = new FileDescriptor();
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        Throwable exc = null;
        try {
            begin();
            int n = accept0(this.fd, newfd, isaa);
            if (n == IOStatus.UNAVAILABLE) {
                synchronized (updateLock) {
                    acceptPending = true;
                }
                port.startPoll(fdVal, Port.POLLIN);
                return;
            }
        } catch (Throwable x) {
            if (x instanceof ClosedChannelException)
                x = new AsynchronousCloseException();
            exc = x;
        } finally {
            end();
        }
        AsynchronousSocketChannel child = null;
        if (exc == null) {
            try {
                child = finishAccept(newfd, isaa[0], acceptAcc);
            } catch (Throwable x) {
                if (!(x instanceof IOException) && !(x instanceof SecurityException))
                    x = new IOException(x);
                exc = x;
            }
        }
        CompletionHandler<AsynchronousSocketChannel,Object> handler = acceptHandler;
        Object att = acceptAttachment;
        PendingFuture<AsynchronousSocketChannel,Object> future = acceptFuture;
        enableAccept();
        if (handler == null) {
            future.setResult(child, exc);
            if (child != null && future.isCancelled()) {
                try {
                    child.close();
                } catch (IOException ignore) { }
            }
        } else {
            Invoker.invoke(this, handler, att, child, exc);
        }
    }
    private AsynchronousSocketChannel finishAccept(FileDescriptor newfd,
                                                   final InetSocketAddress remote,
                                                   AccessControlContext acc)
        throws IOException, SecurityException
    {
        AsynchronousSocketChannel ch = null;
        try {
            ch = new UnixAsynchronousSocketChannelImpl(port, newfd, remote);
        } catch (IOException x) {
            nd.close(newfd);
            throw x;
        }
        try {
            if (acc != null) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkAccept(remote.getAddress().getHostAddress(),
                                           remote.getPort());
                        }
                        return null;
                    }
                }, acc);
            } else {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkAccept(remote.getAddress().getHostAddress(),
                                   remote.getPort());
                }
            }
        } catch (SecurityException x) {
            try {
                ch.close();
            } catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        return ch;
    }
    @Override
    Future<AsynchronousSocketChannel> implAccept(Object att,
        CompletionHandler<AsynchronousSocketChannel,Object> handler)
    {
        if (!isOpen()) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            } else {
                Invoker.invoke(this, handler, att, null, e);
                return null;
            }
        }
        if (localAddress == null)
            throw new NotYetBoundException();
        if (isAcceptKilled())
            throw new RuntimeException("Accept not allowed due cancellation");
        if (!accepting.compareAndSet(false, true))
            throw new AcceptPendingException();
        FileDescriptor newfd = new FileDescriptor();
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        Throwable exc = null;
        try {
            begin();
            int n = accept0(this.fd, newfd, isaa);
            if (n == IOStatus.UNAVAILABLE) {
                PendingFuture<AsynchronousSocketChannel,Object> result = null;
                synchronized (updateLock) {
                    if (handler == null) {
                        this.acceptHandler = null;
                        result = new PendingFuture<AsynchronousSocketChannel,Object>(this);
                        this.acceptFuture = result;
                    } else {
                        this.acceptHandler = handler;
                        this.acceptAttachment = att;
                    }
                    this.acceptAcc = (System.getSecurityManager() == null) ?
                        null : AccessController.getContext();
                    this.acceptPending = true;
                }
                port.startPoll(fdVal, Port.POLLIN);
                return result;
            }
        } catch (Throwable x) {
            if (x instanceof ClosedChannelException)
                x = new AsynchronousCloseException();
            exc = x;
        } finally {
            end();
        }
        AsynchronousSocketChannel child = null;
        if (exc == null) {
            try {
                child = finishAccept(newfd, isaa[0], null);
            } catch (Throwable x) {
                exc = x;
            }
        }
        enableAccept();
        if (handler == null) {
            return CompletedFuture.withResult(child, exc);
        } else {
            Invoker.invokeIndirectly(this, handler, att, child, exc);
            return null;
        }
    }
    private static native void initIDs();
    private native int accept0(FileDescriptor ssfd, FileDescriptor newfd,
                               InetSocketAddress[] isaa)
        throws IOException;
    static {
        Util.load();
        initIDs();
    }
}
