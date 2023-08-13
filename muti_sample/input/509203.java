public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    protected String charset = StringEncodings.UTF8;
    protected static byte ESCAPE_CHAR = '%';
    protected static final BitSet WWW_FORM_URL = new BitSet(256);
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL.set(i);
        }
        WWW_FORM_URL.set('-');
        WWW_FORM_URL.set('_');
        WWW_FORM_URL.set('.');
        WWW_FORM_URL.set('*');
        WWW_FORM_URL.set(' ');
    }
    public URLCodec() {
        super();
    }
    public URLCodec(String charset) {
        super();
        this.charset = charset;
    }
    public static final byte[] encodeUrl(BitSet urlsafe, byte[] bytes) 
    {
        if (bytes == null) {
            return null;
        }
        if (urlsafe == null) {
            urlsafe = WWW_FORM_URL;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b = 256 + b;
            }
            if (urlsafe.get(b)) {
                if (b == ' ') {
                    b = '+';
                }
                buffer.write(b);
            } else {
                buffer.write('%');
                char hex1 = Character.toUpperCase(
                  Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(
                  Character.forDigit(b & 0xF, 16));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        return buffer.toByteArray(); 
    }
    public static final byte[] decodeUrl(byte[] bytes) 
         throws DecoderException
    {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b == '+') {
                buffer.write(' ');
            } else if (b == '%') {
                try {
                    int u = Character.digit((char)bytes[++i], 16);
                    int l = Character.digit((char)bytes[++i], 16);
                    if (u == -1 || l == -1) {
                        throw new DecoderException("Invalid URL encoding");
                    }
                    buffer.write((char)((u << 4) + l));
                } catch(ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid URL encoding");
                }
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray(); 
    }
    public byte[] encode(byte[] bytes) {
        return encodeUrl(WWW_FORM_URL, bytes);
    }
    public byte[] decode(byte[] bytes) throws DecoderException {
        return decodeUrl(bytes);
    }
    public String encode(String pString, String charset) 
        throws UnsupportedEncodingException  
    {
        if (pString == null) {
            return null;
        }
        return new String(encode(pString.getBytes(charset)), StringEncodings.US_ASCII);
    }
    public String encode(String pString) throws EncoderException {
        if (pString == null) {
            return null;
        }
        try {
            return encode(pString, getDefaultCharset());
        } catch(UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage());
        }
    }
    public String decode(String pString, String charset) 
        throws DecoderException, UnsupportedEncodingException 
    {
        if (pString == null) {
            return null;
        }
        return new String(decode(pString.getBytes(StringEncodings.US_ASCII)), charset);
    }
    public String decode(String pString) throws DecoderException {
        if (pString == null) {
            return null;
        }
        try {
            return decode(pString, getDefaultCharset());
        } catch(UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage());
        }
    }
    public Object encode(Object pObject) throws EncoderException {
        if (pObject == null) {
            return null;
        } else if (pObject instanceof byte[]) {
            return encode((byte[])pObject);
        } else if (pObject instanceof String) {
            return encode((String)pObject);
        } else {
            throw new EncoderException("Objects of type " +
                pObject.getClass().getName() + " cannot be URL encoded"); 
        }
    }
    public Object decode(Object pObject) throws DecoderException {
        if (pObject == null) {
            return null;
        } else if (pObject instanceof byte[]) {
            return decode((byte[])pObject);
        } else if (pObject instanceof String) {
            return decode((String)pObject);
        } else {
            throw new DecoderException("Objects of type " +
                pObject.getClass().getName() + " cannot be URL decoded"); 
        }
    }
    public String getEncoding() {
        return this.charset;
    }
    public String getDefaultCharset() {
        return this.charset;
    }
}
