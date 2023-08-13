public class AbsPositiveZero {
    private static boolean isPositiveZero(float f) {
        return Float.floatToIntBits(f) == Float.floatToIntBits(0.0f);
    }
    private static boolean isPositiveZero(double d) {
        return Double.doubleToLongBits(d) == Double.doubleToLongBits(0.0d);
    }
    public static void main(String[] args) throws Exception {
        if (!isPositiveZero(Math.abs(-0.0d))) {
            throw new Exception("abs(-0.0d) failed");
        }
        if (!isPositiveZero(Math.abs(+0.0d))) {
            throw new Exception("abs(+0.0d) failed");
        }
        if (Math.abs(Double.POSITIVE_INFINITY) != Double.POSITIVE_INFINITY) {
            throw new Exception("abs(+Inf) failed");
        }
        if (Math.abs(Double.NEGATIVE_INFINITY) != Double.POSITIVE_INFINITY) {
            throw new Exception("abs(-Inf) failed");
        }
        double dnanval = Math.abs(Double.NaN);
        if (dnanval == dnanval) {
            throw new Exception("abs(NaN) failed");
        }
        if (!isPositiveZero(Math.abs(-0.0f))) {
            throw new Exception("abs(-0.0f) failed");
        }
        if (!isPositiveZero(Math.abs(+0.0f))) {
            throw new Exception("abs(+0.0f) failed");
        }
        if (Math.abs(Float.POSITIVE_INFINITY) != Float.POSITIVE_INFINITY) {
            throw new Exception("abs(+Inf) failed");
        }
        if (Math.abs(Float.NEGATIVE_INFINITY) != Float.POSITIVE_INFINITY) {
            throw new Exception("abs(-Inf) failed");
        }
        float fnanval = Math.abs(Float.NaN);
        if (fnanval == fnanval) {
            throw new Exception("abs(NaN) failed");
        }
    }
}
