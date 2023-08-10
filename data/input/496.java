public final class Base64Variant {
    static final char PADDING_CHAR_NONE = '\0';
    public static final int BASE64_VALUE_INVALID = -1;
    public static final int BASE64_VALUE_PADDING = -2;
    private final int[] _asciiToBase64 = new int[128];
    private final char[] _base64ToAsciiC = new char[64];
    private final byte[] _base64ToAsciiB = new byte[64];
    final String _name;
    final boolean _usesPadding;
    final char _paddingChar;
    final int _maxLineLength;
    public Base64Variant(String name, String base64Alphabet, boolean usesPadding, char paddingChar, int maxLineLength) {
        _name = name;
        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;
        int alphaLen = base64Alphabet.length();
        if (alphaLen != 64) {
            throw new IllegalArgumentException("Base64Alphabet length must be exactly 64 (was " + alphaLen + ")");
        }
        base64Alphabet.getChars(0, alphaLen, _base64ToAsciiC, 0);
        Arrays.fill(_asciiToBase64, BASE64_VALUE_INVALID);
        for (int i = 0; i < alphaLen; ++i) {
            char alpha = _base64ToAsciiC[i];
            _base64ToAsciiB[i] = (byte) alpha;
            _asciiToBase64[alpha] = i;
        }
        if (usesPadding) {
            _asciiToBase64[(int) paddingChar] = BASE64_VALUE_PADDING;
        }
    }
    public Base64Variant(Base64Variant base, String name, int maxLineLength) {
        this(base, name, base._usesPadding, base._paddingChar, maxLineLength);
    }
    public Base64Variant(Base64Variant base, String name, boolean usesPadding, char paddingChar, int maxLineLength) {
        _name = name;
        byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);
        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;
    }
    public String getName() {
        return _name;
    }
    public boolean usesPadding() {
        return _usesPadding;
    }
    public boolean usesPaddingChar(char c) {
        return c == _paddingChar;
    }
    public boolean usesPaddingChar(int ch) {
        return ch == (int) _paddingChar;
    }
    public char getPaddingChar() {
        return _paddingChar;
    }
    public byte getPaddingByte() {
        return (byte) _paddingChar;
    }
    public int getMaxLineLength() {
        return _maxLineLength;
    }
    public int decodeBase64Char(char c) {
        int ch = (int) c;
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }
    public int decodeBase64Char(int ch) {
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }
    public int decodeBase64Byte(byte b) {
        int ch = (int) b;
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }
    public char encodeBase64BitsAsChar(int value) {
        return _base64ToAsciiC[value];
    }
    public int encodeBase64Chunk(int b24, char[] buffer, int ptr) {
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 18) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 12) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 6) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[b24 & 0x3F];
        return ptr;
    }
    public void encodeBase64Chunk(StringBuilder sb, int b24) {
        sb.append(_base64ToAsciiC[(b24 >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 12) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 6) & 0x3F]);
        sb.append(_base64ToAsciiC[b24 & 0x3F]);
    }
    public int encodeBase64Partial(int bits, int outputBytes, char[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 12) & 0x3F];
        if (_usesPadding) {
            buffer[outPtr++] = (outputBytes == 2) ? _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar;
            buffer[outPtr++] = _paddingChar;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiC[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }
    public void encodeBase64Partial(StringBuilder sb, int bits, int outputBytes) {
        sb.append(_base64ToAsciiC[(bits >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(bits >> 12) & 0x3F]);
        if (_usesPadding) {
            sb.append((outputBytes == 2) ? _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar);
            sb.append(_paddingChar);
        } else {
            if (outputBytes == 2) {
                sb.append(_base64ToAsciiC[(bits >> 6) & 0x3F]);
            }
        }
    }
    public byte encodeBase64BitsAsByte(int value) {
        return _base64ToAsciiB[value];
    }
    public int encodeBase64Chunk(int b24, byte[] buffer, int ptr) {
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 18) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 12) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 6) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[b24 & 0x3F];
        return ptr;
    }
    public int encodeBase64Partial(int bits, int outputBytes, byte[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 12) & 0x3F];
        if (_usesPadding) {
            byte pb = (byte) _paddingChar;
            buffer[outPtr++] = (outputBytes == 2) ? _base64ToAsciiB[(bits >> 6) & 0x3F] : pb;
            buffer[outPtr++] = pb;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiB[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }
    public String toString() {
        return _name;
    }
}
