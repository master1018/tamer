@TestTargetClass(CertPathTrustManagerParameters.class) 
public class CertPathTrustManagerParametersTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPathTrustManagerParameters",
        args = {java.security.cert.CertPathParameters.class}
    )
    public void test_ConstructorLjava_security_cert_CertPathParameters() {
        try {
            CertPathParameters parameters = new MyCertPathParameters();
            CertPathTrustManagerParameters p =
                new CertPathTrustManagerParameters(parameters);
            assertNotSame("Parameters were cloned incorrectly",
                    parameters, p.getParameters());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            new CertPathTrustManagerParameters(null);
            fail("Expected CertPathTrustManagerParameters was not thrown");
        } catch (NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParameters",
        args = {}
    )
    public void test_getParameters() {
        CertPathParameters parameters = new MyCertPathParameters();
        CertPathTrustManagerParameters p = new CertPathTrustManagerParameters(
                parameters);
        if (!(p.getParameters() instanceof MyCertPathParameters)) {
            fail("incorrect parameters");
        }
        assertNotSame("Parameters were cloned incorrectly",
                parameters, p.getParameters());
    }
}
class MyCertPathParameters implements CertPathParameters {
    public Object clone() {
        return new MyCertPathParameters();
    }
}
