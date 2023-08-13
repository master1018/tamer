@TestTargetClass(targets.KeyFactories.DSA.class)
public class KeyFactoryTestDSA extends
        KeyFactoryTest<DSAPublicKeySpec, DSAPrivateKeySpec> {
    public KeyFactoryTestDSA() {
        super("DSA", new SignatureHelper("DSA"), DSAPublicKeySpec.class, DSAPrivateKeySpec.class);
    }
}
