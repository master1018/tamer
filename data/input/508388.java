public class EncoderUtil {
    static final byte[] BASE64_TABLE = { 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/' };
    private static final byte BASE64_PAD = '=';
    private static final BitSet Q_REGULAR_CHARS = initChars("=_?");
    private static final BitSet Q_RESTRICTED_CHARS = initChars("=_?\"#$%&'(),.:;<>@[\\]^`{|}~");
    private static final int MAX_USED_CHARACTERS = 50;
    private static final String ENC_WORD_PREFIX = "=?";
    private static final String ENC_WORD_SUFFIX = "?=";
    private static final int ENCODED_WORD_MAX_LENGTH = 75; 
    private static final BitSet TOKEN_CHARS = initChars("()<>@,;:\\\"/[]?=");
    private static final BitSet ATEXT_CHARS = initChars("()<>@.,;:\\\"[]");
    private static BitSet initChars(String specials) {
        BitSet bs = new BitSet(128);
        for (char ch = 33; ch < 127; ch++) {
            if (specials.indexOf(ch) == -1) {
                bs.set(ch);
            }
        }
        return bs;
    }
    public enum Encoding {
        B,
        Q
    }
    public enum Usage {
        TEXT_TOKEN,
        WORD_ENTITY
    }
    private EncoderUtil() {
    }
    public static String encodeAddressDisplayName(String displayName) {
        if (isAtomPhrase(displayName)) {
            return displayName;
        } else if (hasToBeEncoded(displayName, 0)) {
            return encodeEncodedWord(displayName, Usage.WORD_ENTITY);
        } else {
            return quote(displayName);
        }
    }
    public static String encodeAddressLocalPart(String localPart) {
        if (isDotAtomText(localPart)) {
            return localPart;
        } else {
            return quote(localPart);
        }
    }
    public static String encodeHeaderParameter(String name, String value) {
        name = name.toLowerCase(Locale.US);
        if (isToken(value)) {
            return name + "=" + value;
        } else {
            return name + "=" + quote(value);
        }
    }
    public static String encodeIfNecessary(String text, Usage usage,
            int usedCharacters) {
        if (hasToBeEncoded(text, usedCharacters))
            return encodeEncodedWord(text, usage, usedCharacters);
        else
            return text;
    }
    public static boolean hasToBeEncoded(String text, int usedCharacters) {
        if (text == null)
            throw new IllegalArgumentException();
        if (usedCharacters < 0 || usedCharacters > MAX_USED_CHARACTERS)
            throw new IllegalArgumentException();
        int nonWhiteSpaceCount = usedCharacters;
        for (int idx = 0; idx < text.length(); idx++) {
            char ch = text.charAt(idx);
            if (ch == '\t' || ch == ' ') {
                nonWhiteSpaceCount = 0;
            } else {
                nonWhiteSpaceCount++;
                if (nonWhiteSpaceCount > 77) {
                    return true;
                }
                if (ch < 32 || ch >= 127) {
                    return true;
                }
            }
        }
        return false;
    }
    public static String encodeEncodedWord(String text, Usage usage) {
        return encodeEncodedWord(text, usage, 0, null, null);
    }
    public static String encodeEncodedWord(String text, Usage usage,
            int usedCharacters) {
        return encodeEncodedWord(text, usage, usedCharacters, null, null);
    }
    public static String encodeEncodedWord(String text, Usage usage,
            int usedCharacters, Charset charset, Encoding encoding) {
        if (text == null)
            throw new IllegalArgumentException();
        if (usedCharacters < 0 || usedCharacters > MAX_USED_CHARACTERS)
            throw new IllegalArgumentException();
        if (charset == null)
            charset = determineCharset(text);
        String mimeCharset = CharsetUtil.toMimeCharset(charset.name());
        if (mimeCharset == null) {
            throw new IllegalArgumentException("Unsupported charset");
        }
        byte[] bytes = encode(text, charset);
        if (encoding == null)
            encoding = determineEncoding(bytes, usage);
        if (encoding == Encoding.B) {
            String prefix = ENC_WORD_PREFIX + mimeCharset + "?B?";
            return encodeB(prefix, text, usedCharacters, charset, bytes);
        } else {
            String prefix = ENC_WORD_PREFIX + mimeCharset + "?Q?";
            return encodeQ(prefix, text, usage, usedCharacters, charset, bytes);
        }
    }
    public static String encodeB(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        final int end = bytes.length;
        for (; idx < end - 2; idx += 3) {
            int data = (bytes[idx] & 0xff) << 16 | (bytes[idx + 1] & 0xff) << 8
                    | bytes[idx + 2] & 0xff;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 6 & 0x3f]);
            sb.append((char) BASE64_TABLE[data & 0x3f]);
        }
        if (idx == end - 2) {
            int data = (bytes[idx] & 0xff) << 16 | (bytes[idx + 1] & 0xff) << 8;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 6 & 0x3f]);
            sb.append((char) BASE64_PAD);
        } else if (idx == end - 1) {
            int data = (bytes[idx] & 0xff) << 16;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append((char) BASE64_PAD);
            sb.append((char) BASE64_PAD);
        }
        return sb.toString();
    }
    public static String encodeQ(byte[] bytes, Usage usage) {
        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;
        StringBuilder sb = new StringBuilder();
        final int end = bytes.length;
        for (int idx = 0; idx < end; idx++) {
            int v = bytes[idx] & 0xff;
            if (v == 32) {
                sb.append('_');
            } else if (!qChars.get(v)) {
                sb.append('=');
                sb.append(hexDigit(v >>> 4));
                sb.append(hexDigit(v & 0xf));
            } else {
                sb.append((char) v);
            }
        }
        return sb.toString();
    }
    public static boolean isToken(String str) {
        final int length = str.length();
        if (length == 0)
            return false;
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            if (!TOKEN_CHARS.get(ch))
                return false;
        }
        return true;
    }
    private static boolean isAtomPhrase(String str) {
        boolean containsAText = false;
        final int length = str.length();
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            if (ATEXT_CHARS.get(ch)) {
                containsAText = true;
            } else if (!CharsetUtil.isWhitespace(ch)) {
                return false;
            }
        }
        return containsAText;
    }
    private static boolean isDotAtomText(String str) {
        char prev = '.';
        final int length = str.length();
        if (length == 0)
            return false;
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            if (ch == '.') {
                if (prev == '.' || idx == length - 1)
                    return false;
            } else {
                if (!ATEXT_CHARS.get(ch))
                    return false;
            }
            prev = ch;
        }
        return true;
    }
    private static String quote(String str) {
        String escaped = str.replaceAll("[\\\\\"]", "\\\\$0");
        return "\"" + escaped + "\"";
    }
    private static String encodeB(String prefix, String text,
            int usedCharacters, Charset charset, byte[] bytes) {
        int encodedLength = bEncodedLength(bytes);
        int totalLength = prefix.length() + encodedLength
                + ENC_WORD_SUFFIX.length();
        if (totalLength <= ENCODED_WORD_MAX_LENGTH - usedCharacters) {
            return prefix + encodeB(bytes) + ENC_WORD_SUFFIX;
        } else {
            int splitOffset = text.offsetByCodePoints(text.length() / 2, -1);
            String part1 = text.substring(0, splitOffset);
            byte[] bytes1 = encode(part1, charset);
            String word1 = encodeB(prefix, part1, usedCharacters, charset,
                    bytes1);
            String part2 = text.substring(splitOffset);
            byte[] bytes2 = encode(part2, charset);
            String word2 = encodeB(prefix, part2, 0, charset, bytes2);
            return word1 + " " + word2;
        }
    }
    private static int bEncodedLength(byte[] bytes) {
        return (bytes.length + 2) / 3 * 4;
    }
    private static String encodeQ(String prefix, String text, Usage usage,
            int usedCharacters, Charset charset, byte[] bytes) {
        int encodedLength = qEncodedLength(bytes, usage);
        int totalLength = prefix.length() + encodedLength
                + ENC_WORD_SUFFIX.length();
        if (totalLength <= ENCODED_WORD_MAX_LENGTH - usedCharacters) {
            return prefix + encodeQ(bytes, usage) + ENC_WORD_SUFFIX;
        } else {
            int splitOffset = text.offsetByCodePoints(text.length() / 2, -1);
            String part1 = text.substring(0, splitOffset);
            byte[] bytes1 = encode(part1, charset);
            String word1 = encodeQ(prefix, part1, usage, usedCharacters,
                    charset, bytes1);
            String part2 = text.substring(splitOffset);
            byte[] bytes2 = encode(part2, charset);
            String word2 = encodeQ(prefix, part2, usage, 0, charset, bytes2);
            return word1 + " " + word2;
        }
    }
    private static int qEncodedLength(byte[] bytes, Usage usage) {
        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;
        int count = 0;
        for (int idx = 0; idx < bytes.length; idx++) {
            int v = bytes[idx] & 0xff;
            if (v == 32) {
                count++;
            } else if (!qChars.get(v)) {
                count += 3;
            } else {
                count++;
            }
        }
        return count;
    }
    private static byte[] encode(String text, Charset charset) {
        ByteBuffer buffer = charset.encode(text);
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }
    private static Charset determineCharset(String text) {
        boolean ascii = true;
        final int len = text.length();
        for (int index = 0; index < len; index++) {
            char ch = text.charAt(index);
            if (ch > 0xff) {
                return CharsetUtil.UTF_8;
            }
            if (ch > 0x7f) {
                ascii = false;
            }
        }
        return ascii ? CharsetUtil.US_ASCII : CharsetUtil.ISO_8859_1;
    }
    private static Encoding determineEncoding(byte[] bytes, Usage usage) {
        if (bytes.length == 0)
            return Encoding.Q;
        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;
        int qEncoded = 0;
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            if (v != 32 && !qChars.get(v)) {
                qEncoded++;
            }
        }
        int percentage = qEncoded * 100 / bytes.length;
        return percentage > 30 ? Encoding.B : Encoding.Q;
    }
    private static char hexDigit(int i) {
        return i < 10 ? (char) (i + '0') : (char) (i - 10 + 'A');
    }
}
