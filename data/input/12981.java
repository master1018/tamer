public class JMXConnectorFactory {
    public static final String DEFAULT_CLASS_LOADER =
        "jmx.remote.default.class.loader";
    public static final String PROTOCOL_PROVIDER_PACKAGES =
        "jmx.remote.protocol.provider.pkgs";
    public static final String PROTOCOL_PROVIDER_CLASS_LOADER =
        "jmx.remote.protocol.provider.class.loader";
    private static final String PROTOCOL_PROVIDER_DEFAULT_PACKAGE =
        "com.sun.jmx.remote.protocol";
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc", "JMXConnectorFactory");
    private JMXConnectorFactory() {
    }
    public static JMXConnector connect(JMXServiceURL serviceURL)
            throws IOException {
        return connect(serviceURL, null);
    }
    public static JMXConnector connect(JMXServiceURL serviceURL,
                                       Map<String,?> environment)
            throws IOException {
        if (serviceURL == null)
            throw new NullPointerException("Null JMXServiceURL");
        JMXConnector conn = newJMXConnector(serviceURL, environment);
        conn.connect(environment);
        return conn;
    }
    private static <K,V> Map<K,V> newHashMap() {
        return new HashMap<K,V>();
    }
    private static <K> Map<K,Object> newHashMap(Map<K,?> map) {
        return new HashMap<K,Object>(map);
    }
    public static JMXConnector newJMXConnector(JMXServiceURL serviceURL,
                                               Map<String,?> environment)
            throws IOException {
        final Map<String,Object> envcopy;
        if (environment == null)
            envcopy = newHashMap();
        else {
            EnvHelp.checkAttributes(environment);
            envcopy = newHashMap(environment);
        }
        final ClassLoader loader = resolveClassLoader(envcopy);
        final Class<JMXConnectorProvider> targetInterface =
                JMXConnectorProvider.class;
        final String protocol = serviceURL.getProtocol();
        final String providerClassName = "ClientProvider";
        final JMXServiceURL providerURL = serviceURL;
        JMXConnectorProvider provider = getProvider(providerURL, envcopy,
                                               providerClassName,
                                               targetInterface,
                                               loader);
        IOException exception = null;
        if (provider == null) {
            if (loader != null) {
                try {
                    JMXConnector connection =
                        getConnectorAsService(loader, providerURL, envcopy);
                    if (connection != null)
                        return connection;
                } catch (JMXProviderException e) {
                    throw e;
                } catch (IOException e) {
                    exception = e;
                }
            }
            provider = getProvider(protocol, PROTOCOL_PROVIDER_DEFAULT_PACKAGE,
                            JMXConnectorFactory.class.getClassLoader(),
                            providerClassName, targetInterface);
        }
        if (provider == null) {
            MalformedURLException e =
                new MalformedURLException("Unsupported protocol: " + protocol);
            if (exception == null) {
                throw e;
            } else {
                throw EnvHelp.initCause(e, exception);
            }
        }
        final Map<String,Object> fixedenv =
                Collections.unmodifiableMap(envcopy);
        return provider.newJMXConnector(serviceURL, fixedenv);
    }
    private static String resolvePkgs(Map<String, ?> env)
            throws JMXProviderException {
        Object pkgsObject = null;
        if (env != null)
            pkgsObject = env.get(PROTOCOL_PROVIDER_PACKAGES);
        if (pkgsObject == null)
            pkgsObject =
                AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(PROTOCOL_PROVIDER_PACKAGES);
                    }
                });
        if (pkgsObject == null)
            return null;
        if (!(pkgsObject instanceof String)) {
            final String msg = "Value of " + PROTOCOL_PROVIDER_PACKAGES +
                " parameter is not a String: " +
                pkgsObject.getClass().getName();
            throw new JMXProviderException(msg);
        }
        final String pkgs = (String) pkgsObject;
        if (pkgs.trim().equals(""))
            return null;
        if (pkgs.startsWith("|") || pkgs.endsWith("|") ||
            pkgs.indexOf("||") >= 0) {
            final String msg = "Value of " + PROTOCOL_PROVIDER_PACKAGES +
                " contains an empty element: " + pkgs;
            throw new JMXProviderException(msg);
        }
        return pkgs;
    }
    static <T> T getProvider(JMXServiceURL serviceURL,
                             Map<String, Object> environment,
                             String providerClassName,
                             Class<T> targetInterface,
                             ClassLoader loader)
            throws IOException {
        final String protocol = serviceURL.getProtocol();
        final String pkgs = resolvePkgs(environment);
        T instance = null;
        if (pkgs != null) {
            environment.put(PROTOCOL_PROVIDER_CLASS_LOADER, loader);
            instance =
                getProvider(protocol, pkgs, loader, providerClassName,
                            targetInterface);
        }
        return instance;
    }
    static <T> Iterator<T> getProviderIterator(final Class<T> providerClass,
                                               final ClassLoader loader) {
       ServiceLoader<T> serviceLoader =
                ServiceLoader.load(providerClass, loader);
       return serviceLoader.iterator();
    }
    private static JMXConnector getConnectorAsService(ClassLoader loader,
                                                      JMXServiceURL url,
                                                      Map<String, ?> map)
        throws IOException {
        Iterator<JMXConnectorProvider> providers =
                getProviderIterator(JMXConnectorProvider.class, loader);
        JMXConnector connection;
        IOException exception = null;
        while (providers.hasNext()) {
            JMXConnectorProvider provider = providers.next();
            try {
                connection = provider.newJMXConnector(url, map);
                return connection;
            } catch (JMXProviderException e) {
                throw e;
            } catch (Exception e) {
                if (logger.traceOn())
                    logger.trace("getConnectorAsService",
                                 "URL[" + url +
                                 "] Service provider exception: " + e);
                if (!(e instanceof MalformedURLException)) {
                    if (exception == null) {
                        if (e instanceof IOException) {
                            exception = (IOException) e;
                        } else {
                            exception = EnvHelp.initCause(
                                new IOException(e.getMessage()), e);
                        }
                    }
                }
                continue;
            }
        }
        if (exception == null)
            return null;
        else
            throw exception;
    }
    static <T> T getProvider(String protocol,
                              String pkgs,
                              ClassLoader loader,
                              String providerClassName,
                              Class<T> targetInterface)
            throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(pkgs, "|");
        while (tokenizer.hasMoreTokens()) {
            String pkg = tokenizer.nextToken();
            String className = (pkg + "." + protocol2package(protocol) +
                                "." + providerClassName);
            Class<?> providerClass;
            try {
                providerClass = Class.forName(className, true, loader);
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (!targetInterface.isAssignableFrom(providerClass)) {
                final String msg =
                    "Provider class does not implement " +
                    targetInterface.getName() + ": " +
                    providerClass.getName();
                throw new JMXProviderException(msg);
            }
            Class<? extends T> providerClassT = Util.cast(providerClass);
            try {
                return providerClassT.newInstance();
            } catch (Exception e) {
                final String msg =
                    "Exception when instantiating provider [" + className +
                    "]";
                throw new JMXProviderException(msg, e);
            }
        }
        return null;
    }
    static ClassLoader resolveClassLoader(Map<String, ?> environment) {
        ClassLoader loader = null;
        if (environment != null) {
            try {
                loader = (ClassLoader)
                    environment.get(PROTOCOL_PROVIDER_CLASS_LOADER);
            } catch (ClassCastException e) {
                final String msg =
                    "The ClassLoader supplied in the environment map using " +
                    "the " + PROTOCOL_PROVIDER_CLASS_LOADER +
                    " attribute is not an instance of java.lang.ClassLoader";
                throw new IllegalArgumentException(msg);
            }
        }
        if (loader == null)
            loader = AccessController.doPrivileged(
                    new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return
                                Thread.currentThread().getContextClassLoader();
                        }
                    });
        return loader;
    }
    private static String protocol2package(String protocol) {
        return protocol.replace('+', '.').replace('-', '_');
    }
}
