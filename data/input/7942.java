public final class UTF16
{
    public static final int CODEPOINT_MIN_VALUE = 0;
    public static final int CODEPOINT_MAX_VALUE = 0x10ffff;
    public static final int SUPPLEMENTARY_MIN_VALUE  = 0x10000;
    public static final int LEAD_SURROGATE_MIN_VALUE = 0xD800;
    public static final int TRAIL_SURROGATE_MIN_VALUE = 0xDC00;
    public static final int LEAD_SURROGATE_MAX_VALUE = 0xDBFF;
    public static final int TRAIL_SURROGATE_MAX_VALUE = 0xDFFF;
    public static final int SURROGATE_MIN_VALUE = LEAD_SURROGATE_MIN_VALUE;
    public static int charAt(String source, int offset16) {
        char single = source.charAt(offset16);
        if (single < LEAD_SURROGATE_MIN_VALUE) {
            return single;
        }
        return _charAt(source, offset16, single);
    }
    private static int _charAt(String source, int offset16, char single) {
        if (single > TRAIL_SURROGATE_MAX_VALUE) {
            return single;
        }
        if (single <= LEAD_SURROGATE_MAX_VALUE) {
            ++offset16;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (trail >= TRAIL_SURROGATE_MIN_VALUE && trail <= TRAIL_SURROGATE_MAX_VALUE) {
                    return UCharacterProperty.getRawSupplementary(single, trail);
                }
            }
        } else {
            --offset16;
            if (offset16 >= 0) {
                char lead = source.charAt(offset16);
                if (lead >= LEAD_SURROGATE_MIN_VALUE && lead <= LEAD_SURROGATE_MAX_VALUE) {
                    return UCharacterProperty.getRawSupplementary(lead, single);
                }
            }
        }
        return single; 
    }
    public static int charAt(char source[], int start, int limit,
                             int offset16)
    {
        offset16 += start;
        if (offset16 < start || offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        char single = source[offset16];
        if (!isSurrogate(single)) {
            return single;
        }
        if (single <= LEAD_SURROGATE_MAX_VALUE) {
            offset16 ++;
            if (offset16 >= limit) {
                return single;
            }
            char trail = source[offset16];
            if (isTrailSurrogate(trail)) {
                return UCharacterProperty.getRawSupplementary(single, trail);
            }
        }
        else { 
            if (offset16 == start) {
                return single;
            }
            offset16 --;
            char lead = source[offset16];
            if (isLeadSurrogate(lead))
                return UCharacterProperty.getRawSupplementary(lead, single);
        }
        return single; 
    }
    public static int getCharCount(int char32)
    {
        if (char32 < SUPPLEMENTARY_MIN_VALUE) {
            return 1;
        }
        return 2;
    }
    public static boolean isSurrogate(char char16)
    {
        return LEAD_SURROGATE_MIN_VALUE <= char16 &&
            char16 <= TRAIL_SURROGATE_MAX_VALUE;
    }
    public static boolean isTrailSurrogate(char char16)
    {
        return (TRAIL_SURROGATE_MIN_VALUE <= char16 &&
                char16 <= TRAIL_SURROGATE_MAX_VALUE);
    }
    public static boolean isLeadSurrogate(char char16)
    {
        return LEAD_SURROGATE_MIN_VALUE <= char16 &&
            char16 <= LEAD_SURROGATE_MAX_VALUE;
    }
    public static char getLeadSurrogate(int char32)
    {
        if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            return (char)(LEAD_SURROGATE_OFFSET_ +
                          (char32 >> LEAD_SURROGATE_SHIFT_));
        }
        return 0;
    }
    public static char getTrailSurrogate(int char32)
    {
        if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            return (char)(TRAIL_SURROGATE_MIN_VALUE +
                          (char32 & TRAIL_SURROGATE_MASK_));
        }
        return (char)char32;
    }
    public static String valueOf(int char32)
    {
        if (char32 < CODEPOINT_MIN_VALUE || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Illegal codepoint");
        }
        return toString(char32);
    }
    public static StringBuffer append(StringBuffer target, int char32)
    {
        if (char32 < CODEPOINT_MIN_VALUE || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Illegal codepoint: " + Integer.toHexString(char32));
        }
        if (char32 >= SUPPLEMENTARY_MIN_VALUE)
            {
                target.append(getLeadSurrogate(char32));
                target.append(getTrailSurrogate(char32));
            }
        else {
            target.append((char)char32);
        }
        return target;
    }
    public static int moveCodePointOffset(char source[], int start, int limit,
                                          int offset16, int shift32)
    {
        int         size = source.length;
        int         count;
        char        ch;
        int         result = offset16 + start;
        if (start<0 || limit<start) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (limit>size) {
            throw new StringIndexOutOfBoundsException(limit);
        }
        if (offset16<0 || result>limit) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        if (shift32 > 0 ) {
            if (shift32 + result > size) {
                throw new StringIndexOutOfBoundsException(result);
            }
            count = shift32;
            while (result < limit && count > 0)
            {
                ch = source[result];
                if (isLeadSurrogate(ch) && (result+1 < limit) &&
                        isTrailSurrogate(source[result+1])) {
                    result ++;
                }
                count --;
                result ++;
            }
        } else {
            if (result + shift32 < start) {
                throw new StringIndexOutOfBoundsException(result);
            }
            for (count=-shift32; count>0; count--) {
                result--;
                if (result<start) {
                    break;
                }
                ch = source[result];
                if (isTrailSurrogate(ch) && result>start && isLeadSurrogate(source[result-1])) {
                    result--;
                }
            }
        }
        if (count != 0)  {
            throw new StringIndexOutOfBoundsException(shift32);
        }
        result -= start;
        return result;
    }
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    private static final int TRAIL_SURROGATE_MASK_     = 0x3FF;
    private static final int LEAD_SURROGATE_OFFSET_ =
        LEAD_SURROGATE_MIN_VALUE -
        (SUPPLEMENTARY_MIN_VALUE
         >> LEAD_SURROGATE_SHIFT_);
    private static String toString(int ch)
    {
        if (ch < SUPPLEMENTARY_MIN_VALUE) {
            return String.valueOf((char)ch);
        }
        StringBuffer result = new StringBuffer();
        result.append(getLeadSurrogate(ch));
        result.append(getTrailSurrogate(ch));
        return result.toString();
    }
}
