class MemoryImpl extends NotificationEmitterSupport
                 implements MemoryMXBean {
    private final VMManagement jvm;
    private static MemoryPoolMXBean[] pools = null;
    private static MemoryManagerMXBean[] mgrs = null;
    MemoryImpl(VMManagement vm) {
        this.jvm = vm;
    }
    public int getObjectPendingFinalizationCount() {
        return sun.misc.VM.getFinalRefCount();
    }
    public void gc() {
        Runtime.getRuntime().gc();
    }
    public MemoryUsage getHeapMemoryUsage() {
        return getMemoryUsage0(true);
    }
    public MemoryUsage getNonHeapMemoryUsage() {
        return getMemoryUsage0(false);
    }
    public boolean isVerbose() {
        return jvm.getVerboseGC();
    }
    public void setVerbose(boolean value) {
        Util.checkControlAccess();
        setVerboseGC(value);
    }
    static synchronized MemoryPoolMXBean[] getMemoryPools() {
        if (pools == null) {
            pools = getMemoryPools0();
        }
        return pools;
    }
    static synchronized MemoryManagerMXBean[] getMemoryManagers() {
        if (mgrs == null) {
            mgrs = getMemoryManagers0();
        }
        return mgrs;
    }
    private static native MemoryPoolMXBean[] getMemoryPools0();
    private static native MemoryManagerMXBean[] getMemoryManagers0();
    private native MemoryUsage getMemoryUsage0(boolean heap);
    private native void setVerboseGC(boolean value);
    private final static String notifName =
        "javax.management.Notification";
    private final static String[] notifTypes = {
        MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED,
        MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED
    };
    private final static String[] notifMsgs  = {
        "Memory usage exceeds usage threshold",
        "Memory usage exceeds collection usage threshold"
    };
    private MBeanNotificationInfo[] notifInfo = null;
    public MBeanNotificationInfo[] getNotificationInfo() {
        synchronized (this) {
            if (notifInfo == null) {
                 notifInfo = new MBeanNotificationInfo[1];
                 notifInfo[0] = new MBeanNotificationInfo(notifTypes,
                                                          notifName,
                                                          "Memory Notification");
            }
        }
        return notifInfo;
    }
    private static String getNotifMsg(String notifType) {
        for (int i = 0; i < notifTypes.length; i++) {
            if (notifType == notifTypes[i]) {
                return notifMsgs[i];
            }
        }
        return "Unknown message";
    }
    private static long seqNumber = 0;
    private static long getNextSeqNumber() {
        return ++seqNumber;
    }
    static void createNotification(String notifType,
                                   String poolName,
                                   MemoryUsage usage,
                                   long count) {
        MemoryImpl mbean = (MemoryImpl) ManagementFactory.getMemoryMXBean();
        if (!mbean.hasListeners()) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        String msg = getNotifMsg(notifType);
        Notification notif = new Notification(notifType,
                                              mbean.getObjectName(),
                                              getNextSeqNumber(),
                                              timestamp,
                                              msg);
        MemoryNotificationInfo info =
            new MemoryNotificationInfo(poolName,
                                       usage,
                                       count);
        CompositeData cd =
            MemoryNotifInfoCompositeData.toCompositeData(info);
        notif.setUserData(cd);
        mbean.sendNotification(notif);
    }
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
    }
}
