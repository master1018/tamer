public class XML11Char {
    private static final byte XML11CHARS [] = new byte [1 << 16];
    public static final int MASK_XML11_VALID = 0x01;
    public static final int MASK_XML11_SPACE = 0x02;
    public static final int MASK_XML11_NAME_START = 0x04;
    public static final int MASK_XML11_NAME = 0x08;
    public static final int MASK_XML11_CONTROL = 0x10;
    public static final int MASK_XML11_CONTENT = 0x20;
    public static final int MASK_XML11_NCNAME_START = 0x40;
    public static final int MASK_XML11_NCNAME = 0x80;
    public static final int MASK_XML11_CONTENT_INTERNAL = MASK_XML11_CONTROL | MASK_XML11_CONTENT; 
    static {
        Arrays.fill(XML11CHARS, 1, 9, (byte) 17 ); 
        XML11CHARS[9] = 35;
        XML11CHARS[10] = 3;
        Arrays.fill(XML11CHARS, 11, 13, (byte) 17 ); 
        XML11CHARS[13] = 3;
        Arrays.fill(XML11CHARS, 14, 32, (byte) 17 ); 
        XML11CHARS[32] = 35;
        Arrays.fill(XML11CHARS, 33, 38, (byte) 33 ); 
        XML11CHARS[38] = 1;
        Arrays.fill(XML11CHARS, 39, 45, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 45, 47, (byte) -87 ); 
        XML11CHARS[47] = 33;
        Arrays.fill(XML11CHARS, 48, 58, (byte) -87 ); 
        XML11CHARS[58] = 45;
        XML11CHARS[59] = 33;
        XML11CHARS[60] = 1;
        Arrays.fill(XML11CHARS, 61, 65, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 65, 91, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 91, 93, (byte) 33 ); 
        XML11CHARS[93] = 1;
        XML11CHARS[94] = 33;
        XML11CHARS[95] = -19;
        XML11CHARS[96] = 33;
        Arrays.fill(XML11CHARS, 97, 123, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 123, 127, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 127, 133, (byte) 17 ); 
        XML11CHARS[133] = 35;
        Arrays.fill(XML11CHARS, 134, 160, (byte) 17 ); 
        Arrays.fill(XML11CHARS, 160, 183, (byte) 33 ); 
        XML11CHARS[183] = -87;
        Arrays.fill(XML11CHARS, 184, 192, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 192, 215, (byte) -19 ); 
        XML11CHARS[215] = 33;
        Arrays.fill(XML11CHARS, 216, 247, (byte) -19 ); 
        XML11CHARS[247] = 33;
        Arrays.fill(XML11CHARS, 248, 768, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 768, 880, (byte) -87 ); 
        Arrays.fill(XML11CHARS, 880, 894, (byte) -19 ); 
        XML11CHARS[894] = 33;
        Arrays.fill(XML11CHARS, 895, 8192, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 8192, 8204, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 8204, 8206, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 8206, 8232, (byte) 33 ); 
        XML11CHARS[8232] = 35;
        Arrays.fill(XML11CHARS, 8233, 8255, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 8255, 8257, (byte) -87 ); 
        Arrays.fill(XML11CHARS, 8257, 8304, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 8304, 8592, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 8592, 11264, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 11264, 12272, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 12272, 12289, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 12289, 55296, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 57344, 63744, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 63744, 64976, (byte) -19 ); 
        Arrays.fill(XML11CHARS, 64976, 65008, (byte) 33 ); 
        Arrays.fill(XML11CHARS, 65008, 65534, (byte) -19 ); 
    } 
    public static boolean isXML11Space(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_SPACE) != 0);
    } 
    public static boolean isXML11Valid(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_VALID) != 0) 
                || (0x10000 <= c && c <= 0x10FFFF);
    } 
    public static boolean isXML11Invalid(int c) {
        return !isXML11Valid(c);
    } 
    public static boolean isXML11ValidLiteral(int c) {
        return ((c < 0x10000 && ((XML11CHARS[c] & MASK_XML11_VALID) != 0 && (XML11CHARS[c] & MASK_XML11_CONTROL) == 0))
            || (0x10000 <= c && c <= 0x10FFFF)); 
    } 
    public static boolean isXML11Content(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_CONTENT) != 0) ||
               (0x10000 <= c && c <= 0x10FFFF);
    } 
    public static boolean isXML11InternalEntityContent(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_CONTENT_INTERNAL) != 0) ||
               (0x10000 <= c && c <= 0x10FFFF);
    } 
    public static boolean isXML11NameStart(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NAME_START) != 0)
            || (0x10000 <= c && c < 0xF0000);
    } 
    public static boolean isXML11Name(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NAME) != 0) 
            || (c >= 0x10000 && c < 0xF0000);
    } 
    public static boolean isXML11NCNameStart(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NCNAME_START) != 0)
            || (0x10000 <= c && c < 0xF0000);
    } 
    public static boolean isXML11NCName(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NCNAME) != 0)
            || (0x10000 <= c && c < 0xF0000);
    } 
    public static boolean isXML11NameHighSurrogate(int c) {
        return (0xD800 <= c && c <= 0xDB7F);
    }
    public static boolean isXML11ValidName(String name) {
        int length = name.length();
        if (length == 0)
            return false;
        int i = 1;
        char ch = name.charAt(0);
        if( !isXML11NameStart(ch) ) {
            if ( length > 1 && isXML11NameHighSurrogate(ch) ) {
                char ch2 = name.charAt(1);
                if ( !XMLChar.isLowSurrogate(ch2) || 
                     !isXML11NameStart(XMLChar.supplemental(ch, ch2)) ) {
                    return false;
                }
                i = 2;
            }
            else {
                return false;
            }
        }
        while (i < length) {
            ch = name.charAt(i);
            if ( !isXML11Name(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = name.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            ++i;
        }
        return true;
    } 
    public static boolean isXML11ValidNCName(String ncName) {
        int length = ncName.length();
        if (length == 0)
            return false;
        int i = 1;
        char ch = ncName.charAt(0);
        if( !isXML11NCNameStart(ch) ) {
            if ( length > 1 && isXML11NameHighSurrogate(ch) ) {
                char ch2 = ncName.charAt(1);
                if ( !XMLChar.isLowSurrogate(ch2) || 
                     !isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                    return false;
                }
                i = 2;
            }
            else {
                return false;
            }
        }
        while (i < length) {
            ch = ncName.charAt(i);
            if ( !isXML11NCName(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = ncName.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11NCName(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            ++i;
        }
        return true;
    } 
    public static boolean isXML11ValidNmtoken(String nmtoken) {
        int length = nmtoken.length();
        if (length == 0)
            return false;
        for (int i = 0; i < length; ++i ) {
            char ch = nmtoken.charAt(i);
            if( !isXML11Name(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = nmtoken.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        }
        return true;
    } 
     public static boolean isXML11ValidQName(String str) {
        final int colon = str.indexOf(':');
        if (colon == 0 || colon == str.length() - 1) {
            return false;
        }
        if (colon > 0) {
            final String prefix = str.substring(0,colon);
            final String localPart = str.substring(colon+1);
            return isXML11ValidNCName(prefix) && isXML11ValidNCName(localPart);
        }
        else {
            return isXML11ValidNCName(str);
        }
     }
} 
