public class DefaultSocketFactoryImpl
    implements ORBSocketFactory
{
    private ORB orb;
    public void setORB(ORB orb)
    {
        this.orb = orb;
    }
    public ServerSocket createServerSocket(String type,
                                           InetSocketAddress inetSocketAddress)
        throws IOException
    {
        ServerSocketChannel serverSocketChannel = null;
        ServerSocket serverSocket = null;
        if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocket = serverSocketChannel.socket();
        } else {
            serverSocket = new ServerSocket();
        }
        serverSocket.bind(inetSocketAddress);
        return serverSocket;
    }
    public Socket createSocket(String type,
                               InetSocketAddress inetSocketAddress)
        throws IOException
    {
        SocketChannel socketChannel = null;
        Socket socket = null;
        if (orb.getORBData().connectionSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            socketChannel = SocketChannel.open(inetSocketAddress);
            socket = socketChannel.socket();
        } else {
            socket = new Socket(inetSocketAddress.getHostName(),
                                inetSocketAddress.getPort());
        }
        socket.setTcpNoDelay(true);
        return socket;
    }
    public void setAcceptedSocketOptions(Acceptor acceptor,
                                         ServerSocket serverSocket,
                                         Socket socket)
        throws SocketException
    {
        socket.setTcpNoDelay(true);
    }
}
