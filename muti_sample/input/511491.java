@TestTargetClass(java.security.AlgorithmParameterGeneratorSpi.class)
public class AlgorithmParameterGeneratorSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AlgorithmParameterGeneratorSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGenerateParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineInit",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineInit",
            args = {int.class, java.security.SecureRandom.class}
        )
    })
    public void testAlgorithmParameterGeneratorSpi01()
            throws InvalidAlgorithmParameterException {
        MyAlgorithmParameterGeneratorSpi algParGen = new MyAlgorithmParameterGeneratorSpi();
        AlgorithmParameters param = algParGen.engineGenerateParameters();
        assertNull("Not null parameters", param);
        AlgorithmParameterSpec pp = null;
        algParGen.engineInit(pp, new SecureRandom());
        try {
            algParGen.engineInit(pp, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        algParGen.engineInit(0, null);
        algParGen.engineInit(0, new SecureRandom());
        try {
            algParGen.engineInit(-10, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            algParGen.engineInit(-10, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }
}
