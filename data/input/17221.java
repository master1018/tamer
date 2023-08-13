public class JvmRTLibraryPathTableMeta extends SnmpMibTable implements Serializable {
    public JvmRTLibraryPathTableMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        super(myMib);
        objectserver = objserv;
    }
    protected JvmRTLibraryPathEntryMeta createJvmRTLibraryPathEntryMetaNode(String snmpEntryName, String tableName, SnmpMib mib, MBeanServer server)  {
        return new JvmRTLibraryPathEntryMeta(mib, objectserver);
    }
    public void createNewEntry(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (factory != null)
            factory.createNewEntry(req, rowOid, depth, this);
        else
            throw new SnmpStatusException(
                SnmpStatusException.snmpRspNoAccess);
    }
    public boolean isRegistrationRequired()  {
        return false;
    }
    public void registerEntryNode(SnmpMib mib, MBeanServer server)  {
        node = createJvmRTLibraryPathEntryMetaNode("JvmRTLibraryPathEntry", "JvmRTLibraryPathTable", mib, server);
    }
    public synchronized void addEntry(SnmpOid rowOid, ObjectName objname,
                 Object entry)
        throws SnmpStatusException {
        if (! (entry instanceof JvmRTLibraryPathEntryMBean) )
            throw new ClassCastException("Entries for Table \"" +
                           "JvmRTLibraryPathTable" + "\" must implement the \"" +
                           "JvmRTLibraryPathEntryMBean" + "\" interface.");
        super.addEntry(rowOid, objname, entry);
    }
    public void get(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        JvmRTLibraryPathEntryMBean entry = (JvmRTLibraryPathEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.get(req,depth);
        }
    }
    public void set(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (req.getSize() == 0) return;
        JvmRTLibraryPathEntryMBean entry = (JvmRTLibraryPathEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.set(req,depth);
        }
    }
    public void check(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (req.getSize() == 0) return;
        JvmRTLibraryPathEntryMBean entry = (JvmRTLibraryPathEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.check(req,depth);
        }
    }
    public void validateVarEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        node.validateVarId(var, data);
    }
    public boolean isReadableEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        return node.isReadable(var);
    }
    public long getNextVarEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        long nextvar = node.getNextVarId(var, data);
        while (!isReadableEntryId(rowOid, nextvar, data))
            nextvar = node.getNextVarId(nextvar, data);
        return nextvar;
    }
    public boolean skipEntryVariable( SnmpOid rowOid, long var, Object data, int pduVersion) {
        try {
            JvmRTLibraryPathEntryMBean entry = (JvmRTLibraryPathEntryMBean) getEntry(rowOid);
            synchronized (this) {
                node.setInstance(entry);
                return node.skipVariable(var, data, pduVersion);
            }
        } catch (SnmpStatusException x) {
            return false;
        }
    }
    private JvmRTLibraryPathEntryMeta node;
    protected SnmpStandardObjectServer objectserver;
}
