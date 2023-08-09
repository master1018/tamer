public class JvmThreadingMetaImpl extends JvmThreadingMeta {
    public JvmThreadingMetaImpl(SnmpMib myMib,
                                SnmpStandardObjectServer objserv) {
        super(myMib, objserv);
    }
    protected JvmThreadInstanceTableMeta
        createJvmThreadInstanceTableMetaNode(String tableName,
                                             String groupName,
                                             SnmpMib mib,
                                             MBeanServer server)  {
        return new JvmThreadInstanceTableMetaImpl(mib, objectserver);
    }
}
