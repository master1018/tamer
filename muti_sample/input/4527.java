public abstract class SSLServerSocketFactory extends ServerSocketFactory
{
    private static SSLServerSocketFactory theFactory;
    private static boolean propertyChecked;
    private static void log(String msg) {
        if (SSLSocketFactory.DEBUG) {
            System.out.println(msg);
        }
    }
    protected SSLServerSocketFactory() {  }
    public static synchronized ServerSocketFactory getDefault() {
        if (theFactory != null) {
            return theFactory;
        }
        if (propertyChecked == false) {
            propertyChecked = true;
            String clsName = SSLSocketFactory.getSecurityProperty
                                        ("ssl.ServerSocketFactory.provider");
            if (clsName != null) {
                log("setting up default SSLServerSocketFactory");
                try {
                    Class cls = null;
                    try {
                        cls = Class.forName(clsName);
                    } catch (ClassNotFoundException e) {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        if (cl != null) {
                            cls = cl.loadClass(clsName);
                        }
                    }
                    log("class " + clsName + " is loaded");
                    SSLServerSocketFactory fac = (SSLServerSocketFactory)cls.newInstance();
                    log("instantiated an instance of class " + clsName);
                    theFactory = fac;
                    return fac;
                } catch (Exception e) {
                    log("SSLServerSocketFactory instantiation failed: " + e);
                    theFactory = new DefaultSSLServerSocketFactory(e);
                    return theFactory;
                }
            }
        }
        try {
            return SSLContext.getDefault().getServerSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            return new DefaultSSLServerSocketFactory(e);
        }
    }
    public abstract String [] getDefaultCipherSuites();
    public abstract String [] getSupportedCipherSuites();
}
class DefaultSSLServerSocketFactory extends SSLServerSocketFactory {
    private final Exception reason;
    DefaultSSLServerSocketFactory(Exception reason) {
        this.reason = reason;
    }
    private ServerSocket throwException() throws SocketException {
        throw (SocketException)
            new SocketException(reason.toString()).initCause(reason);
    }
    public ServerSocket createServerSocket() throws IOException {
        return throwException();
    }
    public ServerSocket createServerSocket(int port)
    throws IOException
    {
        return throwException();
    }
    public ServerSocket createServerSocket(int port, int backlog)
    throws IOException
    {
        return throwException();
    }
    public ServerSocket
    createServerSocket(int port, int backlog, InetAddress ifAddress)
    throws IOException
    {
        return throwException();
    }
    public String [] getDefaultCipherSuites() {
        return new String[0];
    }
    public String [] getSupportedCipherSuites() {
        return new String[0];
    }
}
