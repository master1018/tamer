public final class ResourceManager {
    private static final String PROVIDER_RESOURCE_FILE_NAME =
            "jndiprovider.properties";
    private static final String APP_RESOURCE_FILE_NAME = "jndi.properties";
    private static final String JRELIB_PROPERTY_FILE_NAME = "jndi.properties";
    private static final String[] listProperties = {
        Context.OBJECT_FACTORIES,
        Context.URL_PKG_PREFIXES,
        Context.STATE_FACTORIES,
        javax.naming.ldap.LdapContext.CONTROL_FACTORIES
    };
    private static final VersionHelper helper =
            VersionHelper.getVersionHelper();
    private static final WeakHashMap propertiesCache = new WeakHashMap(11);
    private static final WeakHashMap factoryCache = new WeakHashMap(11);
    private static final WeakHashMap urlFactoryCache = new WeakHashMap(11);
    private static final WeakReference NO_FACTORY = new WeakReference(null);
    private static class AppletParameter {
        private static final Class<?> clazz = getClass("java.applet.Applet");
        private static final Method getMethod =
            getMethod(clazz, "getParameter", String.class);
        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        private static Method getMethod(Class<?> clazz,
                                        String name,
                                        Class<?>... paramTypes)
        {
            if (clazz != null) {
                try {
                    return clazz.getMethod(name, paramTypes);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            } else {
                return null;
            }
        }
        static Object get(Object applet, String name) {
            if (clazz == null || !clazz.isInstance(applet))
                throw new ClassCastException(applet.getClass().getName());
            try {
                return getMethod.invoke(applet, name);
            } catch (InvocationTargetException e) {
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }
    }
    private ResourceManager() {
    }
    public static Hashtable getInitialEnvironment(Hashtable env)
            throws NamingException
    {
        String[] props = VersionHelper.PROPS;   
        if (env == null) {
            env = new Hashtable(11);
        }
        Object applet = env.get(Context.APPLET);
        String[] jndiSysProps = helper.getJndiProperties();
        for (int i = 0; i < props.length; i++) {
            Object val = env.get(props[i]);
            if (val == null) {
                if (applet != null) {
                    val = AppletParameter.get(applet, props[i]);
                }
                if (val == null) {
                    val = (jndiSysProps != null)
                        ? jndiSysProps[i]
                        : helper.getJndiProperty(i);
                }
                if (val != null) {
                    env.put(props[i], val);
                }
            }
        }
        mergeTables(env, getApplicationResources());
        return env;
    }
    public static String getProperty(String propName, Hashtable env,
        Context ctx, boolean concat)
            throws NamingException {
        String val1 = (env != null) ? (String)env.get(propName) : null;
        if ((ctx == null) ||
            ((val1 != null) && !concat)) {
            return val1;
        }
        String val2 = (String)getProviderResource(ctx).get(propName);
        if (val1 == null) {
            return val2;
        } else if ((val2 == null) || !concat) {
            return val1;
        } else {
            return (val1 + ":" + val2);
        }
    }
    public static FactoryEnumeration getFactories(String propName, Hashtable env,
        Context ctx) throws NamingException {
        String facProp = getProperty(propName, env, ctx, true);
        if (facProp == null)
            return null;  
        ClassLoader loader = helper.getContextClassLoader();
        Map perLoaderCache = null;
        synchronized (factoryCache) {
            perLoaderCache = (Map) factoryCache.get(loader);
            if (perLoaderCache == null) {
                perLoaderCache = new HashMap(11);
                factoryCache.put(loader, perLoaderCache);
            }
        }
        synchronized (perLoaderCache) {
            List factories = (List) perLoaderCache.get(facProp);
            if (factories != null) {
                return factories.size() == 0 ? null
                    : new FactoryEnumeration(factories, loader);
            } else {
                StringTokenizer parser = new StringTokenizer(facProp, ":");
                factories = new ArrayList(5);
                while (parser.hasMoreTokens()) {
                    try {
                        String className = parser.nextToken();
                        Class c = helper.loadClass(className, loader);
                        factories.add(new NamedWeakReference(c, className));
                    } catch (Exception e) {
                    }
                }
                perLoaderCache.put(facProp, factories);
                return new FactoryEnumeration(factories, loader);
            }
        }
    }
    public static Object getFactory(String propName, Hashtable env, Context ctx,
        String classSuffix, String defaultPkgPrefix) throws NamingException {
        String facProp = getProperty(propName, env, ctx, true);
        if (facProp != null)
            facProp += (":" + defaultPkgPrefix);
        else
            facProp = defaultPkgPrefix;
        ClassLoader loader = helper.getContextClassLoader();
        String key = classSuffix + " " + facProp;
        Map perLoaderCache = null;
        synchronized (urlFactoryCache) {
            perLoaderCache = (Map) urlFactoryCache.get(loader);
            if (perLoaderCache == null) {
                perLoaderCache = new HashMap(11);
                urlFactoryCache.put(loader, perLoaderCache);
            }
        }
        synchronized (perLoaderCache) {
            Object factory = null;
            WeakReference factoryRef = (WeakReference) perLoaderCache.get(key);
            if (factoryRef == NO_FACTORY) {
                return null;
            } else if (factoryRef != null) {
                factory = factoryRef.get();
                if (factory != null) {  
                    return factory;
                }
            }
            StringTokenizer parser = new StringTokenizer(facProp, ":");
            String className;
            while (factory == null && parser.hasMoreTokens()) {
                className = parser.nextToken() + classSuffix;
                try {
                    factory = helper.loadClass(className, loader).newInstance();
                } catch (InstantiationException e) {
                    NamingException ne =
                        new NamingException("Cannot instantiate " + className);
                    ne.setRootCause(e);
                    throw ne;
                } catch (IllegalAccessException e) {
                    NamingException ne =
                        new NamingException("Cannot access " + className);
                    ne.setRootCause(e);
                    throw ne;
                } catch (Exception e) {
                }
            }
            perLoaderCache.put(key, (factory != null)
                                        ? new WeakReference(factory)
                                        : NO_FACTORY);
            return factory;
        }
    }
    private static Hashtable getProviderResource(Object obj)
            throws NamingException
    {
        if (obj == null) {
            return (new Hashtable(1));
        }
        synchronized (propertiesCache) {
            Class c = obj.getClass();
            Hashtable props = (Hashtable)propertiesCache.get(c);
            if (props != null) {
                return props;
            }
            props = new Properties();
            InputStream istream =
                helper.getResourceAsStream(c, PROVIDER_RESOURCE_FILE_NAME);
            if (istream != null) {
                try {
                    ((Properties)props).load(istream);
                } catch (IOException e) {
                    NamingException ne = new ConfigurationException(
                            "Error reading provider resource file for " + c);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            propertiesCache.put(c, props);
            return props;
        }
    }
    private static Hashtable getApplicationResources() throws NamingException {
        ClassLoader cl = helper.getContextClassLoader();
        synchronized (propertiesCache) {
            Hashtable result = (Hashtable)propertiesCache.get(cl);
            if (result != null) {
                return result;
            }
            try {
                NamingEnumeration resources =
                    helper.getResources(cl, APP_RESOURCE_FILE_NAME);
                while (resources.hasMore()) {
                    Properties props = new Properties();
                    props.load((InputStream)resources.next());
                    if (result == null) {
                        result = props;
                    } else {
                        mergeTables(result, props);
                    }
                }
                InputStream istream =
                    helper.getJavaHomeLibStream(JRELIB_PROPERTY_FILE_NAME);
                if (istream != null) {
                    Properties props = new Properties();
                    props.load(istream);
                    if (result == null) {
                        result = props;
                    } else {
                        mergeTables(result, props);
                    }
                }
            } catch (IOException e) {
                NamingException ne = new ConfigurationException(
                        "Error reading application resource file");
                ne.setRootCause(e);
                throw ne;
            }
            if (result == null) {
                result = new Hashtable(11);
            }
            propertiesCache.put(cl, result);
            return result;
        }
    }
    private static void mergeTables(Hashtable props1, Hashtable props2) {
        Enumeration keys = props2.keys();
        while (keys.hasMoreElements()) {
            String prop = (String)keys.nextElement();
            Object val1 = props1.get(prop);
            if (val1 == null) {
                props1.put(prop, props2.get(prop));
            } else if (isListProperty(prop)) {
                String val2 = (String)props2.get(prop);
                props1.put(prop, ((String)val1) + ":" + val2);
            }
        }
    }
    private static boolean isListProperty(String prop) {
        prop = prop.intern();
        for (int i = 0; i < listProperties.length; i++) {
            if (prop == listProperties[i]) {
                return true;
            }
        }
        return false;
    }
}
