public class JvmRTClassPathEntryImpl implements JvmRTClassPathEntryMBean,
                                                Serializable {
    private final String item;
    private final int index;
    public JvmRTClassPathEntryImpl(String item, int index) {
        this.item = validPathElementTC(item);
        this.index = index;
    }
    private String validPathElementTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(str);
    }
    public String getJvmRTClassPathItem() throws SnmpStatusException {
        return item;
    }
    public Integer getJvmRTClassPathIndex() throws SnmpStatusException {
        return new Integer(index);
    }
}
