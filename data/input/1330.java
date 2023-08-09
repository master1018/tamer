public class JvmRuntimeMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {
    public JvmRuntimeMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(23);
            registerObject(22);
            registerObject(21);
            registerObject(9);
            registerObject(20);
            registerObject(8);
            registerObject(7);
            registerObject(6);
            registerObject(5);
            registerObject(4);
            registerObject(3);
            registerObject(12);
            registerObject(11);
            registerObject(2);
            registerObject(1);
            registerObject(10);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 23: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 22: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 21: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 9:
                return new SnmpInt(node.getJvmRTBootClassPathSupport());
            case 20: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 8:
                return new SnmpString(node.getJvmRTManagementSpecVersion());
            case 7:
                return new SnmpString(node.getJvmRTSpecVersion());
            case 6:
                return new SnmpString(node.getJvmRTSpecVendor());
            case 5:
                return new SnmpString(node.getJvmRTSpecName());
            case 4:
                return new SnmpString(node.getJvmRTVMVersion());
            case 3:
                return new SnmpString(node.getJvmRTVMVendor());
            case 12:
                return new SnmpCounter64(node.getJvmRTStartTimeMs());
            case 11:
                return new SnmpCounter64(node.getJvmRTUptimeMs());
            case 2:
                return new SnmpString(node.getJvmRTVMName());
            case 1:
                return new SnmpString(node.getJvmRTName());
            case 10:
                return new SnmpInt(node.getJvmRTInputArgsCount());
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 23: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 22: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 21: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 20: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 8:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 7:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 6:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 5:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 12:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 23: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 22: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 21: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 20: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 8:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 7:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 6:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 5:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 12:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmRuntimeMBean var) {
        node = var;
    }
    public void get(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.get(this,req,depth);
    }
    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.set(this,req,depth);
    }
    public void check(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.check(this,req,depth);
    }
    public boolean isVariable(long arc) {
        switch((int)arc) {
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 12:
            case 11:
            case 2:
            case 1:
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean isReadable(long arc) {
        switch((int)arc) {
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 12:
            case 11:
            case 2:
            case 1:
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 12:
            case 11:
                if (pduVersion==SnmpDefinitions.snmpVersionOne) return true;
                break;
            default:
                break;
        }
        return super.skipVariable(var,data,pduVersion);
    }
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 23: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 22: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 21: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 9:
                return "JvmRTBootClassPathSupport";
            case 20: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 8:
                return "JvmRTManagementSpecVersion";
            case 7:
                return "JvmRTSpecVersion";
            case 6:
                return "JvmRTSpecVendor";
            case 5:
                return "JvmRTSpecName";
            case 4:
                return "JvmRTVMVersion";
            case 3:
                return "JvmRTVMVendor";
            case 12:
                return "JvmRTStartTimeMs";
            case 11:
                return "JvmRTUptimeMs";
            case 2:
                return "JvmRTVMName";
            case 1:
                return "JvmRTName";
            case 10:
                return "JvmRTInputArgsCount";
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public boolean isTable(long arc) {
        switch((int)arc) {
            case 23:
                return true;
            case 22:
                return true;
            case 21:
                return true;
            case 20:
                return true;
            default:
                break;
        }
        return false;
    }
    public SnmpMibTable getTable(long arc) {
        switch((int)arc) {
            case 23:
                return tableJvmRTLibraryPathTable;
            case 22:
                return tableJvmRTClassPathTable;
            case 21:
                return tableJvmRTBootClassPathTable;
            case 20:
                return tableJvmRTInputArgsTable;
        default:
            break;
        }
        return null;
    }
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
        tableJvmRTLibraryPathTable = createJvmRTLibraryPathTableMetaNode("JvmRTLibraryPathTable", "JvmRuntime", mib, server);
        if ( tableJvmRTLibraryPathTable != null)  {
            tableJvmRTLibraryPathTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmRTLibraryPathTable", tableJvmRTLibraryPathTable);
        }
        tableJvmRTClassPathTable = createJvmRTClassPathTableMetaNode("JvmRTClassPathTable", "JvmRuntime", mib, server);
        if ( tableJvmRTClassPathTable != null)  {
            tableJvmRTClassPathTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmRTClassPathTable", tableJvmRTClassPathTable);
        }
        tableJvmRTBootClassPathTable = createJvmRTBootClassPathTableMetaNode("JvmRTBootClassPathTable", "JvmRuntime", mib, server);
        if ( tableJvmRTBootClassPathTable != null)  {
            tableJvmRTBootClassPathTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmRTBootClassPathTable", tableJvmRTBootClassPathTable);
        }
        tableJvmRTInputArgsTable = createJvmRTInputArgsTableMetaNode("JvmRTInputArgsTable", "JvmRuntime", mib, server);
        if ( tableJvmRTInputArgsTable != null)  {
            tableJvmRTInputArgsTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmRTInputArgsTable", tableJvmRTInputArgsTable);
        }
    }
    protected JvmRTLibraryPathTableMeta createJvmRTLibraryPathTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmRTLibraryPathTableMeta(mib, objectserver);
    }
    protected JvmRTClassPathTableMeta createJvmRTClassPathTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmRTClassPathTableMeta(mib, objectserver);
    }
    protected JvmRTBootClassPathTableMeta createJvmRTBootClassPathTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmRTBootClassPathTableMeta(mib, objectserver);
    }
    protected JvmRTInputArgsTableMeta createJvmRTInputArgsTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmRTInputArgsTableMeta(mib, objectserver);
    }
    protected JvmRuntimeMBean node;
    protected SnmpStandardObjectServer objectserver = null;
    protected JvmRTLibraryPathTableMeta tableJvmRTLibraryPathTable = null;
    protected JvmRTClassPathTableMeta tableJvmRTClassPathTable = null;
    protected JvmRTBootClassPathTableMeta tableJvmRTBootClassPathTable = null;
    protected JvmRTInputArgsTableMeta tableJvmRTInputArgsTable = null;
}
