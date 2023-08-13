@TestTargetClass(KeyGeneratorSpi.class)
public class KeyGeneratorSpiTest extends TestCase {
    class Mock_KeyGeneratorSpi extends MyKeyGeneratorSpi {
        @Override
        protected SecretKey engineGenerateKey() {
            return super.engineGenerateKey();
        }
        @Override
        protected void engineInit(SecureRandom random) {
            super.engineInit(random);
        }
        @Override
        protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidAlgorithmParameterException {
            super.engineInit(params, random);
        }
        @Override
        protected void engineInit(int keysize, SecureRandom random) {
            super.engineInit(keysize, random);
        }
    }
    @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "KeyGeneratorSpi", args = {})
    public void testKeyGeneratorSpi01() throws InvalidAlgorithmParameterException {
        Mock_KeyGeneratorSpi kgSpi = new Mock_KeyGeneratorSpi();
        assertNull("Not null result", kgSpi.engineGenerateKey());
        try {
            kgSpi.engineInit(77, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            kgSpi.engineInit(new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        AlgorithmParameterSpec aps = null;
        try {
            kgSpi.engineInit(aps, new SecureRandom());
            fail("InvalidAlgorithmParameterException must be thrown when parameter is null");
        } catch (InvalidAlgorithmParameterException e) {
        }
        aps = new APSpecSpi();
        kgSpi.engineInit(aps, new SecureRandom());
    }
}
class APSpecSpi implements AlgorithmParameterSpec {
}
