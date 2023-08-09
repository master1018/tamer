public class JvmCompilationImpl implements JvmCompilationMBean {
    static final EnumJvmJITCompilerTimeMonitoring
        JvmJITCompilerTimeMonitoringSupported =
        new EnumJvmJITCompilerTimeMonitoring("supported");
    static final EnumJvmJITCompilerTimeMonitoring
        JvmJITCompilerTimeMonitoringUnsupported =
        new EnumJvmJITCompilerTimeMonitoring("unsupported");
    public JvmCompilationImpl(SnmpMib myMib) {
    }
    public JvmCompilationImpl(SnmpMib myMib, MBeanServer server) {
    }
    private static CompilationMXBean getCompilationMXBean() {
        return ManagementFactory.getCompilationMXBean();
    }
    public EnumJvmJITCompilerTimeMonitoring getJvmJITCompilerTimeMonitoring()
        throws SnmpStatusException {
        if(getCompilationMXBean().isCompilationTimeMonitoringSupported())
            return JvmJITCompilerTimeMonitoringSupported;
        else
            return JvmJITCompilerTimeMonitoringUnsupported;
    }
    public Long getJvmJITCompilerTimeMs() throws SnmpStatusException {
        final long t;
        if(getCompilationMXBean().isCompilationTimeMonitoringSupported())
            t = getCompilationMXBean().getTotalCompilationTime();
        else
            t = 0;
        return new Long(t);
    }
    public String getJvmJITCompilerName() throws SnmpStatusException {
        return JVM_MANAGEMENT_MIB_IMPL.
            validJavaObjectNameTC(getCompilationMXBean().getName());
    }
}
