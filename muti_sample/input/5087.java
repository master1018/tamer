public abstract class SocketFactory
{
    private static SocketFactory                theFactory;
    protected SocketFactory() {  }
    public static SocketFactory getDefault()
    {
        synchronized (SocketFactory.class) {
            if (theFactory == null) {
                theFactory = new DefaultSocketFactory();
            }
        }
        return theFactory;
    }
    public Socket createSocket() throws IOException {
        UnsupportedOperationException uop = new
                UnsupportedOperationException();
        SocketException se =  new SocketException(
                "Unconnected sockets not implemented");
        se.initCause(uop);
        throw se;
    }
    public abstract Socket createSocket(String host, int port)
    throws IOException, UnknownHostException;
    public abstract Socket
    createSocket(String host, int port, InetAddress localHost, int localPort)
    throws IOException, UnknownHostException;
    public abstract Socket createSocket(InetAddress host, int port)
    throws IOException;
    public abstract Socket
    createSocket(InetAddress address, int port,
        InetAddress localAddress, int localPort)
    throws IOException;
}
class DefaultSocketFactory extends SocketFactory {
    public Socket createSocket() {
        return new Socket();
    }
    public Socket createSocket(String host, int port)
    throws IOException, UnknownHostException
    {
        return new Socket(host, port);
    }
    public Socket createSocket(InetAddress address, int port)
    throws IOException
    {
        return new Socket(address, port);
    }
    public Socket createSocket(String host, int port,
        InetAddress clientAddress, int clientPort)
    throws IOException, UnknownHostException
    {
        return new Socket(host, port, clientAddress, clientPort);
    }
    public Socket createSocket(InetAddress address, int port,
        InetAddress clientAddress, int clientPort)
    throws IOException
    {
        return new Socket(address, port, clientAddress, clientPort);
    }
}
