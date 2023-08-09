public class AlgorithmParametersTest extends TestCase {
    private final String algorithmName;
    private final TestHelper<AlgorithmParameters> helper;
    private final AlgorithmParameterSpec parameterData;
    public AlgorithmParametersTest(String algorithmName,
            TestHelper<AlgorithmParameters> helper, AlgorithmParameterSpec parameterData) {
        this.algorithmName = algorithmName;
        this.helper = helper;
        this.parameterData = parameterData;
    }
    protected void setUp() throws Exception {
        super.setUp();
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
                args={byte[].class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testAlgorithmParameters() {
        AlgorithmParameters algorithmParameters = null;
        try {
            algorithmParameters = AlgorithmParameters
                    .getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        try {
            algorithmParameters.init(parameterData);
        } catch (InvalidParameterSpecException e) {
            fail(e.getMessage());
        }
        helper.test(algorithmParameters);
    }
}
