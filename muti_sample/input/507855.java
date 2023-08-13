@TestTargetClass(KeyPairGeneratorSpi.class)
public class KeyPairGeneratorSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "KeyPairGeneratorSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "generateKeyPair",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "initialize",
            args = {int.class, java.security.SecureRandom.class}
        )
    })
    public void testKeyPairGeneratorSpi01()
            throws InvalidAlgorithmParameterException,
            InvalidParameterException {
        KeyPairGeneratorSpi keyPairGen = new MyKeyPairGeneratorSpi();
        AlgorithmParameterSpec pp = null;
        try {
            keyPairGen.initialize(pp, null);
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
        keyPairGen.initialize(pp, new SecureRandom());
        keyPairGen.initialize(1024, new SecureRandom());
        try {
            keyPairGen.initialize(-1024, new SecureRandom());
            fail("IllegalArgumentException must be thrown for incorrect keysize");
        } catch (IllegalArgumentException e) {
        }
        try {
            keyPairGen.initialize(1024, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Incorrect exception", e.getMessage(),
                    "Invalid random");
        }
        KeyPair kp = keyPairGen.generateKeyPair();
        assertNull("Not null KeyPair", kp);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyPairGeneratorSpiTest.class);
    }
    class MyAlgorithmParameterSpec implements AlgorithmParameterSpec {}
}
