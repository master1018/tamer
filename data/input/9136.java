public class LegacyServerSocketManagerImpl
    implements
        LegacyServerSocketManager
{
    protected ORB orb;
    private ORBUtilSystemException wrapper ;
    public LegacyServerSocketManagerImpl(ORB orb)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_TRANSPORT ) ;
    }
    public int legacyGetTransientServerPort(String type)
    {
        return legacyGetServerPort(type, false);
    }
    public synchronized int legacyGetPersistentServerPort(String socketType)
    {
        if (orb.getORBData().getServerIsORBActivated()) {
            return legacyGetServerPort(socketType, true);
        } else if (orb.getORBData().getPersistentPortInitialized()) {
            return orb.getORBData().getPersistentServerPort();
        } else {
            throw wrapper.persistentServerportNotSet(
                CompletionStatus.COMPLETED_MAYBE);
        }
    }
    public synchronized int legacyGetTransientOrPersistentServerPort(
        String socketType)
    {
            return legacyGetServerPort(socketType,
                                       orb.getORBData()
                                       .getServerIsORBActivated());
    }
    public synchronized LegacyServerSocketEndPointInfo legacyGetEndpoint(
        String name)
    {
        Iterator iterator = getAcceptorIterator();
        while (iterator.hasNext()) {
            LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
            if (endPoint != null && name.equals(endPoint.getName())) {
                return endPoint;
            }
        }
        throw new INTERNAL("No acceptor for: " + name);
    }
    public boolean legacyIsLocalServerPort(int port)
    {
        Iterator iterator = getAcceptorIterator();
        while (iterator.hasNext()) {
            LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
            if (endPoint != null && endPoint.getPort() == port) {
                return true;
            }
        }
        return false;
    }
    private int legacyGetServerPort (String socketType, boolean isPersistent)
    {
        Iterator endpoints = getAcceptorIterator();
        while (endpoints.hasNext()) {
            LegacyServerSocketEndPointInfo ep = cast(endpoints.next());
            if (ep != null && ep.getType().equals(socketType)) {
                if (isPersistent) {
                    return ep.getLocatorPort();
                } else {
                    return ep.getPort();
                }
            }
        }
        return -1;
    }
    private Iterator getAcceptorIterator()
    {
        Collection acceptors =
            orb.getCorbaTransportManager().getAcceptors(null, null);
        if (acceptors != null) {
            return acceptors.iterator();
        }
        throw wrapper.getServerPortCalledBeforeEndpointsInitialized() ;
    }
    private LegacyServerSocketEndPointInfo cast(Object o)
    {
        if (o instanceof LegacyServerSocketEndPointInfo) {
            return (LegacyServerSocketEndPointInfo) o;
        }
        return null;
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("LegacyServerSocketManagerImpl", msg);
    }
}
