@TestTargetClass(CertificateExpiredException.class)
public class CertificateExpiredExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertificateExpiredException",
        args = {}
    )
    public void testCertificateExpiredException01() {
        CertificateExpiredException tE = new CertificateExpiredException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameter.",
        method = "CertificateExpiredException",
        args = {java.lang.String.class}
    )
    public void testCertificateExpiredException02() {
        CertificateExpiredException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateExpiredException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with null as a parameter.",
        method = "CertificateExpiredException",
        args = {java.lang.String.class}
    )
    public void testCertificateExpiredException03() {
        String msg = null;
        CertificateExpiredException tE = new CertificateExpiredException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
