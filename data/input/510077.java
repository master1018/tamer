@TestTargetClass(CertStoreSpi.class)
public class CertStoreSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CertStoreSpi",
            args = {java.security.cert.CertStoreParameters.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetCertificates",
            args = {java.security.cert.CertSelector.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetCRLs",
            args = {java.security.cert.CRLSelector.class}
        )
    })
    public void testCertStoreSpi01() throws InvalidAlgorithmParameterException,
            CertStoreException {
        CertStoreSpi certStoreSpi = null;
        CertSelector certSelector = new tmpCertSelector();
        CRLSelector crlSelector = new tmpCRLSelector();
        try {
            certStoreSpi = new MyCertStoreSpi(null);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        certStoreSpi = new MyCertStoreSpi(new MyCertStoreParameters());
        assertNull("Not null collection", certStoreSpi
                .engineGetCertificates(certSelector));
        assertNull("Not null collection", certStoreSpi
                .engineGetCRLs(crlSelector));
    }
    public static Test suite() {
        return new TestSuite(CertStoreSpiTest.class);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    public static class tmpCRLSelector implements CRLSelector {
        public Object clone() {
            return null;
        }
        public boolean match (CRL crl) {
            return false;
        }
    }
    public static class tmpCertSelector implements CertSelector {
        public Object clone() {
            return null;
        }
        public boolean match (Certificate crl) {
            return true;
        }
    }
}
