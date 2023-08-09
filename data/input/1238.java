final public class SSLSocketFactoryImpl extends SSLSocketFactory {
    private static SSLContextImpl defaultContext;
    private SSLContextImpl context;
    public SSLSocketFactoryImpl() throws Exception {
        this.context = SSLContextImpl.DefaultSSLContext.getDefaultImpl();
    }
    SSLSocketFactoryImpl(SSLContextImpl context) {
        this.context = context;
    }
    public Socket createSocket() {
        return new SSLSocketImpl(context);
    }
    public Socket createSocket(String host, int port)
    throws IOException, UnknownHostException
    {
        return new SSLSocketImpl(context, host, port);
    }
    public Socket createSocket(Socket s, String host, int port,
            boolean autoClose) throws IOException {
        return new SSLSocketImpl(context, s, host, port, autoClose);
    }
    public Socket createSocket(InetAddress address, int port)
    throws IOException
    {
        return new SSLSocketImpl(context, address, port);
    }
    public Socket createSocket(String host, int port,
        InetAddress clientAddress, int clientPort)
    throws IOException
    {
        return new SSLSocketImpl(context, host, port,
                clientAddress, clientPort);
    }
    public Socket createSocket(InetAddress address, int port,
        InetAddress clientAddress, int clientPort)
    throws IOException
    {
        return new SSLSocketImpl(context, address, port,
                clientAddress, clientPort);
    }
    public String[] getDefaultCipherSuites() {
        return context.getDefaultCipherSuiteList(false).toStringArray();
    }
    public String[] getSupportedCipherSuites() {
        return context.getSuportedCipherSuiteList().toStringArray();
    }
}
