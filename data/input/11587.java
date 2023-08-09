public class JvmMemPoolEntryImpl implements JvmMemPoolEntryMBean {
    final protected int jvmMemPoolIndex;
    final static String memoryTag = "jvmMemPoolEntry.getUsage";
    final static String peakMemoryTag = "jvmMemPoolEntry.getPeakUsage";
    final static String collectMemoryTag =
        "jvmMemPoolEntry.getCollectionUsage";
    final static MemoryUsage ZEROS = new MemoryUsage(0,0,0,0);
    final String entryMemoryTag;
    final String entryPeakMemoryTag;
    final String entryCollectMemoryTag;
    MemoryUsage getMemoryUsage() {
        try {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            if (m != null) {
                final MemoryUsage cached = (MemoryUsage)
                    m.get(entryMemoryTag);
                if (cached != null) {
                    log.debug("getMemoryUsage",entryMemoryTag+
                          " found in cache.");
                    return cached;
                }
                MemoryUsage u = pool.getUsage();
                if (u == null) u = ZEROS;
                m.put(entryMemoryTag,u);
                return u;
            }
            log.trace("getMemoryUsage", "ERROR: should never come here!");
            return pool.getUsage();
        } catch (RuntimeException x) {
            log.trace("getMemoryUsage",
                  "Failed to get MemoryUsage: " + x);
            log.debug("getMemoryUsage",x);
            throw x;
        }
    }
    MemoryUsage getPeakMemoryUsage() {
        try {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            if (m != null) {
                final MemoryUsage cached = (MemoryUsage)
                    m.get(entryPeakMemoryTag);
                if (cached != null) {
                    if (log.isDebugOn())
                        log.debug("getPeakMemoryUsage",
                              entryPeakMemoryTag + " found in cache.");
                    return cached;
                }
                MemoryUsage u = pool.getPeakUsage();
                if (u == null) u = ZEROS;
                m.put(entryPeakMemoryTag,u);
                return u;
            }
            log.trace("getPeakMemoryUsage", "ERROR: should never come here!");
            return ZEROS;
        } catch (RuntimeException x) {
            log.trace("getPeakMemoryUsage",
                  "Failed to get MemoryUsage: " + x);
            log.debug("getPeakMemoryUsage",x);
            throw x;
        }
    }
    MemoryUsage getCollectMemoryUsage() {
        try {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            if (m != null) {
                final MemoryUsage cached = (MemoryUsage)
                    m.get(entryCollectMemoryTag);
                if (cached != null) {
                    if (log.isDebugOn())
                        log.debug("getCollectMemoryUsage",
                                  entryCollectMemoryTag + " found in cache.");
                    return cached;
                }
                MemoryUsage u = pool.getCollectionUsage();
                if (u == null) u = ZEROS;
                m.put(entryCollectMemoryTag,u);
                return u;
            }
            log.trace("getCollectMemoryUsage",
                      "ERROR: should never come here!");
            return ZEROS;
        } catch (RuntimeException x) {
            log.trace("getPeakMemoryUsage",
                  "Failed to get MemoryUsage: " + x);
            log.debug("getPeakMemoryUsage",x);
            throw x;
        }
    }
    final MemoryPoolMXBean pool;
    public JvmMemPoolEntryImpl(MemoryPoolMXBean mp, final int index) {
        this.pool=mp;
        this.jvmMemPoolIndex = index;
        this.entryMemoryTag = memoryTag + "." + index;
        this.entryPeakMemoryTag = peakMemoryTag + "." + index;
        this.entryCollectMemoryTag = collectMemoryTag + "." + index;
    }
    public Long getJvmMemPoolMaxSize() throws SnmpStatusException {
        final long val = getMemoryUsage().getMax();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolUsed() throws SnmpStatusException {
        final long val = getMemoryUsage().getUsed();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolInitSize() throws SnmpStatusException {
        final long val = getMemoryUsage().getInit();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolCommitted() throws SnmpStatusException {
        final long val = getMemoryUsage().getCommitted();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolPeakMaxSize() throws SnmpStatusException {
        final long val = getPeakMemoryUsage().getMax();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolPeakUsed() throws SnmpStatusException {
        final long val = getPeakMemoryUsage().getUsed();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolPeakCommitted() throws SnmpStatusException {
        final long val = getPeakMemoryUsage().getCommitted();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolCollectMaxSize() throws SnmpStatusException {
        final long val = getCollectMemoryUsage().getMax();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolCollectUsed() throws SnmpStatusException {
        final long val = getCollectMemoryUsage().getUsed();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolCollectCommitted() throws SnmpStatusException {
        final long val = getCollectMemoryUsage().getCommitted();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolThreshold() throws SnmpStatusException {
        if (!pool.isUsageThresholdSupported())
            return JvmMemoryImpl.Long0;
        final long val = pool.getUsageThreshold();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public void setJvmMemPoolThreshold(Long x) throws SnmpStatusException {
        final long val = x.longValue();
        if (val < 0 )
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
        pool.setUsageThreshold(val);
    }
    public void checkJvmMemPoolThreshold(Long x) throws SnmpStatusException {
        if (!pool.isUsageThresholdSupported())
            throw new
                SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
        final long val = x.longValue();
        if (val < 0 )
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public EnumJvmMemPoolThreshdSupport getJvmMemPoolThreshdSupport()
        throws SnmpStatusException {
        if (pool.isUsageThresholdSupported())
            return EnumJvmMemPoolThreshdSupported;
        else
            return EnumJvmMemPoolThreshdUnsupported;
    }
    public Long getJvmMemPoolThreshdCount()
        throws SnmpStatusException {
        if (!pool.isUsageThresholdSupported())
            return JvmMemoryImpl.Long0;
        final long val = pool.getUsageThresholdCount();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public Long getJvmMemPoolCollectThreshold() throws SnmpStatusException {
        if (!pool.isCollectionUsageThresholdSupported())
            return JvmMemoryImpl.Long0;
        final long val = pool.getCollectionUsageThreshold();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public void setJvmMemPoolCollectThreshold(Long x)
        throws SnmpStatusException {
        final long val = x.longValue();
        if (val < 0 )
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
        pool.setCollectionUsageThreshold(val);
    }
    public void checkJvmMemPoolCollectThreshold(Long x)
        throws SnmpStatusException {
        if (!pool.isCollectionUsageThresholdSupported())
            throw new
                SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
        final long val = x.longValue();
        if (val < 0 )
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public EnumJvmMemPoolCollectThreshdSupport
        getJvmMemPoolCollectThreshdSupport()
        throws SnmpStatusException {
        if (pool.isCollectionUsageThresholdSupported())
            return EnumJvmMemPoolCollectThreshdSupported;
        else
            return EnumJvmMemPoolCollectThreshdUnsupported;
    }
    public Long getJvmMemPoolCollectThreshdCount()
        throws SnmpStatusException {
        if (!pool.isCollectionUsageThresholdSupported())
            return JvmMemoryImpl.Long0;
        final long val = pool.getCollectionUsageThresholdCount();
        if (val > -1) return  new Long(val);
        else return JvmMemoryImpl.Long0;
    }
    public static EnumJvmMemPoolType jvmMemPoolType(MemoryType type)
        throws SnmpStatusException {
        if (type.equals(MemoryType.HEAP))
            return  EnumJvmMemPoolTypeHeap;
        else if (type.equals(MemoryType.NON_HEAP))
            return EnumJvmMemPoolTypeNonHeap;
        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
    }
    public EnumJvmMemPoolType getJvmMemPoolType() throws SnmpStatusException {
        return jvmMemPoolType(pool.getType());
    }
    public String getJvmMemPoolName() throws SnmpStatusException {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(pool.getName());
    }
    public Integer getJvmMemPoolIndex() throws SnmpStatusException {
        return new Integer(jvmMemPoolIndex);
    }
    public EnumJvmMemPoolState getJvmMemPoolState()
        throws SnmpStatusException {
        if (pool.isValid())
            return JvmMemPoolStateValid;
        else
            return JvmMemPoolStateInvalid;
    }
    public synchronized Long getJvmMemPoolPeakReset()
        throws SnmpStatusException {
        return new Long(jvmMemPoolPeakReset);
    }
    public synchronized void setJvmMemPoolPeakReset(Long x)
        throws SnmpStatusException {
        final long l = x.longValue();
        if (l > jvmMemPoolPeakReset) {
            final long stamp = System.currentTimeMillis();
            pool.resetPeakUsage();
            jvmMemPoolPeakReset = stamp;
            log.debug("setJvmMemPoolPeakReset",
                      "jvmMemPoolPeakReset="+stamp);
        }
    }
    public void checkJvmMemPoolPeakReset(Long x) throws SnmpStatusException {
    }
    private long jvmMemPoolPeakReset = 0;
    private final static EnumJvmMemPoolState JvmMemPoolStateValid =
        new EnumJvmMemPoolState("valid");
    private final static EnumJvmMemPoolState JvmMemPoolStateInvalid =
        new EnumJvmMemPoolState("invalid");
    private static final EnumJvmMemPoolType EnumJvmMemPoolTypeHeap =
        new EnumJvmMemPoolType("heap");
    private static final EnumJvmMemPoolType EnumJvmMemPoolTypeNonHeap =
        new EnumJvmMemPoolType("nonheap");
    private static final EnumJvmMemPoolThreshdSupport
        EnumJvmMemPoolThreshdSupported =
        new EnumJvmMemPoolThreshdSupport("supported");
    private static final EnumJvmMemPoolThreshdSupport
        EnumJvmMemPoolThreshdUnsupported =
        new EnumJvmMemPoolThreshdSupport("unsupported");
    private static final EnumJvmMemPoolCollectThreshdSupport
        EnumJvmMemPoolCollectThreshdSupported =
        new EnumJvmMemPoolCollectThreshdSupport("supported");
    private static final EnumJvmMemPoolCollectThreshdSupport
        EnumJvmMemPoolCollectThreshdUnsupported=
        new EnumJvmMemPoolCollectThreshdSupport("unsupported");
    static final MibLogger log = new MibLogger(JvmMemPoolEntryImpl.class);
}
