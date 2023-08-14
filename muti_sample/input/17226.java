public class JVM_MANAGEMENT_MIB_IMPL extends JVM_MANAGEMENT_MIB {
    private static final long serialVersionUID = -8104825586888859831L;
    private static final MibLogger log =
        new MibLogger(JVM_MANAGEMENT_MIB_IMPL.class);
    private static WeakReference<SnmpOidTable> tableRef;
    public static SnmpOidTable getOidTable() {
        SnmpOidTable table = null;
        if(tableRef == null) {
            table =  new JVM_MANAGEMENT_MIBOidTable();
            tableRef = new WeakReference<SnmpOidTable>(table);
            return table;
        }
        table = tableRef.get();
        if(table == null) {
            table = new JVM_MANAGEMENT_MIBOidTable();
            tableRef = new WeakReference<SnmpOidTable>(table);
        }
        return table;
    }
    private class NotificationHandler implements NotificationListener {
        public void handleNotification(Notification notification,
                                       Object handback) {
            log.debug("handleNotification", "Received notification [ " +
                      notification.getType() + "]");
            String type = notification.getType();
            if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED) ||
                type.equals(MemoryNotificationInfo.
                    MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
                MemoryNotificationInfo minfo = MemoryNotificationInfo.
                    from((CompositeData) notification.getUserData());
                SnmpCounter64 count = new SnmpCounter64(minfo.getCount());
                SnmpCounter64 used =
                    new SnmpCounter64(minfo.getUsage().getUsed());
                SnmpString poolName = new SnmpString(minfo.getPoolName());
                SnmpOid entryIndex =
                    getJvmMemPoolEntryIndex(minfo.getPoolName());
                if (entryIndex == null) {
                    log.error("handleNotification",
                              "Error: Can't find entry index for Memory Pool: "
                              + minfo.getPoolName() +": " +
                              "No trap emitted for " + type);
                    return;
                }
                SnmpOid trap = null;
                final SnmpOidTable mibTable = getOidTable();
                try {
                    SnmpOid usedOid  = null;
                    SnmpOid countOid = null;
                    if (type.equals(MemoryNotificationInfo.
                                   MEMORY_THRESHOLD_EXCEEDED)) {
                        trap = new SnmpOid(mibTable.
                        resolveVarName("jvmLowMemoryPoolUsageNotif").getOid());
                        usedOid =
                            new SnmpOid(mibTable.
                            resolveVarName("jvmMemPoolUsed").getOid() +
                                    "." + entryIndex);
                        countOid =
                            new SnmpOid(mibTable.
                            resolveVarName("jvmMemPoolThreshdCount").getOid()
                                    + "." + entryIndex);
                    } else if  (type.equals(MemoryNotificationInfo.
                                   MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
                        trap = new SnmpOid(mibTable.
                        resolveVarName("jvmLowMemoryPoolCollectNotif").
                                           getOid());
                        usedOid =
                            new SnmpOid(mibTable.
                            resolveVarName("jvmMemPoolCollectUsed").getOid() +
                                        "." + entryIndex);
                        countOid =
                            new SnmpOid(mibTable.
                            resolveVarName("jvmMemPoolCollectThreshdCount").
                                        getOid() +
                                        "." + entryIndex);
                    }
                    SnmpVarBindList list = new SnmpVarBindList();
                    SnmpOid poolNameOid =
                        new SnmpOid(mibTable.
                                    resolveVarName("jvmMemPoolName").getOid() +
                                    "." + entryIndex);
                    SnmpVarBind varCount = new SnmpVarBind(countOid, count);
                    SnmpVarBind varUsed = new SnmpVarBind(usedOid, used);
                    SnmpVarBind varPoolName = new SnmpVarBind(poolNameOid,
                                              poolName);
                    list.add(varPoolName);
                    list.add(varCount);
                    list.add(varUsed);
                    sendTrap(trap, list);
                }catch(Exception e) {
                    log.error("handleNotification",
                              "Exception occured : " + e);
                }
            }
        }
    }
    private ArrayList<NotificationTarget> notificationTargets =
            new ArrayList<NotificationTarget>();
    private final NotificationEmitter emitter;
    private final NotificationHandler handler;
    public JVM_MANAGEMENT_MIB_IMPL() {
        handler = new NotificationHandler();
        emitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        emitter.addNotificationListener(handler, null, null);
    }
    private synchronized void sendTrap(SnmpOid trap, SnmpVarBindList list) {
        final Iterator iterator = notificationTargets.iterator();
        final SnmpAdaptorServer adaptor =
            (SnmpAdaptorServer) getSnmpAdaptor();
        if (adaptor == null) {
            log.error("sendTrap", "Cannot send trap: adaptor is null.");
            return;
        }
        if (!adaptor.isActive()) {
            log.config("sendTrap", "Adaptor is not active: trap not sent.");
            return;
        }
        while(iterator.hasNext()) {
            NotificationTarget target = null;
            try {
                target = (NotificationTarget) iterator.next();
                SnmpPeer peer =
                    new SnmpPeer(target.getAddress(), target.getPort());
                SnmpParameters p = new SnmpParameters();
                p.setRdCommunity(target.getCommunity());
                peer.setParams(p);
                log.debug("handleNotification", "Sending trap to " +
                          target.getAddress() + ":" + target.getPort());
                adaptor.snmpV2Trap(peer, trap, list, null);
            }catch(Exception e) {
                log.error("sendTrap",
                          "Exception occured while sending trap to [" +
                          target + "]. Exception : " + e);
                log.debug("sendTrap",e);
            }
        }
    }
    public synchronized void addTarget(NotificationTarget target)
        throws IllegalArgumentException {
        if(target == null)
            throw new IllegalArgumentException("Target is null");
        notificationTargets.add(target);
    }
    public void terminate() {
        try {
            emitter.removeNotificationListener(handler);
        }catch(ListenerNotFoundException e) {
            log.error("terminate", "Listener Not found : " + e);
        }
    }
    public synchronized void addTargets(List<NotificationTarget> targets)
        throws IllegalArgumentException {
        if(targets == null)
            throw new IllegalArgumentException("Target list is null");
        notificationTargets.addAll(targets);
    }
    protected Object createJvmMemoryMBean(String groupName,
                String groupOid,  ObjectName groupObjname,
                                          MBeanServer server)  {
        if (server != null)
            return new JvmMemoryImpl(this,server);
        else
            return new JvmMemoryImpl(this);
    }
    protected JvmMemoryMeta createJvmMemoryMetaNode(String groupName,
                                                    String groupOid,
                                                    ObjectName groupObjname,
                                                    MBeanServer server) {
        return new JvmMemoryMetaImpl(this, objectserver);
    }
    protected JvmThreadingMeta createJvmThreadingMetaNode(String groupName,
                                                          String groupOid,
                                                          ObjectName groupObjname,
                                                          MBeanServer server)  {
        return new JvmThreadingMetaImpl(this, objectserver);
    }
    protected Object createJvmThreadingMBean(String groupName,
                                             String groupOid,
                                             ObjectName groupObjname,
                                             MBeanServer server)  {
        if (server != null)
            return new JvmThreadingImpl(this,server);
        else
            return new JvmThreadingImpl(this);
    }
    protected JvmRuntimeMeta createJvmRuntimeMetaNode(String groupName,
                                                      String groupOid,
                                                      ObjectName groupObjname,
                                                      MBeanServer server)  {
        return new JvmRuntimeMetaImpl(this, objectserver);
    }
    protected Object createJvmRuntimeMBean(String groupName,
                                           String groupOid,
                                           ObjectName groupObjname,
                                           MBeanServer server)  {
        if (server != null)
            return new JvmRuntimeImpl(this,server);
        else
            return new JvmRuntimeImpl(this);
    }
    protected JvmCompilationMeta
        createJvmCompilationMetaNode(String groupName,
                                     String groupOid,
                                     ObjectName groupObjname,
                                     MBeanServer server)  {
        if (ManagementFactory.getCompilationMXBean() == null) return null;
        return super.createJvmCompilationMetaNode(groupName,groupOid,
                                                  groupObjname,server);
    }
    protected Object createJvmCompilationMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server)  {
        if (server != null)
            return new JvmCompilationImpl(this,server);
        else
            return new JvmCompilationImpl(this);
    }
    protected Object createJvmOSMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server)  {
        if (server != null)
            return new JvmOSImpl(this,server);
        else
            return new JvmOSImpl(this);
    }
    protected Object createJvmClassLoadingMBean(String groupName,
                                                String groupOid,
                                                ObjectName groupObjname,
                                                MBeanServer server)  {
        if (server != null)
            return new JvmClassLoadingImpl(this,server);
        else
            return new JvmClassLoadingImpl(this);
    }
    static String validDisplayStringTC(String str) {
        if(str == null) return "";
        if(str.length() > DISPLAY_STRING_MAX_LENGTH) {
            return str.substring(0, DISPLAY_STRING_MAX_LENGTH);
        }
        else
            return str;
    }
    static String validJavaObjectNameTC(String str) {
        if(str == null) return "";
        if(str.length() > JAVA_OBJECT_NAME_MAX_LENGTH) {
            return str.substring(0, JAVA_OBJECT_NAME_MAX_LENGTH);
        }
        else
            return str;
    }
    static String validPathElementTC(String str) {
        if(str == null) return "";
        if(str.length() > PATH_ELEMENT_MAX_LENGTH) {
            return str.substring(0, PATH_ELEMENT_MAX_LENGTH);
        }
        else
            return str;
    }
    static String validArgValueTC(String str) {
        if(str == null) return "";
        if(str.length() > ARG_VALUE_MAX_LENGTH) {
            return str.substring(0, ARG_VALUE_MAX_LENGTH);
        }
        else
            return str;
    }
    private SnmpTableHandler getJvmMemPoolTableHandler(Object userData) {
        final SnmpMibTable meta =
            getRegisteredTableMeta("JvmMemPoolTable");
        if (! (meta instanceof JvmMemPoolTableMetaImpl)) {
            final String err = ((meta==null)?"No metadata for JvmMemPoolTable":
                                "Bad metadata class for JvmMemPoolTable: " +
                                meta.getClass().getName());
            log.error("getJvmMemPoolTableHandler", err);
            return null;
        }
        final JvmMemPoolTableMetaImpl memPoolTable =
            (JvmMemPoolTableMetaImpl) meta;
        return memPoolTable.getHandler(userData);
    }
    private int findInCache(SnmpTableHandler handler,
                            String poolName) {
        if (!(handler instanceof SnmpCachedData)) {
            if (handler != null) {
                final String err = "Bad class for JvmMemPoolTable datas: " +
                    handler.getClass().getName();
                log.error("getJvmMemPoolEntry", err);
            }
            return -1;
        }
        final SnmpCachedData data = (SnmpCachedData)handler;
        final int len = data.datas.length;
        for (int i=0; i < data.datas.length ; i++) {
            final MemoryPoolMXBean pool = (MemoryPoolMXBean) data.datas[i];
            if (poolName.equals(pool.getName())) return i;
        }
        return -1;
    }
    private SnmpOid getJvmMemPoolEntryIndex(SnmpTableHandler handler,
                                            String poolName) {
        final int index = findInCache(handler,poolName);
        if (index < 0) return null;
        return ((SnmpCachedData)handler).indexes[index];
    }
    private SnmpOid getJvmMemPoolEntryIndex(String poolName) {
        return getJvmMemPoolEntryIndex(getJvmMemPoolTableHandler(null),
                                       poolName);
    }
    public long validity() {
        return DEFAULT_CACHE_VALIDITY_PERIOD;
    }
    private final static int DISPLAY_STRING_MAX_LENGTH=255;
    private final static int JAVA_OBJECT_NAME_MAX_LENGTH=1023;
    private final static int PATH_ELEMENT_MAX_LENGTH=1023;
    private final static int ARG_VALUE_MAX_LENGTH=1023;
    private final static int DEFAULT_CACHE_VALIDITY_PERIOD=1000;
}
