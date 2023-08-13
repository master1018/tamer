public class SnmpSecurityException extends Exception {
    private static final long serialVersionUID = 5574448147432833480L;
    public SnmpVarBind[] list = null;
    public int status = SnmpDefinitions.snmpReqUnknownError;
    public SnmpSecurityParameters params = null;
    public byte[] contextEngineId = null;
    public byte[] contextName = null;
    public byte flags = (byte) SnmpDefinitions.noAuthNoPriv;
    public SnmpSecurityException(String msg) {
        super(msg);
    }
}
