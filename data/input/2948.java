final public class UrlUtil {
    private UrlUtil() {
    }
    public static final String decode(String s) throws MalformedURLException {
        try {
            return decode(s, "8859_1");
        } catch (UnsupportedEncodingException e) {
            throw new MalformedURLException("ISO-Latin-1 decoder unavailable");
        }
    }
    public static final String decode(String s, String enc)
        throws MalformedURLException, UnsupportedEncodingException {
        int length = s.length();
        byte[] bytes = new byte[length];
        int j = 0;
        for (int i = 0; i < length; i++) {
            if (s.charAt(i) == '%') {
                i++;  
                try {
                    bytes[j++] = (byte)
                        Integer.parseInt(s.substring(i, i + 2), 16);
                } catch (Exception e) {
                    throw new MalformedURLException("Invalid URI encoding: " + s);
                }
                i++;  
            } else {
                bytes[j++] = (byte) s.charAt(i);
            }
        }
        return new String(bytes, 0, j, enc);
    }
    public static final String encode(String s, String enc)
        throws UnsupportedEncodingException {
        byte[] bytes = s.getBytes(enc);
        int count = bytes.length;
        final String allowed = "=,+;.'-@&/$_()!~*:"; 
        char[] buf = new char[3 * count];
        int j = 0;
        for (int i = 0; i < count; i++) {
            if ((bytes[i] >= 0x61 && bytes[i] <= 0x7A) || 
                (bytes[i] >= 0x41 && bytes[i] <= 0x5A) || 
                (bytes[i] >= 0x30 && bytes[i] <= 0x39) || 
                (allowed.indexOf(bytes[i]) >= 0)) {
                buf[j++] = (char) bytes[i];
            } else {
                buf[j++] = '%';
                buf[j++] = Character.forDigit(0xF & (bytes[i] >>> 4), 16);
                buf[j++] = Character.forDigit(0xF & bytes[i], 16);
            }
        }
        return new String(buf, 0, j);
    }
}
