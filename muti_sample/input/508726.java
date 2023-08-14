public abstract class KeyPairGeneratorTest extends TestCase {
    private final String algorithmName;
    private final TestHelper<KeyPair> helper;
    private KeyPairGenerator generator;
    protected KeyPairGeneratorTest(String algorithmName, TestHelper<KeyPair> helper) {
        this.algorithmName = algorithmName;
        this.helper = helper;
    }
    protected void setUp() throws Exception {
        super.setUp();
        generator = getKeyPairGenerator();
    }
    private KeyPairGenerator getKeyPairGenerator() {
        try {
            return KeyPairGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail("cannot get KeyPairGenerator: " + e);
            return null;
        }
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "initialize",
                args = {int.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "generateKeyPair",
                args = {}
            ),
            @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
            )
    })
    public void testKeyPairGenerator() {
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        assertNotNull("no keypair generated", keyPair);
        assertNotNull("no public key generated", keyPair.getPublic());
        assertNotNull("no private key generated", keyPair.getPrivate());
        helper.test(keyPair);
    }
}
