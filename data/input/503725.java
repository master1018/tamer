@TestTargetClass(DHPrivateKeySpec.class)
public class DHPrivateKeySpecTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "DHPrivateKeySpec",
            args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
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
            method = "getP",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "getX",
            args = {}
        )
    })
    public void testDHPrivateKeySpec() {
        BigInteger[] xs = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] ps = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] gs = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        for (int i=0; i<ps.length; i++) {
            DHPrivateKeySpec dhpks = new DHPrivateKeySpec(xs[i], ps[i], gs[i]);
            assertEquals("The value returned by getX() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getX(), xs[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getG(), gs[i]);
        }
    }
    public static Test suite() {
        return new TestSuite(DHPrivateKeySpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
