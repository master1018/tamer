public class JvmCompilationMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {
    public JvmCompilationMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
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
            case 3:
                return new SnmpInt(node.getJvmJITCompilerTimeMonitoring());
            case 2:
                return new SnmpCounter64(node.getJvmJITCompilerTimeMs());
            case 1:
                return new SnmpString(node.getJvmJITCompilerName());
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
    protected void setInstance(JvmCompilationMBean var) {
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
            case 1:
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
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }
    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
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
                return "JvmJITCompilerTimeMonitoring";
            case 2:
                return "JvmJITCompilerTimeMs";
            case 1:
                return "JvmJITCompilerName";
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
    protected JvmCompilationMBean node;
    protected SnmpStandardObjectServer objectserver = null;
}
