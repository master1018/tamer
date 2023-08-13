@TestTargetClass(KeyPairGenerators.DH.class)
public class DHTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "method",
        args = {}
    )
    @BrokenTest("Suffers from DH slowness, disabling for now")
    public void testDHGen() throws Exception {
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("DH");
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        AlgorithmParameterGenerator algorithmparametergenerator = AlgorithmParameterGenerator.getInstance("DH");
        algorithmparametergenerator.init(1024, new SecureRandom());
        AlgorithmParameters algorithmparameters = algorithmparametergenerator.generateParameters();
        DHParameterSpec dhparameterspec = algorithmparameters.getParameterSpec(DHParameterSpec.class);
        gen.initialize(dhparameterspec);
        KeyPair key = gen.generateKeyPair();
    }
}
