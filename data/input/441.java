public class RangeTests {
    private static int addTest(BigDecimal arg1, BigDecimal arg2, BigDecimal expectedResult) {
        int failures = 0;
        BigDecimal result = arg1.add(arg2);
        if (!result.equals(expectedResult)) {
            System.out.println("Sum:" +
                    arg1 + " + " +
                    arg2 + " == " +
                    result + "; expected  " +
                    expectedResult
            );
            failures++;
        }
        result = arg2.add(arg1);
        if (!result.equals(expectedResult)) {
            System.out.println("Sum:" +
                    arg2 + " + " +
                    arg1 + " == " +
                    result + "; expected  " +
                    expectedResult
            );
            failures++;
        }
        return failures;
    }
    private static int addBoundaryTest() {
        int failures = 0;
        failures += addTest(
                new BigDecimal("85070591730234615847396907784232501249"),
                BigDecimal.valueOf(0),
                new BigDecimal("85070591730234615847396907784232501249") );
        failures += addTest(
                new BigDecimal("-85070591730234615847396907784232501249"),
                BigDecimal.valueOf(0),
                new BigDecimal("-85070591730234615847396907784232501249") );
        failures += addTest(
                new BigDecimal("85070591730234615847396907784232501249"),
                BigDecimal.valueOf(1),
                new BigDecimal("85070591730234615847396907784232501250") );
        failures += addTest(
                new BigDecimal("85070591730234615847396907784232501249"),
                BigDecimal.valueOf(-1),
                new BigDecimal("85070591730234615847396907784232501248") );
        failures += addTest(
                new BigDecimal("-85070591730234615847396907784232501250"),
                BigDecimal.valueOf(-1),
                new BigDecimal("-85070591730234615847396907784232501251") );
        failures += addTest(
                new BigDecimal("-85070591730234615847396907784232501249"),
                BigDecimal.valueOf(1),
                new BigDecimal("-85070591730234615847396907784232501248") );
        failures += addTest(
                new BigDecimal("147573952589676412927"),
                BigDecimal.valueOf(Integer.MAX_VALUE),
                new BigDecimal("147573952591823896574") );
        failures += addTest(
                new BigDecimal("-147573952589676412927"),
                BigDecimal.valueOf(Integer.MAX_VALUE),
                new BigDecimal("-147573952587528929280") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(999),
                new BigDecimal("79228162514264337593543951334") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(Integer.MAX_VALUE/2),
                new BigDecimal("79228162514264337594617692158") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(Integer.MIN_VALUE/2),
                new BigDecimal("79228162514264337592470208511") );
        failures += addTest(
                new BigDecimal("-79228162514264337593543950335"),
                BigDecimal.valueOf(Integer.MAX_VALUE/2),
                new BigDecimal("-79228162514264337592470208512") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(-(Integer.MIN_VALUE/2)),
                new BigDecimal("79228162514264337594617692159") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(Long.MAX_VALUE/2),
                new BigDecimal("79228162518876023611971338238") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(Long.MIN_VALUE/2),
                new BigDecimal("79228162509652651575116562431") );
        failures += addTest(
                new BigDecimal("-79228162514264337593543950335"),
                BigDecimal.valueOf(Long.MAX_VALUE/2),
                new BigDecimal("-79228162509652651575116562432") );
        failures += addTest(
                new BigDecimal("79228162514264337593543950335"),
                BigDecimal.valueOf(-(Long.MIN_VALUE/2)),
                new BigDecimal("79228162518876023611971338239") );
        failures += addTest(
                new BigDecimal("-9223372036854775808"),
                BigDecimal.valueOf(1),
                new BigDecimal("-9223372036854775807") );
        failures += addTest(
                new BigDecimal("-9223372036854775808"),
                BigDecimal.valueOf(Long.MAX_VALUE/2),
                new BigDecimal("-4611686018427387905") );
        failures += addTest(
                new BigDecimal("9223372036854775808"),
                BigDecimal.valueOf(-1),
                new BigDecimal("9223372036854775807") );
        failures += addTest(
                new BigDecimal("9223372036854775808"),
                BigDecimal.valueOf(-Long.MAX_VALUE/2),
                new BigDecimal("4611686018427387905") );
        return failures;
    }
    private static int testRoundingFromBigInteger(BigInteger bi, int scale, MathContext mc) {
        int failures = 0;
        BigDecimal bd1 = new BigDecimal(bi,scale, mc);
        BigDecimal bd2 = (new BigDecimal(bi,scale)).round(mc);
        if (!bd1.equals(bd2)) {
            System.out.println("new BigDecimal(BigInteger,int,MathContext):" +
                    "BigInteger == " +
                    bi + ";  scale == " + scale + "; result == " +
                    bd1 + "; expected  == " +
                    bd2
            );
            failures++;
        }
        return failures;
    }
    private static int roundingConstructorTest() {
        int failures = 0;
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                7, MathContext.DECIMAL64);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                0, MathContext.DECIMAL64);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                -7, MathContext.DECIMAL64);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                7, MathContext.DECIMAL128);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                177, MathContext.DECIMAL128);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                177, MathContext.DECIMAL32);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                177, MathContext.UNLIMITED);
        failures += testRoundingFromBigInteger(
                new BigInteger("85070591730234615847396907784232501249"),
                0, MathContext.UNLIMITED);
        return failures;
    }
    private static int minLongConstructorTest(MathContext mc) {
        int failures = 0;
        BigDecimal bd1 = new BigDecimal(Long.MIN_VALUE,mc);
        BigDecimal bd2 = new BigDecimal(Long.MIN_VALUE).round(mc);
        if (!bd1.equals(bd2)) {
            System.out.println("new BigDecimal(long,MathContext):" +
                    "long == " +
                    Long.MIN_VALUE + "; result == " +
                    bd1 + "; expected  == " +
                    bd2
            );
            failures++;
        }
        return failures;
    }
    private static int minLongConstructorTest() {
        int failures = 0;
        failures+=minLongConstructorTest(MathContext.UNLIMITED);
        failures+=minLongConstructorTest(MathContext.DECIMAL32);
        failures+=minLongConstructorTest(MathContext.DECIMAL64);
        failures+=minLongConstructorTest(MathContext.DECIMAL128);
        return failures;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += addBoundaryTest();
        failures += roundingConstructorTest();
        failures += minLongConstructorTest();
        if (failures > 0) {
            throw new RuntimeException("Incurred " + failures +
                                       " failures while testing.");
        }
    }
}
