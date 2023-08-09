@TestTargetClass(ProviderException.class)
public class ProviderExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {}
    )
    public void testProviderException01() {
        ProviderException tE = new ProviderException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.String.class}
    )
    public void testProviderException02() {
        ProviderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new ProviderException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.String.class}
    )
    public void testProviderException03() {
        String msg = null;
        ProviderException tE = new ProviderException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.Throwable.class}
    )
    public void testProviderException04() {
        Throwable cause = null;
        ProviderException tE = new ProviderException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.Throwable.class}
    )
    public void testProviderException05() {
        ProviderException tE = new ProviderException(tCause);
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
        method = "ProviderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testProviderException06() {
        ProviderException tE = new ProviderException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testProviderException07() {
        ProviderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new ProviderException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProviderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testProviderException08() {
        ProviderException tE = new ProviderException(null, tCause);
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
        method = "ProviderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testProviderException09() {
        ProviderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new ProviderException(msgs[i], tCause);
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
