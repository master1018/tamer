@TestTargetClass(BigDecimal.class)
public class BigDecimalCompareTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for abs method.",
        method = "abs",
        args = {}
    )
    public void testAbsNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "123809648392384754573567356745735635678902957849027687.87678287";
        assertEquals("incorrect value", result, aNumber.abs().toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for abs method.",
        method = "abs",
        args = {}
    )
    public void testAbsPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "123809648392384754573567356745735635678902957849027687.87678287";
        assertEquals("incorrect value", result, aNumber.abs().toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "abs",
        args = {java.math.MathContext.class}
    )
    public void testAbsMathContextNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int precision = 15;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mc = new MathContext(precision, rm);
        String result = "1.23809648392385E+53";
        int resScale = -39;
        BigDecimal res = aNumber.abs(mc);
        assertEquals("incorrect value", result, res.toString());
        assertEquals("incorrect scale", resScale, res.scale());
        mc = new MathContext(34, RoundingMode.UP);
        assertEquals("incorrect value", "1.238096483923847545735673567457357E+53", aNumber.abs(mc).toString());
        mc = new MathContext(34, RoundingMode.DOWN);
        assertEquals("incorrect value", "1.238096483923847545735673567457356E+53", aNumber.abs(mc).toString());
        mc = new MathContext(34, RoundingMode.FLOOR);
        assertEquals("incorrect value", "1.238096483923847545735673567457356E+53", aNumber.abs(mc).toString());
        mc = new MathContext(34, RoundingMode.CEILING);
        assertEquals("incorrect value", "1.238096483923847545735673567457357E+53", aNumber.abs(mc).toString());
        mc = new MathContext(34, RoundingMode.UNNECESSARY);
        try {
            res = aNumber.abs(mc);
            fail("No ArithmeticException for RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "abs",
        args = {java.math.MathContext.class}
    )
    public void testAbsMathContextPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int precision = 41;
        RoundingMode rm = RoundingMode.HALF_EVEN;
        MathContext mc = new MathContext(precision, rm);
        String result = "1.2380964839238475457356735674573563567890E+53";
        int resScale = -13;
        BigDecimal res = aNumber.abs(mc);
        assertEquals("incorrect value", result, res.toString());
        assertEquals("incorrect scale", resScale, res.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareEqualScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareEqualScale2() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563923487289357829759278282992758247567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareGreaterScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 28;
        String b = "4573563567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareGreaterScale2() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 48;
        String b = "4573563567890295784902768787678287";
        int bScale = 2;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareLessScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563567890295784902768787678287";
        int bScale = 28;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigDecimal.class}
    )
    public void testCompareLessScale2() {
        String a = "12380964839238475457356735674573";
        int aScale = 36;
        String b = "45735635948573894578349572001798379183767890295784902768787678287";
        int bScale = 48;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "7472334223847623782375469293018787918347987234564568";
       int bScale = 13;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertFalse(aNumber.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 13;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertFalse(aNumber.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsUnequal3() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertFalse(aNumber.equals(b));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsEqual() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertEquals(aNumber, bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsNull() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertFalse(aNumber.equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for hashCode method.",
        method = "hashCode",
        args = {}
    )
    public void testHashCodeEqual() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertEquals("incorrect value", aNumber.hashCode(), bNumber.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for hashCode method.",
        method = "hashCode",
        args = {}
    )
    public void testHashCodeUnequal() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertTrue("incorrect value", aNumber.hashCode() != bNumber.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigDecimal.class}
    )
    public void testMaxEqual() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "8478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "8478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigDecimal.class}
    )
    public void testMaxUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigDecimal.class}
    )
    public void testMaxUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "94488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for min method.",
        method = "min",
        args = {java.math.BigDecimal.class}
    )
    public void testMinEqual() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "8478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "8478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for min method.",
        method = "min",
        args = {java.math.BigDecimal.class}
    )
    public void testMinUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for min method.",
        method = "min",
        args = {java.math.BigDecimal.class}
    )
    public void testMinUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "94488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "94488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for plus method.",
        method = "plus",
        args = {}
    )
    public void testPlusPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.plus());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "plus",
        args = {java.math.MathContext.class}
    )
    public void testPlusMathContextPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       int precision = 37;
       RoundingMode rm = RoundingMode.FLOOR;
       MathContext mc = new MathContext(precision, rm);
       String c = "929487820944884782312124789.8748298842";
       int cScale = 10;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.plus(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for plus method.",
        method = "plus",
        args = {}
    )
    public void testPlusNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.plus());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "plus",
        args = {java.math.MathContext.class}
    )
    public void testPlusMathContextNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 49;
       int precision = 46;
       RoundingMode rm = RoundingMode.CEILING;
       MathContext mc = new MathContext(precision, rm);
       String c = "-9294878209448847823.121247898748298842980877981";
       int cScale = 27;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.plus(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for negate method.",
        method = "negate",
        args = {}
    )
    public void testNegatePositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.negate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "negate",
        args = {java.math.MathContext.class}
    )
    public void testNegateMathContextPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       MathContext mc = new MathContext(37, RoundingMode.FLOOR);
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), 41);
       BigDecimal res = aNumber.negate(mc);
       assertEquals("incorrect value", "-929487820944884782312124789.8748298843", res.toString());
       assertEquals("incorrect scale", 10, res.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for negate method.",
        method = "negate",
        args = {}
    )
    public void testNegateNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.negate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Together with all other methods including a MathContext these tests for a complete test set.",
        method = "negate",
        args = {java.math.MathContext.class}
    )
    public void testNegateMathContextNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 49;
       int precision = 46;
       RoundingMode rm = RoundingMode.CEILING;
       MathContext mc = new MathContext(precision, rm);
       String c = "9294878209448847823.121247898748298842980877982";
       int cScale = 27;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.negate(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
    public void testSignumPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", 1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
    public void testSignumNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", -1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
    public void testSignumZero() {
       String a = "0";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", 0, aNumber.signum());
    }
}
