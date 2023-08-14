public class MySecretKeyFactorySpi  extends SecretKeyFactorySpi {
    @Override
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {
        return null;
    }
    @SuppressWarnings("unchecked")
    @Override
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {
        return null;
    }
    @Override
    protected SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException {
        return null;
    }
}
