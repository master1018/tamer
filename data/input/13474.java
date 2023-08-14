public class BytecodeName {
    private BytecodeName() { }  
    public static String toBytecodeName(String s) {
        String bn = mangle(s);
        assert((Object)bn == s || looksMangled(bn)) : bn;
        assert(s.equals(toSourceName(bn))) : s;
        return bn;
    }
    public static String toSourceName(String s) {
        checkSafeBytecodeName(s);
        String sn = s;
        if (looksMangled(s)) {
            sn = demangle(s);
            assert(s.equals(mangle(sn))) : s+" => "+sn+" => "+mangle(sn);
        }
        return sn;
    }
    public static Object[] parseBytecodeName(String s) {
        int slen = s.length();
        Object[] res = null;
        for (int pass = 0; pass <= 1; pass++) {
            int fillp = 0;
            int lasti = 0;
            for (int i = 0; i <= slen; i++) {
                int whichDC = -1;
                if (i < slen) {
                    whichDC = DANGEROUS_CHARS.indexOf(s.charAt(i));
                    if (whichDC < DANGEROUS_CHAR_FIRST_INDEX)  continue;
                }
                if (lasti < i) {
                    if (pass != 0)
                        res[fillp] = toSourceName(s.substring(lasti, i));
                    fillp++;
                    lasti = i+1;
                }
                if (whichDC >= DANGEROUS_CHAR_FIRST_INDEX) {
                    if (pass != 0)
                        res[fillp] = DANGEROUS_CHARS_CA[whichDC];
                    fillp++;
                    lasti = i+1;
                }
            }
            if (pass != 0)  break;
            res = new Object[fillp];
            if (fillp <= 1 && lasti == 0) {
                if (fillp != 0)  res[0] = toSourceName(s);
                break;
            }
        }
        return res;
    }
    public static String unparseBytecodeName(Object[] components) {
        Object[] components0 = components;
        for (int i = 0; i < components.length; i++) {
            Object c = components[i];
            if (c instanceof String) {
                String mc = toBytecodeName((String) c);
                if (i == 0 && components.length == 1)
                    return mc;  
                if ((Object)mc != c) {
                    if (components == components0)
                        components = components.clone();
                    components[i] = c = mc;
                }
            }
        }
        return appendAll(components);
    }
    private static String appendAll(Object[] components) {
        if (components.length <= 1) {
            if (components.length == 1) {
                return String.valueOf(components[0]);
            }
            return "";
        }
        int slen = 0;
        for (Object c : components) {
            if (c instanceof String)
                slen += String.valueOf(c).length();
            else
                slen += 1;
        }
        StringBuilder sb = new StringBuilder(slen);
        for (Object c : components) {
            sb.append(c);
        }
        return sb.toString();
    }
    public static String toDisplayName(String s) {
        Object[] components = parseBytecodeName(s);
        for (int i = 0; i < components.length; i++) {
            if (!(components[i] instanceof String))
                continue;
            String sn = (String) components[i];
            if (!isJavaIdent(sn) || sn.indexOf('$') >=0 ) {
                components[i] = quoteDisplay(sn);
            }
        }
        return appendAll(components);
    }
    private static boolean isJavaIdent(String s) {
        int slen = s.length();
        if (slen == 0)  return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0)))
            return false;
        for (int i = 1; i < slen; i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
        }
        return true;
    }
    private static String quoteDisplay(String s) {
        return "'"+s.replaceAll("['\\\\]", "\\\\$0")+"'";
    }
    private static void checkSafeBytecodeName(String s)
            throws IllegalArgumentException {
        if (!isSafeBytecodeName(s)) {
            throw new IllegalArgumentException(s);
        }
    }
    public static boolean isSafeBytecodeName(String s) {
        if (s.length() == 0)  return false;
        for (char xc : DANGEROUS_CHARS_A) {
            if (xc == ESCAPE_C)  continue;  
            if (s.indexOf(xc) >= 0)  return false;
        }
        return true;
    }
    public static boolean isSafeBytecodeChar(char c) {
        return DANGEROUS_CHARS.indexOf(c) < DANGEROUS_CHAR_FIRST_INDEX;
    }
    private static boolean looksMangled(String s) {
        return s.charAt(0) == ESCAPE_C;
    }
    private static String mangle(String s) {
        if (s.length() == 0)
            return NULL_ESCAPE;
        StringBuilder sb = null;
        for (int i = 0, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);
            boolean needEscape = false;
            if (c == ESCAPE_C) {
                if (i+1 < slen) {
                    char c1 = s.charAt(i+1);
                    if ((i == 0 && c1 == NULL_ESCAPE_C)
                        || c1 != originalOfReplacement(c1)) {
                        needEscape = true;
                    }
                }
            } else {
                needEscape = isDangerous(c);
            }
            if (!needEscape) {
                if (sb != null)  sb.append(c);
                continue;
            }
            if (sb == null) {
                sb = new StringBuilder(s.length()+10);
                if (s.charAt(0) != ESCAPE_C && i > 0)
                    sb.append(NULL_ESCAPE);
                sb.append(s.substring(0, i));
            }
            sb.append(ESCAPE_C);
            sb.append(replacementOf(c));
        }
        if (sb != null)   return sb.toString();
        return s;
    }
    private static String demangle(String s) {
        StringBuilder sb = null;
        int stringStart = 0;
        if (s.startsWith(NULL_ESCAPE))
            stringStart = 2;
        for (int i = stringStart, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);
            if (c == ESCAPE_C && i+1 < slen) {
                char rc = s.charAt(i+1);
                char oc = originalOfReplacement(rc);
                if (oc != rc) {
                    if (sb == null) {
                        sb = new StringBuilder(s.length());
                        sb.append(s.substring(stringStart, i));
                    }
                    ++i;  
                    c = oc;
                }
            }
            if (sb != null)
                sb.append(c);
        }
        if (sb != null)   return sb.toString();
        return s.substring(stringStart);
    }
    static char ESCAPE_C = '\\';
    static char NULL_ESCAPE_C = '=';
    static String NULL_ESCAPE = ESCAPE_C+""+NULL_ESCAPE_C;
    static final String DANGEROUS_CHARS   = "\\/.;:$[]<>"; 
    static final String REPLACEMENT_CHARS =  "-|,?!%{}^_";
    static final int DANGEROUS_CHAR_FIRST_INDEX = 1; 
    static char[] DANGEROUS_CHARS_A   = DANGEROUS_CHARS.toCharArray();
    static char[] REPLACEMENT_CHARS_A = REPLACEMENT_CHARS.toCharArray();
    static final Character[] DANGEROUS_CHARS_CA;
    static {
        Character[] dcca = new Character[DANGEROUS_CHARS.length()];
        for (int i = 0; i < dcca.length; i++)
            dcca[i] = Character.valueOf(DANGEROUS_CHARS.charAt(i));
        DANGEROUS_CHARS_CA = dcca;
    }
    static final long[] SPECIAL_BITMAP = new long[2];  
    static {
        String SPECIAL = DANGEROUS_CHARS + REPLACEMENT_CHARS;
        for (char c : SPECIAL.toCharArray()) {
            SPECIAL_BITMAP[c >>> 6] |= 1L << c;
        }
    }
    static boolean isSpecial(char c) {
        if ((c >>> 6) < SPECIAL_BITMAP.length)
            return ((SPECIAL_BITMAP[c >>> 6] >> c) & 1) != 0;
        else
            return false;
    }
    static char replacementOf(char c) {
        if (!isSpecial(c))  return c;
        int i = DANGEROUS_CHARS.indexOf(c);
        if (i < 0)  return c;
        return REPLACEMENT_CHARS.charAt(i);
    }
    static char originalOfReplacement(char c) {
        if (!isSpecial(c))  return c;
        int i = REPLACEMENT_CHARS.indexOf(c);
        if (i < 0)  return c;
        return DANGEROUS_CHARS.charAt(i);
    }
    static boolean isDangerous(char c) {
        if (!isSpecial(c))  return false;
        return (DANGEROUS_CHARS.indexOf(c) >= DANGEROUS_CHAR_FIRST_INDEX);
    }
    static int indexOfDangerousChar(String s, int from) {
        for (int i = from, slen = s.length(); i < slen; i++) {
            if (isDangerous(s.charAt(i)))
                return i;
        }
        return -1;
    }
    static int lastIndexOfDangerousChar(String s, int from) {
        for (int i = Math.min(from, s.length()-1); i >= 0; i--) {
            if (isDangerous(s.charAt(i)))
                return i;
        }
        return -1;
    }
}
