public class JvmMemoryMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {
    private static final long serialVersionUID = 9047644262627149214L;
    public JvmMemoryMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(120);
            registerObject(23);
            registerObject(22);
            registerObject(21);
            registerObject(110);
            registerObject(20);
            registerObject(13);
            registerObject(12);
            registerObject(3);
            registerObject(11);
            registerObject(2);
            registerObject(101);
            registerObject(10);
            registerObject(1);
            registerObject(100);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 120: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 23:
                return new SnmpCounter64(node.getJvmMemoryNonHeapMaxSize());
            case 22:
                return new SnmpCounter64(node.getJvmMemoryNonHeapCommitted());
            case 21:
                return new SnmpCounter64(node.getJvmMemoryNonHeapUsed());
            case 110: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 20:
                return new SnmpCounter64(node.getJvmMemoryNonHeapInitSize());
            case 13:
                return new SnmpCounter64(node.getJvmMemoryHeapMaxSize());
            case 12:
                return new SnmpCounter64(node.getJvmMemoryHeapCommitted());
            case 3:
                return new SnmpInt(node.getJvmMemoryGCCall());
            case 11:
                return new SnmpCounter64(node.getJvmMemoryHeapUsed());
            case 2:
                return new SnmpInt(node.getJvmMemoryGCVerboseLevel());
            case 101: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 10:
                return new SnmpCounter64(node.getJvmMemoryHeapInitSize());
            case 1:
                return new SnmpGauge(node.getJvmMemoryPendingFinalCount());
            case 100: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 120: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 23:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 22:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 21:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 110: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 20:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 13:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 12:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                if (x instanceof SnmpInt) {
                    try  {
                        node.setJvmMemoryGCCall( new EnumJvmMemoryGCCall (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                    return new SnmpInt(node.getJvmMemoryGCCall());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                if (x instanceof SnmpInt) {
                    try  {
                        node.setJvmMemoryGCVerboseLevel( new EnumJvmMemoryGCVerboseLevel (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                    return new SnmpInt(node.getJvmMemoryGCVerboseLevel());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
            case 101: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 100: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 120: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 23:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 22:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 21:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 110: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 20:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 13:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 12:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                if (x instanceof SnmpInt) {
                    try  {
                        node.checkJvmMemoryGCCall( new EnumJvmMemoryGCCall (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
                break;
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                if (x instanceof SnmpInt) {
                    try  {
                        node.checkJvmMemoryGCVerboseLevel( new EnumJvmMemoryGCVerboseLevel (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
                break;
            case 101: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 100: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmMemoryMBean var) {
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
            case 23:
            case 22:
            case 21:
            case 20:
            case 13:
            case 12:
            case 3:
            case 11:
            case 2:
            case 10:
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean isReadable(long arc) {
        switch((int)arc) {
            case 23:
            case 22:
            case 21:
            case 20:
            case 13:
            case 12:
            case 3:
            case 11:
            case 2:
            case 10:
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 23:
            case 22:
            case 21:
            case 20:
            case 13:
            case 12:
            case 11:
            case 10:
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
            case 120: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 23:
                return "JvmMemoryNonHeapMaxSize";
            case 22:
                return "JvmMemoryNonHeapCommitted";
            case 21:
                return "JvmMemoryNonHeapUsed";
            case 110: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 20:
                return "JvmMemoryNonHeapInitSize";
            case 13:
                return "JvmMemoryHeapMaxSize";
            case 12:
                return "JvmMemoryHeapCommitted";
            case 3:
                return "JvmMemoryGCCall";
            case 11:
                return "JvmMemoryHeapUsed";
            case 2:
                return "JvmMemoryGCVerboseLevel";
            case 101: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 10:
                return "JvmMemoryHeapInitSize";
            case 1:
                return "JvmMemoryPendingFinalCount";
            case 100: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public boolean isTable(long arc) {
        switch((int)arc) {
            case 120:
                return true;
            case 110:
                return true;
            case 101:
                return true;
            case 100:
                return true;
            default:
                break;
        }
        return false;
    }
    public SnmpMibTable getTable(long arc) {
        switch((int)arc) {
            case 120:
                return tableJvmMemMgrPoolRelTable;
            case 110:
                return tableJvmMemPoolTable;
            case 101:
                return tableJvmMemGCTable;
            case 100:
                return tableJvmMemManagerTable;
        default:
            break;
        }
        return null;
    }
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
        tableJvmMemMgrPoolRelTable = createJvmMemMgrPoolRelTableMetaNode("JvmMemMgrPoolRelTable", "JvmMemory", mib, server);
        if ( tableJvmMemMgrPoolRelTable != null)  {
            tableJvmMemMgrPoolRelTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmMemMgrPoolRelTable", tableJvmMemMgrPoolRelTable);
        }
        tableJvmMemPoolTable = createJvmMemPoolTableMetaNode("JvmMemPoolTable", "JvmMemory", mib, server);
        if ( tableJvmMemPoolTable != null)  {
            tableJvmMemPoolTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmMemPoolTable", tableJvmMemPoolTable);
        }
        tableJvmMemGCTable = createJvmMemGCTableMetaNode("JvmMemGCTable", "JvmMemory", mib, server);
        if ( tableJvmMemGCTable != null)  {
            tableJvmMemGCTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmMemGCTable", tableJvmMemGCTable);
        }
        tableJvmMemManagerTable = createJvmMemManagerTableMetaNode("JvmMemManagerTable", "JvmMemory", mib, server);
        if ( tableJvmMemManagerTable != null)  {
            tableJvmMemManagerTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmMemManagerTable", tableJvmMemManagerTable);
        }
    }
    protected JvmMemMgrPoolRelTableMeta createJvmMemMgrPoolRelTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemMgrPoolRelTableMeta(mib, objectserver);
    }
    protected JvmMemPoolTableMeta createJvmMemPoolTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemPoolTableMeta(mib, objectserver);
    }
    protected JvmMemGCTableMeta createJvmMemGCTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemGCTableMeta(mib, objectserver);
    }
    protected JvmMemManagerTableMeta createJvmMemManagerTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmMemManagerTableMeta(mib, objectserver);
    }
    protected JvmMemoryMBean node;
    protected SnmpStandardObjectServer objectserver = null;
    protected JvmMemMgrPoolRelTableMeta tableJvmMemMgrPoolRelTable = null;
    protected JvmMemPoolTableMeta tableJvmMemPoolTable = null;
    protected JvmMemGCTableMeta tableJvmMemGCTable = null;
    protected JvmMemManagerTableMeta tableJvmMemManagerTable = null;
}
