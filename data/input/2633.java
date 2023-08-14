public class CorbaOutboundConnectionCacheImpl
    extends
        CorbaConnectionCacheBase
    implements
        OutboundConnectionCache
{
    protected Hashtable connectionCache;
    public CorbaOutboundConnectionCacheImpl(ORB orb, ContactInfo contactInfo)
    {
        super(orb, contactInfo.getConnectionCacheType(),
              ((CorbaContactInfo)contactInfo).getMonitoringName());
        this.connectionCache = new Hashtable();
    }
    public Connection get(ContactInfo contactInfo)
    {
        if (orb.transportDebugFlag) {
            dprint(".get: " + contactInfo + " " + contactInfo.hashCode());
        }
        synchronized (backingStore()) {
            dprintStatistics();
            return (Connection) connectionCache.get(contactInfo);
        }
    }
    public void put(ContactInfo contactInfo, Connection connection)
    {
        if (orb.transportDebugFlag) {
            dprint(".put: " + contactInfo + " " + contactInfo.hashCode() + " "
                   + connection);
        }
        synchronized (backingStore()) {
            connectionCache.put(contactInfo, connection);
            connection.setConnectionCache(this);
            dprintStatistics();
        }
    }
    public void remove(ContactInfo contactInfo)
    {
        if (orb.transportDebugFlag) {
            dprint(".remove: " + contactInfo + " " + contactInfo.hashCode());
        }
        synchronized (backingStore()) {
            if (contactInfo != null) {
                connectionCache.remove(contactInfo);
            }
            dprintStatistics();
        }
    }
    public Collection values()
    {
        return connectionCache.values();
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
        MonitoredObject outboundConnectionMO =
            connectionMO.getChild(
                MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT);
        if (outboundConnectionMO == null) {
            outboundConnectionMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT,
                        MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION);
            connectionMO.addChild(outboundConnectionMO);
        }
        MonitoredObject thisMO =
            outboundConnectionMO.getChild(getMonitoringName());
        if (thisMO == null) {
            thisMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        getMonitoringName(),
                        MonitoringConstants.CONNECTION_MONITORING_DESCRIPTION);
            outboundConnectionMO.addChild(thisMO);
        }
        LongMonitoredAttributeBase attribute;
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS,
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaOutboundConnectionCacheImpl.this.numberOfConnections());
                }
            };
        thisMO.addAttribute(attribute);
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaOutboundConnectionCacheImpl.this.numberOfIdleConnections());
                }
            };
        thisMO.addAttribute(attribute);
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaOutboundConnectionCacheImpl.this.numberOfBusyConnections());
                }
            };
        thisMO.addAttribute(attribute);
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaOutboundConnectionCacheImpl", msg);
    }
}
