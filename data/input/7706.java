public abstract class HttpServerProvider {
    public abstract HttpServer createHttpServer (InetSocketAddress addr, int backlog) throws IOException;
    public abstract HttpsServer createHttpsServer (InetSocketAddress addr, int backlog) throws IOException;
    private static final Object lock = new Object();
    private static HttpServerProvider provider = null;
    protected HttpServerProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("httpServerProvider"));
    }
    private static boolean loadProviderFromProperty() {
        String cn = System.getProperty("com.sun.net.httpserver.HttpServerProvider");
        if (cn == null)
            return false;
        try {
            Class c = Class.forName(cn, true,
                                    ClassLoader.getSystemClassLoader());
            provider = (HttpServerProvider)c.newInstance();
            return true;
        } catch (ClassNotFoundException x) {
            throw new ServiceConfigurationError(x);
        } catch (IllegalAccessException x) {
            throw new ServiceConfigurationError(x);
        } catch (InstantiationException x) {
            throw new ServiceConfigurationError(x);
        } catch (SecurityException x) {
            throw new ServiceConfigurationError(x);
        }
    }
    private static boolean loadProviderAsService() {
        Iterator i = Service.providers(HttpServerProvider.class,
                                       ClassLoader.getSystemClassLoader());
        for (;;) {
            try {
                if (!i.hasNext())
                    return false;
                provider = (HttpServerProvider)i.next();
                return true;
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) {
                    continue;
                }
                throw sce;
            }
        }
    }
    public static HttpServerProvider provider () {
        synchronized (lock) {
            if (provider != null)
                return provider;
            return (HttpServerProvider)AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            if (loadProviderFromProperty())
                                return provider;
                            if (loadProviderAsService())
                                return provider;
                            provider = new sun.net.httpserver.DefaultHttpServerProvider();
                            return provider;
                        }
                    });
        }
    }
}
