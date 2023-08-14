public final class Math {
    private Math() {}
    public static final double E = 2.7182818284590452354;
    public static final double PI = 3.14159265358979323846;
    public static double sin(double a) {
        return StrictMath.sin(a); 
    }
    public static double cos(double a) {
        return StrictMath.cos(a); 
    }
    public static double tan(double a) {
        return StrictMath.tan(a); 
    }
    public static double asin(double a) {
        return StrictMath.asin(a); 
    }
    public static double acos(double a) {
        return StrictMath.acos(a); 
    }
    public static double atan(double a) {
        return StrictMath.atan(a); 
    }
    public static double toRadians(double angdeg) {
        return angdeg / 180.0 * PI;
    }
    public static double toDegrees(double angrad) {
        return angrad * 180.0 / PI;
    }
    public static double exp(double a) {
        return StrictMath.exp(a); 
    }
    public static double log(double a) {
        return StrictMath.log(a); 
    }
    public static double log10(double a) {
        return StrictMath.log10(a); 
    }
    public static double sqrt(double a) {
        return StrictMath.sqrt(a); 
    }
    public static double cbrt(double a) {
        return StrictMath.cbrt(a);
    }
    public static double IEEEremainder(double f1, double f2) {
        return StrictMath.IEEEremainder(f1, f2); 
    }
    public static double ceil(double a) {
        return StrictMath.ceil(a); 
    }
    public static double floor(double a) {
        return StrictMath.floor(a); 
    }
    public static double rint(double a) {
        return StrictMath.rint(a); 
    }
    public static double atan2(double y, double x) {
        return StrictMath.atan2(y, x); 
    }
    public static double pow(double a, double b) {
        return StrictMath.pow(a, b); 
    }
    public static int round(float a) {
        if (a != 0x1.fffffep-2f) 
            return (int)floor(a + 0.5f);
        else
            return 0;
    }
    public static long round(double a) {
        if (a != 0x1.fffffffffffffp-2) 
            return (long)floor(a + 0.5d);
        else
            return 0;
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
    private static long negativeZeroFloatBits = Float.floatToIntBits(-0.0f);
    private static long negativeZeroDoubleBits = Double.doubleToLongBits(-0.0d);
    public static float max(float a, float b) {
        if (a != a) return a;   
        if ((a == 0.0f) && (b == 0.0f)
            && (Float.floatToIntBits(a) == negativeZeroFloatBits)) {
            return b;
        }
        return (a >= b) ? a : b;
    }
    public static double max(double a, double b) {
        if (a != a) return a;   
        if ((a == 0.0d) && (b == 0.0d)
            && (Double.doubleToLongBits(a) == negativeZeroDoubleBits)) {
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
        if (a != a) return a;   
        if ((a == 0.0f) && (b == 0.0f)
            && (Float.floatToIntBits(b) == negativeZeroFloatBits)) {
            return b;
        }
        return (a <= b) ? a : b;
    }
    public static double min(double a, double b) {
        if (a != a) return a;   
        if ((a == 0.0d) && (b == 0.0d)
            && (Double.doubleToLongBits(b) == negativeZeroDoubleBits)) {
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
    public static double sinh(double x) {
        return StrictMath.sinh(x);
    }
    public static double cosh(double x) {
        return StrictMath.cosh(x);
    }
    public static double tanh(double x) {
        return StrictMath.tanh(x);
    }
    public static double hypot(double x, double y) {
        return StrictMath.hypot(x, y);
    }
    public static double expm1(double x) {
        return StrictMath.expm1(x);
    }
    public static double log1p(double x) {
        return StrictMath.log1p(x);
    }
    public static double copySign(double magnitude, double sign) {
        return sun.misc.FpUtils.rawCopySign(magnitude, sign);
    }
    public static float copySign(float magnitude, float sign) {
        return sun.misc.FpUtils.rawCopySign(magnitude, sign);
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
