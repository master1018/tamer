public class JvmMemGCEntryMeta extends SnmpMibEntry
     implements Serializable, SnmpStandardMetaServer {
    public JvmMemGCEntryMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        varList = new int[2];
        varList[0] = 3;
        varList[1] = 2;
        SnmpMibNode.sort(varList);
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 3:
                return new SnmpCounter64(node.getJvmMemGCTimeMs());
            case 2:
                return new SnmpCounter64(node.getJvmMemGCCount());
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmMemGCEntryMBean var) {
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
            case 3:
            case 2:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean isReadable(long arc) {
        switch((int)arc) {
            case 3:
            case 2:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 3:
            case 2:
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
            case 3:
                return "JvmMemGCTimeMs";
            case 2:
                return "JvmMemGCCount";
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    protected JvmMemGCEntryMBean node;
    protected SnmpStandardObjectServer objectserver = null;
}
