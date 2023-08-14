@TestTargetClass(targets.KeyPairGenerators.DH.class)
public class KeyPairGeneratorTestDH extends KeyPairGeneratorTest {
    public KeyPairGeneratorTestDH() {
        super("DH", new KeyAgreementHelper("DH"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            method = "initialize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            method = "generateKeyPair",
            args = {}
        ),
        @TestTargetNew(
            level=TestLevel.COMPLETE,
            method="method",
            args={}
        )
    })
    @BrokenTest("Takes ages due to DH computations. Disabling for now.")
    public void testKeyPairGenerator() {
        super.testKeyPairGenerator();
    }
}
