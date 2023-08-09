public class P1SecretKeyFactory extends SecretKeyFactorySpi {
    public P1SecretKeyFactory() {
        System.out.println("Creating a P1SecretKeyFactory");
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {
        System.out.println("Trying the broken provider");
        throw new InvalidKeySpecException("I'm broken!!!");
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {
        System.out.println("Trying the broken provider");
        throw new InvalidKeySpecException("I'm broken!!!");
    }
    protected SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException {
        System.out.println("Trying the broken provider");
        throw new InvalidKeyException("I'm broken!!!");
    }
}
