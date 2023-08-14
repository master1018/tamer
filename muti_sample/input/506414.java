@TestTargetClass(CertPathValidatorSpi.class)
public class CertPathValidatorSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CertPathValidatorSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineValidate",
            args = {java.security.cert.CertPath.class, java.security.cert.CertPathParameters.class}
        )
    })
    public void testCertPathValidatorSpi01() throws CertPathValidatorException,
            InvalidAlgorithmParameterException {
        CertPathValidatorSpi certPathValid = new MyCertPathValidatorSpi();
        CertPathParameters params = null;
        CertPath certPath = null;
        CertPathValidatorResult cpvResult = certPathValid.engineValidate(
                certPath, params);
        assertNull("Not null CertPathValidatorResult", cpvResult);
        try {
            certPathValid.engineValidate(certPath, params);
            fail("CertPathValidatorException must be thrown");
        } catch (CertPathValidatorException e) {            
        }
        try {
            certPathValid.engineValidate(certPath, params);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {            
        }
    }
}
