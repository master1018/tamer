public abstract class CorbaConnectionCacheBase
    implements
        ConnectionCache,
        CorbaConnectionCache
{
    protected ORB orb;
    protected long timestamp = 0;
    protected String cacheType;
    protected String monitoringName;
    protected ORBUtilSystemException wrapper;
    protected CorbaConnectionCacheBase(ORB orb, String cacheType,
                                       String monitoringName)
    {
        this.orb = orb;
        this.cacheType = cacheType;
        this.monitoringName = monitoringName;
        wrapper =ORBUtilSystemException.get(orb,CORBALogDomains.RPC_TRANSPORT);
        registerWithMonitoring();
        dprintCreation();
    }
    public String getCacheType()
    {
        return cacheType;
    }
    public synchronized void stampTime(Connection c)
    {
        c.setTimeStamp(timestamp++);
    }
    public long numberOfConnections()
    {
        synchronized (backingStore()) {
            return values().size();
        }
    }
    public void close() {
        synchronized (backingStore()) {
            for (Object obj : values()) {
                ((CorbaConnection)obj).closeConnectionResources() ;
            }
        }
    }
    public long numberOfIdleConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (! ((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }
        return count;
    }
    public long numberOfBusyConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }
        return count;
    }
    synchronized public boolean reclaim()
    {
        try {
            long numberOfConnections = numberOfConnections();
            if (orb.transportDebugFlag) {
                dprint(".reclaim->: " + numberOfConnections
                        + " ("
                        + orb.getORBData().getHighWaterMark()
                        + "/"
                        + orb.getORBData().getLowWaterMark()
                        + "/"
                        + orb.getORBData().getNumberToReclaim()
                        + ")");
            }
            if (numberOfConnections <= orb.getORBData().getHighWaterMark() ||
                numberOfConnections < orb.getORBData().getLowWaterMark()) {
                return false;
            }
            Object backingStore = backingStore();
            synchronized (backingStore) {
                for (int i=0; i < orb.getORBData().getNumberToReclaim(); i++) {
                    Connection toClose = null;
                    long lru = java.lang.Long.MAX_VALUE;
                    Iterator iterator = values().iterator();
                    while ( iterator.hasNext() ) {
                        Connection c = (Connection) iterator.next();
                        if ( !c.isBusy() && c.getTimeStamp() < lru ) {
                            toClose = c;
                            lru = c.getTimeStamp();
                        }
                    }
                    if ( toClose == null ) {
                        return false;
                    }
                    try {
                        if (orb.transportDebugFlag) {
                            dprint(".reclaim: closing: " + toClose);
                        }
                        toClose.close();
                    } catch (Exception ex) {
                    }
                }
                if (orb.transportDebugFlag) {
                    dprint(".reclaim: connections reclaimed ("
                            + (numberOfConnections - numberOfConnections()) + ")");
                }
            }
            return true;
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".reclaim<-: " + numberOfConnections());
            }
        }
    }
    public String getMonitoringName()
    {
        return monitoringName;
    }
    public abstract Collection values();
    protected abstract Object backingStore();
    protected abstract void registerWithMonitoring();
    protected void dprintCreation()
    {
        if (orb.transportDebugFlag) {
            dprint(".constructor: cacheType: " + getCacheType()
                   + " monitoringName: " + getMonitoringName());
        }
    }
    protected void dprintStatistics()
    {
        if (orb.transportDebugFlag) {
            dprint(".stats: "
                   + numberOfConnections() + "/total "
                   + numberOfBusyConnections() + "/busy "
                   + numberOfIdleConnections() + "/idle"
                   + " ("
                   + orb.getORBData().getHighWaterMark() + "/"
                   + orb.getORBData().getLowWaterMark() + "/"
                   + orb.getORBData().getNumberToReclaim()
                   + ")");
        }
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaConnectionCacheBase", msg);
    }
}
