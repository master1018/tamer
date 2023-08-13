public class SnmpPduTrap extends SnmpPduPacket {
    private static final long serialVersionUID = -3670886636491433011L;
    public SnmpOid        enterprise ;
    public SnmpIpAddress  agentAddr ;
    public int            genericTrap ;
    public int            specificTrap ;
    public long            timeStamp ;
    public SnmpPduTrap() {
        type = pduV1TrapPdu ;
        version = snmpVersionOne ;
    }
}
