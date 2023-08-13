@TestTargetClass(SSLException.class)
public class SSLExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class}
    )
    public void testSSLException01() {
        SSLException sE;
        for (int i = 0; i < msgs.length; i++) {
            sE = new SSLException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), sE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", sE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class}
    )
    public void testSSLException02() {
        String msg = null;
        SSLException sE = new SSLException(msg);
        assertNull("getMessage() must return null.", sE.getMessage());
        assertNull("getCause() must return null", sE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.Throwable.class}
    )
    public void testSSLException03() {
        Throwable cause = null;
        SSLException sE = new SSLException(cause);
        assertNull("getMessage() must return null.", sE.getMessage());
        assertNull("getCause() must return null", sE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.Throwable.class}
    )
    public void testSSLException04() {
        SSLException sE = new SSLException(tCause);
        if (sE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = sE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", sE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), sE.getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testSSLException05() {
        SSLException sE = new SSLException(null, null);
        assertNull("getMessage() must return null", sE.getMessage());
        assertNull("getCause() must return null", sE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testSSLException06() {
        SSLException sE;
        for (int i = 0; i < msgs.length; i++) {
            sE = new SSLException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), sE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", sE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testSSLException07() {
        SSLException sE = new SSLException(null, tCause);
        if (sE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = sE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", sE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), sE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testSSLException08() {
        SSLException sE;
        for (int i = 0; i < msgs.length; i++) {
            sE = new SSLException(msgs[i], tCause);
            String getM = sE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", sE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    sE.getCause(), tCause);
        }
    }
}