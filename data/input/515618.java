public abstract class CertPathValidatorTest extends TestCase {
    private final String algorithmName;
    public CertPathValidatorTest(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    abstract CertPathParameters getParams();
    abstract CertPath getCertPath();
    abstract void validateResult(CertPathValidatorResult validatorResult);
    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="validate",
                args={CertPath.class, CertPathParameters.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertPathValidator() throws Exception {
        CertPathValidator certPathValidator = CertPathValidator.getInstance(
                algorithmName);
        CertPathValidatorResult validatorResult = certPathValidator.validate(
                getCertPath(), getParams());
        validateResult(validatorResult);
    }
}
