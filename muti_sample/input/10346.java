public class JvmThreadingImpl implements JvmThreadingMBean {
    final static EnumJvmThreadCpuTimeMonitoring
        JvmThreadCpuTimeMonitoringUnsupported =
        new EnumJvmThreadCpuTimeMonitoring("unsupported");
    final static EnumJvmThreadCpuTimeMonitoring
        JvmThreadCpuTimeMonitoringEnabled =
        new EnumJvmThreadCpuTimeMonitoring("enabled");
    final static EnumJvmThreadCpuTimeMonitoring
        JvmThreadCpuTimeMonitoringDisabled =
        new EnumJvmThreadCpuTimeMonitoring("disabled");
    static final EnumJvmThreadContentionMonitoring
        JvmThreadContentionMonitoringUnsupported =
        new EnumJvmThreadContentionMonitoring("unsupported");
    static final EnumJvmThreadContentionMonitoring
        JvmThreadContentionMonitoringEnabled =
        new EnumJvmThreadContentionMonitoring("enabled");
    static final EnumJvmThreadContentionMonitoring
        JvmThreadContentionMonitoringDisabled =
        new EnumJvmThreadContentionMonitoring("disabled");
    public JvmThreadingImpl(SnmpMib myMib) {
        log.debug("JvmThreadingImpl","Constructor");
    }
    public JvmThreadingImpl(SnmpMib myMib, MBeanServer server) {
        log.debug("JvmThreadingImpl","Constructor with server");
    }
    static ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }
    public EnumJvmThreadCpuTimeMonitoring getJvmThreadCpuTimeMonitoring()
        throws SnmpStatusException {
        ThreadMXBean mbean = getThreadMXBean();
        if(!mbean.isThreadCpuTimeSupported()) {
            log.debug("getJvmThreadCpuTimeMonitoring",
                      "Unsupported ThreadCpuTimeMonitoring");
            return JvmThreadCpuTimeMonitoringUnsupported;
        }
        try {
            if(mbean.isThreadCpuTimeEnabled()) {
                log.debug("getJvmThreadCpuTimeMonitoring",
                      "Enabled ThreadCpuTimeMonitoring");
                return JvmThreadCpuTimeMonitoringEnabled;
            } else {
                log.debug("getJvmThreadCpuTimeMonitoring",
                          "Disabled ThreadCpuTimeMonitoring");
                return JvmThreadCpuTimeMonitoringDisabled;
            }
        }catch(UnsupportedOperationException e) {
            log.debug("getJvmThreadCpuTimeMonitoring",
                      "Newly unsupported ThreadCpuTimeMonitoring");
            return JvmThreadCpuTimeMonitoringUnsupported;
        }
    }
    public void setJvmThreadCpuTimeMonitoring(EnumJvmThreadCpuTimeMonitoring x)
        throws SnmpStatusException {
        ThreadMXBean mbean = getThreadMXBean();
        if(JvmThreadCpuTimeMonitoringEnabled.intValue() == x.intValue())
            mbean.setThreadCpuTimeEnabled(true);
        else
            mbean.setThreadCpuTimeEnabled(false);
    }
    public void checkJvmThreadCpuTimeMonitoring(EnumJvmThreadCpuTimeMonitoring
                                                x)
        throws SnmpStatusException {
        if(JvmThreadCpuTimeMonitoringUnsupported.intValue() == x.intValue()) {
             log.debug("checkJvmThreadCpuTimeMonitoring",
                      "Try to set to illegal unsupported value");
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
        }
        if ((JvmThreadCpuTimeMonitoringEnabled.intValue() == x.intValue()) ||
            (JvmThreadCpuTimeMonitoringDisabled.intValue() == x.intValue())) {
            ThreadMXBean mbean = getThreadMXBean();
            if(mbean.isThreadCpuTimeSupported()) return;
            log.debug("checkJvmThreadCpuTimeMonitoring",
                      "Unsupported operation, can't set state");
            throw new
                SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
        }
        log.debug("checkJvmThreadCpuTimeMonitoring",
                  "unknown enum value ");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public EnumJvmThreadContentionMonitoring getJvmThreadContentionMonitoring()
        throws SnmpStatusException {
        ThreadMXBean mbean = getThreadMXBean();
        if(!mbean.isThreadContentionMonitoringSupported()) {
            log.debug("getJvmThreadContentionMonitoring",
                      "Unsupported ThreadContentionMonitoring");
            return JvmThreadContentionMonitoringUnsupported;
        }
        if(mbean.isThreadContentionMonitoringEnabled()) {
            log.debug("getJvmThreadContentionMonitoring",
                      "Enabled ThreadContentionMonitoring");
            return JvmThreadContentionMonitoringEnabled;
        } else {
            log.debug("getJvmThreadContentionMonitoring",
                      "Disabled ThreadContentionMonitoring");
            return JvmThreadContentionMonitoringDisabled;
        }
    }
    public void setJvmThreadContentionMonitoring(
                            EnumJvmThreadContentionMonitoring x)
        throws SnmpStatusException {
        ThreadMXBean mbean = getThreadMXBean();
        if(JvmThreadContentionMonitoringEnabled.intValue() == x.intValue())
            mbean.setThreadContentionMonitoringEnabled(true);
        else
            mbean.setThreadContentionMonitoringEnabled(false);
    }
    public void checkJvmThreadContentionMonitoring(
                              EnumJvmThreadContentionMonitoring x)
        throws SnmpStatusException {
        if(JvmThreadContentionMonitoringUnsupported.intValue()==x.intValue()) {
            log.debug("checkJvmThreadContentionMonitoring",
                      "Try to set to illegal unsupported value");
            throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
        }
        if ((JvmThreadContentionMonitoringEnabled.intValue()==x.intValue()) ||
            (JvmThreadContentionMonitoringDisabled.intValue()==x.intValue())) {
            ThreadMXBean mbean = getThreadMXBean();
            if(mbean.isThreadContentionMonitoringSupported()) return;
            log.debug("checkJvmThreadContentionMonitoring",
                      "Unsupported operation, can't set state");
            throw new
                SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
        }
        log.debug("checkJvmThreadContentionMonitoring",
                  "Try to set to unknown value");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
    }
    public Long getJvmThreadTotalStartedCount() throws SnmpStatusException {
        return new Long(getThreadMXBean().getTotalStartedThreadCount());
    }
    public Long getJvmThreadPeakCount() throws SnmpStatusException {
        return  new Long(getThreadMXBean().getPeakThreadCount());
    }
    public Long getJvmThreadDaemonCount() throws SnmpStatusException {
        return new Long(getThreadMXBean().getDaemonThreadCount());
    }
    public Long getJvmThreadCount() throws SnmpStatusException {
        return new Long(getThreadMXBean().getThreadCount());
    }
    public synchronized Long getJvmThreadPeakCountReset()
        throws SnmpStatusException {
        return new Long(jvmThreadPeakCountReset);
    }
    public synchronized void setJvmThreadPeakCountReset(Long x)
        throws SnmpStatusException {
        final long l = x.longValue();
        if (l > jvmThreadPeakCountReset) {
            final long stamp = System.currentTimeMillis();
            getThreadMXBean().resetPeakThreadCount();
            jvmThreadPeakCountReset = stamp;
            log.debug("setJvmThreadPeakCountReset",
                      "jvmThreadPeakCountReset="+stamp);
        }
    }
    public void checkJvmThreadPeakCountReset(Long x)
        throws SnmpStatusException {
    }
    private long jvmThreadPeakCountReset=0;
    static final MibLogger log = new MibLogger(JvmThreadingImpl.class);
}
