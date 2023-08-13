public class JvmThreadInstanceEntryMeta extends SnmpMibEntry
     implements Serializable, SnmpStandardMetaServer {
    public JvmThreadInstanceEntryMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        varList = new int[10];
        varList[0] = 9;
        varList[1] = 8;
        varList[2] = 7;
        varList[3] = 6;
        varList[4] = 5;
        varList[5] = 4;
        varList[6] = 3;
        varList[7] = 11;
        varList[8] = 2;
        varList[9] = 10;
        SnmpMibNode.sort(varList);
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 9:
                return new SnmpString(node.getJvmThreadInstName());
            case 8:
                return new SnmpCounter64(node.getJvmThreadInstCpuTimeNs());
            case 7:
                return new SnmpCounter64(node.getJvmThreadInstWaitTimeMs());
            case 6:
                return new SnmpCounter64(node.getJvmThreadInstWaitCount());
            case 5:
                return new SnmpCounter64(node.getJvmThreadInstBlockTimeMs());
            case 4:
                return new SnmpCounter64(node.getJvmThreadInstBlockCount());
            case 3:
                return new SnmpString(node.getJvmThreadInstState());
            case 11:
                return new SnmpOid(node.getJvmThreadInstLockOwnerPtr());
            case 2:
                return new SnmpCounter64(node.getJvmThreadInstId());
            case 10:
                return new SnmpString(node.getJvmThreadInstLockName());
            case 1:
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
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
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
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
            case 11:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmThreadInstanceEntryMBean var) {
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
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 11:
            case 2:
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 2:
                if (pduVersion==SnmpDefinitions.snmpVersionOne) return true;
                break;
            case 1:
                return true;
            default:
                break;
        }
        return super.skipVariable(var,data,pduVersion);
    }
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 9:
                return "JvmThreadInstName";
            case 8:
                return "JvmThreadInstCpuTimeNs";
            case 7:
                return "JvmThreadInstWaitTimeMs";
            case 6:
                return "JvmThreadInstWaitCount";
            case 5:
                return "JvmThreadInstBlockTimeMs";
            case 4:
                return "JvmThreadInstBlockCount";
            case 3:
                return "JvmThreadInstState";
            case 11:
                return "JvmThreadInstLockOwnerPtr";
            case 2:
                return "JvmThreadInstId";
            case 10:
                return "JvmThreadInstLockName";
            case 1:
                return "JvmThreadInstIndex";
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    protected JvmThreadInstanceEntryMBean node;
    protected SnmpStandardObjectServer objectserver = null;
}
