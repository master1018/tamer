public final class Sdp {
    private Sdp() { }
    private static final Constructor<ServerSocket> serverSocketCtor;
    static {
        try {
            serverSocketCtor = (Constructor<ServerSocket>)
                ServerSocket.class.getDeclaredConstructor(SocketImpl.class);
            setAccessible(serverSocketCtor);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
    private static final Constructor<SocketImpl> socketImplCtor;
    static {
        try {
            Class<?> cl = Class.forName("java.net.SdpSocketImpl", true, null);
            socketImplCtor = (Constructor<SocketImpl>)cl.getDeclaredConstructor();
            setAccessible(socketImplCtor);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
    private static void setAccessible(final AccessibleObject o) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                o.setAccessible(true);
                return null;
            }
        });
    }
    private static class SdpSocket extends Socket {
        SdpSocket(SocketImpl impl) throws SocketException {
            super(impl);
        }
    }
    private static SocketImpl createSocketImpl() {
        try {
            return socketImplCtor.newInstance();
        } catch (InstantiationException x) {
            throw new AssertionError(x);
        } catch (IllegalAccessException x) {
            throw new AssertionError(x);
        } catch (InvocationTargetException x) {
            throw new AssertionError(x);
        }
    }
    public static Socket openSocket() throws IOException {
        SocketImpl impl = createSocketImpl();
        return new SdpSocket(impl);
    }
    public static ServerSocket openServerSocket() throws IOException {
        SocketImpl impl = createSocketImpl();
        try {
            return serverSocketCtor.newInstance(impl);
        } catch (IllegalAccessException x) {
            throw new AssertionError(x);
        } catch (InstantiationException x) {
            throw new AssertionError(x);
        } catch (InvocationTargetException x) {
            Throwable cause = x.getCause();
            if (cause instanceof IOException)
                throw (IOException)cause;
            if (cause instanceof RuntimeException)
                throw (RuntimeException)cause;
            throw new RuntimeException(x);
        }
    }
    public static SocketChannel openSocketChannel() throws IOException {
        FileDescriptor fd = SdpSupport.createSocket();
        return sun.nio.ch.Secrets.newSocketChannel(fd);
    }
    public static ServerSocketChannel openServerSocketChannel()
        throws IOException
    {
        FileDescriptor fd = SdpSupport.createSocket();
        return sun.nio.ch.Secrets.newServerSocketChannel(fd);
    }
}
