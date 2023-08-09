public final class Math {
    public static final double E = 2.718281828459045;
    public static final double PI = 3.141592653589793;
    private static java.util.Random random;
    private Math() {
    }
    public static double abs(double d) {
        long bits = Double.doubleToLongBits(d);
        bits &= 0x7fffffffffffffffL;
        return Double.longBitsToDouble(bits);
    }
    public static float abs(float f) {
        int bits = Float.floatToIntBits(f);
        bits &= 0x7fffffff;
        return Float.intBitsToFloat(bits);
    }
    public static int abs(int i) {
        return i >= 0 ? i : -i;
    }
    public static long abs(long l) {
        return l >= 0 ? l : -l;
    }
    public static native double acos(double d);
    public static native double asin(double d);
    public static native double atan(double d);
    public static native double atan2(double x, double y);
    public static native double cbrt(double d);
    public static native double ceil(double d);
    public static native double cos(double d);
    public static native double cosh(double d);
    public static native double exp(double d);
    public static native double expm1(double d);
    public static native double floor(double d);
    public static native double hypot(double x, double y);
    public static native double IEEEremainder(double x, double y);
    public static native double log(double d);
    public static native double log10(double d);
    public static native double log1p(double d);
    public static double max(double d1, double d2) {
        if (d1 > d2) {
            return d1;
        }
        if (d1 < d2) {
            return d2;
        }
        if (d1 != d2) {
            return Double.NaN;
        }
        if (Double.doubleToRawLongBits(d1) != 0) {
            return d2;
        }
        return 0.0d;
    }
    public static float max(float f1, float f2) {
        if (f1 > f2) {
            return f1;
        }
        if (f1 < f2) {
            return f2;
        }
        if (f1 != f2) {
            return Float.NaN;
        }
        if (Float.floatToRawIntBits(f1) != 0) {
            return f2;
        }
        return 0.0f;
    }
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }
    public static double min(double d1, double d2) {
        if (d1 > d2) {
            return d2;
        }
        if (d1 < d2) {
            return d1;
        }
        if (d1 != d2) {
            return Double.NaN;
        }
        if (Double.doubleToRawLongBits(d1) == 0x8000000000000000L) {
            return -0.0d;
        }
        return d2;
    }
    public static float min(float f1, float f2) {
        if (f1 > f2) {
            return f2;
        }
        if (f1 < f2) {
            return f1;
        }
        if (f1 != f2) {
            return Float.NaN;
        }
        if (Float.floatToRawIntBits(f1) == 0x80000000) {
            return -0.0f;
        }
        return f2;
    }
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }
    public static native double pow(double x, double y);
    public static native double rint(double d);
    public static long round(double d) {
        if (d != d) {
            return 0L;
        }
        return (long) floor(d + 0.5d);
    }
    public static int round(float f) {
        if (f != f) {
            return 0;
        }
        return (int) floor(f + 0.5f);
    }
    public static double signum(double d) {
        return StrictMath.signum(d);
    }
    public static float signum(float f) {
        return StrictMath.signum(f);
    }
    public static native double sin(double d);
    public static native double sinh(double d);
    public static native double sqrt(double d);
    public static native double tan(double d);
    public static native double tanh(double d);
    public static double random() {
        if (random == null) {
            random = new java.util.Random();
        }
        return random.nextDouble();
    }
    public static double toRadians(double angdeg) {
        return angdeg / 180d * PI;
    }
    public static double toDegrees(double angrad) {
        return angrad * 180d / PI;
    }
    public static double ulp(double d) {
        if (Double.isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        } else if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
            return pow(2, 971);
        }
        d = abs(d);
        return nextafter(d, Double.MAX_VALUE) - d;
    }
    public static float ulp(float f) {
        if (Float.isNaN(f)) {
            return Float.NaN;
        } else if (Float.isInfinite(f)) {
            return Float.POSITIVE_INFINITY;
        } else if (f == Float.MAX_VALUE || f == -Float.MAX_VALUE) {
            return (float) pow(2, 104);
        }
        f = abs(f);
        return nextafterf(f, Float.MAX_VALUE) - f;
    }
    private native static double nextafter(double x, double y);
    private native static float nextafterf(float x, float y);
}
