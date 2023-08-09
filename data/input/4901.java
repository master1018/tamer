public class SnmpV3Message extends SnmpMsg {
    public int msgId = 0;
    public int msgMaxSize = 0;
    public byte msgFlags = 0;
    public int msgSecurityModel = 0;
    public byte[] msgSecurityParameters = null;
    public byte[] contextEngineId = null;
    public byte[] contextName = null;
    public byte[] encryptedPdu = null;
    public SnmpV3Message() {
    }
    public int encodeMessage(byte[] outputBytes)
        throws SnmpTooBigException {
        int encodingLength = 0;
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpV3Message.class.getName(),
                    "encodeMessage",
                    "Can't encode directly V3Message! Need a SecuritySubSystem");
        }
        throw new IllegalArgumentException("Can't encode");
    }
    public void decodeMessage(byte[] inputBytes, int byteCount)
        throws SnmpStatusException {
        try {
            BerDecoder bdec = new BerDecoder(inputBytes);
            bdec.openSequence();
            version = bdec.fetchInteger();
            bdec.openSequence();
            msgId = bdec.fetchInteger();
            msgMaxSize = bdec.fetchInteger();
            msgFlags = bdec.fetchOctetString()[0];
            msgSecurityModel =bdec.fetchInteger();
            bdec.closeSequence();
            msgSecurityParameters = bdec.fetchOctetString();
            if( (msgFlags & SnmpDefinitions.privMask) == 0 ) {
                bdec.openSequence();
                contextEngineId = bdec.fetchOctetString();
                contextName = bdec.fetchOctetString();
                data = bdec.fetchAny();
                dataLength = data.length;
                bdec.closeSequence();
            }
            else {
                encryptedPdu = bdec.fetchOctetString();
            }
            bdec.closeSequence() ;
        }
        catch(BerException x) {
            x.printStackTrace();
            throw new SnmpStatusException("Invalid encoding") ;
        }
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            final StringBuilder strb = new StringBuilder()
            .append("Unmarshalled message : \n")
            .append("version : ").append(version)
            .append("\n")
            .append("msgId : ").append(msgId)
            .append("\n")
            .append("msgMaxSize : ").append(msgMaxSize)
            .append("\n")
            .append("msgFlags : ").append(msgFlags)
            .append("\n")
            .append("msgSecurityModel : ").append(msgSecurityModel)
            .append("\n")
            .append("contextEngineId : ").append(contextEngineId == null ? null :
                SnmpEngineId.createEngineId(contextEngineId))
            .append("\n")
            .append("contextName : ").append(contextName)
            .append("\n")
            .append("data : ").append(data)
            .append("\n")
            .append("dat len : ").append((data == null) ? 0 : data.length)
            .append("\n")
            .append("encryptedPdu : ").append(encryptedPdu)
            .append("\n");
            SNMP_LOGGER.logp(Level.FINER, SnmpV3Message.class.getName(),
                    "decodeMessage", strb.toString());
        }
    }
    public int getRequestId(byte[] data) throws SnmpStatusException {
        BerDecoder bdec = null;
        int msgId = 0;
        try {
            bdec = new BerDecoder(data);
            bdec.openSequence();
            bdec.fetchInteger();
            bdec.openSequence();
            msgId = bdec.fetchInteger();
        }catch(BerException x) {
            throw new SnmpStatusException("Invalid encoding") ;
        }
        try {
            bdec.closeSequence();
        }
        catch(BerException x) {
        }
        return msgId;
    }
    public void encodeSnmpPdu(SnmpPdu p,
                              int maxDataLength)
        throws SnmpStatusException, SnmpTooBigException {
        SnmpScopedPduPacket pdu = (SnmpScopedPduPacket) p;
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            final StringBuilder strb = new StringBuilder()
            .append("PDU to marshall: \n")
            .append("security parameters : ").append(pdu.securityParameters)
            .append("\n")
            .append("type : ").append(pdu.type)
            .append("\n")
            .append("version : ").append(pdu.version)
            .append("\n")
            .append("requestId : ").append(pdu.requestId)
            .append("\n")
            .append("msgId : ").append(pdu.msgId)
            .append("\n")
            .append("msgMaxSize : ").append(pdu.msgMaxSize)
            .append("\n")
            .append("msgFlags : ").append(pdu.msgFlags)
            .append("\n")
            .append("msgSecurityModel : ").append(pdu.msgSecurityModel)
            .append("\n")
            .append("contextEngineId : ").append(pdu.contextEngineId)
            .append("\n")
            .append("contextName : ").append(pdu.contextName)
            .append("\n");
            SNMP_LOGGER.logp(Level.FINER, SnmpV3Message.class.getName(),
                    "encodeSnmpPdu", strb.toString());
        }
        version = pdu.version;
        address = pdu.address;
        port = pdu.port;
        msgId = pdu.msgId;
        msgMaxSize = pdu.msgMaxSize;
        msgFlags = pdu.msgFlags;
        msgSecurityModel = pdu.msgSecurityModel;
        contextEngineId = pdu.contextEngineId;
        contextName = pdu.contextName;
        securityParameters = pdu.securityParameters;
        data = new byte[maxDataLength];
        try {
            BerEncoder benc = new BerEncoder(data) ;
            benc.openSequence() ;
            encodeVarBindList(benc, pdu.varBindList) ;
            switch(pdu.type) {
            case pduGetRequestPdu :
            case pduGetNextRequestPdu :
            case pduInformRequestPdu :
            case pduGetResponsePdu :
            case pduSetRequestPdu :
            case pduV2TrapPdu :
            case pduReportPdu :
                SnmpPduRequestType reqPdu = (SnmpPduRequestType) pdu;
                benc.putInteger(reqPdu.getErrorIndex());
                benc.putInteger(reqPdu.getErrorStatus());
                benc.putInteger(pdu.requestId);
                break;
            case pduGetBulkRequestPdu :
                SnmpPduBulkType bulkPdu = (SnmpPduBulkType) pdu;
                benc.putInteger(bulkPdu.getMaxRepetitions());
                benc.putInteger(bulkPdu.getNonRepeaters());
                benc.putInteger(pdu.requestId);
                break ;
            default:
                throw new SnmpStatusException("Invalid pdu type " + String.valueOf(pdu.type)) ;
            }
            benc.closeSequence(pdu.type) ;
            dataLength = benc.trim() ;
        }
        catch(ArrayIndexOutOfBoundsException x) {
            throw new SnmpTooBigException() ;
        }
    }
    public SnmpPdu decodeSnmpPdu()
        throws SnmpStatusException {
        SnmpScopedPduPacket pdu = null;
        BerDecoder bdec = new BerDecoder(data) ;
        try {
            int type = bdec.getTag() ;
            bdec.openSequence(type) ;
            switch(type) {
            case pduGetRequestPdu :
            case pduGetNextRequestPdu :
            case pduInformRequestPdu :
            case pduGetResponsePdu :
            case pduSetRequestPdu :
            case pduV2TrapPdu :
            case pduReportPdu :
                SnmpScopedPduRequest reqPdu = new SnmpScopedPduRequest() ;
                reqPdu.requestId = bdec.fetchInteger() ;
                reqPdu.setErrorStatus(bdec.fetchInteger());
                reqPdu.setErrorIndex(bdec.fetchInteger());
                pdu = reqPdu ;
                break ;
            case pduGetBulkRequestPdu :
                SnmpScopedPduBulk bulkPdu = new SnmpScopedPduBulk() ;
                bulkPdu.requestId = bdec.fetchInteger() ;
                bulkPdu.setNonRepeaters(bdec.fetchInteger());
                bulkPdu.setMaxRepetitions(bdec.fetchInteger());
                pdu = bulkPdu ;
                break ;
            default:
                throw new SnmpStatusException(snmpRspWrongEncoding) ;
            }
            pdu.type = type;
            pdu.varBindList = decodeVarBindList(bdec);
            bdec.closeSequence() ;
        } catch(BerException e) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, SnmpV3Message.class.getName(),
                        "decodeSnmpPdu", "BerException", e);
            }
            throw new SnmpStatusException(snmpRspWrongEncoding);
        }
        pdu.address = address;
        pdu.port = port;
        pdu.msgFlags = msgFlags;
        pdu.version = version;
        pdu.msgId = msgId;
        pdu.msgMaxSize = msgMaxSize;
        pdu.msgSecurityModel = msgSecurityModel;
        pdu.contextEngineId = contextEngineId;
        pdu.contextName = contextName;
        pdu.securityParameters = securityParameters;
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            final StringBuilder strb = new StringBuilder()
            .append("Unmarshalled PDU : \n")
            .append("type : ").append(pdu.type)
            .append("\n")
            .append("version : ").append(pdu.version)
            .append("\n")
            .append("requestId : ").append(pdu.requestId)
            .append("\n")
            .append("msgId : ").append(pdu.msgId)
            .append("\n")
            .append("msgMaxSize : ").append(pdu.msgMaxSize)
            .append("\n")
            .append("msgFlags : ").append(pdu.msgFlags)
            .append("\n")
            .append("msgSecurityModel : ").append(pdu.msgSecurityModel)
            .append("\n")
            .append("contextEngineId : ").append(pdu.contextEngineId)
            .append("\n")
            .append("contextName : ").append(pdu.contextName)
            .append("\n");
            SNMP_LOGGER.logp(Level.FINER, SnmpV3Message.class.getName(),
                    "decodeSnmpPdu", strb.toString());
        }
        return pdu ;
    }
    public String printMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("msgId : " + msgId + "\n");
        sb.append("msgMaxSize : " + msgMaxSize + "\n");
        sb.append("msgFlags : " + msgFlags + "\n");
        sb.append("msgSecurityModel : " + msgSecurityModel + "\n");
        if (contextEngineId == null) {
            sb.append("contextEngineId : null");
        }
        else {
            sb.append("contextEngineId : {\n");
            sb.append(dumpHexBuffer(contextEngineId,
                                    0,
                                    contextEngineId.length));
            sb.append("\n}\n");
        }
        if (contextName == null) {
            sb.append("contextName : null");
        }
        else {
            sb.append("contextName : {\n");
            sb.append(dumpHexBuffer(contextName,
                                    0,
                                    contextName.length));
            sb.append("\n}\n");
        }
        return sb.append(super.printMessage()).toString();
    }
}
