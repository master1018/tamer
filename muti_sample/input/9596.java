public final class LocateRegistry {
    private LocateRegistry() {}
    public static Registry getRegistry()
        throws RemoteException
    {
        return getRegistry(null, Registry.REGISTRY_PORT);
    }
    public static Registry getRegistry(int port)
        throws RemoteException
    {
        return getRegistry(null, port);
    }
    public static Registry getRegistry(String host)
        throws RemoteException
    {
        return getRegistry(host, Registry.REGISTRY_PORT);
    }
    public static Registry getRegistry(String host, int port)
        throws RemoteException
    {
        return getRegistry(host, port, null);
    }
    public static Registry getRegistry(String host, int port,
                                       RMIClientSocketFactory csf)
        throws RemoteException
    {
        Registry registry = null;
        if (port <= 0)
            port = Registry.REGISTRY_PORT;
        if (host == null || host.length() == 0) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                host = "";
            }
        }
        LiveRef liveRef =
            new LiveRef(new ObjID(ObjID.REGISTRY_ID),
                        new TCPEndpoint(host, port, csf, null),
                        false);
        RemoteRef ref =
            (csf == null) ? new UnicastRef(liveRef) : new UnicastRef2(liveRef);
        return (Registry) Util.createProxy(RegistryImpl.class, ref, false);
    }
    public static Registry createRegistry(int port) throws RemoteException {
        return new RegistryImpl(port);
    }
    public static Registry createRegistry(int port,
                                          RMIClientSocketFactory csf,
                                          RMIServerSocketFactory ssf)
        throws RemoteException
    {
        return new RegistryImpl(port, csf, ssf);
    }
}
