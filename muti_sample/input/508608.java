@TestTargetClass(SecretKeyFactorySpi.class)
public class SecretKeyFactorySpiTest extends TestCase {
    class Mock_SecretKeyFactorySpi extends MySecretKeyFactorySpi {
        @Override
        protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            return super.engineGenerateSecret(keySpec);
        }
        @Override
        protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
                throws InvalidKeySpecException {
            return super.engineGetKeySpec(key, keySpec);
        }
        @Override
        protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
            return super.engineTranslateKey(key);
        }
    }
    @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "SecretKeyFactorySpi", args = {})
    public void testSecretKeyFactorySpi01() throws InvalidKeyException, InvalidKeySpecException {
        Mock_SecretKeyFactorySpi skfSpi = new Mock_SecretKeyFactorySpi();
        SecretKey sk = null;
        assertNull("Not null result", skfSpi.engineTranslateKey(sk));
        KeySpec kspec = null;
        assertNull("Not null result", skfSpi.engineGenerateSecret(kspec));
        assertNull("Not null result", skfSpi.engineGetKeySpec(sk, null));
    }
}
