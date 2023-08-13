public abstract class FtpClientProvider {
    public abstract FtpClient createFtpClient();
    private static final Object lock = new Object();
    private static FtpClientProvider provider = null;
    protected FtpClientProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("ftpClientProvider"));
        }
    }
    private static boolean loadProviderFromProperty() {
        String cm = System.getProperty("sun.net.ftpClientProvider");
        if (cm == null) {
            return false;
        }
        try {
            Class c = Class.forName(cm, true, null);
            provider = (FtpClientProvider) c.newInstance();
            return true;
        } catch (ClassNotFoundException x) {
            throw new ServiceConfigurationError(x.toString());
        } catch (IllegalAccessException x) {
            throw new ServiceConfigurationError(x.toString());
        } catch (InstantiationException x) {
            throw new ServiceConfigurationError(x.toString());
        } catch (SecurityException x) {
            throw new ServiceConfigurationError(x.toString());
        }
    }
    private static boolean loadProviderAsService() {
        return false;
    }
    public static FtpClientProvider provider() {
        synchronized (lock) {
            if (provider != null) {
                return provider;
            }
            return (FtpClientProvider) AccessController.doPrivileged(
                    new PrivilegedAction<Object>() {
                        public Object run() {
                            if (loadProviderFromProperty()) {
                                return provider;
                            }
                            if (loadProviderAsService()) {
                                return provider;
                            }
                            provider = new sun.net.ftp.impl.DefaultFtpClientProvider();
                            return provider;
                        }
                    });
        }
    }
}
