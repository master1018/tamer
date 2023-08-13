public class JvmMemManagerEntryImpl implements JvmMemManagerEntryMBean {
    protected final int JvmMemManagerIndex;
    protected MemoryManagerMXBean manager;
    public JvmMemManagerEntryImpl(MemoryManagerMXBean m, int myindex) {
        manager = m;
        JvmMemManagerIndex = myindex;
    }
    public String getJvmMemManagerName() throws SnmpStatusException {
        return JVM_MANAGEMENT_MIB_IMPL.
            validJavaObjectNameTC(manager.getName());
    }
    public Integer getJvmMemManagerIndex() throws SnmpStatusException {
        return new Integer(JvmMemManagerIndex);
    }
    public EnumJvmMemManagerState getJvmMemManagerState()
        throws SnmpStatusException {
        if (manager.isValid())
            return JvmMemManagerStateValid;
        else
            return JvmMemManagerStateInvalid;
    }
    private final static EnumJvmMemManagerState JvmMemManagerStateValid =
        new EnumJvmMemManagerState("valid");
    private final static EnumJvmMemManagerState JvmMemManagerStateInvalid =
        new EnumJvmMemManagerState("invalid");
}
