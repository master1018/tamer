public class Convert {
    public static int string2int(String s, int radix)
        throws NumberFormatException {
        if (radix == 10) {
            return Integer.parseInt(s, radix);
        } else {
            char[] cs = s.toCharArray();
            int limit = Integer.MAX_VALUE / (radix/2);
            int n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                if (n < 0 ||
                    n > limit ||
                    n * radix > Integer.MAX_VALUE - d)
                    throw new NumberFormatException();
                n = n * radix + d;
            }
            return n;
        }
    }
    public static long string2long(String s, int radix)
        throws NumberFormatException {
        if (radix == 10) {
            return Long.parseLong(s, radix);
        } else {
            char[] cs = s.toCharArray();
            long limit = Long.MAX_VALUE / (radix/2);
            long n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                if (n < 0 ||
                    n > limit ||
                    n * radix > Long.MAX_VALUE - d)
                    throw new NumberFormatException();
                n = n * radix + d;
            }
            return n;
        }
    }
    public static int utf2chars(byte[] src, int sindex,
                                char[] dst, int dindex,
                                int len) {
        int i = sindex;
        int j = dindex;
        int limit = sindex + len;
        while (i < limit) {
            int b = src[i++] & 0xFF;
            if (b >= 0xE0) {
                b = (b & 0x0F) << 12;
                b = b | (src[i++] & 0x3F) << 6;
                b = b | (src[i++] & 0x3F);
            } else if (b >= 0xC0) {
                b = (b & 0x1F) << 6;
                b = b | (src[i++] & 0x3F);
            }
            dst[j++] = (char)b;
        }
        return j;
    }
    public static char[] utf2chars(byte[] src, int sindex, int len) {
        char[] dst = new char[len];
        int len1 = utf2chars(src, sindex, dst, 0, len);
        char[] result = new char[len1];
        System.arraycopy(dst, 0, result, 0, len1);
        return result;
    }
    public static char[] utf2chars(byte[] src) {
        return utf2chars(src, 0, src.length);
    }
    public static String utf2string(byte[] src, int sindex, int len) {
        char dst[] = new char[len];
        int len1 = utf2chars(src, sindex, dst, 0, len);
        return new String(dst, 0, len1);
    }
    public static String utf2string(byte[] src) {
        return utf2string(src, 0, src.length);
    }
    public static int chars2utf(char[] src, int sindex,
                                byte[] dst, int dindex,
                                int len) {
        int j = dindex;
        int limit = sindex + len;
        for (int i = sindex; i < limit; i++) {
            char ch = src[i];
            if (1 <= ch && ch <= 0x7F) {
                dst[j++] = (byte)ch;
            } else if (ch <= 0x7FF) {
                dst[j++] = (byte)(0xC0 | (ch >> 6));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            } else {
                dst[j++] = (byte)(0xE0 | (ch >> 12));
                dst[j++] = (byte)(0x80 | ((ch >> 6) & 0x3F));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            }
        }
        return j;
    }
    public static byte[] chars2utf(char[] src, int sindex, int len) {
        byte[] dst = new byte[len * 3];
        int len1 = chars2utf(src, sindex, dst, 0, len);
        byte[] result = new byte[len1];
        System.arraycopy(dst, 0, result, 0, len1);
        return result;
    }
    public static byte[] chars2utf(char[] src) {
        return chars2utf(src, 0, src.length);
    }
    public static byte[] string2utf(String s) {
        return chars2utf(s.toCharArray());
    }
    public static String quote(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            buf.append(quote(s.charAt(i)));
        }
        return buf.toString();
    }
    public static String quote(char ch) {
        switch (ch) {
        case '\b':  return "\\b";
        case '\f':  return "\\f";
        case '\n':  return "\\n";
        case '\r':  return "\\r";
        case '\t':  return "\\t";
        case '\'':  return "\\'";
        case '\"':  return "\\\"";
        case '\\':  return "\\\\";
        default:
            return (isPrintableAscii(ch))
                ? String.valueOf(ch)
                : String.format("\\u%04x", (int) ch);
        }
    }
    private static boolean isPrintableAscii(char ch) {
        return ch >= ' ' && ch <= '~';
    }
    public static String escapeUnicode(String s) {
        int len = s.length();
        int i = 0;
        while (i < len) {
            char ch = s.charAt(i);
            if (ch > 255) {
                StringBuffer buf = new StringBuffer();
                buf.append(s.substring(0, i));
                while (i < len) {
                    ch = s.charAt(i);
                    if (ch > 255) {
                        buf.append("\\u");
                        buf.append(Character.forDigit((ch >> 12) % 16, 16));
                        buf.append(Character.forDigit((ch >>  8) % 16, 16));
                        buf.append(Character.forDigit((ch >>  4) % 16, 16));
                        buf.append(Character.forDigit((ch      ) % 16, 16));
                    } else {
                        buf.append(ch);
                    }
                    i++;
                }
                s = buf.toString();
            } else {
                i++;
            }
        }
        return s;
    }
    public static Name shortName(Name classname) {
        return classname.subName(
            classname.lastIndexOf((byte)'.') + 1, classname.getByteLength());
    }
    public static String shortName(String classname) {
        return classname.substring(classname.lastIndexOf('.') + 1);
    }
    public static Name packagePart(Name classname) {
        return classname.subName(0, classname.lastIndexOf((byte)'.'));
    }
    public static String packagePart(String classname) {
        int lastDot = classname.lastIndexOf('.');
        return (lastDot < 0 ? "" : classname.substring(0, lastDot));
    }
    public static List<Name> enclosingCandidates(Name name) {
        List<Name> names = List.nil();
        int index;
        while ((index = name.lastIndexOf((byte)'$')) > 0) {
            name = name.subName(0, index);
            names = names.prepend(name);
        }
        return names;
    }
}
