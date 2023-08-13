@TestTargetClass(targets.AlgorithmParameterGenerators.DH.class)
public class AlgorithmParameterGeneratorTestDH extends
        AlgorithmParameterGeneratorTest {
    public AlgorithmParameterGeneratorTestDH() {
        super("DH", new AlgorithmParameterKeyAgreementHelper("DH"));
    }
    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="init",
                args={int.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    @BrokenTest("Suffers from DH slowness, disabling for now")
    public void testAlgorithmParameterGenerator() {
        super.testAlgorithmParameterGenerator();
    }
}
