@TestTargetClass(DHPublicKeySpec.class)
public class DHPublicKeySpecTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "All functionality tested in one method. Probably it should be divided into several tests.",
            method = "DHPublicKeySpec",
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
            method = "getY",
            args = {}
        )
    })
    public void testDHPrivateKeySpec() {
        BigInteger[] ys = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] ps = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] gs = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        for (int i=0; i<ps.length; i++) {
            DHPublicKeySpec dhpks = new DHPublicKeySpec(ys[i], ps[i], gs[i]);
            assertEquals("The value returned by getY() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getY(), ys[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhpks.getG(), gs[i]);
        }
    }
    public static Test suite() {
        return new TestSuite(DHPublicKeySpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
