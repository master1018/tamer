public abstract class AlgorithmParameterGeneratorTest extends TestCase {
    private final String algorithmName;
    private final TestHelper<AlgorithmParameters> helper;
    protected AlgorithmParameterGeneratorTest(String algorithmName, TestHelper<AlgorithmParameters> helper) {
        this.algorithmName = algorithmName;
        this.helper = helper;
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
    public void testAlgorithmParameterGenerator() {
        AlgorithmParameterGenerator generator = null;
        try {
            generator = AlgorithmParameterGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        generator.init(1024);
        AlgorithmParameters parameters = generator.generateParameters();
        assertNotNull("generated parameters are null", parameters);
        helper.test(parameters);
    }
}