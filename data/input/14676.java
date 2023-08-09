public class JvmMemGCEntryImpl implements JvmMemGCEntryMBean {
    protected final int JvmMemManagerIndex;
    protected final GarbageCollectorMXBean gcm;
    public JvmMemGCEntryImpl(GarbageCollectorMXBean gcm, int index) {
        this.gcm=gcm;
        this.JvmMemManagerIndex = index;
    }
    public Long getJvmMemGCTimeMs() throws SnmpStatusException {
        return new Long(gcm.getCollectionTime());
    }
    public Long getJvmMemGCCount() throws SnmpStatusException {
        return new Long(gcm.getCollectionCount());
    }
    public Integer getJvmMemManagerIndex() throws SnmpStatusException {
        return new Integer(JvmMemManagerIndex);
    }
}
