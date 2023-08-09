@TestTargetClass(targets.KeyFactories.RSA.class)
public class KeyFactoryTestRSA extends
        KeyFactoryTest<RSAPublicKeySpec, RSAPrivateKeySpec> {
    @SuppressWarnings("unchecked")
    public KeyFactoryTestRSA() {
        super("RSA", new CipherAsymmetricCryptHelper("RSA"), RSAPublicKeySpec.class, RSAPrivateKeySpec.class);
    }
}
