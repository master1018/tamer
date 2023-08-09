public class NamingManager {
    NamingManager() {}
    static final VersionHelper helper = VersionHelper.getVersionHelper();
    private static ObjectFactoryBuilder object_factory_builder = null;
    public static synchronized void setObjectFactoryBuilder(
            ObjectFactoryBuilder builder) throws NamingException {
        if (object_factory_builder != null)
            throw new IllegalStateException("ObjectFactoryBuilder already set");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        object_factory_builder = builder;
    }
    static synchronized ObjectFactoryBuilder getObjectFactoryBuilder() {
        return object_factory_builder;
    }
    static ObjectFactory getObjectFactoryFromReference(
        Reference ref, String factoryName)
        throws IllegalAccessException,
        InstantiationException,
        MalformedURLException {
        Class clas = null;
        try {
             clas = helper.loadClass(factoryName);
        } catch (ClassNotFoundException e) {
        }
        String codebase;
        if (clas == null &&
                (codebase = ref.getFactoryClassLocation()) != null) {
            try {
                clas = helper.loadClass(factoryName, codebase);
            } catch (ClassNotFoundException e) {
            }
        }
        return (clas != null) ? (ObjectFactory) clas.newInstance() : null;
    }
    private static Object createObjectFromFactories(Object obj, Name name,
            Context nameCtx, Hashtable environment) throws Exception {
        FactoryEnumeration factories = ResourceManager.getFactories(
            Context.OBJECT_FACTORIES, environment, nameCtx);
        if (factories == null)
            return null;
        ObjectFactory factory;
        Object answer = null;
        while (answer == null && factories.hasMore()) {
            factory = (ObjectFactory)factories.next();
            answer = factory.getObjectInstance(obj, name, nameCtx, environment);
        }
        return answer;
    }
    private static String getURLScheme(String str) {
        int colon_posn = str.indexOf(':');
        int slash_posn = str.indexOf('/');
        if (colon_posn > 0 && (slash_posn == -1 || colon_posn < slash_posn))
            return str.substring(0, colon_posn);
        return null;
    }
    public static Object
        getObjectInstance(Object refInfo, Name name, Context nameCtx,
                          Hashtable<?,?> environment)
        throws Exception
    {
        ObjectFactory factory;
        ObjectFactoryBuilder builder = getObjectFactoryBuilder();
        if (builder != null) {
            factory = builder.createObjectFactory(refInfo, environment);
            return factory.getObjectInstance(refInfo, name, nameCtx,
                environment);
        }
        Reference ref = null;
        if (refInfo instanceof Reference) {
            ref = (Reference) refInfo;
        } else if (refInfo instanceof Referenceable) {
            ref = ((Referenceable)(refInfo)).getReference();
        }
        Object answer;
        if (ref != null) {
            String f = ref.getFactoryClassName();
            if (f != null) {
                factory = getObjectFactoryFromReference(ref, f);
                if (factory != null) {
                    return factory.getObjectInstance(ref, name, nameCtx,
                                                     environment);
                }
                return refInfo;
            } else {
                answer = processURLAddrs(ref, name, nameCtx, environment);
                if (answer != null) {
                    return answer;
                }
            }
        }
        answer =
            createObjectFromFactories(refInfo, name, nameCtx, environment);
        return (answer != null) ? answer : refInfo;
    }
    static Object processURLAddrs(Reference ref, Name name, Context nameCtx,
                                  Hashtable environment)
            throws NamingException {
        for (int i = 0; i < ref.size(); i++) {
            RefAddr addr = ref.get(i);
            if (addr instanceof StringRefAddr &&
                addr.getType().equalsIgnoreCase("URL")) {
                String url = (String)addr.getContent();
                Object answer = processURL(url, name, nameCtx, environment);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }
    private static Object processURL(Object refInfo, Name name,
                                     Context nameCtx, Hashtable environment)
            throws NamingException {
        Object answer;
        if (refInfo instanceof String) {
            String url = (String)refInfo;
            String scheme = getURLScheme(url);
            if (scheme != null) {
                answer = getURLObject(scheme, refInfo, name, nameCtx,
                                      environment);
                if (answer != null) {
                    return answer;
                }
            }
        }
        if (refInfo instanceof String[]) {
            String[] urls = (String[])refInfo;
            for (int i = 0; i <urls.length; i++) {
                String scheme = getURLScheme(urls[i]);
                if (scheme != null) {
                    answer = getURLObject(scheme, refInfo, name, nameCtx,
                                          environment);
                    if (answer != null)
                        return answer;
                }
            }
        }
        return null;
    }
    static Context getContext(Object obj, Name name, Context nameCtx,
                              Hashtable environment) throws NamingException {
        Object answer;
        if (obj instanceof Context) {
            return (Context)obj;
        }
        try {
            answer = getObjectInstance(obj, name, nameCtx, environment);
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
        return (answer instanceof Context)
            ? (Context)answer
            : null;
    }
    static Resolver getResolver(Object obj, Name name, Context nameCtx,
                                Hashtable environment) throws NamingException {
        Object answer;
        if (obj instanceof Resolver) {
            return (Resolver)obj;
        }
        try {
            answer = getObjectInstance(obj, name, nameCtx, environment);
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
        return (answer instanceof Resolver)
            ? (Resolver)answer
            : null;
    }
    public static Context getURLContext(String scheme,
                                        Hashtable<?,?> environment)
        throws NamingException
    {
            Object answer = getURLObject(scheme, null, null, null, environment);
            if (answer instanceof Context) {
                return (Context)answer;
            } else {
                return null;
            }
    }
    private static final String defaultPkgPrefix = "com.sun.jndi.url";
    private static Object getURLObject(String scheme, Object urlInfo,
                                       Name name, Context nameCtx,
                                       Hashtable environment)
            throws NamingException {
        ObjectFactory factory = (ObjectFactory)ResourceManager.getFactory(
            Context.URL_PKG_PREFIXES, environment, nameCtx,
            "." + scheme + "." + scheme + "URLContextFactory", defaultPkgPrefix);
        if (factory == null)
          return null;
        try {
            return factory.getObjectInstance(urlInfo, name, nameCtx, environment);
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }
    private static InitialContextFactoryBuilder initctx_factory_builder = null;
    private static synchronized InitialContextFactoryBuilder
    getInitialContextFactoryBuilder() {
        return initctx_factory_builder;
    }
    public static Context getInitialContext(Hashtable<?,?> env)
        throws NamingException {
        InitialContextFactory factory;
        InitialContextFactoryBuilder builder = getInitialContextFactoryBuilder();
        if (builder == null) {
            String className = env != null ?
                (String)env.get(Context.INITIAL_CONTEXT_FACTORY) : null;
            if (className == null) {
                NoInitialContextException ne = new NoInitialContextException(
                    "Need to specify class name in environment or system " +
                    "property, or as an applet parameter, or in an " +
                    "application resource file:  " +
                    Context.INITIAL_CONTEXT_FACTORY);
                throw ne;
            }
            try {
                factory = (InitialContextFactory)
                    helper.loadClass(className).newInstance();
            } catch(Exception e) {
                NoInitialContextException ne =
                    new NoInitialContextException(
                        "Cannot instantiate class: " + className);
                ne.setRootCause(e);
                throw ne;
            }
        } else {
            factory = builder.createInitialContextFactory(env);
        }
        return factory.getInitialContext(env);
    }
    public static synchronized void setInitialContextFactoryBuilder(
        InitialContextFactoryBuilder builder)
        throws NamingException {
            if (initctx_factory_builder != null)
                throw new IllegalStateException(
                    "InitialContextFactoryBuilder already set");
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            initctx_factory_builder = builder;
    }
    public static boolean hasInitialContextFactoryBuilder() {
        return (getInitialContextFactoryBuilder() != null);
    }
    public static final String CPE = "java.naming.spi.CannotProceedException";
    public static Context getContinuationContext(CannotProceedException cpe)
            throws NamingException {
        Hashtable env = cpe.getEnvironment();
        if (env == null) {
            env = new Hashtable(7);
        } else {
            env = (Hashtable) env.clone();
        }
        env.put(CPE, cpe);
        ContinuationContext cctx = new ContinuationContext(cpe, env);
        return cctx.getTargetContext();
    }
    public static Object
        getStateToBind(Object obj, Name name, Context nameCtx,
                       Hashtable<?,?> environment)
        throws NamingException
    {
        FactoryEnumeration factories = ResourceManager.getFactories(
            Context.STATE_FACTORIES, environment, nameCtx);
        if (factories == null) {
            return obj;
        }
        StateFactory factory;
        Object answer = null;
        while (answer == null && factories.hasMore()) {
            factory = (StateFactory)factories.next();
            answer = factory.getStateToBind(obj, name, nameCtx, environment);
        }
        return (answer != null) ? answer : obj;
    }
}
