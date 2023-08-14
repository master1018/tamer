public class JvmRTInputArgsEntryImpl implements JvmRTInputArgsEntryMBean,
                                                Serializable {
    private final String item;
    private final int index;
    public JvmRTInputArgsEntryImpl(String item, int index) {
        this.item = validArgValueTC(item);
        this.index = index;
    }
    private String validArgValueTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validArgValueTC(str);
    }
    public String getJvmRTInputArgsItem() throws SnmpStatusException {
        return item;
    }
    public Integer getJvmRTInputArgsIndex() throws SnmpStatusException {
        return new Integer(index);
    }
}
