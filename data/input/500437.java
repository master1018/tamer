public abstract class CertPathBuilderTest extends TestCase {
    private final String algorithmName;
    private CertPathParameters params;
    public CertPathBuilderTest(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        params = getCertPathParameters();
    }
    abstract CertPathParameters getCertPathParameters() throws Exception;
    abstract void validateCertPath(CertPath path);
    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="build",
                args={CertPathParameters.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                clazz=CertPathBuilderResult.class,
                method="getCertPath",
                args={}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertPathBuilder() throws Exception {
        CertPathBuilder pathBuilder = CertPathBuilder.getInstance(
                algorithmName);
        CertPathBuilderResult builderResult = pathBuilder.build(params);
        CertPath path = builderResult.getCertPath();
        assertNotNull("built path is null", path);
        validateCertPath(path);
    }
}
