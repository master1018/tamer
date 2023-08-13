@TestTargetClass(CertificateNotYetValidException.class)
public class CertificateNotYetValidExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertificateNotYetValidException",
        args = {}
    )
    public void testCertificateNotYetValidException01() {
        CertificateNotYetValidException tE = new CertificateNotYetValidException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies CertificateNotYetValidException constructor with valid parameters.",
        method = "CertificateNotYetValidException",
        args = {java.lang.String.class}
    )
    public void testCertificateNotYetValidException02() {
        CertificateNotYetValidException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateNotYetValidException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies  CertificateNotYetValidException constructor with null as a parameter.",
        method = "CertificateNotYetValidException",
        args = {java.lang.String.class}
    )
    public void testCertificateNotYetValidException03() {
        String msg = null;
        CertificateNotYetValidException tE = new CertificateNotYetValidException(
                msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
