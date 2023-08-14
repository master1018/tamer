public class NegTokenTargFields {
    public static byte[] nomech = {
        (byte)0xA1, (byte)0x0F, (byte)0x30, (byte)0x0D,
        (byte)0xA0, (byte)0x03, (byte)0x0A, (byte)0x01,
        (byte)0x02, (byte)0xA2, (byte)0x02, (byte)0x04,
        (byte)0x00, (byte)0xA3, (byte)0x02, (byte)0x04,
        (byte)0x00,
    };
    public static byte[] badorder = {
        (byte)0xA1, (byte)0x1E, (byte)0x30, (byte)0x1C,
        (byte)0xA1, (byte)0x0B, (byte)0x06, (byte)0x09,
        (byte)0x2A, (byte)0x86, (byte)0x48, (byte)0x86,
        (byte)0xF7, (byte)0x12, (byte)0x01, (byte)0x02,
        (byte)0x02, (byte)0xA0, (byte)0x03, (byte)0x0A,
        (byte)0x01, (byte)0x00, (byte)0xA2, (byte)0x03,
        (byte)0x04, (byte)0x01, (byte)0x00, (byte)0xA3,
        (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x00,
    };
    public static void main(String[] args) throws Exception {
        byte[] buf = (byte[])NegTokenTargFields.class.getField(args[0]).get(null);
        new NegTokenTarg(buf);
    }
}
