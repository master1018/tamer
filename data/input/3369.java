public class SnmpPduFactoryBER implements SnmpPduFactory, Serializable {
   private static final long serialVersionUID = -3525318344000547635L;
    public SnmpPdu decodeSnmpPdu(SnmpMsg msg) throws SnmpStatusException {
        return msg.decodeSnmpPdu();
    }
    public SnmpMsg encodeSnmpPdu(SnmpPdu p, int maxDataLength)
        throws SnmpStatusException, SnmpTooBigException {
        switch(p.version) {
        case SnmpDefinitions.snmpVersionOne:
        case SnmpDefinitions.snmpVersionTwo: {
            SnmpMessage result = new SnmpMessage();
            result.encodeSnmpPdu((SnmpPduPacket) p, maxDataLength);
            return result;
        }
        case SnmpDefinitions.snmpVersionThree: {
            SnmpV3Message result = new SnmpV3Message();
            result.encodeSnmpPdu(p, maxDataLength);
            return result;
        }
        default:
            return null;
        }
    }
}
