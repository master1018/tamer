public abstract class SnmpPdu implements SnmpDefinitions, Serializable {
    public int type=0 ;
    public int version=0 ;
    public SnmpVarBind[] varBindList ;
    public int requestId=0 ;
    public InetAddress address ;
    public int port=0 ;
    public static String pduTypeToString(int cmd) {
        switch (cmd) {
        case pduGetRequestPdu :
            return "SnmpGet" ;
        case pduGetNextRequestPdu :
            return "SnmpGetNext" ;
        case pduWalkRequest :
            return "SnmpWalk(*)" ;
        case pduSetRequestPdu :
            return "SnmpSet" ;
        case pduGetResponsePdu :
            return "SnmpResponse" ;
        case pduV1TrapPdu :
            return "SnmpV1Trap" ;
        case pduV2TrapPdu :
            return "SnmpV2Trap" ;
        case pduGetBulkRequestPdu :
            return "SnmpGetBulk" ;
        case pduInformRequestPdu :
            return "SnmpInform" ;
        }
        return "Unknown Command = " + cmd ;
    }
}
