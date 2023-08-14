public class CorbaInboundConnectionCacheImpl
    extends
        CorbaConnectionCacheBase
    implements
        InboundConnectionCache
{
    protected Collection connectionCache;
    public CorbaInboundConnectionCacheImpl(ORB orb, Acceptor acceptor)
    {
        super(orb, acceptor.getConnectionCacheType(),
              ((CorbaAcceptor)acceptor).getMonitoringName());
        this.connectionCache = new ArrayList();
    }
    public Connection get(Acceptor acceptor)
    {
        throw wrapper.methodShouldNotBeCalled();
    }
    public void put(Acceptor acceptor, Connection connection)
    {
        if (orb.transportDebugFlag) {
            dprint(".put: " + acceptor + " " + connection);
        }
        synchronized (backingStore()) {
            connectionCache.add(connection);
            connection.setConnectionCache(this);
            dprintStatistics();
        }
    }
    public void remove(Connection connection)
    {
        if (orb.transportDebugFlag) {
            dprint(".remove: " +  connection);
        }
        synchronized (backingStore()) {
            connectionCache.remove(connection);
            dprintStatistics();
        }
    }
    public Collection values()
    {
        return connectionCache;
    }
    protected Object backingStore()
    {
        return connectionCache;
    }
    protected void registerWithMonitoring()
    {
        MonitoredObject orbMO =
            orb.getMonitoringManager().getRootMonitoredObject();
        MonitoredObject connectionMO =
            orbMO.getChild(MonitoringConstants.CONNECTION_MONITORING_ROOT);
        if (connectionMO == null) {
            connectionMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        MonitoringConstants.CONNECTION_MONITORING_ROOT,
                        MonitoringConstants.CONNECTION_MONITORING_ROOT_DESCRIPTION);
            orbMO.addChild(connectionMO);
        }
        MonitoredObject inboundConnectionMO =
            connectionMO.getChild(
                MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT);
        if (inboundConnectionMO == null) {
            inboundConnectionMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT,
                        MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION);
            connectionMO.addChild(inboundConnectionMO);
        }
        MonitoredObject thisMO =
            inboundConnectionMO.getChild(getMonitoringName());
        if (thisMO == null) {
            thisMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        getMonitoringName(),
                        MonitoringConstants.CONNECTION_MONITORING_DESCRIPTION);
            inboundConnectionMO.addChild(thisMO);
        }
        LongMonitoredAttributeBase attribute;
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS,
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfConnections());
                }
            };
        thisMO.addAttribute(attribute);
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfIdleConnections());
                }
            };
        thisMO.addAttribute(attribute);
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfBusyConnections());
                }
            };
        thisMO.addAttribute(attribute);
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaInboundConnectionCacheImpl", msg);
    }
}
