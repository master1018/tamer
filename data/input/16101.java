public class InitialContext implements Context {
    protected Hashtable<Object,Object> myProps = null;
    protected Context defaultInitCtx = null;
    protected boolean gotDefault = false;
    protected InitialContext(boolean lazy) throws NamingException {
        if (!lazy) {
            init(null);
        }
    }
    public InitialContext() throws NamingException {
        init(null);
    }
    public InitialContext(Hashtable<?,?> environment)
        throws NamingException
    {
        if (environment != null) {
            environment = (Hashtable)environment.clone();
        }
        init(environment);
    }
    protected void init(Hashtable<?,?> environment)
        throws NamingException
    {
        myProps = ResourceManager.getInitialEnvironment(environment);
        if (myProps.get(Context.INITIAL_CONTEXT_FACTORY) != null) {
            getDefaultInitCtx();
        }
    }
    public static <T> T doLookup(Name name)
        throws NamingException {
        return (T) (new InitialContext()).lookup(name);
    }
    public static <T> T doLookup(String name)
        throws NamingException {
        return (T) (new InitialContext()).lookup(name);
    }
    private static String getURLScheme(String str) {
        int colon_posn = str.indexOf(':');
        int slash_posn = str.indexOf('/');
        if (colon_posn > 0 && (slash_posn == -1 || colon_posn < slash_posn))
            return str.substring(0, colon_posn);
        return null;
    }
    protected Context getDefaultInitCtx() throws NamingException{
        if (!gotDefault) {
            defaultInitCtx = NamingManager.getInitialContext(myProps);
            gotDefault = true;
        }
        if (defaultInitCtx == null)
            throw new NoInitialContextException();
        return defaultInitCtx;
    }
    protected Context getURLOrDefaultInitCtx(String name)
        throws NamingException {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            return getDefaultInitCtx();
        }
        String scheme = getURLScheme(name);
        if (scheme != null) {
            Context ctx = NamingManager.getURLContext(scheme, myProps);
            if (ctx != null) {
                return ctx;
            }
        }
        return getDefaultInitCtx();
    }
    protected Context getURLOrDefaultInitCtx(Name name)
        throws NamingException {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            return getDefaultInitCtx();
        }
        if (name.size() > 0) {
            String first = name.get(0);
            String scheme = getURLScheme(first);
            if (scheme != null) {
                Context ctx = NamingManager.getURLContext(scheme, myProps);
                if (ctx != null) {
                    return ctx;
                }
            }
        }
        return getDefaultInitCtx();
    }
    public Object lookup(String name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookup(name);
    }
    public Object lookup(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookup(name);
    }
    public void bind(String name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).bind(name, obj);
    }
    public void bind(Name name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).bind(name, obj);
    }
    public void rebind(String name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).rebind(name, obj);
    }
    public void rebind(Name name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).rebind(name, obj);
    }
    public void unbind(String name) throws NamingException  {
        getURLOrDefaultInitCtx(name).unbind(name);
    }
    public void unbind(Name name) throws NamingException  {
        getURLOrDefaultInitCtx(name).unbind(name);
    }
    public void rename(String oldName, String newName) throws NamingException {
        getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
    }
    public void rename(Name oldName, Name newName)
        throws NamingException
    {
        getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
    }
    public NamingEnumeration<NameClassPair> list(String name)
        throws NamingException
    {
        return (getURLOrDefaultInitCtx(name).list(name));
    }
    public NamingEnumeration<NameClassPair> list(Name name)
        throws NamingException
    {
        return (getURLOrDefaultInitCtx(name).list(name));
    }
    public NamingEnumeration<Binding> listBindings(String name)
            throws NamingException  {
        return getURLOrDefaultInitCtx(name).listBindings(name);
    }
    public NamingEnumeration<Binding> listBindings(Name name)
            throws NamingException  {
        return getURLOrDefaultInitCtx(name).listBindings(name);
    }
    public void destroySubcontext(String name) throws NamingException  {
        getURLOrDefaultInitCtx(name).destroySubcontext(name);
    }
    public void destroySubcontext(Name name) throws NamingException  {
        getURLOrDefaultInitCtx(name).destroySubcontext(name);
    }
    public Context createSubcontext(String name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).createSubcontext(name);
    }
    public Context createSubcontext(Name name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).createSubcontext(name);
    }
    public Object lookupLink(String name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).lookupLink(name);
    }
    public Object lookupLink(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookupLink(name);
    }
    public NameParser getNameParser(String name) throws NamingException {
        return getURLOrDefaultInitCtx(name).getNameParser(name);
    }
    public NameParser getNameParser(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).getNameParser(name);
    }
    public String composeName(String name, String prefix)
            throws NamingException {
        return name;
    }
    public Name composeName(Name name, Name prefix)
        throws NamingException
    {
        return (Name)name.clone();
    }
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        myProps.put(propName, propVal);
        return getDefaultInitCtx().addToEnvironment(propName, propVal);
    }
    public Object removeFromEnvironment(String propName)
            throws NamingException {
        myProps.remove(propName);
        return getDefaultInitCtx().removeFromEnvironment(propName);
    }
    public Hashtable<?,?> getEnvironment() throws NamingException {
        return getDefaultInitCtx().getEnvironment();
    }
    public void close() throws NamingException {
        myProps = null;
        if (defaultInitCtx != null) {
            defaultInitCtx.close();
            defaultInitCtx = null;
        }
        gotDefault = false;
    }
    public String getNameInNamespace() throws NamingException {
        return getDefaultInitCtx().getNameInNamespace();
    }
};
