public class ParseUtil_4922813 {
    public static void main(String[] argv) throws Exception {
        int num = 400;
        while (num-- >= 0) {
            String source = getTestSource();
            String ec = sun.net.www.ParseUtil.encodePath(source);
            String v117 = ParseUtil_V117.encodePath(source);
            if (!ec.equals(v117)) {
                throw new RuntimeException("Test Failed for : \n"
                                           + "   source  =<"
                                           + getUnicodeString(source)
                                           + ">");
            }
        }
    }
    static int maxCharCount = 200;
    static int maxCodePoint = 0x10ffff;
    static Random random;
    static String getTestSource() {
        if (random == null) {
            long seed = System.currentTimeMillis();
            random = new Random(seed);
        }
        String source = "";
        int i = 0;
        int count = random.nextInt(maxCharCount) + 1;
        while (i < count) {
            int codepoint = random.nextInt(127);
            source = source + String.valueOf((char)codepoint);
            codepoint = random.nextInt(0x7ff);
            source = source + String.valueOf((char)codepoint);
            codepoint = random.nextInt(maxCodePoint);
            source = source + new String(Character.toChars(codepoint));
            i += 3;
        }
        return source;
    }
    static String getUnicodeString(String s){
        String unicodeString = "";
        for(int j=0; j< s.length(); j++){
             unicodeString += "0x"+ Integer.toString(s.charAt(j), 16);
        }
        return unicodeString;
    }
}
class ParseUtil_V117 {
    static BitSet encodedInPath;
    static {
        encodedInPath = new BitSet(256);
        encodedInPath.set('=');
        encodedInPath.set(';');
        encodedInPath.set('?');
        encodedInPath.set('/');
        encodedInPath.set('#');
        encodedInPath.set(' ');
        encodedInPath.set('<');
        encodedInPath.set('>');
        encodedInPath.set('%');
        encodedInPath.set('"');
        encodedInPath.set('{');
        encodedInPath.set('}');
        encodedInPath.set('|');
        encodedInPath.set('\\');
        encodedInPath.set('^');
        encodedInPath.set('[');
        encodedInPath.set(']');
        encodedInPath.set('`');
        for (int i=0; i<32; i++)
            encodedInPath.set(i);
        encodedInPath.set(127);
    }
    public static String encodePath(String path) {
        StringBuffer sb = new StringBuffer();
        int n = path.length();
        for (int i=0; i<n; i++) {
            char c = path.charAt(i);
            if (c == File.separatorChar)
                sb.append('/');
            else {
                if (c <= 0x007F) {
                    if (encodedInPath.get(c))
                        escape(sb, c);
                    else
                        sb.append(c);
                } else if (c > 0x07FF) {
                    escape(sb, (char)(0xE0 | ((c >> 12) & 0x0F)));
                    escape(sb, (char)(0x80 | ((c >>  6) & 0x3F)));
                    escape(sb, (char)(0x80 | ((c >>  0) & 0x3F)));
                } else {
                    escape(sb, (char)(0xC0 | ((c >>  6) & 0x1F)));
                    escape(sb, (char)(0x80 | ((c >>  0) & 0x3F)));
                }
            }
        }
        return sb.toString();
    }
    private static void escape(StringBuffer s, char c) {
        s.append('%');
        s.append(Character.forDigit((c >> 4) & 0xF, 16));
        s.append(Character.forDigit(c & 0xF, 16));
    }
}
