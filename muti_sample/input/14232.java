public class ThreadInfoCompositeData extends LazyCompositeData {
    private final ThreadInfo threadInfo;
    private final CompositeData cdata;
    private final boolean currentVersion;
    private ThreadInfoCompositeData(ThreadInfo ti) {
        this.threadInfo = ti;
        this.currentVersion = true;
        this.cdata = null;
    }
    private ThreadInfoCompositeData(CompositeData cd) {
        this.threadInfo = null;
        this.currentVersion = ThreadInfoCompositeData.isCurrentVersion(cd);
        this.cdata = cd;
    }
    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }
    public boolean isCurrentVersion() {
        return currentVersion;
    }
    public static ThreadInfoCompositeData getInstance(CompositeData cd) {
        validateCompositeData(cd);
        return new ThreadInfoCompositeData(cd);
    }
    public static CompositeData toCompositeData(ThreadInfo ti) {
        ThreadInfoCompositeData ticd = new ThreadInfoCompositeData(ti);
        return ticd.getCompositeData();
    }
    protected CompositeData getCompositeData() {
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        CompositeData[] stackTraceData =
            new CompositeData[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            stackTraceData[i] = StackTraceElementCompositeData.toCompositeData(ste);
        }
        LockDataConverter converter = new LockDataConverter(threadInfo);
        CompositeData lockInfoData = converter.toLockInfoCompositeData();
        CompositeData[] lockedSyncsData = converter.toLockedSynchronizersCompositeData();
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
        CompositeData[] lockedMonitorsData =
            new CompositeData[lockedMonitors.length];
        for (int i = 0; i < lockedMonitors.length; i++) {
            MonitorInfo mi = lockedMonitors[i];
            lockedMonitorsData[i] = MonitorInfoCompositeData.toCompositeData(mi);
        }
        final Object[] threadInfoItemValues = {
            new Long(threadInfo.getThreadId()),
            threadInfo.getThreadName(),
            threadInfo.getThreadState().name(),
            new Long(threadInfo.getBlockedTime()),
            new Long(threadInfo.getBlockedCount()),
            new Long(threadInfo.getWaitedTime()),
            new Long(threadInfo.getWaitedCount()),
            lockInfoData,
            threadInfo.getLockName(),
            new Long(threadInfo.getLockOwnerId()),
            threadInfo.getLockOwnerName(),
            stackTraceData,
            new Boolean(threadInfo.isSuspended()),
            new Boolean(threadInfo.isInNative()),
            lockedMonitorsData,
            lockedSyncsData,
        };
        try {
            return new CompositeDataSupport(threadInfoCompositeType,
                                            threadInfoItemNames,
                                            threadInfoItemValues);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    private static final String THREAD_ID       = "threadId";
    private static final String THREAD_NAME     = "threadName";
    private static final String THREAD_STATE    = "threadState";
    private static final String BLOCKED_TIME    = "blockedTime";
    private static final String BLOCKED_COUNT   = "blockedCount";
    private static final String WAITED_TIME     = "waitedTime";
    private static final String WAITED_COUNT    = "waitedCount";
    private static final String LOCK_INFO       = "lockInfo";
    private static final String LOCK_NAME       = "lockName";
    private static final String LOCK_OWNER_ID   = "lockOwnerId";
    private static final String LOCK_OWNER_NAME = "lockOwnerName";
    private static final String STACK_TRACE     = "stackTrace";
    private static final String SUSPENDED       = "suspended";
    private static final String IN_NATIVE       = "inNative";
    private static final String LOCKED_MONITORS = "lockedMonitors";
    private static final String LOCKED_SYNCS    = "lockedSynchronizers";
    private static final String[] threadInfoItemNames = {
        THREAD_ID,
        THREAD_NAME,
        THREAD_STATE,
        BLOCKED_TIME,
        BLOCKED_COUNT,
        WAITED_TIME,
        WAITED_COUNT,
        LOCK_INFO,
        LOCK_NAME,
        LOCK_OWNER_ID,
        LOCK_OWNER_NAME,
        STACK_TRACE,
        SUSPENDED,
        IN_NATIVE,
        LOCKED_MONITORS,
        LOCKED_SYNCS,
    };
    private static final String[] threadInfoV6Attributes = {
        LOCK_INFO,
        LOCKED_MONITORS,
        LOCKED_SYNCS,
    };
    private static final CompositeType threadInfoCompositeType;
    private static final CompositeType threadInfoV5CompositeType;
    private static final CompositeType lockInfoCompositeType;
    static {
        try {
            threadInfoCompositeType = (CompositeType)
                MappedMXBeanType.toOpenType(ThreadInfo.class);
            String[] itemNames =
                threadInfoCompositeType.keySet().toArray(new String[0]);
            int numV5Attributes = threadInfoItemNames.length -
                                      threadInfoV6Attributes.length;
            String[] v5ItemNames = new String[numV5Attributes];
            String[] v5ItemDescs = new String[numV5Attributes];
            OpenType[] v5ItemTypes = new OpenType[numV5Attributes];
            int i = 0;
            for (String n : itemNames) {
                if (isV5Attribute(n)) {
                    v5ItemNames[i] = n;
                    v5ItemDescs[i] = threadInfoCompositeType.getDescription(n);
                    v5ItemTypes[i] = threadInfoCompositeType.getType(n);
                    i++;
                }
            }
            threadInfoV5CompositeType =
                new CompositeType("java.lang.management.ThreadInfo",
                                  "J2SE 5.0 java.lang.management.ThreadInfo",
                                  v5ItemNames,
                                  v5ItemDescs,
                                  v5ItemTypes);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
        Object o = new Object();
        LockInfo li = new LockInfo(o.getClass().getName(),
                                   System.identityHashCode(o));
        CompositeData cd = LockDataConverter.toLockInfoCompositeData(li);
        lockInfoCompositeType = cd.getCompositeType();
    }
    private static boolean isV5Attribute(String itemName) {
        for (String n : threadInfoV6Attributes) {
            if (itemName.equals(n)) {
                return false;
            }
        }
        return true;
    }
    public static boolean isCurrentVersion(CompositeData cd) {
        if (cd == null) {
            throw new NullPointerException("Null CompositeData");
        }
        return isTypeMatched(threadInfoCompositeType, cd.getCompositeType());
    }
    public long threadId() {
        return getLong(cdata, THREAD_ID);
    }
    public String threadName() {
        String name = getString(cdata, THREAD_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Invalid composite data: " +
                "Attribute " + THREAD_NAME + " has null value");
        }
        return name;
    }
    public Thread.State threadState() {
        return Thread.State.valueOf(getString(cdata, THREAD_STATE));
    }
    public long blockedTime() {
        return getLong(cdata, BLOCKED_TIME);
    }
    public long blockedCount() {
        return getLong(cdata, BLOCKED_COUNT);
    }
    public long waitedTime() {
        return getLong(cdata, WAITED_TIME);
    }
    public long waitedCount() {
        return getLong(cdata, WAITED_COUNT);
    }
    public String lockName() {
        return getString(cdata, LOCK_NAME);
    }
    public long lockOwnerId() {
        return getLong(cdata, LOCK_OWNER_ID);
    }
    public String lockOwnerName() {
        return getString(cdata, LOCK_OWNER_NAME);
    }
    public boolean suspended() {
        return getBoolean(cdata, SUSPENDED);
    }
    public boolean inNative() {
        return getBoolean(cdata, IN_NATIVE);
    }
    public StackTraceElement[] stackTrace() {
        CompositeData[] stackTraceData =
            (CompositeData[]) cdata.get(STACK_TRACE);
        StackTraceElement[] stackTrace =
            new StackTraceElement[stackTraceData.length];
        for (int i = 0; i < stackTraceData.length; i++) {
            CompositeData cdi = stackTraceData[i];
            stackTrace[i] = StackTraceElementCompositeData.from(cdi);
        }
        return stackTrace;
    }
    public LockInfo lockInfo() {
        LockDataConverter converter = new LockDataConverter();
        CompositeData lockInfoData = (CompositeData) cdata.get(LOCK_INFO);
        return converter.toLockInfo(lockInfoData);
    }
    public MonitorInfo[] lockedMonitors() {
        CompositeData[] lockedMonitorsData =
            (CompositeData[]) cdata.get(LOCKED_MONITORS);
        MonitorInfo[] monitors =
            new MonitorInfo[lockedMonitorsData.length];
        for (int i = 0; i < lockedMonitorsData.length; i++) {
            CompositeData cdi = lockedMonitorsData[i];
            monitors[i] = MonitorInfo.from(cdi);
        }
        return monitors;
    }
    public LockInfo[] lockedSynchronizers() {
        LockDataConverter converter = new LockDataConverter();
        CompositeData[] lockedSyncsData =
            (CompositeData[]) cdata.get(LOCKED_SYNCS);
        return converter.toLockedSynchronizers(lockedSyncsData);
    }
    public static void validateCompositeData(CompositeData cd) {
        if (cd == null) {
            throw new NullPointerException("Null CompositeData");
        }
        CompositeType type = cd.getCompositeType();
        boolean currentVersion = true;
        if (!isTypeMatched(threadInfoCompositeType, type)) {
            currentVersion = false;
            if (!isTypeMatched(threadInfoV5CompositeType, type)) {
                throw new IllegalArgumentException(
                    "Unexpected composite type for ThreadInfo");
            }
        }
        CompositeData[] stackTraceData =
            (CompositeData[]) cd.get(STACK_TRACE);
        if (stackTraceData == null) {
            throw new IllegalArgumentException(
                "StackTraceElement[] is missing");
        }
        if (stackTraceData.length > 0) {
            StackTraceElementCompositeData.validateCompositeData(stackTraceData[0]);
        }
        if (currentVersion) {
            CompositeData li = (CompositeData) cd.get(LOCK_INFO);
            if (li != null) {
                if (!isTypeMatched(lockInfoCompositeType,
                                   li.getCompositeType())) {
                    throw new IllegalArgumentException(
                        "Unexpected composite type for \"" +
                        LOCK_INFO + "\" attribute.");
                }
            }
            CompositeData[] lms = (CompositeData[]) cd.get(LOCKED_MONITORS);
            if (lms == null) {
                throw new IllegalArgumentException("MonitorInfo[] is null");
            }
            if (lms.length > 0) {
                MonitorInfoCompositeData.validateCompositeData(lms[0]);
            }
            CompositeData[] lsyncs = (CompositeData[]) cd.get(LOCKED_SYNCS);
            if (lsyncs == null) {
                throw new IllegalArgumentException("LockInfo[] is null");
            }
            if (lsyncs.length > 0) {
                if (!isTypeMatched(lockInfoCompositeType,
                                   lsyncs[0].getCompositeType())) {
                    throw new IllegalArgumentException(
                        "Unexpected composite type for \"" +
                        LOCKED_SYNCS + "\" attribute.");
                }
            }
        }
    }
    private static final long serialVersionUID = 2464378539119753175L;
}
