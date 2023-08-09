public class JvmClassLoadingImpl implements JvmClassLoadingMBean {
    static final EnumJvmClassesVerboseLevel JvmClassesVerboseLevelVerbose =
        new EnumJvmClassesVerboseLevel("verbose");
    static final EnumJvmClassesVerboseLevel JvmClassesVerboseLevelSilent =
        new EnumJvmClassesVerboseLevel("silent");
    public JvmClassLoadingImpl(SnmpMib myMib) {
    }
    public JvmClassLoadingImpl(SnmpMib myMib, MBeanServer server) {
    }
    static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactory.getClassLoadingMXBean();
    }
    public EnumJvmClassesVerboseLevel getJvmClassesVerboseLevel()
        throws SnmpStatusException {
        if(getClassLoadingMXBean().isVerbose())
            return JvmClassesVerboseLevelVerbose;
        else
            return JvmClassesVerboseLevelSilent;
    }
    public void setJvmClassesVerboseLevel(EnumJvmClassesVerboseLevel x)
        throws SnmpStatusException {
        final boolean verbose;
        if (JvmClassesVerboseLevelVerbose.equals(x)) verbose=true;
        else if (JvmClassesVerboseLevelSilent.equals(x)) verbose=false;
        else throw new
            SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
        getClassLoadingMXBean().setVerbose(verbose);
    }
    public void checkJvmClassesVerboseLevel(EnumJvmClassesVerboseLevel x)
        throws SnmpStatusException {
        if (JvmClassesVerboseLevelVerbose.equals(x)) return;
        if (JvmClassesVerboseLevelSilent.equals(x))  return;
        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
    }
    public Long getJvmClassesUnloadedCount() throws SnmpStatusException {
        return new Long(getClassLoadingMXBean().getUnloadedClassCount());
    }
    public Long getJvmClassesTotalLoadedCount() throws SnmpStatusException {
        return new Long(getClassLoadingMXBean().getTotalLoadedClassCount());
    }
    public Long getJvmClassesLoadedCount() throws SnmpStatusException {
        return new Long(getClassLoadingMXBean().getLoadedClassCount());
    }
}
