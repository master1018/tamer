public class RegistryContextFactory
        implements ObjectFactory, InitialContextFactory
{
    public final static String ADDRESS_TYPE = "URL";
    public Context getInitialContext(Hashtable<?,?> env) throws NamingException {
        if (env != null) {
            env = (Hashtable) env.clone();
        }
        return URLToContext(getInitCtxURL(env), env);
    }
    public Object getObjectInstance(Object ref, Name name, Context nameCtx,
                                    Hashtable<?,?> env)
            throws NamingException
    {
        if (!isRegistryRef(ref)) {
            return null;
        }
        Object obj = URLsToObject(getURLs((Reference)ref), env);
        if (obj instanceof RegistryContext) {
            RegistryContext ctx = (RegistryContext)obj;
            ctx.reference = (Reference)ref;
        }
        return obj;
    }
    private static Context URLToContext(String url, Hashtable env)
            throws NamingException
    {
        rmiURLContextFactory factory = new rmiURLContextFactory();
        Object obj = factory.getObjectInstance(url, null, null, env);
        if (obj instanceof Context) {
            return (Context)obj;
        } else {
            throw (new NotContextException(url));
        }
    }
    private static Object URLsToObject(String[] urls, Hashtable env)
            throws NamingException
    {
        rmiURLContextFactory factory = new rmiURLContextFactory();
        return factory.getObjectInstance(urls, null, null, env);
    }
    private static String getInitCtxURL(Hashtable env) {
        final String defaultURL = "rmi:";
        String url = null;
        if (env != null) {
            url = (String)env.get(Context.PROVIDER_URL);
        }
        return ((url != null) ? url : defaultURL);
    }
    private static boolean isRegistryRef(Object obj) {
        if (!(obj instanceof Reference)) {
            return false;
        }
        String thisClassName = RegistryContextFactory.class.getName();
        Reference ref = (Reference)obj;
        return thisClassName.equals(ref.getFactoryClassName());
    }
    private static String[] getURLs(Reference ref) throws NamingException {
        int size = 0;   
        String[] urls = new String[ref.size()];
        Enumeration addrs = ref.getAll();
        while (addrs.hasMoreElements()) {
            RefAddr addr = (RefAddr)addrs.nextElement();
            if ((addr instanceof StringRefAddr) &&
                addr.getType().equals(ADDRESS_TYPE)) {
                urls[size++] = (String)addr.getContent();
            }
        }
        if (size == 0) {
            throw (new ConfigurationException(
                    "Reference contains no valid addresses"));
        }
        if (size == ref.size()) {
            return urls;
        }
        String[] urls2 = new String[size];
        System.arraycopy(urls, 0, urls2, 0, size);
        return urls2;
    }
}
