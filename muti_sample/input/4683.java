public class P2SecretKeyFactory extends SecretKeyFactorySpi {
    public P2SecretKeyFactory() {
        System.out.println("Creating a P2SecretKeyFactory");
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {
        System.out.println("Trying the good provider");
        return null;
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {
        System.out.println("Trying the good provider");
        return null;
    }
    protected SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException {
        System.out.println("Trying the good provider");
        return null;
    }
}
