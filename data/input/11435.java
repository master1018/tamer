public class RMIClassLoader {
    private static final RMIClassLoaderSpi defaultProvider =
        newDefaultProviderInstance();
    private static final RMIClassLoaderSpi provider =
        AccessController.doPrivileged(
            new PrivilegedAction<RMIClassLoaderSpi>() {
                public RMIClassLoaderSpi run() { return initializeProvider(); }
            });
    private RMIClassLoader() {}
    @Deprecated
    public static Class<?> loadClass(String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return loadClass((String) null, name);
    }
    public static Class<?> loadClass(URL codebase, String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(
            codebase != null ? codebase.toString() : null, name, null);
    }
    public static Class<?> loadClass(String codebase, String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(codebase, name, null);
    }
    public static Class<?> loadClass(String codebase, String name,
                                     ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(codebase, name, defaultLoader);
    }
    public static Class<?> loadProxyClass(String codebase, String[] interfaces,
                                          ClassLoader defaultLoader)
        throws ClassNotFoundException, MalformedURLException
    {
        return provider.loadProxyClass(codebase, interfaces, defaultLoader);
    }
    public static ClassLoader getClassLoader(String codebase)
        throws MalformedURLException, SecurityException
    {
        return provider.getClassLoader(codebase);
    }
    public static String getClassAnnotation(Class<?> cl) {
        return provider.getClassAnnotation(cl);
    }
    public static RMIClassLoaderSpi getDefaultProviderInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setFactory"));
        }
        return defaultProvider;
    }
    @Deprecated
    public static Object getSecurityContext(ClassLoader loader)
    {
        return sun.rmi.server.LoaderHandler.getSecurityContext(loader);
    }
    private static RMIClassLoaderSpi newDefaultProviderInstance() {
        return new RMIClassLoaderSpi() {
            public Class<?> loadClass(String codebase, String name,
                                      ClassLoader defaultLoader)
                throws MalformedURLException, ClassNotFoundException
            {
                return sun.rmi.server.LoaderHandler.loadClass(
                    codebase, name, defaultLoader);
            }
            public Class<?> loadProxyClass(String codebase,
                                           String[] interfaces,
                                           ClassLoader defaultLoader)
                throws MalformedURLException, ClassNotFoundException
            {
                return sun.rmi.server.LoaderHandler.loadProxyClass(
                    codebase, interfaces, defaultLoader);
            }
            public ClassLoader getClassLoader(String codebase)
                throws MalformedURLException
            {
                return sun.rmi.server.LoaderHandler.getClassLoader(codebase);
            }
            public String getClassAnnotation(Class<?> cl) {
                return sun.rmi.server.LoaderHandler.getClassAnnotation(cl);
            }
        };
    }
    private static RMIClassLoaderSpi initializeProvider() {
        String providerClassName =
            System.getProperty("java.rmi.server.RMIClassLoaderSpi");
        if (providerClassName != null) {
            if (providerClassName.equals("default")) {
                return defaultProvider;
            }
            try {
                Class<? extends RMIClassLoaderSpi> providerClass =
                    Class.forName(providerClassName, false,
                                  ClassLoader.getSystemClassLoader())
                    .asSubclass(RMIClassLoaderSpi.class);
                return providerClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (ClassCastException e) {
                Error error = new LinkageError(
                    "provider class not assignable to RMIClassLoaderSpi");
                error.initCause(e);
                throw error;
            }
        }
        Iterator<RMIClassLoaderSpi> iter =
            ServiceLoader.load(RMIClassLoaderSpi.class,
                               ClassLoader.getSystemClassLoader()).iterator();
        if (iter.hasNext()) {
            try {
                return iter.next();
            } catch (ClassCastException e) {
                Error error = new LinkageError(
                    "provider class not assignable to RMIClassLoaderSpi");
                error.initCause(e);
                throw error;
            }
        }
        return defaultProvider;
    }
}
