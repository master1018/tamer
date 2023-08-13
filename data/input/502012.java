@TestTargetClass(CertificateEncodingException.class)
public class CertificateEncodingException2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertificateEncodingException",
        args = {}
    )
    public void test_Constructor() {
        try {
            if (true) {
                throw new CertificateEncodingException();
            }
            fail("Should have thrown CertificateEncodingException");
        } catch (CertificateEncodingException e) {
            assertEquals("Initializer failed : " + e.toString(),
                    "java.security.cert.CertificateEncodingException",
                    e.toString());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }
}
