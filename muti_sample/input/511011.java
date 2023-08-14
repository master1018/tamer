@TestTargetClass(CertStoreException.class)
public class CertStoreExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertStoreException",
        args = {}
    )
    public void testCertStoreException01() {
        CertStoreException tE = new CertStoreException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertStoreException",
        args = {java.lang.String.class}
    )
    public void testCertStoreException02() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null asa parameter.",
        method = "CertStoreException",
        args = {java.lang.String.class}
    )
    public void testCertStoreException03() {
        String msg = null;
        CertStoreException tE = new CertStoreException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testCertStoreException04() {
        Throwable cause = null;
        CertStoreException tE = new CertStoreException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testCertStoreException05() {
        CertStoreException tE = new CertStoreException(tCause);
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
        method = "CertStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertStoreException06() {
        CertStoreException tE = new CertStoreException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the second parameter.",
        method = "CertStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertStoreException07() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the first parameter.",
        method = "CertStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertStoreException08() {
        CertStoreException tE = new CertStoreException(null, tCause);
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
        method = "CertStoreException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertStoreException09() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i], tCause);
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
