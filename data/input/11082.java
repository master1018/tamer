public class SnmpPduRequest extends SnmpPduPacket
    implements SnmpPduRequestType {
    private static final long serialVersionUID = 2218754017025258979L;
    public int errorStatus=0 ;
    public int errorIndex=0 ;
    public void setErrorIndex(int i) {
        errorIndex = i;
    }
    public void setErrorStatus(int i) {
        errorStatus = i;
    }
    public int getErrorIndex() { return errorIndex; }
    public int getErrorStatus() { return errorStatus; }
    public SnmpPdu getResponsePdu() {
        SnmpPduRequest result = new SnmpPduRequest();
        result.address = address;
        result.port = port;
        result.version = version;
        result.community = community;
        result.type = SnmpDefinitions.pduGetResponsePdu;
        result.requestId = requestId;
        result.errorStatus = SnmpDefinitions.snmpRspNoError;
        result.errorIndex = 0;
        return result;
    }
}
