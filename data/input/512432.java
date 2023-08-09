@TestTargetClass(targets.KeyFactories.DH.class)
public class KeyFactoryTestDH extends KeyFactoryTest<DHPublicKeySpec, DHPrivateKeySpec> {
    public KeyFactoryTestDH() {
        super("DH", new KeyAgreementHelper("DH"), DHPublicKeySpec.class, DHPrivateKeySpec.class);
    }
}
