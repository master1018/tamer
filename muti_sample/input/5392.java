public class JvmMemoryMetaImpl extends JvmMemoryMeta {
    public JvmMemoryMetaImpl(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        super(myMib,objserv);
    }
    protected JvmMemManagerTableMeta createJvmMemManagerTableMetaNode(
        String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemManagerTableMetaImpl(mib, objectserver);
    }
    protected JvmMemGCTableMeta createJvmMemGCTableMetaNode(String tableName,
                      String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemGCTableMetaImpl(mib, objectserver);
    }
    protected JvmMemPoolTableMeta
        createJvmMemPoolTableMetaNode(String tableName, String groupName,
                                      SnmpMib mib, MBeanServer server)  {
        return new JvmMemPoolTableMetaImpl(mib, objectserver);
    }
    protected JvmMemMgrPoolRelTableMeta
        createJvmMemMgrPoolRelTableMetaNode(String tableName,
                                            String groupName,
                                            SnmpMib mib, MBeanServer server) {
        return new JvmMemMgrPoolRelTableMetaImpl(mib, objectserver);
    }
}
