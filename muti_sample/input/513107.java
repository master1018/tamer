@TestTargetClass(DSAPublicKey.class)
public class DSAPublicKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getY",
        args = {}
    )
    public void test_getY() throws Exception {
        KeyPairGenerator keyGen = null;
        KeyPair keys = null;
        DSAPrivateKey priv = null;
        DSAPublicKey publ = null;
        keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(new DSAParameterSpec(Util.P, Util.Q, Util.G),
                new SecureRandom(new MySecureRandomSpi(), null) {
                });
        keys = keyGen.generateKeyPair();
        priv = (DSAPrivateKey) keys.getPrivate();
        publ = (DSAPublicKey) keys.getPublic();
        assertNotNull("Invalid Y value", publ.getY());
        keyGen = KeyPairGenerator.getInstance("DSA");
        keys = keyGen.generateKeyPair();
        priv = (DSAPrivateKey) keys.getPrivate();
        publ = (DSAPublicKey) keys.getPublic();
        assertNotNull("Invalid Y value", publ.getY());
    }
}
