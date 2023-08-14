public class URLEncoder {
    static final String digits = "0123456789ABCDEF"; 
    private URLEncoder() {
    }
    @Deprecated
    public static String encode(String s) {
        StringBuilder buf = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9') || ".-*_".indexOf(ch) > -1) { 
                buf.append(ch);
            } else if (ch == ' ') {
                buf.append('+');
            } else {
                byte[] bytes = new String(new char[] { ch }).getBytes();
                for (int j = 0; j < bytes.length; j++) {
                    buf.append('%');
                    buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
                    buf.append(digits.charAt(bytes[j] & 0xf));
                }
            }
        }
        return buf.toString();
    }
    public static String encode(String s, String enc)
            throws UnsupportedEncodingException {
        if (s == null || enc == null) {
            throw new NullPointerException();
        }
        "".getBytes(enc); 
        StringBuffer buf = new StringBuffer(s.length() + 16);
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9') || " .-*_".indexOf(ch) > -1) { 
                if (start >= 0) {
                    convert(s.substring(start, i), buf, enc);
                    start = -1;
                }
                if (ch != ' ') {
                    buf.append(ch);
                } else {
                    buf.append('+');
                }
            } else {
                if (start < 0) {
                    start = i;
                }
            }
        }
        if (start >= 0) {
            convert(s.substring(start, s.length()), buf, enc);
        }
        return buf.toString();
    }
    private static void convert(String s, StringBuffer buf, String enc)
            throws UnsupportedEncodingException {
        byte[] bytes = s.getBytes(enc);
        for (int j = 0; j < bytes.length; j++) {
            buf.append('%');
            buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
            buf.append(digits.charAt(bytes[j] & 0xf));
        }
    }
}
