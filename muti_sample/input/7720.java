public class RegistryImpl extends java.rmi.server.RemoteServer
        implements Registry
{
    private static final long serialVersionUID = 4666870661827494597L;
    private Hashtable<String, Remote> bindings
        = new Hashtable<String, Remote>(101);
    private static Hashtable<InetAddress, InetAddress> allowedAccessCache
        = new Hashtable<InetAddress, InetAddress>(3);
    private static RegistryImpl registry;
    private static ObjID id = new ObjID(ObjID.REGISTRY_ID);
    private static ResourceBundle resources = null;
    public RegistryImpl(int port,
                        RMIClientSocketFactory csf,
                        RMIServerSocketFactory ssf)
        throws RemoteException
    {
        LiveRef lref = new LiveRef(id, port, csf, ssf);
        setup(new UnicastServerRef2(lref));
    }
    public RegistryImpl(int port)
        throws RemoteException
    {
        LiveRef lref = new LiveRef(id, port);
        setup(new UnicastServerRef(lref));
    }
    private void setup(UnicastServerRef uref)
        throws RemoteException
    {
        ref = uref;
        uref.exportObject(this, null, true);
    }
    public Remote lookup(String name)
        throws RemoteException, NotBoundException
    {
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null)
                throw new NotBoundException(name);
            return obj;
        }
    }
    public void bind(String name, Remote obj)
        throws RemoteException, AlreadyBoundException, AccessException
    {
        checkAccess("Registry.bind");
        synchronized (bindings) {
            Remote curr = bindings.get(name);
            if (curr != null)
                throw new AlreadyBoundException(name);
            bindings.put(name, obj);
        }
    }
    public void unbind(String name)
        throws RemoteException, NotBoundException, AccessException
    {
        checkAccess("Registry.unbind");
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null)
                throw new NotBoundException(name);
            bindings.remove(name);
        }
    }
    public void rebind(String name, Remote obj)
        throws RemoteException, AccessException
    {
        checkAccess("Registry.rebind");
        bindings.put(name, obj);
    }
    public String[] list()
        throws RemoteException
    {
        String[] names;
        synchronized (bindings) {
            int i = bindings.size();
            names = new String[i];
            Enumeration enum_ = bindings.keys();
            while ((--i) >= 0)
                names[i] = (String)enum_.nextElement();
        }
        return names;
    }
    public static void checkAccess(String op) throws AccessException {
        try {
            final String clientHostName = getClientHost();
            InetAddress clientHost;
            try {
                clientHost = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<InetAddress>() {
                        public InetAddress run()
                            throws java.net.UnknownHostException
                        {
                            return InetAddress.getByName(clientHostName);
                        }
                    });
            } catch (PrivilegedActionException pae) {
                throw (java.net.UnknownHostException) pae.getException();
            }
            if (allowedAccessCache.get(clientHost) == null) {
                if (clientHost.isAnyLocalAddress()) {
                    throw new AccessException(
                        "Registry." + op + " disallowed; origin unknown");
                }
                try {
                    final InetAddress finalClientHost = clientHost;
                    java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedExceptionAction<Void>() {
                            public Void run() throws java.io.IOException {
                                (new ServerSocket(0, 10, finalClientHost)).close();
                                allowedAccessCache.put(finalClientHost,
                                                       finalClientHost);
                                return null;
                            }
                    });
                } catch (PrivilegedActionException pae) {
                    throw new AccessException(
                        "Registry." + op + " disallowed; origin " +
                        clientHost + " is non-local host");
                }
            }
        } catch (ServerNotActiveException ex) {
        } catch (java.net.UnknownHostException ex) {
            throw new AccessException("Registry." + op +
                                      " disallowed; origin is unknown host");
        }
    }
    public static ObjID getID() {
        return id;
    }
    private static String getTextResource(String key) {
        if (resources == null) {
            try {
                resources = ResourceBundle.getBundle(
                    "sun.rmi.registry.resources.rmiregistry");
            } catch (MissingResourceException mre) {
            }
            if (resources == null) {
                return ("[missing resource file: " + key + "]");
            }
        }
        String val = null;
        try {
            val = resources.getString(key);
        } catch (MissingResourceException mre) {
        }
        if (val == null) {
            return ("[missing resource: " + key + "]");
        } else {
            return (val);
        }
    }
    public static void main(String args[])
    {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        try {
            String envcp = System.getProperty("env.class.path");
            if (envcp == null) {
                envcp = ".";            
            }
            URL[] urls = sun.misc.URLClassPath.pathToURLs(envcp);
            ClassLoader cl = new URLClassLoader(urls);
            sun.rmi.server.LoaderHandler.registerCodebaseLoader(cl);
            Thread.currentThread().setContextClassLoader(cl);
            int regPort = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                regPort = Integer.parseInt(args[0]);
            }
            registry = new RegistryImpl(regPort);
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                }
            }
        } catch (NumberFormatException e) {
            System.err.println(MessageFormat.format(
                getTextResource("rmiregistry.port.badnumber"),
                args[0] ));
            System.err.println(MessageFormat.format(
                getTextResource("rmiregistry.usage"),
                "rmiregistry" ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
