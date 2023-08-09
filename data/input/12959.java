public class RegistryContext implements Context, Referenceable {
    private Hashtable environment;
    private Registry registry;
    private String host;
    private int port;
    private static final NameParser nameParser = new AtomicNameParser();
    private static final String SOCKET_FACTORY = "com.sun.jndi.rmi.factory.socket";
    Reference reference = null; 
    public static final String SECURITY_MGR =
            "java.naming.rmi.security.manager";
    public RegistryContext(String host, int port, Hashtable env)
            throws NamingException
    {
        environment = ((env == null) ? new Hashtable(5) : env);
        if (environment.get(SECURITY_MGR) != null) {
            installSecurityMgr();
        }
        if ((host != null) && (host.charAt(0) == '[')) {
            host = host.substring(1, host.length() - 1);
        }
        RMIClientSocketFactory socketFactory =
                (RMIClientSocketFactory) environment.get(SOCKET_FACTORY);
        registry = getRegistry(host, port, socketFactory);
        this.host = host;
        this.port = port;
    }
    RegistryContext(RegistryContext ctx) {
        environment = (Hashtable)ctx.environment.clone();
        registry = ctx.registry;
        host = ctx.host;
        port = ctx.port;
        reference = ctx.reference;
    }
    protected void finalize() {
        close();
    }
    public Object lookup(Name name) throws NamingException {
        if (name.isEmpty()) {
            return (new RegistryContext(this));
        }
        Remote obj;
        try {
            obj = registry.lookup(name.get(0));
        } catch (NotBoundException e) {
            throw (new NameNotFoundException(name.get(0)));
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
        return (decodeObject(obj, name.getPrefix(1)));
    }
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }
    public void bind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw (new InvalidNameException(
                    "RegistryContext: Cannot bind empty name"));
        }
        try {
            registry.bind(name.get(0), encodeObject(obj, name.getPrefix(1)));
        } catch (AlreadyBoundException e) {
            NamingException ne = new NameAlreadyBoundException(name.get(0));
            ne.setRootCause(e);
            throw ne;
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }
    public void rebind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw (new InvalidNameException(
                    "RegistryContext: Cannot rebind empty name"));
        }
        try {
            registry.rebind(name.get(0), encodeObject(obj, name.getPrefix(1)));
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }
    public void unbind(Name name) throws NamingException {
        if (name.isEmpty()) {
            throw (new InvalidNameException(
                    "RegistryContext: Cannot unbind empty name"));
        }
        try {
            registry.unbind(name.get(0));
        } catch (NotBoundException e) {
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }
    public void rename(Name oldName, Name newName) throws NamingException {
        bind(newName, lookup(oldName));
        unbind(oldName);
    }
    public void rename(String name, String newName) throws NamingException {
        rename(new CompositeName(name), new CompositeName(newName));
    }
    public NamingEnumeration list(Name name)    throws NamingException {
        if (!name.isEmpty()) {
            throw (new InvalidNameException(
                    "RegistryContext: can only list \"\""));
        }
        try {
            String[] names = registry.list();
            return (new NameClassPairEnumeration(names));
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    public NamingEnumeration list(String name) throws NamingException {
        return list(new CompositeName(name));
    }
    public NamingEnumeration listBindings(Name name)
            throws NamingException
    {
        if (!name.isEmpty()) {
            throw (new InvalidNameException(
                    "RegistryContext: can only list \"\""));
        }
        try {
            String[] names = registry.list();
            return (new BindingEnumeration(this, names));
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }
    public void destroySubcontext(Name name) throws NamingException {
        throw (new OperationNotSupportedException());
    }
    public void destroySubcontext(String name) throws NamingException {
        throw (new OperationNotSupportedException());
    }
    public Context createSubcontext(Name name) throws NamingException {
        throw (new OperationNotSupportedException());
    }
    public Context createSubcontext(String name) throws NamingException {
        throw (new OperationNotSupportedException());
    }
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }
    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }
    public NameParser getNameParser(Name name) throws NamingException {
        return nameParser;
    }
    public NameParser getNameParser(String name) throws NamingException {
        return nameParser;
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name)prefix.clone();
        return result.addAll(name);
    }
    public String composeName(String name, String prefix)
            throws NamingException
    {
        return composeName(new CompositeName(name),
                           new CompositeName(prefix)).toString();
    }
    public Object removeFromEnvironment(String propName)
            throws NamingException
    {
        return environment.remove(propName);
    }
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException
    {
        if (propName.equals(SECURITY_MGR)) {
            installSecurityMgr();
        }
        return environment.put(propName, propVal);
    }
    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable)environment.clone();
    }
    public void close() {
        environment = null;
        registry = null;
    }
    public String getNameInNamespace() {
        return ""; 
    }
    public Reference getReference() throws NamingException {
        if (reference != null) {
            return (Reference)reference.clone();  
        }
        if (host == null || host.equals("localhost")) {
            throw (new ConfigurationException(
                    "Cannot create a reference for an RMI registry whose " +
                    "host was unspecified or specified as \"localhost\""));
        }
        String url = "rmi:
        url = (host.indexOf(":") > -1) ? url + "[" + host + "]" :
                                         url + host;
        if (port > 0) {
            url += ":" + Integer.toString(port);
        }
        RefAddr addr = new StringRefAddr(RegistryContextFactory.ADDRESS_TYPE,
                                         url);
        return (new Reference(RegistryContext.class.getName(),
                              addr,
                              RegistryContextFactory.class.getName(),
                              null));
    }
    public static NamingException wrapRemoteException(RemoteException re) {
        NamingException ne;
        if (re instanceof ConnectException) {
            ne = new ServiceUnavailableException();
        } else if (re instanceof AccessException) {
            ne = new NoPermissionException();
        } else if (re instanceof StubNotFoundException ||
                   re instanceof UnknownHostException ||
                   re instanceof SocketSecurityException) {
            ne = new ConfigurationException();
        } else if (re instanceof ExportException ||
                   re instanceof ConnectIOException ||
                   re instanceof MarshalException ||
                   re instanceof UnmarshalException ||
                   re instanceof NoSuchObjectException) {
            ne = new CommunicationException();
        } else if (re instanceof ServerException &&
                   re.detail instanceof RemoteException) {
            ne = wrapRemoteException((RemoteException)re.detail);
        } else {
            ne = new NamingException();
        }
        ne.setRootCause(re);
        return ne;
    }
    private static Registry getRegistry(String host, int port,
                RMIClientSocketFactory socketFactory)
            throws NamingException
    {
        try {
            if (socketFactory == null) {
                return LocateRegistry.getRegistry(host, port);
            } else {
                return LocateRegistry.getRegistry(host, port, socketFactory);
            }
        } catch (RemoteException e) {
            throw (NamingException)wrapRemoteException(e).fillInStackTrace();
        }
    }
    private static void installSecurityMgr() {
        try {
            System.setSecurityManager(new RMISecurityManager());
        } catch (Exception e) {
        }
    }
    private Remote encodeObject(Object obj, Name name)
            throws NamingException, RemoteException
    {
        obj = NamingManager.getStateToBind(obj, name, this, environment);
        if (obj instanceof Remote) {
            return (Remote)obj;
        }
        if (obj instanceof Reference) {
            return (new ReferenceWrapper((Reference)obj));
        }
        if (obj instanceof Referenceable) {
            return (new ReferenceWrapper(((Referenceable)obj).getReference()));
        }
        throw (new IllegalArgumentException(
                "RegistryContext: " +
                "object to bind must be Remote, Reference, or Referenceable"));
    }
    private Object decodeObject(Remote r, Name name) throws NamingException {
        try {
            Object obj = (r instanceof RemoteReference)
                        ? ((RemoteReference)r).getReference()
                        : (Object)r;
            return NamingManager.getObjectInstance(obj, name, this,
                                                   environment);
        } catch (NamingException e) {
            throw e;
        } catch (RemoteException e) {
            throw (NamingException)
                wrapRemoteException(e).fillInStackTrace();
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }
}
class AtomicNameParser implements NameParser {
    private static final Properties syntax = new Properties();
    public Name parse(String name) throws NamingException {
        return (new CompoundName(name, syntax));
    }
}
class NameClassPairEnumeration implements NamingEnumeration {
    private final String[] names;
    private int nextName;       
    NameClassPairEnumeration(String[] names) {
        this.names = names;
        nextName = 0;
    }
    public boolean hasMore() {
        return (nextName < names.length);
    }
    public Object next() throws NamingException {
        if (!hasMore()) {
            throw (new java.util.NoSuchElementException());
        }
        String name = names[nextName++];
        Name cname = (new CompositeName()).add(name);
        NameClassPair ncp = new NameClassPair(cname.toString(),
                                            "java.lang.Object");
        ncp.setNameInNamespace(name);
        return ncp;
    }
    public boolean hasMoreElements() {
        return hasMore();
    }
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {   
            throw (new java.util.NoSuchElementException(
                    "javax.naming.NamingException was thrown"));
        }
    }
    public void close() {
        nextName = names.length;
    }
}
class BindingEnumeration implements NamingEnumeration {
    private RegistryContext ctx;
    private final String[] names;
    private int nextName;       
    BindingEnumeration(RegistryContext ctx, String[] names) {
        this.ctx = new RegistryContext(ctx);
        this.names = names;
        nextName = 0;
    }
    protected void finalize() {
        ctx.close();
    }
    public boolean hasMore() {
        if (nextName >= names.length) {
            ctx.close();
        }
        return (nextName < names.length);
    }
    public Object next() throws NamingException {
        if (!hasMore()) {
            throw (new java.util.NoSuchElementException());
        }
        String name = names[nextName++];
        Name cname = (new CompositeName()).add(name);
        Object obj = ctx.lookup(cname);
        String cnameStr = cname.toString();
        Binding binding = new Binding(cnameStr, obj);
        binding.setNameInNamespace(cnameStr);
        return binding;
    }
    public boolean hasMoreElements() {
        return hasMore();
    }
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw (new java.util.NoSuchElementException(
                    "javax.naming.NamingException was thrown"));
        }
    }
    public void close () {
        finalize();
    }
}
