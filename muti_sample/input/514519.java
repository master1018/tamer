public class UserData {
    public static final int ENCODING_OCTET                      = 0x00;
    public static final int ENCODING_IS91_EXTENDED_PROTOCOL     = 0x01;
    public static final int ENCODING_7BIT_ASCII                 = 0x02;
    public static final int ENCODING_IA5                        = 0x03;
    public static final int ENCODING_UNICODE_16                 = 0x04;
    public static final int ENCODING_LATIN                      = 0x08;
    public static final int ENCODING_GSM_7BIT_ALPHABET          = 0x09;
    public static final int ENCODING_GSM_DCS                    = 0x0A;
    public static final int IS91_MSG_TYPE_VOICEMAIL_STATUS   = 0x82;
    public static final int IS91_MSG_TYPE_SHORT_MESSAGE_FULL = 0x83;
    public static final int IS91_MSG_TYPE_CLI                = 0x84;
    public static final int IS91_MSG_TYPE_SHORT_MESSAGE      = 0x85;
    public static final char[] ASCII_MAP = {
        ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
        '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~'};
    static final byte UNENCODABLE_7_BIT_CHAR = 0x20;
    public static final int PRINTABLE_ASCII_MIN_INDEX = 0x20;
    public static final int ASCII_NL_INDEX = 0x0A;
    public static final int ASCII_CR_INDEX = 0x0D;
    public static final SparseIntArray charToAscii = new SparseIntArray();
    static {
        for (int i = 0; i < ASCII_MAP.length; i++) {
            charToAscii.put(ASCII_MAP[i], PRINTABLE_ASCII_MIN_INDEX + i);
        }
        charToAscii.put('\n', ASCII_NL_INDEX);
        charToAscii.put('\r', ASCII_CR_INDEX);
    }
    public static byte[] stringToAscii(String str) {
        int len = str.length();
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            int charCode = charToAscii.get(str.charAt(i), -1);
            if (charCode == -1) return null;
            result[i] = (byte)charCode;
        }
        return result;
    }
    public static final int ASCII_MAP_BASE_INDEX = 0x20;
    public static final int ASCII_MAP_MAX_INDEX = ASCII_MAP_BASE_INDEX + ASCII_MAP.length - 1;
    public SmsHeader userDataHeader;
    public int msgEncoding;
    public boolean msgEncodingSet = false;
    public int msgType;
    public int paddingBits;
    public int numFields;
    public byte[] payload;
    public String payloadStr;
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserData ");
        builder.append("{ msgEncoding=" + (msgEncodingSet ? msgEncoding : "unset"));
        builder.append(", msgType=" + msgType);
        builder.append(", paddingBits=" + paddingBits);
        builder.append(", numFields=" + numFields);
        builder.append(", userDataHeader=" + userDataHeader);
        builder.append(", payload='" + HexDump.toHexString(payload) + "'");
        builder.append(", payloadStr='" + payloadStr + "'");
        builder.append(" }");
        return builder.toString();
    }
}
