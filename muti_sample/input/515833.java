@TestTargetClass(BigDecimal.class)
public class BigDecimalScaleOperationsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for scale method.",
        method = "scale",
        args = {}
    )
    public void testScaleDefault() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a));
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for scale method.",
        method = "scale",
        args = {}
    )
    public void testScaleNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -10;
        int cScale = -10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for scale method.",
        method = "scale",
        args = {}
    )
    public void testScalePos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for scale method.",
        method = "scale",
        args = {}
    )
    public void testScaleZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 0;
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "unscaledValue",
        args = {}
    )
    public void testUnscaledValue() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 100;
        BigInteger bNumber = new BigInteger(a);
        BigDecimal aNumber = new BigDecimal(bNumber, aScale);
        assertTrue("incorrect unscaled value", aNumber.unscaledValue().equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setScale method.",
        method = "setScale",
        args = {int.class}
    )
    public void testSetScaleGreater() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 18;
        int newScale = 28;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertEquals("incorrect value", 0, bNumber.compareTo(aNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setScale method.",
        method = "setScale",
        args = {int.class}
    )
    public void testSetScaleLess() {
        String a = "2.345726458768760000E+10";
        int newScale = 5;
        BigDecimal aNumber = new BigDecimal(a);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertEquals("incorrect value", 0, bNumber.compareTo(aNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setScale method.",
        method = "setScale",
        args = {int.class}
    )
    public void testSetScaleException() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        try {
            aNumber.setScale(newScale);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Rounding necessary", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setScale method.",
        method = "setScale",
        args = {int.class}
    )
    public void testSetScaleSame() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 18;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.equals(aNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundUp() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478139";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_UP);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundDown() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_DOWN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundCeiling() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478139";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_CEILING);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundFloor() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_FLOOR);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundHalfUp() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_UP);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundHalfDown() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_DOWN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleRoundHalfEven() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_EVEN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "setScale",
        args = {int.class, int.class}
    )
    public void testSetScaleIntRoundingMode() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.setScale(newScale, RoundingMode.HALF_EVEN);
        String res = "123121247898748298842980.877981045763478138";
        int resScale = 18;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "movePointLeft",
        args = {int.class}
    )
    public void testMovePointLeftPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = 18;
        int resScale = 46;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointLeft(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "movePointLeft",
        args = {int.class}
    )
    public void testMovePointLeftNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = -18;
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointLeft(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "movePointLeft",
        args = {int.class}
    )
    public void testMovePointLeftEx() {
        BigDecimal a = new BigDecimal("12345.6789012345678901234567890123456789");
        BigDecimal res = a.movePointLeft(10);
        assertEquals("incorrect scale", 44, res.scale());
        assertEquals("incorrect value", "0.00000123456789012345678901234567890123456789", res.toString());
        res = a.movePointLeft(-50);
        assertEquals("incorrect scale", 0, res.scale());
        assertEquals("incorrect value", "1234567890123456789012345678901234567890000000000000000", res.toString());
        try {
            res = a.movePointLeft(Integer.MAX_VALUE - 2);
            fail("ArithmeticException is not thrown");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for movePointRight method.",
        method = "movePointRight",
        args = {int.class}
    )
    public void testMovePointRightPosGreater() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = 18;
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for movePointRight method.",
        method = "movePointRight",
        args = {int.class}
    )
    public void testMovePointRightPosLess() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138475679498700";
        int aScale = 28;
        int shift = 30;
        int resScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for movePointRight method.",
        method = "movePointRight",
        args = {int.class}
    )
    public void testMovePointRightNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = -18;
        int resScale = 46;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for movePointRight method.",
        method = "movePointRight",
        args = {int.class}
    )
    public void testMovePointRightException() {
        String a = "12312124789874829887348723648726347429808779810457634781384756794987";
        int aScale = Integer.MAX_VALUE; 
        int shift = -18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        try {
            aNumber.movePointRight(shift);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Underflow", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "movePointRight",
        args = {int.class}
    )
    public void testMovePointRightEx() {
        BigDecimal a = new BigDecimal("12345.6789012345678901234567890123456789");
        BigDecimal res = a.movePointRight(10);
        assertEquals("incorrect scale", 24, res.scale());
        assertEquals("incorrect value", "123456789012345.678901234567890123456789", res.toString());
        res = a.movePointRight(-50);
        assertEquals("incorrect scale", 84, res.scale());
        assertEquals("incorrect value", "1.23456789012345678901234567890123456789E-46", res.toString());
        try {
            res = a.movePointRight(Integer.MIN_VALUE + 2);
            fail("ArithmeticException is not thrown");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "movePointRight",
        args = {int.class}
    )
    @KnownFailure("Throws OutOfMemoryError instead of ArithmeticException!")
    public void testMovePointRightEx2() {
        BigDecimal a = new BigDecimal("123456789012345678901234567890123456789E25");
        try {
            BigDecimal res = a.movePointRight(Integer.MAX_VALUE - 2);
            fail("ArithmeticException is not thrown");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scaleByPowerOfTen",
        args = {int.class}
    )
    public void testScaleByPowerOfTenEx() {
        BigDecimal a = new BigDecimal("12345.6789012345678901234567890123456789");
        BigDecimal res = a.movePointRight(10);
        assertEquals("incorrect scale", 24, res.scale());
        assertEquals("incorrect value", "123456789012345.678901234567890123456789", res.toString());
        res = a.scaleByPowerOfTen(-50);
        assertEquals("incorrect scale", 84, res.scale());
        assertEquals("incorrect value", "1.23456789012345678901234567890123456789E-46", res.toString());
        res = a.scaleByPowerOfTen(50);
        assertEquals("incorrect scale", -16, res.scale());
        assertEquals("incorrect value", "1.23456789012345678901234567890123456789E+54", res.toString());
        try {
            res = a.scaleByPowerOfTen(Integer.MIN_VALUE + 2);
            fail("ArithmeticException is not thrown");
        } catch (ArithmeticException e) {
        }
        a = new BigDecimal("123456789012345678901234567890123456789E25");
        try {
            res = a.scaleByPowerOfTen(Integer.MAX_VALUE - 2);
            fail("ArithmeticException is not thrown");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "precision",
        args = {}
    )
    public void testPrecision() {
        String a = "12312124789874829887348723648726347429808779810457634781384756794987";
        int aScale = 14;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        int prec = aNumber.precision();
        assertEquals(68, prec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "precision",
        args = {}
    )
    public void testSetScale() {
        BigDecimal x1 = new BigDecimal(1.23400);
        BigDecimal x2 = x1.setScale(75);
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
        x1.precision();
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
        x2.precision();
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
    }
}
