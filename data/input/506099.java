@TestTargetClass(MathContext.class)
public class MathContextTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "MathContext",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "MathContext",
            args = {int.class, java.math.RoundingMode.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "MathContext",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrecision",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getRoundingMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "equals",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "hashCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "toString",
            args = {}
        )
    })
    public void test_MathContextConstruction() {
        String a = "-12380945E+61";
        BigDecimal aNumber = new BigDecimal(a);
        MathContext mcIntRm6hd = new MathContext(6, RoundingMode.HALF_DOWN);
        MathContext mcStr6hd = new MathContext("precision=6 roundingMode=HALF_DOWN");
        MathContext mcInt6 = new MathContext(6);
        MathContext mcInt134 = new MathContext(134);
        assertEquals("MathContext.getPrecision() returns incorrect value",
                6, mcIntRm6hd.getPrecision() );
        assertEquals("MathContext.getPrecision() returns incorrect value",
                134, mcInt134.getPrecision() );
        assertEquals("MathContext.getRoundingMode() returns incorrect value",
                RoundingMode.HALF_UP,
                mcInt6.getRoundingMode());
        assertEquals("MathContext.getRoundingMode() returns incorrect value",
                RoundingMode.HALF_DOWN, mcIntRm6hd.getRoundingMode() );
        assertEquals("MathContext.toString() returning incorrect value",
                "precision=6 roundingMode=HALF_DOWN", mcIntRm6hd.toString() );
        assertEquals("MathContext.toString() returning incorrect value",
                "precision=6 roundingMode=HALF_UP", mcInt6.toString() );
        assertEquals("Equal MathContexts are not equal ",
                mcIntRm6hd, mcStr6hd );
        assertFalse("Different MathContexts are reported as equal ",
                mcInt6.equals(mcStr6hd) );
        assertFalse("Different MathContexts are reported as equal ",
                mcInt6.equals(mcInt134) );
        assertEquals("Equal MathContexts have different hashcodes ",
                mcIntRm6hd.hashCode(), mcStr6hd.hashCode() );
        assertFalse("Different MathContexts have equal hashcodes ",
                mcInt6.hashCode() == mcStr6hd.hashCode() );
        assertFalse("Different MathContexts have equal hashcodes ",
                mcInt6.hashCode() == mcInt134.hashCode() );
        BigDecimal res = aNumber.abs(mcInt6);
        assertEquals("MathContext Constructor with int precision failed",
                new BigDecimal("1.23809E+68"),
                res);
    }
}