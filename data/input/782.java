public class Base64URLSafe {
    protected static final char PAD_CHAR = '*';
    protected static final char LETTER_62 = '-';
    protected static final char LETTER_63 = '_';
    protected static final char[] encodeMap = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', LETTER_62, LETTER_63 };
    protected static byte[] decodeMap = new byte[256];
    static {
        for (int i = 0; i < 256; i++) decodeMap[i] = -1;
        for (int i = 'A'; i <= 'Z'; i++) decodeMap[i] = (byte) (i - 'A');
        for (int i = 'a'; i <= 'z'; i++) decodeMap[i] = (byte) (26 + i - 'a');
        for (int i = '0'; i <= '9'; i++) decodeMap[i] = (byte) (52 + i - '0');
        decodeMap[LETTER_62] = 62;
        decodeMap[LETTER_63] = 63;
    }
    public static String encode(byte[] raw) {
        int end = raw.length;
        int slop = end % 3;
        char[] buf = new char[(slop == 0) ? (4 * (end / 3)) : (4 * (end / 3 + 1))];
        int i = 0, j = 0;
        end = end - slop;
        while (i < end) {
            int block = ((raw[i++] & 0xff) << 16) | ((raw[i++] & 0xff) << 8) | (raw[i++] & 0xff);
            buf[j++] = encodeMap[(block >>> 18) & 0x3f];
            buf[j++] = encodeMap[(block >>> 12) & 0x3f];
            buf[j++] = encodeMap[(block >>> 6) & 0x3f];
            buf[j++] = encodeMap[(block & 0x3f)];
        }
        if (slop == 2) {
            int block = ((raw[i++] & 0xff) << 16) | ((raw[i++] & 0xff) << 8);
            buf[j++] = encodeMap[(block >>> 18) & 0x3f];
            buf[j++] = encodeMap[(block >>> 12) & 0x3f];
            buf[j++] = encodeMap[(block >>> 6) & 0x3f];
            buf[j] = PAD_CHAR;
        } else if (slop == 1) {
            int block = (raw[i++] & 0xff) << 16;
            buf[j++] = encodeMap[(block >>> 18) & 0x3f];
            buf[j++] = encodeMap[(block >>> 12) & 0x3f];
            buf[j++] = PAD_CHAR;
            buf[j] = PAD_CHAR;
        }
        return new String(buf);
    }
    public static byte[] decode(String base64String) throws InvalidFormatException {
        char[] base64 = base64String.toCharArray();
        int pad = 0;
        int max = base64.length;
        if (max == 0) return new byte[0];
        for (int i = max - 1; base64[i] == PAD_CHAR; i--) pad++;
        byte[] r = new byte[max * 6 / 8 - pad];
        if (pad > 0) max = max - 4;
        int ri = 0, i = 0;
        while (i < max) {
            int block = (getValue(base64[i++]) << 18) | (getValue(base64[i++]) << 12) | (getValue(base64[i++]) << 6) | (getValue(base64[i++]));
            r[ri++] = (byte) ((block >> 16) & 0xff);
            r[ri++] = (byte) ((block >> 8) & 0xff);
            r[ri++] = (byte) (block & 0xff);
        }
        if (pad == 2) {
            int block = (getValue(base64[i++]) << 18) | (getValue(base64[i++]) << 12);
            r[ri++] = (byte) ((block >> 16) & 0xff);
        } else if (pad == 1) {
            int block = (getValue(base64[i++]) << 18) | (getValue(base64[i++]) << 12) | (getValue(base64[i++]) << 6);
            r[ri++] = (byte) ((block >> 16) & 0xff);
            r[ri++] = (byte) ((block >> 8) & 0xff);
        }
        return r;
    }
    protected static int getValue(char c) throws InvalidFormatException {
        byte x = decodeMap[c];
        if (x == -1) {
            throw new InvalidFormatException("Bad base64 character of value " + (int) c + " found in decode");
        }
        return x;
    }
}
