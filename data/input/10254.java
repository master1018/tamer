public class JvmOSImpl implements JvmOSMBean, Serializable {
    public JvmOSImpl(SnmpMib myMib) {
    }
    public JvmOSImpl(SnmpMib myMib, MBeanServer server) {
    }
    static OperatingSystemMXBean getOSMBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }
    private static String validDisplayStringTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validDisplayStringTC(str);
    }
    private static String validJavaObjectNameTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(str);
    }
    public Integer getJvmOSProcessorCount() throws SnmpStatusException {
        return new Integer(getOSMBean().getAvailableProcessors());
    }
    public String getJvmOSVersion() throws SnmpStatusException {
        return validDisplayStringTC(getOSMBean().getVersion());
    }
    public String getJvmOSArch() throws SnmpStatusException {
        return validDisplayStringTC(getOSMBean().getArch());
    }
    public String getJvmOSName() throws SnmpStatusException {
        return validJavaObjectNameTC(getOSMBean().getName());
    }
}
