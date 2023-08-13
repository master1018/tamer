public class SSLServerSocketFactoryImpl extends SSLServerSocketFactory
{
    private static final int DEFAULT_BACKLOG = 50;
    private SSLContextImpl context;
    public SSLServerSocketFactoryImpl() throws Exception {
        this.context = SSLContextImpl.DefaultSSLContext.getDefaultImpl();
    }
    SSLServerSocketFactoryImpl (SSLContextImpl context)
    {
        this.context = context;
    }
    public ServerSocket createServerSocket() throws IOException {
        return new SSLServerSocketImpl(context);
    }
    public ServerSocket createServerSocket (int port)
    throws IOException
    {
        return new SSLServerSocketImpl (port, DEFAULT_BACKLOG, context);
    }
    public ServerSocket createServerSocket (int port, int backlog)
    throws IOException
    {
        return new SSLServerSocketImpl (port, backlog, context);
    }
    public ServerSocket
    createServerSocket (int port, int backlog, InetAddress ifAddress)
    throws IOException
    {
        return new SSLServerSocketImpl (port, backlog, ifAddress, context);
    }
    public String[] getDefaultCipherSuites() {
        return context.getDefaultCipherSuiteList(true).toStringArray();
    }
    public String[] getSupportedCipherSuites() {
        return context.getSuportedCipherSuiteList().toStringArray();
    }
}
