@TestTargetClass(KeyPairGenerator.class)
public class KeyPairGenerator4Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "genKeyPair",
        args = {}
    )
    public void test_genKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
        gen.initialize(1024);
        assertNotNull("KeyPair is null", gen.genKeyPair());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() throws Exception {
        String alg = KeyPairGenerator.getInstance("DSA").getAlgorithm();
        assertEquals("getAlgorithm returned unexpected value", "DSA", alg);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verification of other string parameters and exception cases missed",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        KeyPairGenerator.getInstance("DSA");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException, NoSuchProviderException checking missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String()
            throws Exception {
        Provider[] providers = Security.getProviders("KeyPairGenerator.DSA");
        for (int i = 0; i < providers.length; i++) {
            KeyPairGenerator.getInstance("DSA", providers[i].getName());
        }
        try {
            KeyPairGenerator.getInstance("DSA", "");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws Exception {
        Provider p = KeyPairGenerator.getInstance("DSA").getProvider();
        assertNotNull("provider is null", p);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidParameterException checking missed",
        method = "initialize",
        args = {int.class}
    )
    public void test_initializeI() throws Exception {
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
        keyPair.initialize(1024);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidParameterException checking missed",
        method = "initialize",
        args = {int.class, java.security.SecureRandom.class}
    )
    public void test_initializeILjava_security_SecureRandom() throws Exception {
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
        keyPair.initialize(1024, new SecureRandom());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidAlgorithmParameterException checking missed",
        method = "initialize",
        args = {java.security.spec.AlgorithmParameterSpec.class}
    )
    public void test_initializeLjava_security_spec_AlgorithmParameterSpec()
            throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        DSAPublicKey key = (DSAPublicKey) keyPairGenerator.genKeyPair()
                .getPublic();
        DSAParams params = key.getParams();
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
        keyPair.initialize(new DSAParameterSpec(params.getP(), params.getQ(),
                params.getG()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidAlgorithmParameterException checking missed",
        method = "initialize",
        args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
    )
    public void test_initializeLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom()
            throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        DSAPublicKey key = (DSAPublicKey) keyPairGenerator.genKeyPair()
                .getPublic();
        DSAParams params = key.getParams();
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
        keyPair.initialize(new DSAParameterSpec(params.getP(), params.getQ(),
                params.getG()), new SecureRandom());
    }
}
