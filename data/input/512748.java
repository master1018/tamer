@TestTargetClass(DigestException.class)
public class DigestExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DigestException",
        args = {}
    )
    public void testDigestException01() {
        DigestException tE = new DigestException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies case with differents parameters (parameter is not null)",
        method = "DigestException",
        args = {java.lang.String.class}
    )
    public void testDigestException02() {
        DigestException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new DigestException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies case with null parameter",
        method = "DigestException",
        args = {java.lang.String.class}
    )
    public void testDigestException03() {
        String msg = null;
        DigestException tE = new DigestException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies case with null parameter",
        method = "DigestException",
        args = {java.lang.Throwable.class}
    )
    public void testDigestException04() {
        Throwable cause = null;
        DigestException tE = new DigestException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies case with not null parameter",
        method = "DigestException",
        args = {java.lang.Throwable.class}
    )
    public void testDigestException05() {
        DigestException tE = new DigestException(tCause);
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
        method = "DigestException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testDigestException06() {
        DigestException tE = new DigestException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "DigestException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testDigestException07() {
        DigestException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new DigestException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "DigestException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testDigestException08() {
        DigestException tE = new DigestException(null, tCause);
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
        method = "DigestException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testDigestException09() {
        DigestException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new DigestException(msgs[i], tCause);
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
