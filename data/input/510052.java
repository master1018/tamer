@TestTargetClass(NoSuchAlgorithmException.class)
public class NoSuchAlgorithmExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {}
    )
    public void testNoSuchAlgorithmException01() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class}
    )
    public void testNoSuchAlgorithmException02() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class}
    )
    public void testNoSuchAlgorithmException03() {
        String msg = null;
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException04() {
        Throwable cause = null;
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException05() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(tCause);
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
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException06() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException07() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException08() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(null, tCause);
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
        method = "NoSuchAlgorithmException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testNoSuchAlgorithmException09() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i], tCause);
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
