public class SnmpErrorHandlerAgent extends SnmpMibAgent
        implements Serializable {
    private static final long serialVersionUID = 7751082923508885650L;
    public SnmpErrorHandlerAgent() {}
    public void init() throws IllegalAccessException {
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        return name;
    }
    public long[] getRootOid() {
        return null;
    }
    public void get(SnmpMibRequest inRequest) throws SnmpStatusException {
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "get", "Get in Exception");
        if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        Enumeration l = inRequest.getElements();
        while(l.hasMoreElements()) {
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
            varbind.setNoSuchObject();
        }
    }
    public void check(SnmpMibRequest inRequest) throws SnmpStatusException {
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "check", "Check in Exception");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }
    public void set(SnmpMibRequest inRequest) throws SnmpStatusException {
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "set", "Set in Exception, CANNOT be called");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }
    public void getNext(SnmpMibRequest inRequest) throws SnmpStatusException {
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "getNext", "GetNext in Exception");
        if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        Enumeration l = inRequest.getElements();
        while(l.hasMoreElements()) {
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
            varbind.setEndOfMibView();
        }
    }
    public void getBulk(SnmpMibRequest inRequest, int nonRepeat, int maxRepeat)
        throws SnmpStatusException {
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "getBulk", "GetBulk in Exception");
        if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr, 0);
        Enumeration l = inRequest.getElements();
        while(l.hasMoreElements()) {
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
            varbind.setEndOfMibView();
        }
    }
}
