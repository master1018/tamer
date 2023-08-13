@TestTargetClass(KeyStoreException.class)
public class KeyStoreExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {}
    )
    public void testKeyStoreException01() {
        KeyStoreException tE = new KeyStoreException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.String.class}
    )
    public void testKeyStoreException02() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.String.class}
    )
    public void testKeyStoreException03() {
        String msg = null;
        KeyStoreException tE = new KeyStoreException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyStoreException04() {
        Throwable cause = null;
        KeyStoreException tE = new KeyStoreException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyStoreException05() {
        KeyStoreException tE = new KeyStoreException(tCause);
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
        method = "KeyStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyStoreException06() {
        KeyStoreException tE = new KeyStoreException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyStoreException07() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyStoreException08() {
        KeyStoreException tE = new KeyStoreException(null, tCause);
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
        method = "KeyStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyStoreException09() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i], tCause);
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
