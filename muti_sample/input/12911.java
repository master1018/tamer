public class JvmRuntimeMetaImpl extends JvmRuntimeMeta {
    public JvmRuntimeMetaImpl(SnmpMib myMib,
                              SnmpStandardObjectServer objserv) {
        super(myMib, objserv);
    }
    protected JvmRTInputArgsTableMeta
        createJvmRTInputArgsTableMetaNode(String tableName, String groupName,
                                          SnmpMib mib, MBeanServer server)  {
        return new JvmRTInputArgsTableMetaImpl(mib, objectserver);
    }
    protected JvmRTLibraryPathTableMeta
        createJvmRTLibraryPathTableMetaNode(String tableName,
                                            String groupName,
                                            SnmpMib mib,
                                            MBeanServer server)  {
        return new JvmRTLibraryPathTableMetaImpl(mib, objectserver);
    }
    protected JvmRTClassPathTableMeta
        createJvmRTClassPathTableMetaNode(String tableName, String groupName,
                                          SnmpMib mib, MBeanServer server)  {
        return new JvmRTClassPathTableMetaImpl(mib, objectserver);
    }
    protected JvmRTBootClassPathTableMeta
        createJvmRTBootClassPathTableMetaNode(String tableName,
                                              String groupName,
                                              SnmpMib mib,
                                              MBeanServer server)  {
        return new JvmRTBootClassPathTableMetaImpl(mib, objectserver);
    }
}
