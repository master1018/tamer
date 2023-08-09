public final class Long extends Number implements Comparable<Long> {
    private static final long serialVersionUID = 4290774380558885855L;
    private final long value;
    public static final long MAX_VALUE = 0x7FFFFFFFFFFFFFFFL;
    public static final long MIN_VALUE = 0x8000000000000000L;
    @SuppressWarnings("unchecked")
    public static final Class<Long> TYPE
            = (Class<Long>) long[].class.getComponentType();
    public static final int SIZE = 64;
    private static final char[] MOD_10_TABLE = {
        0, 1, 2, 2, 3, 3, 4, 5, 5, 6, 7, 7, 8, 8, 9, 0
    };
    public Long(long value) {
        this.value = value;
    }
    public Long(String string) throws NumberFormatException {
        this(parseLong(string));
    }
    @Override
    public byte byteValue() {
        return (byte) value;
    }
    public int compareTo(Long object) {
        long thisValue = this.value;
        long thatValue = object.value;
        return thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1);
    }
    public static Long decode(String string) throws NumberFormatException {
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException();
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                throw new NumberFormatException(string);
            }
            firstDigit = string.charAt(++i);
        }
        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0L);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (i == length) {
                    throw new NumberFormatException(string);
                }
                i++;
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (i == length) {
                throw new NumberFormatException(string);
            }
            i++;
            base = 16;
        }
        long result = parse(string, i, base, negative);
        return valueOf(result);
    }
    @Override
    public double doubleValue() {
        return value;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Long && ((Long) o).value == value;
    }
    @Override
    public float floatValue() {
        return value;
    }
    public static Long getLong(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return null;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    public static Long getLong(String string, long defaultValue) {
        if (string == null || string.length() == 0) {
            return valueOf(defaultValue);
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return valueOf(defaultValue);
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return valueOf(defaultValue);
        }
    }
    public static Long getLong(String string, Long defaultValue) {
        if (string == null || string.length() == 0) {
            return defaultValue;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return defaultValue;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }
    @Override
    public int intValue() {
        return (int) value;
    }
    @Override
    public long longValue() {
        return value;
    }
    public static long parseLong(String string) throws NumberFormatException {
        return parseLong(string, 10);
    }
    public static long parseLong(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            throw new NumberFormatException();
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException(string);
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            throw new NumberFormatException(string);
        }
        return parse(string, i, radix, negative);
    }
    private static long parse(String string, int offset, int radix,
            boolean negative) {
        long max = Long.MIN_VALUE / radix;
        long result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw new NumberFormatException(string);
            }
            if (max > result) {
                throw new NumberFormatException(string);
            }
            long next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(string);
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException(string);
            }
        }
        return result;
    }
    @Override
    public short shortValue() {
        return (short) value;
    }
    public static String toBinaryString(long v) {
        int i = (int) v;
        if (v >= 0 && i == v) {
            return Integer.toBinaryString(i);
        }
        int bufLen = 64;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = (char) ((v & 1) + '0');
        }  while ((v >>>= 1) != 0);
        return new String(cursor, bufLen - cursor, buf);
     }
    public static String toHexString(long v) {
        int i = (int) v;
        if (v >= 0 && i == v) {
            return Integer.toHexString(i);
        }
        int bufLen = 16;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = Integer.DIGITS[((int) v) & 0xF];
        } while ((v >>>= 4) != 0);
        return new String(cursor, bufLen - cursor, buf);
    }
    public static String toOctalString(long v) {
        int i = (int) v;
        if (v >= 0 && i == v) {
            return Integer.toOctalString(i);
        }
        int bufLen = 22;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = (char) (((int)v & 7) + '0');
        } while ((v >>>= 3) != 0);
        return new String(cursor, bufLen - cursor, buf);
    }
    @Override
    public String toString() {
        return Long.toString(value);
    }
    public static String toString(long n) {
        int i = (int) n;
        if (i == n)
            return Integer.toString(i);
        boolean negative = (n < 0);
        if (negative) {
            n = -n;
            if (n < 0)  
                return "-9223372036854775808";
        }
        int bufLen = 20; 
        char[] buf = new char[bufLen];
        int low = (int) (n % 1000000000); 
        int cursor = intIntoCharArray(buf, bufLen, low);
        while (cursor != (bufLen - 9))
            buf[--cursor] = '0';
        n = ((n - low) >>> 9) * 0x8E47CE423A2E9C6DL;
        if ((n & (-1L << 32)) == 0) {
            cursor = intIntoCharArray(buf, cursor, (int) n);
        } else {
            int lo32 = (int) n;
            int hi32 = (int) (n >>> 32);
            int midDigit = MOD_10_TABLE[
                (0x19999999 * lo32 + (lo32 >>> 1) + (lo32 >>> 3)) >>> 28];
            midDigit -= hi32 << 2;  
            if (midDigit < 0)
                midDigit += 10;
            buf[--cursor] = (char) (midDigit + '0');
            int rest = ((int) ((n - midDigit) >>> 1)) * 0xCCCCCCCD;
            cursor = intIntoCharArray(buf, cursor, rest);
        }
        if (negative)
            buf[--cursor] = '-';
        return new String(cursor, bufLen - cursor, buf);
    }
    static int intIntoCharArray(char[] buf, int cursor, int n) {
        while ((n & 0xffff0000) != 0) {
            int q = (int) ((0x51EB851FL * (n >>> 2)) >>> 35);
            int r = n - ((q << 6) + (q << 5) + (q << 2));  
            buf[--cursor] = Integer.ONES[r];
            buf[--cursor] = Integer.TENS[r];
            n = q;
        }
        while (n != 0) {
            int q = (0xCCCD * n) >>> 19;
            int r = n - ((q << 3) + (q << 1));  
            buf[--cursor] = (char) (r + '0');
            n = q;
        }
        return cursor;
    }
    public static String toString(long v, int radix) {
        int i = (int) v;
        if (i == v) {
            return Integer.toString(i, radix);
        }
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(v);
        }
        boolean negative = false;
        if (v < 0) {
            negative = true;
        } else {
            v = -v;
        }
        int bufLen = radix < 8 ? 65 : 23;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            long q = v / radix;
            buf[--cursor] = Integer.DIGITS[(int) (radix * q - v)];
            v = q;
        } while (v != 0);
        if (negative) {
            buf[--cursor] = '-';
        }
        return new String(cursor, bufLen - cursor, buf);
    }
    public static Long valueOf(String string) throws NumberFormatException {
        return valueOf(parseLong(string));
    }
    public static Long valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseLong(string, radix));
    }
    public static long highestOneBit(long v) {
        v |= (v >> 1);
        v |= (v >> 2);
        v |= (v >> 4);
        v |= (v >> 8);
        v |= (v >> 16);
        v |= (v >> 32);
        return v - (v >>> 1);
    }
    public static long lowestOneBit(long v) {
        return v & -v;
    }
    public static int numberOfLeadingZeros(long v) {
        if (v < 0) {
            return 0;
        }
        if (v == 0) {
            return 64;
        }
        int n = 1;
        int i = (int) (v >>> 32);
        if (i == 0) {
            n +=  32;
            i = (int) v;
        }
        if (i >>> 16 == 0) {
            n +=  16;
            i <<= 16;
        }
        if (i >>> 24 == 0) {
            n +=  8;
            i <<= 8;
        }
        if (i >>> 28 == 0) {
            n +=  4;
            i <<= 4;
        }
        if (i >>> 30 == 0) {
            n +=  2;
            i <<= 2;
        }
        return n - (i >>> 31);
    }
    public static int numberOfTrailingZeros(long v) {
        int low = (int) v;
        return low !=0 ? Integer.numberOfTrailingZeros(low)
                       : 32 + Integer.numberOfTrailingZeros((int) (v >>> 32));
    }
    public static int bitCount(long v) {
        v -=  (v >>> 1) & 0x5555555555555555L;
        v = (v & 0x3333333333333333L) + ((v >>> 2) & 0x3333333333333333L);
        int i =  ((int)(v >>> 32)) + (int) v;
        i = (i & 0x0F0F0F0F) + ((i >>> 4) & 0x0F0F0F0F);
        i += i >>> 8;
        i += i >>> 16;
        return i  & 0x0000007F;
    }
    public static long rotateLeft(long v, int distance) {
        return (v << distance) | (v >>> -distance);
    }
    public static long rotateRight(long v, int distance) {
        return (v >>> distance) | (v << -distance);
    }
    public static long reverseBytes(long v) {
        v = ((v >>> 8) & 0x00FF00FF00FF00FFL) | ((v & 0x00FF00FF00FF00FFL) << 8);
        v = ((v >>>16) & 0x0000FFFF0000FFFFL) | ((v & 0x0000FFFF0000FFFFL) <<16);
        return ((v >>>32)                   ) | ((v                      ) <<32);
    }
    public static long reverse(long v) {
        v = ((v >>> 1) & 0x5555555555555555L) | ((v & 0x5555555555555555L) << 1);
        v = ((v >>> 2) & 0x3333333333333333L) | ((v & 0x3333333333333333L) << 2);
        v = ((v >>> 4) & 0x0F0F0F0F0F0F0F0FL) | ((v & 0x0F0F0F0F0F0F0F0FL) << 4);
        v = ((v >>> 8) & 0x00FF00FF00FF00FFL) | ((v & 0x00FF00FF00FF00FFL) << 8);
        v = ((v >>>16) & 0x0000FFFF0000FFFFL) | ((v & 0x0000FFFF0000FFFFL) <<16);
        return ((v >>>32)                   ) | ((v                      ) <<32);
    }
    public static int signum(long v) {
        return v < 0 ? -1 : (v == 0 ? 0 : 1);
    }
    public static Long valueOf(long v) {
        return  v >= 128 || v < -128 ? new Long(v)
                                     : SMALL_VALUES[((int) v) + 128];
    }
    private static final Long[] SMALL_VALUES = new Long[256];
    static {
        for(int i = -128; i < 128; i++) {
            SMALL_VALUES[i + 128] = new Long(i);
        }
    }
}
