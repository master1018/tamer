public abstract class SelectorProvider extends Object {
    private static final String SYMBOL_COMMENT = "#"; 
    private static final String PROVIDER_IN_SYSTEM_PROPERTY = "java.nio.channels.spi.SelectorProvider"; 
    private static final String PROVIDER_IN_JAR_RESOURCE = "META-INF/services/java.nio.channels.spi.SelectorProvider"; 
    private static SelectorProvider provider = null;
    private static Channel inheritedChannel;
    protected SelectorProvider() {
        super();
        if (null != System.getSecurityManager()) {
            System.getSecurityManager().checkPermission(
                    new RuntimePermission("selectorProvider")); 
        }
    }
    synchronized public static SelectorProvider provider() {
        if (null == provider) {
            provider = loadProviderByProperty();
            if (null == provider) {
                provider = loadProviderByJar();
            }
            if (null == provider) {
                provider = AccessController
                        .doPrivileged(new PrivilegedAction<SelectorProvider>() {
                            public SelectorProvider run() {
                                return new SelectorProviderImpl();
                            }
                        });
            }
        }
        return provider;
    }
    static SelectorProvider loadProviderByJar() {
        Enumeration<URL> enumeration = null;
        ClassLoader classLoader = AccessController
                .doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return ClassLoader.getSystemClassLoader();
                    }
                });
        try {
            enumeration = classLoader.getResources(PROVIDER_IN_JAR_RESOURCE);
        } catch (IOException e) {
            throw new Error(e);
        }
        if (null == enumeration) {
            return null;
        }
        while (enumeration.hasMoreElements()) {
            BufferedReader br = null;
            String className = null;
            try {
                br = new BufferedReader(
                        new InputStreamReader(
                                (enumeration.nextElement()).openStream()),
                        8192);
            } catch (Exception e) {
                continue;
            }
            try {
                while ((className = br.readLine()) != null) {
                    className = className.trim();
                    int siteComment = className.indexOf(SYMBOL_COMMENT);
                    className = (-1 == siteComment) ? className : className
                            .substring(0, siteComment);
                    if (0 < className.length()) {
                        return (SelectorProvider) classLoader.loadClass(
                                className).newInstance();
                    }
                }
            } catch (Exception e) {
                throw new Error(e);
            } finally {
                try {
                    br.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }
    static SelectorProvider loadProviderByProperty() {
        return AccessController
                .doPrivileged(new PrivilegedAction<SelectorProvider>() {
                    public SelectorProvider run() {
                        try {
                            final String className = System
                                    .getProperty(PROVIDER_IN_SYSTEM_PROPERTY);
                            if (null != className) {
                                Class<?> spClass = ClassLoader
                                        .getSystemClassLoader().loadClass(
                                                className);
                                return (SelectorProvider) spClass.newInstance();
                            }
                            return null;
                        } catch (Exception e) {
                            throw new Error(e);
                        }
                    }
                });
    }
    public abstract DatagramChannel openDatagramChannel() throws IOException;
    public abstract Pipe openPipe() throws IOException;
    public abstract AbstractSelector openSelector() throws IOException;
    public abstract ServerSocketChannel openServerSocketChannel()
            throws IOException;
    public abstract SocketChannel openSocketChannel() throws IOException;
    public Channel inheritedChannel() throws IOException {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkPermission(
                    new RuntimePermission("inheritedChannel")); 
        }
        if (null == inheritedChannel) {
            inheritedChannel = Platform.getNetworkSystem().inheritedChannel();
        }
        return inheritedChannel;
    }
}
