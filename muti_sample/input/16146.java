class InheritedChannel {
    private static final int UNKNOWN            = -1;
    private static final int SOCK_STREAM        = 1;
    private static final int SOCK_DGRAM         = 2;
    private static final int O_RDONLY           = 0;
    private static final int O_WRONLY           = 1;
    private static final int O_RDWR             = 2;
    private static int devnull = -1;
    private static void detachIOStreams() {
        try {
            dup2(devnull, 0);
            dup2(devnull, 1);
            dup2(devnull, 2);
        } catch (IOException ioe) {
            throw new InternalError();
        }
    }
    public static class InheritedSocketChannelImpl extends SocketChannelImpl {
        InheritedSocketChannelImpl(SelectorProvider sp,
                                   FileDescriptor fd,
                                   InetSocketAddress remote)
            throws IOException
        {
            super(sp, fd, remote);
        }
        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            detachIOStreams();
        }
    }
    public static class InheritedServerSocketChannelImpl extends
        ServerSocketChannelImpl {
        InheritedServerSocketChannelImpl(SelectorProvider sp,
                                         FileDescriptor fd)
            throws IOException
        {
            super(sp, fd, true);
        }
        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            detachIOStreams();
        }
    }
    public static class InheritedDatagramChannelImpl extends
        DatagramChannelImpl {
        InheritedDatagramChannelImpl(SelectorProvider sp,
                                     FileDescriptor fd)
            throws IOException
        {
            super(sp, fd);
        }
        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            detachIOStreams();
        }
    }
    private static void checkAccess(Channel c) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                new RuntimePermission("inheritedChannel")
            );
        }
    }
    private static Channel createChannel() throws IOException {
        int fdVal = dup(0);
        int st;
        st = soType0(fdVal);
        if (st != SOCK_STREAM && st != SOCK_DGRAM) {
            close0(fdVal);
            return null;
        }
        Class paramTypes[] = { int.class };
        Constructor ctr = Reflect.lookupConstructor("java.io.FileDescriptor",
                                                    paramTypes);
        Object args[] = { new Integer(fdVal) };
        FileDescriptor fd = (FileDescriptor)Reflect.invoke(ctr, args);
        SelectorProvider provider = SelectorProvider.provider();
        assert provider instanceof sun.nio.ch.SelectorProviderImpl;
        Channel c;
        if (st == SOCK_STREAM) {
            InetAddress ia = peerAddress0(fdVal);
            if (ia == null) {
               c = new InheritedServerSocketChannelImpl(provider, fd);
            } else {
               int port = peerPort0(fdVal);
               assert port > 0;
               InetSocketAddress isa = new InetSocketAddress(ia, port);
               c = new InheritedSocketChannelImpl(provider, fd, isa);
            }
        } else {
            c = new InheritedDatagramChannelImpl(provider, fd);
        }
        return c;
    }
    private static boolean haveChannel = false;
    private static Channel channel = null;
    public static synchronized Channel getChannel() throws IOException {
        if (devnull < 0) {
            devnull = open0("/dev/null", O_RDWR);
        }
        if (!haveChannel) {
            channel = createChannel();
            haveChannel = true;
        }
        if (channel != null) {
            checkAccess(channel);
        }
        return channel;
    }
    private static native int dup(int fd) throws IOException;
    private static native void dup2(int fd, int fd2) throws IOException;
    private static native int open0(String path, int oflag) throws IOException;
    private static native void close0(int fd) throws IOException;
    private static native int soType0(int fd);
    private static native InetAddress peerAddress0(int fd);
    private static native int peerPort0(int fd);
    static {
        Util.load();
    }
}
