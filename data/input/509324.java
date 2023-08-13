@TestTargetClass(ExemptionMechanismException.class)
public class ExemptionMechanismExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    static String createErr(Exception tE, Exception eE) {
        return "ExemptionMechanismException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ExemptionMechanismException",
        args = {}
    )
    public void testExemptionMechanismException01() {
        ExemptionMechanismException tE = new ExemptionMechanismException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ExemptionMechanismException",
        args = {java.lang.String.class}
    )
    public void testExemptionMechanismException02() {
        ExemptionMechanismException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new ExemptionMechanismException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
            try {
                throw tE;
            } catch (Exception e) {
                assertTrue(createErr(tE, e), tE.equals(e));
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ExemptionMechanismException",
        args = {java.lang.String.class}
    )
    public void testExemptionMechanismException03() {
        String msg = null;
        ExemptionMechanismException tE = new ExemptionMechanismException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }
}
