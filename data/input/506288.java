@TestTargetClass(InvalidKeySpecException.class)
public class InvalidKeySpecExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidKeySpecException",
        args = {}
    )
    public void testInvalidKeySpecException01() {
        InvalidKeySpecException tE = new InvalidKeySpecException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class}
    )
    public void testInvalidKeySpecException02() {
        InvalidKeySpecException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeySpecException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class}
    )
    public void testInvalidKeySpecException03() {
        String msg = null;
        InvalidKeySpecException tE = new InvalidKeySpecException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "InvalidKeySpecException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException04() {
        Throwable cause = null;
        InvalidKeySpecException tE = new InvalidKeySpecException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive cases.",
        method = "InvalidKeySpecException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException05() {
        InvalidKeySpecException tE = new InvalidKeySpecException(tCause);
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
        notes = "Verifies null as parameters.",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException06() {
        InvalidKeySpecException tE = new InvalidKeySpecException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a cause parameter.",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException07() {
        InvalidKeySpecException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeySpecException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a message parameter.",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException08() {
        InvalidKeySpecException tE = new InvalidKeySpecException(null, tCause);
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
        notes = "Verifies positive case.",
        method = "InvalidKeySpecException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeySpecException09() {
        InvalidKeySpecException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeySpecException(msgs[i], tCause);
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
