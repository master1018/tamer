public class GsmAlphabet {
    static final String LOG_TAG = "GSM";
    public static final byte GSM_EXTENDED_ESCAPE = 0x1B;
    public static int
    charToGsm(char c) {
        try {
            return charToGsm(c, false);
        } catch (EncodeException ex) {
            return sGsmSpaceChar;
        }
    }
    public static int
    charToGsm(char c, boolean throwException) throws EncodeException {
        int ret;
        ret = charToGsm.get(c, -1);
        if (ret == -1) {
            ret = charToGsmExtended.get(c, -1);
            if (ret == -1) {
                if (throwException) {
                    throw new EncodeException(c);
                } else {
                    return sGsmSpaceChar;
                }
            } else {
                return GSM_EXTENDED_ESCAPE;
            }
        }
        return ret;
    }
    public static int
    charToGsmExtended(char c) {
        int ret;
        ret = charToGsmExtended.get(c, -1);
        if (ret == -1) {
            return sGsmSpaceChar;
        }
        return ret;
    }
    public static char
    gsmToChar(int gsmChar) {
        return (char)gsmToChar.get(gsmChar, ' ');
    }
    public static char
    gsmExtendedToChar(int gsmChar) {
        int ret;
        ret = gsmExtendedToChar.get(gsmChar, -1);
        if (ret == -1) {
            return ' ';
        }
        return (char)ret;
    }
    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header)
            throws EncodeException {
        if (header == null || header.length == 0) {
            return stringToGsm7BitPacked(data);
        }
        int headerBits = (header.length + 1) * 8;
        int headerSeptets = (headerBits + 6) / 7;
        byte[] ret = stringToGsm7BitPacked(data, headerSeptets, true);
        ret[1] = (byte)header.length;
        System.arraycopy(header, 0, ret, 2, header.length);
        return ret;
    }
    public static byte[] stringToGsm7BitPacked(String data)
            throws EncodeException {
        return stringToGsm7BitPacked(data, 0, true);
    }
    public static byte[] stringToGsm7BitPacked(String data, int startingSeptetOffset,
            boolean throwException) throws EncodeException {
        int dataLen = data.length();
        int septetCount = countGsmSeptets(data, throwException) + startingSeptetOffset;
        if (septetCount > 255) {
            throw new EncodeException("Payload cannot exceed 255 septets");
        }
        int byteCount = ((septetCount * 7) + 7) / 8;
        byte[] ret = new byte[byteCount + 1];  
        for (int i = 0, septets = startingSeptetOffset, bitOffset = startingSeptetOffset * 7;
                 i < dataLen && septets < septetCount;
                 i++, bitOffset += 7) {
            char c = data.charAt(i);
            int v = GsmAlphabet.charToGsm(c, throwException);
            if (v == GSM_EXTENDED_ESCAPE) {
                v = GsmAlphabet.charToGsmExtended(c);  
                packSmsChar(ret, bitOffset, GSM_EXTENDED_ESCAPE);
                bitOffset += 7;
                septets++;
            }
            packSmsChar(ret, bitOffset, v);
            septets++;
        }
        ret[0] = (byte) (septetCount);  
        return ret;
    }
    private static void
    packSmsChar(byte[] packedChars, int bitOffset, int value) {
        int byteOffset = bitOffset / 8;
        int shift = bitOffset % 8;
        packedChars[++byteOffset] |= value << shift;
        if (shift > 1) {
            packedChars[++byteOffset] = (byte)(value >> (8 - shift));
        }
    }
    public static String gsm7BitPackedToString(byte[] pdu, int offset,
            int lengthSeptets) {
        return gsm7BitPackedToString(pdu, offset, lengthSeptets, 0);
    }
    public static String gsm7BitPackedToString(byte[] pdu, int offset,
            int lengthSeptets, int numPaddingBits) {
        StringBuilder ret = new StringBuilder(lengthSeptets);
        boolean prevCharWasEscape;
        try {
            prevCharWasEscape = false;
            for (int i = 0 ; i < lengthSeptets ; i++) {
                int bitOffset = (7 * i) + numPaddingBits;
                int byteOffset = bitOffset / 8;
                int shift = bitOffset % 8;
                int gsmVal;
                gsmVal = (0x7f & (pdu[offset + byteOffset] >> shift));
                if (shift > 1) {
                    gsmVal &= 0x7f >> (shift - 1);
                    gsmVal |= 0x7f & (pdu[offset + byteOffset + 1] << (8 - shift));
                }
                if (prevCharWasEscape) {
                    ret.append(GsmAlphabet.gsmExtendedToChar(gsmVal));
                    prevCharWasEscape = false;
                } else if (gsmVal == GSM_EXTENDED_ESCAPE) {
                    prevCharWasEscape = true;
                } else {
                    ret.append(GsmAlphabet.gsmToChar(gsmVal));
                }
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Error GSM 7 bit packed: ", ex);
            return null;
        }
        return ret.toString();
    }
    public static String
    gsm8BitUnpackedToString(byte[] data, int offset, int length) {
        boolean prevWasEscape;
        StringBuilder ret = new StringBuilder(length);
        prevWasEscape = false;
        for (int i = offset ; i < offset + length ; i++) {
            int c = data[i] & 0xff;
            if (c == 0xff) {
                break;
            } else if (c == GSM_EXTENDED_ESCAPE) {
                if (prevWasEscape) {
                    ret.append(' ');
                    prevWasEscape = false;
                } else {
                    prevWasEscape = true;
                }
            } else {
                if (prevWasEscape) {
                    ret.append((char)gsmExtendedToChar.get(c, ' '));
                } else {
                    ret.append((char)gsmToChar.get(c, ' '));
                }
                prevWasEscape = false;
            }
        }
        return ret.toString();
    }
    public static byte[]
    stringToGsm8BitPacked(String s) {
        byte[] ret;
        int septets = 0;
        septets = countGsmSeptets(s);
        ret = new byte[septets];
        stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
        return ret;
    }
    public static void
    stringToGsm8BitUnpackedField(String s, byte dest[], int offset, int length) {
        int outByteIndex = offset;
        for (int i = 0, sz = s.length()
                ; i < sz && (outByteIndex - offset) < length
                ; i++
        ) {
            char c = s.charAt(i);
            int v = GsmAlphabet.charToGsm(c);
            if (v == GSM_EXTENDED_ESCAPE) {
                if (! (outByteIndex + 1 - offset < length)) {
                    break;
                }
                dest[outByteIndex++] = GSM_EXTENDED_ESCAPE;
                v = GsmAlphabet.charToGsmExtended(c);
            }
            dest[outByteIndex++] = (byte)v;
        }
        while((outByteIndex - offset) < length) {
            dest[outByteIndex++] = (byte)0xff;
        }
    }
    public static int
    countGsmSeptets(char c) {
        try {
            return countGsmSeptets(c, false);
        } catch (EncodeException ex) {
            return 0;
        }
    }
    public static int
    countGsmSeptets(char c, boolean throwsException) throws EncodeException {
        if (charToGsm.get(c, -1) != -1) {
            return 1;
        }
        if (charToGsmExtended.get(c, -1) != -1) {
            return 2;
        }
        if (throwsException) {
            throw new EncodeException(c);
        } else {
            return 1;
        }
    }
    public static int
    countGsmSeptets(CharSequence s) {
        try {
            return countGsmSeptets(s, false);
        } catch (EncodeException ex) {
            return 0;
        }
    }
    public static int
    countGsmSeptets(CharSequence s, boolean throwsException) throws EncodeException {
        int charIndex = 0;
        int sz = s.length();
        int count = 0;
        while (charIndex < sz) {
            count += countGsmSeptets(s.charAt(charIndex), throwsException);
            charIndex++;
        }
        return count;
    }
    public static int
    findGsmSeptetLimitIndex(String s, int start, int limit) {
        int accumulator = 0;
        int size = s.length();
        for (int i = start; i < size; i++) {
            accumulator += countGsmSeptets(s.charAt(i));
            if (accumulator > limit) {
                return i;
            }
        }
        return size;
    }
    private static int sGsmSpaceChar;
    private static final SparseIntArray charToGsm = new SparseIntArray();
    private static final SparseIntArray gsmToChar = new SparseIntArray();
    private static final SparseIntArray charToGsmExtended = new SparseIntArray();
    private static final SparseIntArray gsmExtendedToChar = new SparseIntArray();
    static {
        int i = 0;
        charToGsm.put('@', i++);
        charToGsm.put('\u00a3', i++);
        charToGsm.put('$', i++);
        charToGsm.put('\u00a5', i++);
        charToGsm.put('\u00e8', i++);
        charToGsm.put('\u00e9', i++);
        charToGsm.put('\u00f9', i++);
        charToGsm.put('\u00ec', i++);
        charToGsm.put('\u00f2', i++);
        charToGsm.put('\u00c7', i++);
        charToGsm.put('\n', i++);
        charToGsm.put('\u00d8', i++);
        charToGsm.put('\u00f8', i++);
        charToGsm.put('\r', i++);
        charToGsm.put('\u00c5', i++);
        charToGsm.put('\u00e5', i++);
        charToGsm.put('\u0394', i++);
        charToGsm.put('_', i++);
        charToGsm.put('\u03a6', i++);
        charToGsm.put('\u0393', i++);
        charToGsm.put('\u039b', i++);
        charToGsm.put('\u03a9', i++);
        charToGsm.put('\u03a0', i++);
        charToGsm.put('\u03a8', i++);
        charToGsm.put('\u03a3', i++);
        charToGsm.put('\u0398', i++);
        charToGsm.put('\u039e', i++);
        charToGsm.put('\uffff', i++);
        charToGsm.put('\u00c6', i++);
        charToGsm.put('\u00e6', i++);
        charToGsm.put('\u00df', i++);
        charToGsm.put('\u00c9', i++);
        charToGsm.put(' ', i++);
        charToGsm.put('!', i++);
        charToGsm.put('"', i++);
        charToGsm.put('#', i++);
        charToGsm.put('\u00a4', i++);
        charToGsm.put('%', i++);
        charToGsm.put('&', i++);
        charToGsm.put('\'', i++);
        charToGsm.put('(', i++);
        charToGsm.put(')', i++);
        charToGsm.put('*', i++);
        charToGsm.put('+', i++);
        charToGsm.put(',', i++);
        charToGsm.put('-', i++);
        charToGsm.put('.', i++);
        charToGsm.put('/', i++);
        charToGsm.put('0', i++);
        charToGsm.put('1', i++);
        charToGsm.put('2', i++);
        charToGsm.put('3', i++);
        charToGsm.put('4', i++);
        charToGsm.put('5', i++);
        charToGsm.put('6', i++);
        charToGsm.put('7', i++);
        charToGsm.put('8', i++);
        charToGsm.put('9', i++);
        charToGsm.put(':', i++);
        charToGsm.put(';', i++);
        charToGsm.put('<', i++);
        charToGsm.put('=', i++);
        charToGsm.put('>', i++);
        charToGsm.put('?', i++);
        charToGsm.put('\u00a1', i++);
        charToGsm.put('A', i++);
        charToGsm.put('B', i++);
        charToGsm.put('C', i++);
        charToGsm.put('D', i++);
        charToGsm.put('E', i++);
        charToGsm.put('F', i++);
        charToGsm.put('G', i++);
        charToGsm.put('H', i++);
        charToGsm.put('I', i++);
        charToGsm.put('J', i++);
        charToGsm.put('K', i++);
        charToGsm.put('L', i++);
        charToGsm.put('M', i++);
        charToGsm.put('N', i++);
        charToGsm.put('O', i++);
        charToGsm.put('P', i++);
        charToGsm.put('Q', i++);
        charToGsm.put('R', i++);
        charToGsm.put('S', i++);
        charToGsm.put('T', i++);
        charToGsm.put('U', i++);
        charToGsm.put('V', i++);
        charToGsm.put('W', i++);
        charToGsm.put('X', i++);
        charToGsm.put('Y', i++);
        charToGsm.put('Z', i++);
        charToGsm.put('\u00c4', i++);
        charToGsm.put('\u00d6', i++);
        charToGsm.put('\u00d1', i++);
        charToGsm.put('\u00dc', i++);
        charToGsm.put('\u00a7', i++);
        charToGsm.put('\u00bf', i++);
        charToGsm.put('a', i++);
        charToGsm.put('b', i++);
        charToGsm.put('c', i++);
        charToGsm.put('d', i++);
        charToGsm.put('e', i++);
        charToGsm.put('f', i++);
        charToGsm.put('g', i++);
        charToGsm.put('h', i++);
        charToGsm.put('i', i++);
        charToGsm.put('j', i++);
        charToGsm.put('k', i++);
        charToGsm.put('l', i++);
        charToGsm.put('m', i++);
        charToGsm.put('n', i++);
        charToGsm.put('o', i++);
        charToGsm.put('p', i++);
        charToGsm.put('q', i++);
        charToGsm.put('r', i++);
        charToGsm.put('s', i++);
        charToGsm.put('t', i++);
        charToGsm.put('u', i++);
        charToGsm.put('v', i++);
        charToGsm.put('w', i++);
        charToGsm.put('x', i++);
        charToGsm.put('y', i++);
        charToGsm.put('z', i++);
        charToGsm.put('\u00e4', i++);
        charToGsm.put('\u00f6', i++);
        charToGsm.put('\u00f1', i++);
        charToGsm.put('\u00fc', i++);
        charToGsm.put('\u00e0', i++);
        charToGsmExtended.put('\f', 10);
        charToGsmExtended.put('^', 20);
        charToGsmExtended.put('{', 40);
        charToGsmExtended.put('}', 41);
        charToGsmExtended.put('\\', 47);
        charToGsmExtended.put('[', 60);
        charToGsmExtended.put('~', 61);
        charToGsmExtended.put(']', 62);
        charToGsmExtended.put('|', 64);
        charToGsmExtended.put('\u20ac', 101);
        int size = charToGsm.size();
        for (int j=0; j<size; j++) {
            gsmToChar.put(charToGsm.valueAt(j), charToGsm.keyAt(j));
        }
        size = charToGsmExtended.size();
        for (int j=0; j<size; j++) {
            gsmExtendedToChar.put(charToGsmExtended.valueAt(j), charToGsmExtended.keyAt(j));
        }
        sGsmSpaceChar = charToGsm.get(' ');
    }
}
