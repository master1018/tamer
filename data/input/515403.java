@TestTargetClass(KeyException.class)
public class KeyExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyException",
        args = {}
    )
    public void testKeyException01() {
        KeyException tE = new KeyException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.String.class}
    )
    public void testKeyException02() {
        KeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.String.class}
    )
    public void testKeyException03() {
        String msg = null;
        KeyException tE = new KeyException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyException04() {
        Throwable cause = null;
        KeyException tE = new KeyException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyException05() {
        KeyException tE = new KeyException(tCause);
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
        method = "KeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyException06() {
        KeyException tE = new KeyException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyException07() {
        KeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyException08() {
        KeyException tE = new KeyException(null, tCause);
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
        method = "KeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyException09() {
        KeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyException(msgs[i], tCause);
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
