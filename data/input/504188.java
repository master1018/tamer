@TestTargetClass(BigDecimal.class)
public class BigDecimalConstructorsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests BigDecimal.ONE to be 1.0d",
        method = "!field:BigDecimal.ONE"
    )        
    public void testFieldONE() {
        String oneS = "1";
        double oneD = 1.0;
        assertEquals("incorrect string value", oneS, BigDecimal.ONE.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.ONE.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests BigDecimal.TEN to be 10.0d",
        method = "!field:BigDecimal.TEN"
    )        
    public void testFieldTEN() {
        String oneS = "10";
        double oneD = 10.0;
        assertEquals("incorrect string value", oneS, BigDecimal.TEN.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.TEN.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests BigDecimal.ZERO to be 0.0d",
        method = "!field:BigDecimal.ZERO"
    )            
    public void testFieldZERO() {
        String oneS = "0";
        double oneD = 0.0;
        assertEquals("incorrect string value", oneS, BigDecimal.ZERO.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.ZERO.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.math.BigInteger.class}
    )
    public void testConstrBI() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        BigDecimal aNumber = new BigDecimal(bA);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", 0, aNumber.scale());
        try {
            new BigDecimal((BigInteger) null);
            fail("No NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.math.BigInteger.class, int.class}
    )
    public void testConstrBIScale() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int aScale = 10;
        BigDecimal aNumber = new BigDecimal(bA, aScale);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.math.BigInteger.class, java.math.MathContext.class}
    )
    public void testConstrBigIntegerMathContext() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int precision = 46;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "1231212478987482988429808779810457634781384757";
        int resScale = -6;
        BigDecimal result = new BigDecimal(bA, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
        BigDecimal bd;
        BigInteger bi =  new BigInteger( "12345678901234567890123456789012345");
        BigInteger nbi = new BigInteger("-12345678901234567890123456789012345");
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678902E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678902E+34", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678E+34", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.23456789012345678901234567890124E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.23456789012345678901234567890123E+34", bd.toString());
        mc = new MathContext(34, RoundingMode.FLOOR);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+34", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_EVEN);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+34", bd.toString());
        bd = new BigDecimal(new BigInteger("-12345678901234567890123456789012335"), mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+34", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_UP);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901235E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+34", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_DOWN);
        bd = new BigDecimal(bi, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+34", bd.toString());
        bd = new BigDecimal(nbi, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+34", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(bi, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbi, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "BigDecimal",
        args = {java.math.BigInteger.class, int.class, java.math.MathContext.class}
    )
    public void testConstrBigIntegerScaleMathContext() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int aScale = 10;
        int precision = 46;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "1231212478987482988429808779810457634781384757";
        int resScale = 4;
        BigDecimal result = new BigDecimal(bA, aScale, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
        BigDecimal bd;
        BigInteger bi =  new BigInteger( "12345678901234567890123456789012345");
        BigInteger nbi = new BigInteger("-12345678901234567890123456789012345");
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678902E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678902E+44", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678E+44", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.23456789012345678901234567890124E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.23456789012345678901234567890123E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.FLOOR);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_EVEN);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(new BigInteger("-12345678901234567890123456789012335"), -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_UP);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901235E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_DOWN);
        bd = new BigDecimal(bi, -10, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbi, -10, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(bi, -10, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbi, -10, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678902", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678902", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.67890124", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.67890123", bd.toString());
        mc = new MathContext(34, RoundingMode.FLOOR);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678901234", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678901235", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_EVEN);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678901234", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678901234", bd.toString());
        bd = new BigDecimal(new BigInteger("-12345678901234567890123456789012335"), 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678901234", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_UP);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678901235", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678901235", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_DOWN);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678901234", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.678901234", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(bi, 10, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbi, 10, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        mc = new MathContext(28, RoundingMode.FLOOR);
        bd = new BigDecimal(bi, 10, mc);
        assertEquals("incorrect value",  "1234567890123456789012345.678", bd.toString());
        bd = new BigDecimal(nbi, 10, mc);
        assertEquals("incorrect value", "-1234567890123456789012345.679", bd.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "BigDecimal",
        args = {java.math.BigInteger.class, int.class, java.math.MathContext.class}
    )
    public void testConstrBigIntegerScaleMathContext_AndroidFailure() {
        MathContext mc;
        BigDecimal bd;
        mc = new MathContext(17, RoundingMode.FLOOR);
        bd = new BigDecimal(new BigInteger("123456789012345678"), 3, mc);
        assertEquals("incorrect value", "123456789012345.67", bd.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {char[].class}
    )
    public void testConstrChar() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        BigDecimal result = new BigDecimal(value);
        String res = "-1.23804738E-419";
        int resScale = 427;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        try {
            new BigDecimal(new char[] {});
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {char[].class, int.class, int.class}
    )
    public void testConstrCharIntInt() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 12;
        BigDecimal result = new BigDecimal(value, offset, len);
        String res = "3.804738E-40";
        int resScale = 46;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        try {
            new BigDecimal(new char[] {}, 0, 0);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {char[].class, int.class, int.class, java.math.MathContext.class}
    )
    public void testConstrCharIntIntMathContext() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 12;
        int precision = 4;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        BigDecimal result = new BigDecimal(value, offset, len, mc);
        String res = "3.805E-40";
        int resScale = 43;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        try {
            new BigDecimal(new char[] {}, 0, 0, MathContext.DECIMAL32);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
        char[] biCA =  "bla: 12345678901234567890123456789012345.0E+10, and more bla".toCharArray();
        char[] nbiCA = "bla: -12345678901234567890123456789012345.E+10, and more bla".toCharArray();
        BigDecimal bd;
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678902E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678902E+44", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678E+44", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.23456789012345678901234567890124E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.23456789012345678901234567890123E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.FLOOR);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_EVEN);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal("-123456789012345678901234567890123350000000000".toCharArray(), 0, 46, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_UP);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901235E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901235E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.HALF_DOWN);
        bd = new BigDecimal(biCA, 5, 41, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678901234E+44", bd.toString());
        bd = new BigDecimal(nbiCA, 5, 41, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678901234E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(biCA, 5, 41, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbiCA, 5, 41, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {char[].class, int.class, int.class, java.math.MathContext.class}
    )
    public void testConstrCharIntIntMathContextException1() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 120;
        int precision = 4;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        try {
            new BigDecimal(value, offset, len, mc);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {char[].class, java.math.MathContext.class}
    )
    public void testConstrCharMathContext() {
        try {
            new BigDecimal(new char[] {}, MathContext.DECIMAL32);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
        char[] biCA =  "12345678901234567890123456789012345.0E+10".toCharArray();
        char[] nbiCA = "-12345678901234567890123456789012345.E+10".toCharArray();
        BigDecimal bd;
        MathContext mc;
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(biCA, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678902E+44", bd.toString());
        bd = new BigDecimal(nbiCA, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678902E+44", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(biCA, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678E+44", bd.toString());
        bd = new BigDecimal(nbiCA, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678E+44", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(biCA, mc);
        assertEquals("incorrect value",  "1.23456789012345678901234567890124E+44", bd.toString());
        bd = new BigDecimal(nbiCA, mc);
        assertEquals("incorrect value", "-1.23456789012345678901234567890123E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(biCA, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbiCA, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDoubleNaN() {
        double a = Double.NaN;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN", e
                    .getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDoublePosInfinity() {
        double a = Double.POSITIVE_INFINITY;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN",
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDoubleNegInfinity() {
        double a = Double.NEGATIVE_INFINITY;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN",
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDouble() {
        double a = 732546982374982347892379283571094797.287346782359284756;
        int aScale = 0;
        BigInteger bA = new BigInteger("732546982374982285073458350476230656");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "BigDecimal",
        args = {double.class, java.math.MathContext.class}
    )
    public void testConstrDoubleMathContext() {
        double a = 732546982374982347892379283571094797.287346782359284756;
        int precision = 21;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "732546982374982285074";
        int resScale = -15;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
        BigDecimal bd;
        mc = new MathContext(9, RoundingMode.UP);
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456790", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-123456790", bd.toString());
        mc = new MathContext(8, RoundingMode.DOWN);
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "1.2345678E+8", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-1.2345678E+8", bd.toString());
        mc = new MathContext(10, RoundingMode.CEILING);
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456789.2", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-123456789.1", bd.toString());
        mc = new MathContext(8, RoundingMode.FLOOR);
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "1.2345678E+8", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-1.2345679E+8", bd.toString());
        mc = new MathContext(11, RoundingMode.HALF_EVEN);
        bd = new BigDecimal("123456789.125", mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(-123456789.135, mc);
        assertEquals("incorrect value", "-123456789.14", bd.toString());
        mc = new MathContext(11, RoundingMode.HALF_UP);
        bd = new BigDecimal("123456789.125", mc);
        assertEquals("incorrect value",  "123456789.13", bd.toString());
        mc = new MathContext(11, RoundingMode.HALF_DOWN);
        bd = new BigDecimal("123456789.125", mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        mc = new MathContext(8, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(123456789.125, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(-123456789.125, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "BigDecimal",
        args = {double.class, java.math.MathContext.class}
    )
    public void testConstrDoubleMathContext_AndroidFailure() {
        BigDecimal bd;
        MathContext mc;
        mc = new MathContext(11, RoundingMode.HALF_EVEN);
        bd = new BigDecimal("123456789.125", mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-123456789.12", bd.toString());
        mc = new MathContext(11, RoundingMode.HALF_UP);
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456789.13", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-123456789.13", bd.toString());
        mc = new MathContext(11, RoundingMode.HALF_DOWN);
        bd = new BigDecimal("123456789.125", mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(123456789.125, mc);
        assertEquals("incorrect value",  "123456789.12", bd.toString());
        bd = new BigDecimal(-123456789.125, mc);
        assertEquals("incorrect value", "-123456789.12", bd.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDouble01() {
        double a = 1.E-1;
        int aScale = 55;
        BigInteger bA = new BigInteger("1000000000000000055511151231257827021181583404541015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDouble02() {
        double a = 0.555;
        int aScale = 53;
        BigInteger bA = new BigInteger("55500000000000004884981308350688777863979339599609375");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDoubleMinus01() {
        double a = -1.E-1;
        int aScale = 55;
        BigInteger bA = new BigInteger("-1000000000000000055511151231257827021181583404541015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {int.class}
    )
    public void testConstrInt() {
        int a = 732546982;
        String res = "732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {int.class, java.math.MathContext.class}
    )
    public void testConstrIntMathContext() {
        int a = 732546982;
        int precision = 21;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {long.class}
    )
    public void testConstrLong() {
        long a = 4576578677732546982L;
        String res = "4576578677732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {long.class, java.math.MathContext.class}
    )
    public void testConstrLongMathContext() {
        long a = 4576578677732546982L;
        int precision = 5;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "45766";
        int resScale = -14;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
        BigDecimal bd;
        mc = new MathContext(15, RoundingMode.UP);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.89012345678902E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.89012345678902E+16", bd.toString());
        mc = new MathContext(12, RoundingMode.DOWN);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.89012345678E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.89012345678E+16", bd.toString());
        mc = new MathContext(15, RoundingMode.CEILING);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.89012345678902E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.89012345678901E+16", bd.toString());
        mc = new MathContext(12, RoundingMode.FLOOR);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.89012345678E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.89012345679E+16", bd.toString());
        mc = new MathContext(16, RoundingMode.HALF_EVEN);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.890123456789012E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.890123456789012E+16", bd.toString());
        bd = new BigDecimal(-78901234567890135L, mc);
        assertEquals("incorrect value", "-7.890123456789014E+16", bd.toString());
        mc = new MathContext(16, RoundingMode.HALF_UP);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.890123456789013E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.890123456789013E+16", bd.toString());
        mc = new MathContext(16, RoundingMode.HALF_DOWN);
        bd = new BigDecimal(78901234567890125L, mc);
        assertEquals("incorrect value",  "7.890123456789012E+16", bd.toString());
        bd = new BigDecimal(-78901234567890125L, mc);
        assertEquals("incorrect value", "-7.890123456789012E+16", bd.toString());
        mc = new MathContext(8, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(78901234567890125L, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(-78901234567890125L, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(double) constructor.",
        method = "BigDecimal",
        args = {double.class}
    )
    public void testConstrDoubleDenormalized() {
        double a = 2.274341322658976E-309;
        int aScale = 1073;
        BigInteger bA = new BigInteger("227434132265897633950269241702666687639731047124115603942986140264569528085692462493371029187342478828091760934014851133733918639492582043963243759464684978401240614084312038547315281016804838374623558434472007664427140169018817050565150914041833284370702366055678057809362286455237716100382057360123091641959140448783514464639706721250400288267372238950016114583259228262046633530468551311769574111763316146065958042194569102063373243372766692713192728878701004405568459288708477607744497502929764155046100964958011009313090462293046650352146796805866786767887226278836423536035611825593567576424943331337401071583562754098901412372708947790843318760718495117047155597276492717187936854356663665005157041552436478744491526494952982062613955349661409854888916015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringException() {
        String a = "-238768.787678287a+10";
        try {
            BigDecimal bd = new BigDecimal(a);
            fail("NumberFormatException has not been caught: " + bd.toString());
        } catch (NumberFormatException e) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExceptionEmptyExponent1() {
        String a = "-238768.787678287e";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExceptionEmptyExponent2() {
        String a = "-238768.787678287e-";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExceptionExponentGreaterIntegerMax() {
        String a = "-238768.787678287e214748364767876";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExceptionExponentLessIntegerMin() {
        String a = "-238768.787678287e-214748364767876";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExponentIntegerMax() {
        String a = "-238768.787678287e2147483647";
        int aScale = -2147483638;
        BigInteger bA = new BigInteger("-238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringExponentIntegerMin() {
        String a = ".238768e-2147483648";
        try {
           new BigDecimal(a);
           fail("NumberFormatException expected");
       } catch (NumberFormatException e) {
           assertEquals("Improper exception message","Scale out of range.", 
               e.getMessage());
       }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithoutExpPos1() {
        String a = "732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithoutExpPos2() {
        String a = "+732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithoutExpNeg() {
        String a = "-732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("-732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithoutExpWithoutPoint() {
        String a = "-732546982374982347892379283571094797287346782359284756";
        int aScale = 0;
        BigInteger bA = new BigInteger("-732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithoutPoint1() {
        String a = "-238768787678287e214";
        int aScale = -214;
        BigInteger bA = new BigInteger("-238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithoutPoint2() {
        String a = "-238768787678287e-214";
        int aScale = 214;
        BigInteger bA = new BigInteger("-238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithoutPoint3() {
        String a = "238768787678287e-214";
        int aScale = 214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithoutPoint4() {
        String a = "238768787678287e+214";
        int aScale = -214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithoutPoint5() {
        String a = "238768787678287E214";
        int aScale = -214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithPoint1() {
        String a = "23985439837984782435652424523876878.7678287e+214";
        int aScale = -207;
        BigInteger bA = new BigInteger("239854398379847824356524245238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithPoint2() {
        String a = "238096483923847545735673567457356356789029578490276878.7678287e-214";
        int aScale = 221;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithPoint3() {
        String a = "2380964839238475457356735674573563567890.295784902768787678287E+21";
        int aScale = 0;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithPoint4() {
        String a = "23809648392384754573567356745735635678.90295784902768787678287E+21";
        int aScale = 2;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for BigDecimal(String) constructor.",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void testConstrStringWithExponentWithPoint5() {
        String a = "238096483923847545735673567457356356789029.5784902768787678287E+21";
        int aScale = -2;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.lang.String.class, java.math.MathContext.class}
    )
    public void testConstrStringMathContext() {
        String a = "-238768787678287e214";
        int precision = 5;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "-23876";
        int resScale = -224;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
        String biStr = new String( "12345678901234567890123456789012345.0E+10");
        String nbiStr = new String("-12345678901234567890123456789012345.E+10");
        BigDecimal bd;
        mc = new MathContext(31, RoundingMode.UP);
        bd = new BigDecimal(biStr, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678902E+44", bd.toString());
        bd = new BigDecimal(nbiStr, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678902E+44", bd.toString());
        mc = new MathContext(28, RoundingMode.DOWN);
        bd = new BigDecimal(biStr, mc);
        assertEquals("incorrect value",  "1.234567890123456789012345678E+44", bd.toString());
        bd = new BigDecimal(nbiStr, mc);
        assertEquals("incorrect value", "-1.234567890123456789012345678E+44", bd.toString());
        mc = new MathContext(33, RoundingMode.CEILING);
        bd = new BigDecimal(biStr, mc);
        assertEquals("incorrect value",  "1.23456789012345678901234567890124E+44", bd.toString());
        bd = new BigDecimal(nbiStr, mc);
        assertEquals("incorrect value", "-1.23456789012345678901234567890123E+44", bd.toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            bd = new BigDecimal(biStr, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        try {
            bd = new BigDecimal(nbiStr, mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
        mc = new MathContext(7, RoundingMode.FLOOR);
        bd = new BigDecimal("1000000.9", mc);
        assertEquals("incorrect value", "1000000", bd.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.math.BigInteger.class, int.class}
    )
    public void test_Constructor_java_math_BigInteger_int() {
        BigInteger value = new BigInteger("12345908");
        BigDecimal big = new BigDecimal(value);
        assertTrue("the BigDecimal value is not initialized properly", 
                big.unscaledValue().equals(value)
                && big.scale() == 0);
        BigInteger value2 = new BigInteger("12334560000");
        BigDecimal big2 = new BigDecimal(value2, 5);
        assertTrue("the BigDecimal value is not initialized properly", 
                big2.unscaledValue().equals(value2)
                && big2.scale() == 5);
        assertTrue("the BigDecimal value is not represented properly", big2.toString().equals(
                "123345.60000"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {double.class}
    )
    public void test_Constructor_Double() {
        BigDecimal big = new BigDecimal(123E04);
        assertTrue("the BigDecimal value taking a double argument is not initialized properly", big
                .toString().equals("1230000"));
        big = new BigDecimal(1.2345E-12);
        assertTrue("the double representation is not correct for 1.2345E-12",
                big.doubleValue() == 1.2345E-12);
        big = new BigDecimal(-12345E-3);
        assertTrue("the double representation is not correct for -12345E-3",
                big.doubleValue() == -12.345);
        big = new BigDecimal(5.1234567897654321e138);
        assertTrue("the double representation is not correct for 5.1234567897654321e138", big
                .doubleValue() == 5.1234567897654321E138
                && big.scale() == 0);
        big = new BigDecimal(0.1);
        assertTrue("the double representation of 0.1 bigDecimal is not correct",
                big.doubleValue() == 0.1);
        big = new BigDecimal(0.00345);
        assertTrue("the double representation of 0.00345 bigDecimal is not correct", big
                .doubleValue() == 0.00345);
        big = new BigDecimal(-0.0);
        assertTrue("the double representation of -0.0 bigDecimal is not correct", big.scale() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BigDecimal",
        args = {java.lang.String.class}
    )
    public void test_Constructor_java_lang_String() throws NumberFormatException {
        BigDecimal big = new BigDecimal("345.23499600293850");
        assertTrue("the BigDecimal value is not initialized properly", big.toString().equals(
                "345.23499600293850")
                && big.scale() == 14);
        big = new BigDecimal("-12345");
        assertTrue("the BigDecimal value is not initialized properly", big.toString().equals(
                "-12345")
                && big.scale() == 0);
        big = new BigDecimal("123.");
        assertTrue("the BigDecimal value is not initialized properly", big.toString().equals("123")
                && big.scale() == 0);
    }
}
