public abstract class SSLServerSocketFactory extends ServerSocketFactory {
    private static ServerSocketFactory defaultServerSocketFactory;
    private static String defaultName;
    public static synchronized ServerSocketFactory getDefault() {
        if (defaultServerSocketFactory != null) {
            return defaultServerSocketFactory;
        }
        if (defaultName == null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    defaultName = Security.getProperty("ssl.ServerSocketFactory.provider");
                    if (defaultName != null) {
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        if (cl == null) {
                            cl = ClassLoader.getSystemClassLoader();
                        }
                        try {
                            final Class<?> ssfc = Class.forName(defaultName, true, cl);
                            defaultServerSocketFactory = (ServerSocketFactory) ssfc.newInstance();
                        } catch (Exception e) {
                        }
                    }
                    return null;
                }
            });
        }
        if (defaultServerSocketFactory == null) {
            SSLContext context = DefaultSSLContext.getContext();
            if (context != null) {
                defaultServerSocketFactory = context.getServerSocketFactory();
            }
        }
        if (defaultServerSocketFactory == null) {
            defaultServerSocketFactory = new DefaultSSLServerSocketFactory(
                    "No ServerSocketFactory installed");
        }
        return defaultServerSocketFactory;
    }
    protected SSLServerSocketFactory() {
        super();
    }
    public abstract String[] getDefaultCipherSuites();
    public abstract String[] getSupportedCipherSuites();
}
