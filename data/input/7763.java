public class JvmRTBootClassPathEntryImpl
    implements JvmRTBootClassPathEntryMBean, Serializable {
    private final String item;
    private final int index;
    public JvmRTBootClassPathEntryImpl(String item, int index) {
        this.item = validPathElementTC(item);
        this.index = index;
    }
    private String validPathElementTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(str);
    }
    public String getJvmRTBootClassPathItem() throws SnmpStatusException {
        return item;
    }
    public Integer getJvmRTBootClassPathIndex() throws SnmpStatusException {
        return new Integer(index);
    }
}
