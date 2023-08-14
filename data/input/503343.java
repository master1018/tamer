@TestTargetClass(KeyAgreementSpi.class)
public class KeyAgreementSpiTest extends TestCase {
    class Mock_KeyAgreementSpi extends MyKeyAgreementSpi {
        @Override
        protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException,
                IllegalStateException {
            return super.engineDoPhase(key, lastPhase);
        }
        @Override
        protected byte[] engineGenerateSecret() throws IllegalStateException {
            return super.engineGenerateSecret();
        }
        @Override
        protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException,
                NoSuchAlgorithmException, InvalidKeyException {
            return super.engineGenerateSecret(algorithm);
        }
        @Override
        protected int engineGenerateSecret(byte[] sharedSecret, int offset)
                throws IllegalStateException, ShortBufferException {
            return super.engineGenerateSecret(sharedSecret, offset);
        }
        @Override
        protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
            super.engineInit(key, random);
        }
        @Override
        protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            super.engineInit(key, params, random);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyAgreementSpi",
        args = {}
    )
    public void testKeyAgreementSpi01() throws InvalidKeyException,
            ShortBufferException, NoSuchAlgorithmException, 
            InvalidAlgorithmParameterException {
        Mock_KeyAgreementSpi kaSpi = new Mock_KeyAgreementSpi();
        assertNull("Not null result", kaSpi.engineDoPhase(null, true));
        try {
            kaSpi.engineDoPhase(null, false);
            fail("IllegalStateException must be thrown");
        } catch (IllegalStateException e) {
        }
        byte[] bb = kaSpi.engineGenerateSecret();
        assertEquals("Length is not 0", bb.length, 0);
        assertEquals("Returned integer is not 0", kaSpi.engineGenerateSecret(new byte[1], 10), -1);
        assertNull("Not null result", kaSpi.engineGenerateSecret("aaa"));
        try {
            kaSpi.engineGenerateSecret("");
            fail("NoSuchAlgorithmException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        }
        Key key = null;
        try {
            kaSpi.engineInit(key, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        AlgorithmParameterSpec params = null;
        try {
            kaSpi.engineInit(key, params, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }
}
