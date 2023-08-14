@TestTargetClass(InvalidAlgorithmParameterException.class)
public class InvalidAlgorithmParameterExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {}
    )
    public void testInvalidAlgorithmParameterException01() {
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class}
    )
    public void testInvalidAlgorithmParameterException02() {
        InvalidAlgorithmParameterException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidAlgorithmParameterException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class}
    )
    public void testInvalidAlgorithmParameterException03() {
        String msg = null;
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException(
                msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException04() {
        Throwable cause = null;
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException(
                cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException05() {
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException(
                tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException06() {
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException(
                null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException07() {
        InvalidAlgorithmParameterException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidAlgorithmParameterException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException08() {
        InvalidAlgorithmParameterException tE = new InvalidAlgorithmParameterException(
                null, tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidAlgorithmParameterException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidAlgorithmParameterException09() {
        InvalidAlgorithmParameterException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidAlgorithmParameterException(msgs[i], tCause);
            String getM = tE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
    }
}
