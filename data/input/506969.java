public abstract class ServerSocketFactory {
    private static ServerSocketFactory defaultFactory;
    public static synchronized ServerSocketFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultServerSocketFactory();
        }
        return defaultFactory;
    }
    protected ServerSocketFactory() {
        super();
    }
    public ServerSocket createServerSocket() throws IOException {
        throw new SocketException("Unbound server sockets not implemented");
    }
    public abstract ServerSocket createServerSocket(int port) throws IOException;
    public abstract ServerSocket createServerSocket(int port, int backlog) throws IOException;
    public abstract ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress)
            throws IOException;
}
