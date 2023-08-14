public class JvmThreadingMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {
    public JvmThreadingMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(6);
            registerObject(5);
            registerObject(4);
            registerObject(3);
            registerObject(2);
            registerObject(1);
            registerObject(10);
            registerObject(7);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 6:
                return new SnmpInt(node.getJvmThreadCpuTimeMonitoring());
            case 5:
                return new SnmpInt(node.getJvmThreadContentionMonitoring());
            case 4:
                return new SnmpCounter64(node.getJvmThreadTotalStartedCount());
            case 3:
                return new SnmpCounter(node.getJvmThreadPeakCount());
            case 2:
                return new SnmpGauge(node.getJvmThreadDaemonCount());
            case 1:
                return new SnmpGauge(node.getJvmThreadCount());
            case 10: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 7:
                return new SnmpCounter64(node.getJvmThreadPeakCountReset());
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 6:
                if (x instanceof SnmpInt) {
                    try  {
                        node.setJvmThreadCpuTimeMonitoring( new EnumJvmThreadCpuTimeMonitoring (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                    return new SnmpInt(node.getJvmThreadCpuTimeMonitoring());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
            case 5:
                if (x instanceof SnmpInt) {
                    try  {
                        node.setJvmThreadContentionMonitoring( new EnumJvmThreadContentionMonitoring (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                    return new SnmpInt(node.getJvmThreadContentionMonitoring());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 7:
                if (x instanceof SnmpCounter64) {
                    node.setJvmThreadPeakCountReset(((SnmpCounter64)x).toLong());
                    return new SnmpCounter64(node.getJvmThreadPeakCountReset());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 6:
                if (x instanceof SnmpInt) {
                    try  {
                        node.checkJvmThreadCpuTimeMonitoring( new EnumJvmThreadCpuTimeMonitoring (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
                break;
            case 5:
                if (x instanceof SnmpInt) {
                    try  {
                        node.checkJvmThreadContentionMonitoring( new EnumJvmThreadContentionMonitoring (((SnmpInt)x).toInteger()));
                    } catch(IllegalArgumentException e)  {
                        throw new SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
                    }
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
                break;
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }
            case 7:
                if (x instanceof SnmpCounter64) {
                    node.checkJvmThreadPeakCountReset(((SnmpCounter64)x).toLong());
                } else {
                    throw new SnmpStatusException(SnmpStatusException.snmpRspWrongType);
                }
                break;
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmThreadingMBean var) {
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
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
            case 7:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean isReadable(long arc) {
        switch((int)arc) {
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
            case 7:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 4:
            case 7:
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
            case 6:
                return "JvmThreadCpuTimeMonitoring";
            case 5:
                return "JvmThreadContentionMonitoring";
            case 4:
                return "JvmThreadTotalStartedCount";
            case 3:
                return "JvmThreadPeakCount";
            case 2:
                return "JvmThreadDaemonCount";
            case 1:
                return "JvmThreadCount";
            case 10: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }
            case 7:
                return "JvmThreadPeakCountReset";
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public boolean isTable(long arc) {
        switch((int)arc) {
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }
    public SnmpMibTable getTable(long arc) {
        switch((int)arc) {
            case 10:
                return tableJvmThreadInstanceTable;
        default:
            break;
        }
        return null;
    }
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
        tableJvmThreadInstanceTable = createJvmThreadInstanceTableMetaNode("JvmThreadInstanceTable", "JvmThreading", mib, server);
        if ( tableJvmThreadInstanceTable != null)  {
            tableJvmThreadInstanceTable.registerEntryNode(mib,server);
            mib.registerTableMeta("JvmThreadInstanceTable", tableJvmThreadInstanceTable);
        }
    }
    protected JvmThreadInstanceTableMeta createJvmThreadInstanceTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new JvmThreadInstanceTableMeta(mib, objectserver);
    }
    protected JvmThreadingMBean node;
    protected SnmpStandardObjectServer objectserver = null;
    protected JvmThreadInstanceTableMeta tableJvmThreadInstanceTable = null;
}
