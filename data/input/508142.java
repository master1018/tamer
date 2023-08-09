@TestTargetClass(CertPathBuilderSpi.class)
public class CertPathBuilderSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CertPathBuilderSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineBuild",
            args = {java.security.cert.CertPathParameters.class}
        )
    })
    public void testCertPathBuilderSpi01() throws CertPathBuilderException,
            InvalidAlgorithmParameterException {
        CertPathBuilderSpi certPathBuilder = new MyCertPathBuilderSpi();
        CertPathParameters cpp = null;
        try {
            certPathBuilder.engineBuild(cpp);
            fail("CertPathBuilderException must be thrown");
        } catch (CertPathBuilderException e) {
        }    
        CertPathBuilderResult cpbResult = certPathBuilder.engineBuild(cpp);
        assertNull("Not null CertPathBuilderResult", cpbResult);
    }
}
