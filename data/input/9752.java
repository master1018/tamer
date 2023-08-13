public class TransportConstants {
    public static final int Magic = 0x4a524d49;
    public static final short Version = 2;
    public static final byte StreamProtocol = 0x4b;
    public static final byte SingleOpProtocol = 0x4c;
    public static final byte MultiplexProtocol = 0x4d;
    public static final byte ProtocolAck = 0x4e;
    public static final byte ProtocolNack = 0x4f;
    public static final byte Call = 0x50;
    public static final byte Return = 0x51;
    public static final byte Ping = 0x52;
    public static final byte PingAck = 0x53;
    public static final byte DGCAck = 0x54;
    public static final byte NormalReturn = 0x01;
    public static final byte ExceptionalReturn = 0x02;
}
