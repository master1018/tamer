final class SnmpMibRequestImpl implements SnmpMibRequest {
    public SnmpMibRequestImpl(SnmpEngine engine,
                              SnmpPdu reqPdu,
                              Vector<SnmpVarBind> vblist,
                              int protocolVersion,
                              Object userData,
                              String principal,
                              int securityLevel,
                              int securityModel,
                              byte[] contextName,
                              byte[] accessContextName) {
        varbinds   = vblist;
        version    = protocolVersion;
        data       = userData;
        this.reqPdu = reqPdu;
        this.engine = engine;
        this.principal = principal;
        this.securityLevel = securityLevel;
        this.securityModel = securityModel;
        this.contextName = contextName;
        this.accessContextName = accessContextName;
    }
    public SnmpEngine getEngine() {
        return engine;
    }
    public String getPrincipal() {
        return principal;
    }
    public int getSecurityLevel() {
        return securityLevel;
    }
    public int getSecurityModel() {
        return securityModel;
    }
    public byte[] getContextName() {
        return contextName;
    }
    public byte[] getAccessContextName() {
        return accessContextName;
    }
    public final SnmpPdu getPdu() {
        return reqPdu;
    }
    public final Enumeration getElements()  {return varbinds.elements();}
    public final Vector<SnmpVarBind> getSubList()  {return varbinds;}
    public final int getSize()  {
        if (varbinds == null) return 0;
        return varbinds.size();
    }
    public final int         getVersion()  {return version;}
    public final int         getRequestPduVersion()  {return reqPdu.version;}
    public final Object      getUserData() {return data;}
    public final int getVarIndex(SnmpVarBind varbind) {
        return varbinds.indexOf(varbind);
    }
    public void addVarBind(SnmpVarBind varbind) {
        varbinds.addElement(varbind);
    }
    final void setRequestTree(SnmpRequestTree tree) {this.tree = tree;}
    final SnmpRequestTree getRequestTree() {return tree;}
    final Vector getVarbinds() {return varbinds;}
    private Vector<SnmpVarBind> varbinds;
    private int    version;
    private Object data;
    private SnmpPdu reqPdu = null;
    private SnmpRequestTree tree = null;
    private SnmpEngine engine = null;
    private String principal = null;
    private int securityLevel = -1;
    private int securityModel = -1;
    private byte[] contextName = null;
    private byte[] accessContextName = null;
}
