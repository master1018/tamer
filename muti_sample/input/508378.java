@TestTargetClass(KeyPairGenerator.class)
public class KeyPairGenerator3Test extends TestCase {
    private static String validProviderName = null;
    public static Provider validProvider = null;
    private static boolean DSASupported = false;
    private static String NotSupportMsg = KeyPairGenerator1Test.NotSupportMsg;
    static {
        validProvider = SpiEngUtils.isSupport(
                KeyPairGenerator1Test.validAlgName,
                KeyPairGenerator1Test.srvKeyPairGenerator);
        DSASupported = (validProvider != null);
        validProviderName = (DSASupported ? validProvider.getName() : null);
    }
    protected KeyPairGenerator[] createKPGen() {
        if (!DSASupported) {
            fail(KeyPairGenerator1Test.validAlgName
                    + " algorithm is not supported");
            return null;
        }
        KeyPairGenerator[] kpg = new KeyPairGenerator[3];
        try {
            kpg[0] = KeyPairGenerator
                    .getInstance(KeyPairGenerator1Test.validAlgName);
            kpg[1] = KeyPairGenerator.getInstance(
                    KeyPairGenerator1Test.validAlgName, validProvider);
            kpg[2] = KeyPairGenerator.getInstance(
                    KeyPairGenerator1Test.validAlgName, validProviderName);
            return kpg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "generateKeyPair",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "genKeyPair",
            args = {}
        )
    })
    public void testGenKeyPair01() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator[] kpg = createKPGen();
        assertNotNull("KeyPairGenerator objects were not created", kpg);
        KeyPair kp, kp1;
        SecureRandom rr = new SecureRandom();
        for (int i = 0; i < kpg.length; i++) {
            kpg[i].initialize(512, rr);
            kp = kpg[i].generateKeyPair();
            kp1 = kpg[i].genKeyPair();
            assertFalse("Incorrect private key", kp.getPrivate().equals(
                    kp1.getPrivate()));
            assertFalse("Incorrect public key", kp.getPublic().equals(
                    kp1.getPublic()));
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "generateKeyPair",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "genKeyPair",
            args = {}
        )
    })
    public void testGenKeyPair02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator[] kpg = createKPGen();
        assertNotNull("KeyPairGenerator objects were not created", kpg);
        KeyPair kp, kp1;   
        for (int i = 0; i < kpg.length; i++) {
            kp = kpg[i].generateKeyPair();
            kp1 = kpg[i].genKeyPair();
            assertFalse("Incorrect private key", kp.getPrivate().equals(
                kp1.getPrivate()));
            assertFalse("Incorrect public key", kp.getPublic().equals(
                kp1.getPublic()));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyPairGenerator",
        args = {java.lang.String.class}
    )
    public void testKeyPairGeneratorConst() {
        String[] alg = {null, "", "AsDfGh!#$*", "DSA", "RSA"};
        MykeyPGen kpg;
        for (int i = 0; i < alg.length; i++) {
            try {
                kpg = new MykeyPGen(alg[i]);
                assertNotNull(kpg);
                assertTrue(kpg instanceof KeyPairGenerator);
            } catch (Exception e){
                fail("Exception should not be thrown");
            }
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyPairGenerator3Test.class);
    }
    class MykeyPGen extends KeyPairGenerator {
        public MykeyPGen(String alg) {
            super(alg);
        }
    }
}
