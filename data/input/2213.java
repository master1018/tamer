public class JvmOSMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {
    public JvmOSMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(4);
            registerObject(3);
            registerObject(2);
            registerObject(1);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 4:
                return new SnmpInt(node.getJvmOSProcessorCount());
            case 3:
                return new SnmpString(node.getJvmOSVersion());
            case 2:
                return new SnmpString(node.getJvmOSArch());
            case 1:
                return new SnmpString(node.getJvmOSName());
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
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
            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }
    protected void setInstance(JvmOSMBean var) {
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
            case 4:
            case 3:
            case 2:
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean isReadable(long arc) {
        switch((int)arc) {
            case 4:
            case 3:
            case 2:
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        return super.skipVariable(var,data,pduVersion);
    }
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 4:
                return "JvmOSProcessorCount";
            case 3:
                return "JvmOSVersion";
            case 2:
                return "JvmOSArch";
            case 1:
                return "JvmOSName";
            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    public boolean isTable(long arc) {
        switch((int)arc) {
            default:
                break;
        }
        return false;
    }
    public SnmpMibTable getTable(long arc) {
        return null;
    }
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
    }
    protected JvmOSMBean node;
    protected SnmpStandardObjectServer objectserver = null;
}
