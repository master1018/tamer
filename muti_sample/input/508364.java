public final class Float extends Number implements Comparable<Float> {
    private static final long serialVersionUID = -2671257302660747028L;
    private final float value;
    public static final float MAX_VALUE = 3.40282346638528860e+38f;
    public static final float MIN_VALUE = 1.40129846432481707e-45f;
    public static final float NaN = 0.0f / 0.0f;
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE
            = (Class<Float>) float[].class.getComponentType();
    public static final int SIZE = 32;
    public Float(float value) {
        this.value = value;
    }
    public Float(double value) {
        this.value = (float) value;
    }
    public Float(String string) throws NumberFormatException {
        this(parseFloat(string));
    }
    public int compareTo(Float object) {
        return compare(value, object.value);
    }
    @Override
    public byte byteValue() {
        return (byte) value;
    }
    @Override
    public double doubleValue() {
        return value;
    }
    @Override
    public boolean equals(Object object) {
        return (object == this)
                || (object instanceof Float)
                && (floatToIntBits(this.value) == floatToIntBits(((Float) object).value));
    }
    public static native int floatToIntBits(float value);
    public static native int floatToRawIntBits(float value);
    @Override
    public float floatValue() {
        return value;
    }
    @Override
    public int hashCode() {
        return floatToIntBits(value);
    }
    public static native float intBitsToFloat(int bits);
    @Override
    public int intValue() {
        return (int) value;
    }
    public boolean isInfinite() {
        return isInfinite(value);
    }
    public static boolean isInfinite(float f) {
        return (f == POSITIVE_INFINITY) || (f == NEGATIVE_INFINITY);
    }
    public boolean isNaN() {
        return isNaN(value);
    }
    public static boolean isNaN(float f) {
        return f != f;
    }
    @Override
    public long longValue() {
        return (long) value;
    }
    public static float parseFloat(String string) throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseFloat(string);
    }
    @Override
    public short shortValue() {
        return (short) value;
    }
    @Override
    public String toString() {
        return Float.toString(value);
    }
    public static String toString(float f) {
        return org.apache.harmony.luni.util.NumberConverter.convert(f);
    }
    public static Float valueOf(String string) throws NumberFormatException {
        return parseFloat(string);
    }
    public static int compare(float float1, float float2) {
        if (float1 > float2) {
            return 1;
        }
        if (float2 > float1) {
            return -1;
        }
        if (float1 == float2 && 0.0f != float1) {
            return 0;
        }
        if (isNaN(float1)) {
            if (isNaN(float2)) {
                return 0;
            }
            return 1;
        } else if (isNaN(float2)) {
            return -1;
        }
        int f1 = floatToRawIntBits(float1);
        int f2 = floatToRawIntBits(float2);
        return (f1 >> 31) - (f2 >> 31);
    }
    public static Float valueOf(float f) {
        return new Float(f);
    }
    public static String toHexString(float f) {
        if (f != f) {
            return "NaN"; 
        }
        if (f == POSITIVE_INFINITY) {
            return "Infinity"; 
        }
        if (f == NEGATIVE_INFINITY) {
            return "-Infinity"; 
        }
        int bitValue = floatToIntBits(f);
        boolean negative = (bitValue & 0x80000000) != 0;
        int exponent = (bitValue & 0x7f800000) >>> 23;
        int significand = (bitValue & 0x007FFFFF) << 1;
        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0"); 
        }
        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x"); 
        } else {
            hexString.append("0x"); 
        }
        if (exponent == 0) { 
            hexString.append("0."); 
            int fractionDigits = 6;
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            String hexSignificand = Integer.toHexString(significand);
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-126"); 
        } else { 
            hexString.append("1."); 
            int fractionDigits = 6;
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            String hexSignificand = Integer.toHexString(significand);
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append('p');
            hexString.append(Integer.toString(exponent - 127));
        }
        return hexString.toString();
    }
}
