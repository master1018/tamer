public class SnmpPduBulk extends SnmpPduPacket
    implements SnmpPduBulkType {
    private static final long serialVersionUID = -7431306775883371046L;
    public int            nonRepeaters ;
    public int            maxRepetitions ;
    public SnmpPduBulk() {
        type = pduGetBulkRequestPdu ;
        version = snmpVersionTwo ;
    }
    public void setMaxRepetitions(int i) {
        maxRepetitions = i;
    }
    public void setNonRepeaters(int i) {
        nonRepeaters = i;
    }
    public int getMaxRepetitions() { return maxRepetitions; }
    public int getNonRepeaters() { return nonRepeaters; }
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
