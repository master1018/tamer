public abstract class SecretKeyFactorySpi {
    public SecretKeyFactorySpi() {
    }
    protected abstract SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException;
    @SuppressWarnings("unchecked")
    protected abstract KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException;
    protected abstract SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException;
}