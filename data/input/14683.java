public abstract class ServerSocketFactory
{
    private static ServerSocketFactory          theFactory;
    protected ServerSocketFactory() {  }
    public static ServerSocketFactory getDefault()
    {
        synchronized (ServerSocketFactory.class) {
            if (theFactory == null) {
                theFactory = new DefaultServerSocketFactory();
            }
        }
        return theFactory;
    }
    public ServerSocket createServerSocket() throws IOException {
        throw new SocketException("Unbound server sockets not implemented");
    }
    public abstract ServerSocket createServerSocket(int port)
        throws IOException;
    public abstract ServerSocket
    createServerSocket(int port, int backlog)
    throws IOException;
    public abstract ServerSocket
    createServerSocket(int port, int backlog, InetAddress ifAddress)
    throws IOException;
}
class DefaultServerSocketFactory extends ServerSocketFactory {
    DefaultServerSocketFactory()
    {
    }
    public ServerSocket createServerSocket()
    throws IOException
    {
        return new ServerSocket();
    }
    public ServerSocket createServerSocket(int port)
    throws IOException
    {
        return new ServerSocket(port);
    }
    public ServerSocket createServerSocket(int port, int backlog)
    throws IOException
    {
        return new ServerSocket(port, backlog);
    }
    public ServerSocket
    createServerSocket(int port, int backlog, InetAddress ifAddress)
    throws IOException
    {
        return new ServerSocket(port, backlog, ifAddress);
    }
}
