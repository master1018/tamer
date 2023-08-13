@TestTargetClass(CertificateException.class)
public class CertificateException2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertificateException",
        args = {}
    )
    public void test_Constructor() {
        try {
            if (true) {
                throw new CertificateException();
            }
            fail("Should have thrown CertificateException");
        } catch (CertificateException e) {
            assertEquals("Initializer failed : " + e.toString(),
                    "java.security.cert.CertificateException", e.toString());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Null/empty/invalid parameters checking missed",
        method = "CertificateException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) {
                throw new CertificateException("Some error message");
            }
            fail("Should have thrown CertificateException");
        } catch (CertificateException e) {
            assertEquals("Initializer failed",
                    "Some error message", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }
}
