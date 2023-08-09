public class JvmMemMgrPoolRelEntryImpl
    implements JvmMemMgrPoolRelEntryMBean {
    final protected int JvmMemManagerIndex;
    final protected int JvmMemPoolIndex;
    final protected String mmmName;
    final protected String mpmName;
    public JvmMemMgrPoolRelEntryImpl(String mmmName,
                                     String mpmName,
                                     int mmarc, int mparc) {
        JvmMemManagerIndex = mmarc;
        JvmMemPoolIndex    = mparc;
        this.mmmName = mmmName;
        this.mpmName = mpmName;
    }
    public String getJvmMemMgrRelPoolName() throws SnmpStatusException {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(mpmName);
    }
    public String getJvmMemMgrRelManagerName() throws SnmpStatusException {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(mmmName);
    }
    public Integer getJvmMemManagerIndex() throws SnmpStatusException {
        return new Integer(JvmMemManagerIndex);
    }
    public Integer getJvmMemPoolIndex() throws SnmpStatusException {
        return new Integer(JvmMemPoolIndex);
    }
}
