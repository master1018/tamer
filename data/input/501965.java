@TestTargetClass(DSAPrivateKey.class)
public class DSAPrivateKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getX",
        args = {}
    )
    public void test_getX() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(new DSAParameterSpec(Util.P, Util.Q, Util.G),
                new SecureRandom(new MySecureRandomSpi(), null) {                    
                });
        KeyPair keyPair = keyGen.generateKeyPair();
        DSAPrivateKey key = (DSAPrivateKey) keyPair.getPrivate();
        assertNotNull("Invalid X value", key.getX());
    }
}
