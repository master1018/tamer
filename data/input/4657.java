class GarbageCollectorImpl extends MemoryManagerImpl
    implements GarbageCollectorMXBean {
    GarbageCollectorImpl(String name) {
        super(name);
    }
    public native long getCollectionCount();
    public native long getCollectionTime();
    private String[] poolNames = null;
    synchronized String[] getAllPoolNames() {
        if (poolNames == null) {
            List pools = ManagementFactory.getMemoryPoolMXBeans();
            poolNames = new String[pools.size()];
            int i = 0;
            for (ListIterator iter = pools.listIterator();
                 iter.hasNext();
                 i++) {
                MemoryPoolMXBean p = (MemoryPoolMXBean) iter.next();
                poolNames[i] = p.getName();
            }
        }
        return poolNames;
    }
    private GcInfoBuilder gcInfoBuilder;
    private synchronized GcInfoBuilder getGcInfoBuilder() {
        if(gcInfoBuilder == null) {
            gcInfoBuilder = new GcInfoBuilder(this, getAllPoolNames());
        }
        return gcInfoBuilder;
    }
    public GcInfo getLastGcInfo() {
        GcInfo info = getGcInfoBuilder().getLastGcInfo();
        return info;
    }
    private final static String notifName =
        "javax.management.Notification";
    private final static String[] gcNotifTypes = {
        GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION
    };
    private MBeanNotificationInfo[] notifInfo = null;
    public MBeanNotificationInfo[] getNotificationInfo() {
        synchronized (this) {
            if (notifInfo == null) {
                 notifInfo = new MBeanNotificationInfo[1];
                 notifInfo[0] = new MBeanNotificationInfo(gcNotifTypes,
                                                          notifName,
                                                          "GC Notification");
            }
        }
        return notifInfo;
    }
    private static long seqNumber = 0;
    private static long getNextSeqNumber() {
        return ++seqNumber;
    }
    void createGCNotification(long timestamp,
                              String gcName,
                              String gcAction,
                              String gcCause,
                              GcInfo gcInfo)  {
        if (!hasListeners()) {
            return;
        }
        Notification notif = new Notification(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION,
                                              getObjectName(),
                                              getNextSeqNumber(),
                                              timestamp,
                                              gcName);
        GarbageCollectionNotificationInfo info =
            new GarbageCollectionNotificationInfo(gcName,
                                                  gcAction,
                                                  gcCause,
                                                  gcInfo);
        CompositeData cd =
            GarbageCollectionNotifInfoCompositeData.toCompositeData(info);
        notif.setUserData(cd);
        sendNotification(notif);
    }
    public synchronized void addNotificationListener(NotificationListener listener,
                                                     NotificationFilter filter,
                                                     Object handback)
    {
        boolean before = hasListeners();
        super.addNotificationListener(listener, filter, handback);
        boolean after = hasListeners();
        if (!before && after) {
            setNotificationEnabled(this, true);
        }
    }
    public synchronized void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        boolean before = hasListeners();
        super.removeNotificationListener(listener);
        boolean after = hasListeners();
        if (before && !after) {
            setNotificationEnabled(this,false);
        }
    }
    public synchronized void removeNotificationListener(NotificationListener listener,
                                                        NotificationFilter filter,
                                                        Object handback)
            throws ListenerNotFoundException
    {
        boolean before = hasListeners();
        super.removeNotificationListener(listener,filter,handback);
        boolean after = hasListeners();
        if (before && !after) {
            setNotificationEnabled(this,false);
        }
    }
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, getName());
    }
    native void setNotificationEnabled(GarbageCollectorMXBean gc,
                                       boolean enabled);
}
