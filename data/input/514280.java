public final class SmsEnvelope{
    static public final int MESSAGE_TYPE_POINT_TO_POINT   = 0x00;
    static public final int MESSAGE_TYPE_BROADCAST        = 0x01;
    static public final int MESSAGE_TYPE_ACKNOWLEDGE      = 0x02;
    static public final int TELESERVICE_NOT_SET           = 0x0000;
    static public final int TELESERVICE_WMT               = 0x1002;
    static public final int TELESERVICE_VMN               = 0x1003;
    static public final int TELESERVICE_WAP               = 0x1004;
    static public final int TELESERVICE_WEMT              = 0x1005;
    static public final int TELESERVICE_MWI               = 0x40000;
    static public final int SMS_BEARER_DATA_MAX = 255;
    public int messageType;
    public int teleService = TELESERVICE_NOT_SET;
    public int serviceCategory;
    public CdmaSmsAddress origAddress;
    public CdmaSmsAddress destAddress;
    public int bearerReply;
    public byte replySeqNo;
    public byte errorClass;
    public byte causeCode;
    public byte[] bearerData;
    public SmsEnvelope() {
    }
}
