public final class Integer extends Number implements Comparable<Integer> {
    private static final long serialVersionUID = 1360826667806852920L;
    private final int value;
    public static final int MAX_VALUE = 0x7FFFFFFF;
    public static final int MIN_VALUE = 0x80000000;
    public static final int SIZE = 32;
    private static final String[] SMALL_NONNEGATIVE_VALUES = new String[100];
    private static final String[] SMALL_NEGATIVE_VALUES = new String[100];
    static final char[] TENS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
    };
    static final char[] ONES = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };
    static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };
    private static final byte NTZ_TABLE[] = {
        32,  0,  1, 12,  2,  6, -1, 13,   3, -1,  7, -1, -1, -1, -1, 14,
        10,  4, -1, -1,  8, -1, -1, 25,  -1, -1, -1, -1, -1, 21, 27, 15,
        31, 11,  5, -1, -1, -1, -1, -1,   9, -1, -1, 24, -1, -1, 20, 26,
        30, -1, -1, -1, -1, 23, -1, 19,  29, -1, 22, 18, 28, 17, 16, -1
    };
    @SuppressWarnings("unchecked")
    public static final Class<Integer> TYPE
            = (Class<Integer>) int[].class.getComponentType();
    public Integer(int value) {
        this.value = value;
    }
    public Integer(String string) throws NumberFormatException {
        this(parseInt(string));
    }
    @Override
    public byte byteValue() {
        return (byte) value;
    }
    public int compareTo(Integer object) {
        int thisValue = value;
        int thatValue = object.value;
        return thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1);
    }
    public static Integer decode(String string) throws NumberFormatException {
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
            firstDigit = string.charAt(++i);
        }
        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (++i == length) {
                    throw new NumberFormatException("unable to parse '"+string+"' as integer");
                }
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (++i == length) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
            base = 16;
        }
        int result = parse(string, i, base, negative);
        return valueOf(result);
    }
    @Override
    public double doubleValue() {
        return value;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Integer && ((Integer) o).value == value;
    }
    @Override
    public float floatValue() {
        return value;
    }
    public static Integer getInteger(String string) {
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
    public static Integer getInteger(String string, int defaultValue) {
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
    public static Integer getInteger(String string, Integer defaultValue) {
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
        return value;
    }
    @Override
    public int intValue() {
        return value;
    }
    @Override
    public long longValue() {
        return value;
    }
    public static int parseInt(String string) throws NumberFormatException {
        return parseInt(string, 10);
    }
    public static int parseInt(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
        }
        return parse(string, i, radix, negative);
    }
    private static int parse(String string, int offset, int radix,
            boolean negative) throws NumberFormatException {
        int max = Integer.MIN_VALUE / radix;
        int result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
            if (max > result) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
            int next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
            }
        }
        return result;
    }
    @Override
    public short shortValue() {
        return (short) value;
    }
    public static String toBinaryString(int i) {
        int bufLen = 32;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = (char) ((i & 1) + '0');
        }  while ((i >>>= 1) != 0);
        return new String(cursor, bufLen - cursor, buf);
    }
    public static String toHexString(int i) {
        int bufLen = 8;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = DIGITS[i & 0xF];
        } while ((i >>>= 4) != 0);
        return new String(cursor, bufLen - cursor, buf);
    }
    public static String toOctalString(int i) {
        int bufLen = 11;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            buf[--cursor] = (char) ((i & 7) + '0');
        } while ((i >>>= 3) != 0);
        return new String(cursor, bufLen - cursor, buf);
    }
    @Override
    public String toString() {
        return Integer.toString(value);
    }
    public static String toString(int i) {
        boolean negative = false;
        if (i < 0) {
            negative = true;
            i = -i;
            if (i < 100) {
                if (i < 0) 
                    return "-2147483648";
                String result = SMALL_NEGATIVE_VALUES[i];
                if (result == null) {
                    SMALL_NEGATIVE_VALUES[i] = result =
                            i < 10 ? stringOf('-', ONES[i])
                                    : stringOf('-', TENS[i], ONES[i]);
                }
                return result;
            }
        } else {
            if (i < 100) {
                String result = SMALL_NONNEGATIVE_VALUES[i];
                if (result == null) {
                    SMALL_NONNEGATIVE_VALUES[i] = result =
                        i < 10 ? stringOf(ONES[i]) : stringOf(TENS[i], ONES[i]);
                }
                return result;
            }
        }
        int bufLen = 11; 
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        while (i >= (1 << 16)) {
            int q = (int) ((0x51EB851FL * i) >>> 37);
            int r = i - ((q << 6) + (q << 5) + (q << 2));  
            buf[--cursor] = ONES[r];
            buf[--cursor] = TENS[r];
            i = q;
        }
        while (i != 0) {
            int q = (0xCCCD * i) >>> 19;
            int r = i - ((q << 3) + (q << 1));  
            buf[--cursor] = (char) (r + '0');
            i = q;
        }
        if (negative)
            buf[--cursor] = '-';
        return new String(cursor, bufLen - cursor, buf);
    }
    private static String stringOf(char... args) {
        return new String(0, args.length, args);
    }
    public static String toString(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(i);
        }
        boolean negative = false;
        if (i < 0) {
            negative = true;
        } else {
            i = -i;
        }
        int bufLen = radix < 8 ? 33 : 12;  
        char[] buf = new char[bufLen];
        int cursor = bufLen;
        do {
            int q = i / radix;
            buf[--cursor] = DIGITS[radix * q - i];
            i = q;
        } while (i != 0);
        if (negative) {
            buf[--cursor] = '-';
        }
        return new String(cursor, bufLen - cursor, buf);
    }
    public static Integer valueOf(String string) throws NumberFormatException {
        return valueOf(parseInt(string));
    }
    public static Integer valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseInt(string, radix));
    }
    public static int highestOneBit(int i) {
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }
    public static int lowestOneBit(int i) {
        return i & -i;
    }
    public static int numberOfLeadingZeros(int i) {
        if (i <= 0) {
            return (~i >> 26) & 32;
        }
        int n = 1;
        if (i >> 16 == 0) {
            n +=  16;
            i <<= 16;
        }
        if (i >> 24 == 0) {
            n +=  8;
            i <<= 8;
        }
        if (i >> 28 == 0) {
            n +=  4;
            i <<= 4;
        }
        if (i >> 30 == 0) {
            n +=  2;
            i <<= 2;
        }
        return n - (i >>> 31);
    }
    public static int numberOfTrailingZeros(int i) {
        i &= -i;
        i = (i <<  4) + i;    
        i = (i <<  6) + i;    
        i = (i << 16) - i;    
        return NTZ_TABLE[i >>> 26]; 
    }
    public static int bitCount(int i) {
        i -= (i >> 1) & 0x55555555;
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        i = ((i >> 4) + i) & 0x0F0F0F0F;
        i += i >> 8;
        i += i >> 16;
        return i & 0x0000003F;
    }
    public static int rotateLeft(int i, int distance) {
        return (i << distance) | (i >>> -distance);
    }
    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << -distance);
    }
    public static int reverseBytes(int i) {
        i =    ((i >>>  8) & 0x00FF00FF) | ((i & 0x00FF00FF) <<  8);
        return ( i >>> 16              ) | ( i               << 16);
    }
    public static int reverse(int i) {
        i =    ((i >>>  1) & 0x55555555) | ((i & 0x55555555) <<  1);
        i =    ((i >>>  2) & 0x33333333) | ((i & 0x33333333) <<  2);
        i =    ((i >>>  4) & 0x0F0F0F0F) | ((i & 0x0F0F0F0F) <<  4);
        i =    ((i >>>  8) & 0x00FF00FF) | ((i & 0x00FF00FF) <<  8);
        return ((i >>> 16)             ) | ((i             ) << 16);
    }
    public static int signum(int i) {
        return (i >> 31) | (-i >>> 31); 
    }
    public static Integer valueOf(int i) {
        return  i >= 128 || i < -128 ? new Integer(i) : SMALL_VALUES[i + 128];
    }
    private static final Integer[] SMALL_VALUES = new Integer[256];
    static {
        for(int i = -128; i < 128; i++) {
            SMALL_VALUES[i + 128] = new Integer(i);
        }
    }
}
