public final class IDN {
    public static final int ALLOW_UNASSIGNED = 0x01;
    public static final int USE_STD3_ASCII_RULES = 0x02;
    public static String toASCII(String input, int flag)
    {
        int p = 0, q = 0;
        StringBuffer out = new StringBuffer();
        while (p < input.length()) {
            q = searchDots(input, p);
            out.append(toASCIIInternal(input.substring(p, q),  flag));
            p = q + 1;
            if (p < input.length()) out.append('.');
        }
        return out.toString();
    }
    public static String toASCII(String input) {
        return toASCII(input, 0);
    }
    public static String toUnicode(String input, int flag) {
        int p = 0, q = 0;
        StringBuffer out = new StringBuffer();
        while (p < input.length()) {
            q = searchDots(input, p);
            out.append(toUnicodeInternal(input.substring(p, q),  flag));
            p = q + 1;
            if (p < input.length()) out.append('.');
        }
        return out.toString();
    }
    public static String toUnicode(String input) {
        return toUnicode(input, 0);
    }
    private static final String ACE_PREFIX = "xn--";
    private static final int ACE_PREFIX_LENGTH = ACE_PREFIX.length();
    private static final int MAX_LABEL_LENGTH   = 63;
    private static StringPrep namePrep = null;
    static {
        InputStream stream = null;
        try {
            final String IDN_PROFILE = "uidna.spp";
            if (System.getSecurityManager() != null) {
                stream = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                    public InputStream run() {
                        return StringPrep.class.getResourceAsStream(IDN_PROFILE);
                    }
                });
            } else {
                stream = StringPrep.class.getResourceAsStream(IDN_PROFILE);
            }
            namePrep = new StringPrep(stream);
            stream.close();
        } catch (IOException e) {
            assert false;
        }
    }
    private IDN() {}
    private static String toASCIIInternal(String label, int flag)
    {
        boolean isASCII  = isAllASCII(label);
        StringBuffer dest;
        if (!isASCII) {
            UCharacterIterator iter = UCharacterIterator.getInstance(label);
            try {
                dest = namePrep.prepare(iter, flag);
            } catch (java.text.ParseException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            dest = new StringBuffer(label);
        }
        boolean useSTD3ASCIIRules = ((flag & USE_STD3_ASCII_RULES) != 0);
        if (useSTD3ASCIIRules) {
            for (int i = 0; i < dest.length(); i++) {
                int c = dest.charAt(i);
                if (!isLDHChar(c)) {
                    throw new IllegalArgumentException("Contains non-LDH characters");
                }
            }
            if (dest.charAt(0) == '-' || dest.charAt(dest.length() - 1) == '-') {
                throw new IllegalArgumentException("Has leading or trailing hyphen");
            }
        }
        if (!isASCII) {
            if (!isAllASCII(dest.toString())) {
                if(!startsWithACEPrefix(dest)){
                    try {
                        dest = Punycode.encode(dest, null);
                    } catch (java.text.ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                    dest = toASCIILower(dest);
                    dest.insert(0, ACE_PREFIX);
                } else {
                    throw new IllegalArgumentException("The input starts with the ACE Prefix");
                }
            }
        }
        if(dest.length() > MAX_LABEL_LENGTH){
            throw new IllegalArgumentException("The label in the input is too long");
        }
        return dest.toString();
    }
    private static String toUnicodeInternal(String label, int flag) {
        boolean[] caseFlags = null;
        StringBuffer dest;
        boolean isASCII = isAllASCII(label);
        if(!isASCII){
            try {
                UCharacterIterator iter = UCharacterIterator.getInstance(label);
                dest = namePrep.prepare(iter, flag);
            } catch (Exception e) {
                return label;
            }
        } else {
            dest = new StringBuffer(label);
        }
        if(startsWithACEPrefix(dest)) {
            String temp = dest.substring(ACE_PREFIX_LENGTH, dest.length());
            try {
                StringBuffer decodeOut = Punycode.decode(new StringBuffer(temp), null);
                String toASCIIOut = toASCII(decodeOut.toString(), flag);
                if (toASCIIOut.equalsIgnoreCase(dest.toString())) {
                    return decodeOut.toString();
                }
            } catch (Exception ignored) {
            }
        }
        return label;
    }
    private static boolean isLDHChar(int ch){
        if(ch > 0x007A){
            return false;
        }
        if((ch == 0x002D) ||
           (0x0030 <= ch && ch <= 0x0039) ||
           (0x0041 <= ch && ch <= 0x005A) ||
           (0x0061 <= ch && ch <= 0x007A)
          ){
            return true;
        }
        return false;
    }
    private static int searchDots(String s, int start) {
        int i;
        for (i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.' || c == '\u3002' || c == '\uFF0E' || c == '\uFF61') {
                break;
            }
        }
        return i;
    }
    private static boolean isAllASCII(String input) {
        boolean isASCII = true;
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x7F) {
                isASCII = false;
                break;
            }
        }
        return isASCII;
    }
    private static boolean startsWithACEPrefix(StringBuffer input){
        boolean startsWithPrefix = true;
        if(input.length() < ACE_PREFIX_LENGTH){
            return false;
        }
        for(int i = 0; i < ACE_PREFIX_LENGTH; i++){
            if(toASCIILower(input.charAt(i)) != ACE_PREFIX.charAt(i)){
                startsWithPrefix = false;
            }
        }
        return startsWithPrefix;
    }
    private static char toASCIILower(char ch){
        if('A' <= ch && ch <= 'Z'){
            return (char)(ch + 'a' - 'A');
        }
        return ch;
    }
    private static StringBuffer toASCIILower(StringBuffer input){
        StringBuffer dest = new StringBuffer();
        for(int i = 0; i < input.length();i++){
            dest.append(toASCIILower(input.charAt(i)));
        }
        return dest;
    }
}
