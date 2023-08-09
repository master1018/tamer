@TestTargetClass(GeneralSecurityException.class)
public class GeneralSecurityExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {}
    )
    public void testGeneralSecurityException01() {
        GeneralSecurityException tE = new GeneralSecurityException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.String.class}
    )
    public void testGeneralSecurityException02() {
        GeneralSecurityException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new GeneralSecurityException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.String.class}
    )
    public void testGeneralSecurityException03() {
        String msg = null;
        GeneralSecurityException tE = new GeneralSecurityException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.Throwable.class}
    )
    public void testGeneralSecurityException04() {
        Throwable cause = null;
        GeneralSecurityException tE = new GeneralSecurityException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.Throwable.class}
    )
    public void testGeneralSecurityException05() {
        GeneralSecurityException tE = new GeneralSecurityException(tCause);
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
        method = "GeneralSecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testGeneralSecurityException06() {
        GeneralSecurityException tE = new GeneralSecurityException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testGeneralSecurityException07() {
        GeneralSecurityException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new GeneralSecurityException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GeneralSecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testGeneralSecurityException08() {
        GeneralSecurityException tE = new GeneralSecurityException(null, tCause);
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
        method = "GeneralSecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testGeneralSecurityException09() {
        GeneralSecurityException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new GeneralSecurityException(msgs[i], tCause);
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
