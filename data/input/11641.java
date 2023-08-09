public abstract class RMISocketFactory
        implements RMIClientSocketFactory, RMIServerSocketFactory
{
    private static RMISocketFactory factory = null;
    private static RMISocketFactory defaultSocketFactory;
    private static RMIFailureHandler handler = null;
    public RMISocketFactory() {
        super();
    }
    public abstract Socket createSocket(String host, int port)
        throws IOException;
    public abstract ServerSocket createServerSocket(int port)
        throws IOException;
    public synchronized static void setSocketFactory(RMISocketFactory fac)
        throws IOException
    {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }
    public synchronized static RMISocketFactory getSocketFactory()
    {
        return factory;
    }
    public synchronized static RMISocketFactory getDefaultSocketFactory() {
        if (defaultSocketFactory == null) {
            defaultSocketFactory =
                new sun.rmi.transport.proxy.RMIMasterSocketFactory();
        }
        return defaultSocketFactory;
    }
    public synchronized static void setFailureHandler(RMIFailureHandler fh)
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        handler = fh;
    }
    public synchronized static RMIFailureHandler getFailureHandler()
    {
        return handler;
    }
}
