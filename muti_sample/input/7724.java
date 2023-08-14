public final class IIOPHelper {
    private IIOPHelper() { }
    private static final String IMPL_CLASS =
        "com.sun.jmx.remote.protocol.iiop.IIOPProxyImpl";
    private static final IIOPProxy proxy =
        AccessController.doPrivileged(new PrivilegedAction<IIOPProxy>() {
            public IIOPProxy run() {
                try {
                    Class<?> c = Class.forName(IMPL_CLASS, true, null);
                    return (IIOPProxy)c.newInstance();
                } catch (ClassNotFoundException cnf) {
                    return null;
                } catch (InstantiationException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }});
    public static boolean isAvailable() {
        return proxy != null;
    }
    private static void ensureAvailable() {
        if (proxy == null)
            throw new AssertionError("Should not here");
    }
    public static boolean isStub(Object obj) {
        return (proxy == null) ? false : proxy.isStub(obj);
    }
    public static Object getDelegate(Object stub) {
        ensureAvailable();
        return proxy.getDelegate(stub);
    }
    public static void setDelegate(Object stub, Object delegate) {
        ensureAvailable();
        proxy.setDelegate(stub, delegate);
    }
    public static Object getOrb(Object stub) {
        ensureAvailable();
        return proxy.getOrb(stub);
    }
    public static void connect(Object stub, Object orb)
        throws RemoteException
    {
        ensureAvailable();
        proxy.connect(stub, orb);
    }
    public static boolean isOrb(Object obj) {
        ensureAvailable();
        return proxy.isOrb(obj);
    }
    public static Object createOrb(String[] args, Properties props) {
        ensureAvailable();
        return proxy.createOrb(args, props);
    }
    public static Object stringToObject(Object orb, String str) {
        ensureAvailable();
        return proxy.stringToObject(orb, str);
    }
    public static String objectToString(Object orb, Object obj) {
        ensureAvailable();
        return proxy.objectToString(orb, obj);
    }
    public static <T> T narrow(Object narrowFrom, Class<T> narrowTo) {
        ensureAvailable();
        return proxy.narrow(narrowFrom, narrowTo);
    }
    public static void exportObject(Remote obj) throws RemoteException {
        ensureAvailable();
        proxy.exportObject(obj);
    }
    public static void unexportObject(Remote obj) throws NoSuchObjectException {
        ensureAvailable();
        proxy.unexportObject(obj);
    }
    public static Remote toStub(Remote obj) throws NoSuchObjectException {
        ensureAvailable();
        return proxy.toStub(obj);
    }
}
