@TestTargetClass(DHParameterSpec.class)
public class DHParameterSpecTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "DHParameterSpec",
            args = {java.math.BigInteger.class, java.math.BigInteger.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "DHParameterSpec",
            args = {java.math.BigInteger.class, java.math.BigInteger.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getG",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getL",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getP",
            args = {}
        )
    })
    public void testDHParameterSpec() {
        BigInteger[] ps = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] gs = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        int[] ls = {Integer.MIN_VALUE, 0, 1, Integer.MAX_VALUE};
        for (int i=0; i<ps.length; i++) {
            DHParameterSpec dhps = new DHParameterSpec(ps[i], gs[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getG(), gs[i]);
        }
        for (int i=0; i<ps.length; i++) {
            DHParameterSpec dhps = new DHParameterSpec(ps[i], gs[i], ls[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getG(), gs[i]);
            assertEquals("The value returned by getL() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getL(), ls[i]);
        }
    }
    public static Test suite() {
        return new TestSuite(DHParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
