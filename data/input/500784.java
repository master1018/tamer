@TestTargetClass(CertPathValidator.class)
public class CertPathValidator3Test extends TestCase {
    private static final String defaultType = CertPathBuilder1Test.defaultType;    
    private static boolean PKIXSupport = false;
    private static Provider defaultProvider;
    private static String defaultProviderName;
    private static String NotSupportMsg = "";
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                CertPathValidator1Test.srvCertPathValidator);
        PKIXSupport = (defaultProvider != null);
        defaultProviderName = (PKIXSupport ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");
    }
    private static CertPathValidator[] createCPVs() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertPathValidator[] certPVs = new CertPathValidator[3];
            certPVs[0] = CertPathValidator.getInstance(defaultType);
            certPVs[1] = CertPathValidator.getInstance(defaultType,
                    defaultProviderName);
            certPVs[2] = CertPathValidator.getInstance(defaultType,
                    defaultProvider);
            return certPVs;
        } catch (Exception e) {
            return null;
        }
    }    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exceptions.",
        method = "validate",
        args = {java.security.cert.CertPath.class, java.security.cert.CertPathParameters.class}
    )
    public void testValidate01() throws InvalidAlgorithmParameterException, CertPathValidatorException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        MyCertPath mCP = new MyCertPath(new byte[0]);
        CertPathParameters params = new PKIXParameters(TestUtils.getTrustAnchorSet()); 
        CertPathValidator [] certPV = createCPVs();
        assertNotNull("CertPathValidator objects were not created", certPV);
        for (int i = 0; i < certPV.length; i++) {            
            try {
                certPV[i].validate(mCP, null);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
            try {
                certPV[i].validate(null, params);
                fail("NullPointerException must be thrown");
            } catch(NullPointerException e) {
            }
        }
    }
}
