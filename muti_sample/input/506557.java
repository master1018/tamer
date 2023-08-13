public final class Double extends Number implements Comparable<Double> {
    private static final long serialVersionUID = -9172774392245257468L;
    private final double value;
    public static final double MAX_VALUE = 1.79769313486231570e+308;
    public static final double MIN_VALUE = 5e-324;
    public static final double NaN = 0.0 / 0.0;
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE
            = (Class<Double>) double[].class.getComponentType();
    public static final int SIZE = 64;
    public Double(double value) {
        this.value = value;
    }
    public Double(String string) throws NumberFormatException {
        this(parseDouble(string));
    }
    public int compareTo(Double object) {
        return compare(value, object.value);
    }
    @Override
    public byte byteValue() {
        return (byte) value;
    }
    public static native long doubleToLongBits(double value);
    public static native long doubleToRawLongBits(double value);
    @Override
    public double doubleValue() {
        return value;
    }
    @Override
    public boolean equals(Object object) {
        return (object == this)
                || (object instanceof Double)
                && (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
    }
    @Override
    public float floatValue() {
        return (float) value;
    }
    @Override
    public int hashCode() {
        long v = doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }
    @Override
    public int intValue() {
        return (int) value;
    }
    public boolean isInfinite() {
        return isInfinite(value);
    }
    public static boolean isInfinite(double d) {
        return (d == POSITIVE_INFINITY) || (d == NEGATIVE_INFINITY);
    }
    public boolean isNaN() {
        return isNaN(value);
    }
    public static boolean isNaN(double d) {
        return d != d;
    }
    public static native double longBitsToDouble(long bits);
    @Override
    public long longValue() {
        return (long) value;
    }
    public static double parseDouble(String string)
            throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseDouble(string);
    }
    @Override
    public short shortValue() {
        return (short) value;
    }
    @Override
    public String toString() {
        return Double.toString(value);
    }
    public static String toString(double d) {
        return org.apache.harmony.luni.util.NumberConverter.convert(d);
    }
    public static Double valueOf(String string) throws NumberFormatException {
        return parseDouble(string);
    }
    public static int compare(double double1, double double2) {
        if (double1 > double2) {
            return 1;
        }
        if (double2 > double1) {
            return -1;
        }
        if (double1 == double2 && 0.0d != double1) {
            return 0;
        }
        if (isNaN(double1)) {
            if (isNaN(double2)) {
                return 0;
            }
            return 1;
        } else if (isNaN(double2)) {
            return -1;
        }
        long d1 = doubleToRawLongBits(double1);
        long d2 = doubleToRawLongBits(double2);
        return (int) ((d1 >> 63) - (d2 >> 63));
    }
    public static Double valueOf(double d) {
        return new Double(d);
    }
    public static String toHexString(double d) {
        if (d != d) {
            return "NaN"; 
        }
        if (d == POSITIVE_INFINITY) {
            return "Infinity"; 
        }
        if (d == NEGATIVE_INFINITY) {
            return "-Infinity"; 
        }
        long bitValue = doubleToLongBits(d);
        boolean negative = (bitValue & 0x8000000000000000L) != 0;
        long exponent = (bitValue & 0x7FF0000000000000L) >>> 52;
        long significand = bitValue & 0x000FFFFFFFFFFFFFL;
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
            int fractionDigits = 13;
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            String hexSignificand = Long.toHexString(significand);
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-1022"); 
        } else { 
            hexString.append("1."); 
            int fractionDigits = 13;
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            String hexSignificand = Long.toHexString(significand);
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append('p');
            hexString.append(Long.toString(exponent - 1023));
        }
        return hexString.toString();
    }
}
