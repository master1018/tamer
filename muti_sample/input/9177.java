public final class StrictMath {
    private StrictMath() {}
    public static final double E = 2.7182818284590452354;
    public static final double PI = 3.14159265358979323846;
    public static native double sin(double a);
    public static native double cos(double a);
    public static native double tan(double a);
    public static native double asin(double a);
    public static native double acos(double a);
    public static native double atan(double a);
    public static strictfp double toRadians(double angdeg) {
        return angdeg / 180.0 * PI;
    }
    public static strictfp double toDegrees(double angrad) {
        return angrad * 180.0 / PI;
    }
    public static native double exp(double a);
    public static native double log(double a);
    public static native double log10(double a);
    public static native double sqrt(double a);
    public static native double cbrt(double a);
    public static native double IEEEremainder(double f1, double f2);
    public static double ceil(double a) {
        return floorOrCeil(a, -0.0, 1.0, 1.0);
    }
    public static double floor(double a) {
        return floorOrCeil(a, -1.0, 0.0, -1.0);
    }
    private static double floorOrCeil(double a,
                                      double negativeBoundary,
                                      double positiveBoundary,
                                      double sign) {
        int exponent = Math.getExponent(a);
        if (exponent < 0) {
            return ((a == 0.0) ? a :
                    ( (a < 0.0) ?  negativeBoundary : positiveBoundary) );
        } else if (exponent >= 52) {
            return a;
        }
        assert exponent >= 0 && exponent <= 51;
        long doppel = Double.doubleToRawLongBits(a);
        long mask   = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
        if ( (mask & doppel) == 0L )
            return a; 
        else {
            double result = Double.longBitsToDouble(doppel & (~mask));
            if (sign*a > 0.0)
                result = result + sign;
            return result;
        }
    }
    public static double rint(double a) {
        double twoToThe52 = (double)(1L << 52); 
        double sign = FpUtils.rawCopySign(1.0, a); 
        a = Math.abs(a);
        if (a < twoToThe52) { 
            a = ((twoToThe52 + a ) - twoToThe52);
        }
        return sign * a; 
    }
    public static native double atan2(double y, double x);
    public static native double pow(double a, double b);
    public static int round(float a) {
        return Math.round(a);
    }
    public static long round(double a) {
        return Math.round(a);
    }
    private static Random randomNumberGenerator;
    private static synchronized Random initRNG() {
        Random rnd = randomNumberGenerator;
        return (rnd == null) ? (randomNumberGenerator = new Random()) : rnd;
    }
    public static double random() {
        Random rnd = randomNumberGenerator;
        if (rnd == null) rnd = initRNG();
        return rnd.nextDouble();
    }
    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }
    public static long abs(long a) {
        return (a < 0) ? -a : a;
    }
    public static float abs(float a) {
        return (a <= 0.0F) ? 0.0F - a : a;
    }
    public static double abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }
    public static int max(int a, int b) {
        return (a >= b) ? a : b;
    }
    public static long max(long a, long b) {
        return (a >= b) ? a : b;
    }
    private static long negativeZeroFloatBits  = Float.floatToRawIntBits(-0.0f);
    private static long negativeZeroDoubleBits = Double.doubleToRawLongBits(-0.0d);
    public static float max(float a, float b) {
        if (a != a)
            return a;   
        if ((a == 0.0f) &&
            (b == 0.0f) &&
            (Float.floatToRawIntBits(a) == negativeZeroFloatBits)) {
            return b;
        }
        return (a >= b) ? a : b;
    }
    public static double max(double a, double b) {
        if (a != a)
            return a;   
        if ((a == 0.0d) &&
            (b == 0.0d) &&
            (Double.doubleToRawLongBits(a) == negativeZeroDoubleBits)) {
            return b;
        }
        return (a >= b) ? a : b;
    }
    public static int min(int a, int b) {
        return (a <= b) ? a : b;
    }
    public static long min(long a, long b) {
        return (a <= b) ? a : b;
    }
    public static float min(float a, float b) {
        if (a != a)
            return a;   
        if ((a == 0.0f) &&
            (b == 0.0f) &&
            (Float.floatToRawIntBits(b) == negativeZeroFloatBits)) {
            return b;
        }
        return (a <= b) ? a : b;
    }
    public static double min(double a, double b) {
        if (a != a)
            return a;   
        if ((a == 0.0d) &&
            (b == 0.0d) &&
            (Double.doubleToRawLongBits(b) == negativeZeroDoubleBits)) {
            return b;
        }
        return (a <= b) ? a : b;
    }
    public static double ulp(double d) {
        return sun.misc.FpUtils.ulp(d);
    }
    public static float ulp(float f) {
        return sun.misc.FpUtils.ulp(f);
    }
    public static double signum(double d) {
        return sun.misc.FpUtils.signum(d);
    }
    public static float signum(float f) {
        return sun.misc.FpUtils.signum(f);
    }
    public static native double sinh(double x);
    public static native double cosh(double x);
    public static native double tanh(double x);
    public static native double hypot(double x, double y);
    public static native double expm1(double x);
    public static native double log1p(double x);
    public static double copySign(double magnitude, double sign) {
        return sun.misc.FpUtils.copySign(magnitude, sign);
    }
    public static float copySign(float magnitude, float sign) {
        return sun.misc.FpUtils.copySign(magnitude, sign);
    }
    public static int getExponent(float f) {
        return sun.misc.FpUtils.getExponent(f);
    }
    public static int getExponent(double d) {
        return sun.misc.FpUtils.getExponent(d);
    }
    public static double nextAfter(double start, double direction) {
        return sun.misc.FpUtils.nextAfter(start, direction);
    }
    public static float nextAfter(float start, double direction) {
        return sun.misc.FpUtils.nextAfter(start, direction);
    }
    public static double nextUp(double d) {
        return sun.misc.FpUtils.nextUp(d);
    }
    public static float nextUp(float f) {
        return sun.misc.FpUtils.nextUp(f);
    }
    public static double scalb(double d, int scaleFactor) {
        return sun.misc.FpUtils.scalb(d, scaleFactor);
    }
    public static float scalb(float f, int scaleFactor) {
        return sun.misc.FpUtils.scalb(f, scaleFactor);
    }
}
