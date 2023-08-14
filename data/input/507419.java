public class SimpleIcsWriter {
    private static final int MAX_LINE_LENGTH = 75; 
    private static final int CHAR_MAX_BYTES_IN_UTF8 = 4;  
    private final ByteArrayOutputStream mOut = new ByteArrayOutputStream();
    public SimpleIcsWriter() {
    }
     void writeLine(String string) {
        int numBytes = 0;
        for (byte b : Utility.toUtf8(string)) {
            if (numBytes > (MAX_LINE_LENGTH - CHAR_MAX_BYTES_IN_UTF8)
                    && Utility.isFirstUtf8Byte(b)) { 
                mOut.write((byte) '\r');
                mOut.write((byte) '\n');
                mOut.write((byte) '\t');
                numBytes = 1; 
            }
            mOut.write(b);
            numBytes++;
        }
        mOut.write((byte) '\r');
        mOut.write((byte) '\n');
    }
    public void writeTag(String name, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if ("CALSCALE".equals(name)
                || "METHOD".equals(name)
                || "PRODID".equals(name)
                || "VERSION".equals(name)
                || "CATEGORIES".equals(name)
                || "CLASS".equals(name)
                || "COMMENT".equals(name)
                || "DESCRIPTION".equals(name)
                || "LOCATION".equals(name)
                || "RESOURCES".equals(name)
                || "STATUS".equals(name)
                || "SUMMARY".equals(name)
                || "TRANSP".equals(name)
                || "TZID".equals(name)
                || "TZNAME".equals(name)
                || "CONTACT".equals(name)
                || "RELATED-TO".equals(name)
                || "UID".equals(name)
                || "ACTION".equals(name)
                || "REQUEST-STATUS".equals(name)
                || "X-LIC-LOCATION".equals(name)
                ) {
            value = escapeTextValue(value);
        }
        writeLine(name + ":" + value);
    }
    @Override
    public String toString() {
        return Utility.fromUtf8(getBytes());
    }
    public byte[] getBytes() {
        try {
            mOut.flush();
        } catch (IOException wonthappen) {
        }
        return mOut.toByteArray();
    }
    public static String quoteParamValue(String paramValue) {
        if (paramValue == null) {
            return null;
        }
        return "\"" + paramValue.replace("\"", "'") + "\"";
    }
     static String escapeTextValue(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\r') {
            } else if (ch == ',' || ch == ';' || ch == '\\') {
                sb.append('\\');
                sb.append(ch);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
