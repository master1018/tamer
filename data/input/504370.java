@TestTargetClass(DHGenParameterSpec.class)
public class DHGenParameterSpecTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "DHGenParameterSpec",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getExponentSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getPrimeSize",
            args = {}
        )
    })
    public void testDHGenParameterSpec() {
        int[] primes = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        int[] exponents = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        for (int i=0; i<primes.length; i++) {
            DHGenParameterSpec ps = new DHGenParameterSpec(primes[i],
                                                            exponents[i]);
            assertEquals("The value returned by getPrimeSize() must be "
                        + "equal to the value specified in the constructor",
                        ps.getPrimeSize(), primes[i]);
            assertEquals("The value returned by getExponentSize() must be "
                        + "equal to the value specified in the constructor",
                        ps.getPrimeSize(), exponents[i]);
        }
    }
    public static Test suite() {
        return new TestSuite(DHGenParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
