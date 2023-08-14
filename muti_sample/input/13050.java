public class JvmMemoryImpl implements JvmMemoryMBean {
    final static EnumJvmMemoryGCCall JvmMemoryGCCallSupported
        = new EnumJvmMemoryGCCall("supported");
    final static EnumJvmMemoryGCCall JvmMemoryGCCallStart
        = new EnumJvmMemoryGCCall("start");
    final static EnumJvmMemoryGCCall JvmMemoryGCCallFailed
        = new EnumJvmMemoryGCCall("failed");
    final static EnumJvmMemoryGCCall JvmMemoryGCCallStarted
        = new EnumJvmMemoryGCCall("started");
    final static EnumJvmMemoryGCVerboseLevel JvmMemoryGCVerboseLevelVerbose =
        new EnumJvmMemoryGCVerboseLevel("verbose");
    final static EnumJvmMemoryGCVerboseLevel JvmMemoryGCVerboseLevelSilent =
        new EnumJvmMemoryGCVerboseLevel("silent");
    public JvmMemoryImpl(SnmpMib myMib) {
    }
    public JvmMemoryImpl(SnmpMib myMib, MBeanServer server) {
    }
    final static String heapMemoryTag = "jvmMemory.getHeapMemoryUsage";
    final static String nonHeapMemoryTag = "jvmMemory.getNonHeapMemoryUsage";
    private MemoryUsage getMemoryUsage(MemoryType type) {
        if (type == MemoryType.HEAP) {
            return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        } else {
            return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        }
    }
    MemoryUsage getNonHeapMemoryUsage() {
        try {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            if (m != null) {
                final MemoryUsage cached = (MemoryUsage)
                    m.get(nonHeapMemoryTag);
                if (cached != null) {
                    log.debug("getNonHeapMemoryUsage",
                          "jvmMemory.getNonHeapMemoryUsage found in cache.");
                    return cached;
                }
                final MemoryUsage u = getMemoryUsage(MemoryType.NON_HEAP);
                m.put(nonHeapMemoryTag,u);
                return u;
            }
            log.trace("getNonHeapMemoryUsage",
                      "ERROR: should never come here!");
            return getMemoryUsage(MemoryType.NON_HEAP);
        } catch (RuntimeException x) {
            log.trace("getNonHeapMemoryUsage",
                  "Failed to get NonHeapMemoryUsage: " + x);
            log.debug("getNonHeapMemoryUsage",x);
            throw x;
        }
    }
    MemoryUsage getHeapMemoryUsage() {
        try {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            if (m != null) {
                final MemoryUsage cached = (MemoryUsage)m.get(heapMemoryTag);
                if (cached != null) {
                    log.debug("getHeapMemoryUsage",
                          "jvmMemory.getHeapMemoryUsage found in cache.");
                    return cached;
                }
                final MemoryUsage u = getMemoryUsage(MemoryType.HEAP);
                m.put(heapMemoryTag,u);
                return u;
            }
            log.trace("getHeapMemoryUsage", "ERROR: should never come here!");
            return getMemoryUsage(MemoryType.HEAP);
        } catch (RuntimeException x) {
            log.trace("getHeapMemoryUsage",
                  "Failed to get HeapMemoryUsage: " + x);
            log.debug("getHeapMemoryUsage",x);
            throw x;
        }
    }
    static final Long Long0 = new Long(0);
    public Long getJvmMemoryNonHeapMaxSize()
        throws SnmpStatusException {
        final long val = getNonHeapMemoryUsage().getMax();
        if (val > -1) return  new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryNonHeapCommitted() throws SnmpStatusException {
        final long val = getNonHeapMemoryUsage().getCommitted();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryNonHeapUsed() throws SnmpStatusException {
        final long val = getNonHeapMemoryUsage().getUsed();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryNonHeapInitSize() throws SnmpStatusException {
        final long val = getNonHeapMemoryUsage().getInit();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryHeapMaxSize() throws SnmpStatusException {
        final long val = getHeapMemoryUsage().getMax();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public EnumJvmMemoryGCCall getJvmMemoryGCCall()
        throws SnmpStatusException {
        final Map m = JvmContextFactory.getUserData();
        if (m != null) {
            final EnumJvmMemoryGCCall cached
                = (EnumJvmMemoryGCCall) m.get("jvmMemory.getJvmMemoryGCCall");
            if (cached != null) return cached;
        }
        return JvmMemoryGCCallSupported;
    }
    public void setJvmMemoryGCCall(EnumJvmMemoryGCCall x)
        throws SnmpStatusException {
        if (x.intValue() == JvmMemoryGCCallStart.intValue()) {
            final Map<Object, Object> m = JvmContextFactory.getUserData();
            try {
                ManagementFactory.getMemoryMXBean().gc();
                if (m != null) m.put("jvmMemory.getJvmMemoryGCCall",
                                     JvmMemoryGCCallStarted);
            } catch (Exception ex) {
                if (m != null) m.put("jvmMemory.getJvmMemoryGCCall",
                                     JvmMemoryGCCallFailed);
            }
            return;
        }
        throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public void checkJvmMemoryGCCall(EnumJvmMemoryGCCall x)
        throws SnmpStatusException {
        if (x.intValue() != JvmMemoryGCCallStart.intValue())
        throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public Long getJvmMemoryHeapCommitted() throws SnmpStatusException {
        final long val = getHeapMemoryUsage().getCommitted();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public EnumJvmMemoryGCVerboseLevel getJvmMemoryGCVerboseLevel()
        throws SnmpStatusException {
        if (ManagementFactory.getMemoryMXBean().isVerbose())
            return JvmMemoryGCVerboseLevelVerbose;
        else
            return JvmMemoryGCVerboseLevelSilent;
    }
    public void setJvmMemoryGCVerboseLevel(EnumJvmMemoryGCVerboseLevel x)
        throws SnmpStatusException {
        if (JvmMemoryGCVerboseLevelVerbose.intValue() == x.intValue())
            ManagementFactory.getMemoryMXBean().setVerbose(true);
        else
            ManagementFactory.getMemoryMXBean().setVerbose(false);
    }
    public void checkJvmMemoryGCVerboseLevel(EnumJvmMemoryGCVerboseLevel x)
        throws SnmpStatusException {
    }
    public Long getJvmMemoryHeapUsed() throws SnmpStatusException {
        final long val = getHeapMemoryUsage().getUsed();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryHeapInitSize() throws SnmpStatusException {
        final long val = getHeapMemoryUsage().getInit();
        if (val > -1) return new Long(val);
        else return Long0;
    }
    public Long getJvmMemoryPendingFinalCount()
        throws SnmpStatusException {
        final long val = ManagementFactory.getMemoryMXBean().
            getObjectPendingFinalizationCount();
        if (val > -1) return new Long((int)val);
        else return new Long(0);
    }
    static final MibLogger log = new MibLogger(JvmMemoryImpl.class);
}
