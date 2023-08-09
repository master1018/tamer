public class JvmRTLibraryPathEntryImpl implements JvmRTLibraryPathEntryMBean,
                                                Serializable {
    private final String item;
    private final int index;
    public JvmRTLibraryPathEntryImpl(String item, int index) {
        this.item  = validPathElementTC(item);
        this.index = index;
    }
    private String validPathElementTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(str);
    }
    public String getJvmRTLibraryPathItem() throws SnmpStatusException {
        return item;
    }
    public Integer getJvmRTLibraryPathIndex() throws SnmpStatusException {
        return new Integer(index);
    }
}
