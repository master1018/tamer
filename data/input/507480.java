@TestTargetClass(targets.KeyPairGenerators.DSA.class)
public class KeyPairGeneratorTestDSA extends KeyPairGeneratorTest {
    public KeyPairGeneratorTestDSA() {
        super("DSA", new SignatureHelper("DSA"));
    }
}
