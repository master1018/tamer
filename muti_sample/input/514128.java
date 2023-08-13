@TestTargetClass(DSAPrivateKeySpec.class)
public class DSAPrivateKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DSAPrivateKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testDSAPrivateKeySpec() {
        KeySpec ks = new DSAPrivateKeySpec(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigInteger("3"),
                new BigInteger("4"));
        assertTrue(ks instanceof DSAPrivateKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getG",
        args = {}
    )
    public final void testGetG() {
        DSAPrivateKeySpec dpks = new DSAPrivateKeySpec(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigInteger("3"),
                new BigInteger("4"));
        assertEquals(4, dpks.getG().intValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getP",
        args = {}
    )
    public final void testGetP() {
        DSAPrivateKeySpec dpks = new DSAPrivateKeySpec(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigInteger("3"),
                new BigInteger("4"));
        assertEquals(2, dpks.getP().intValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getQ",
        args = {}
    )
    public final void testGetQ() {
        DSAPrivateKeySpec dpks = new DSAPrivateKeySpec(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigInteger("3"),
                new BigInteger("4"));
        assertEquals(3, dpks.getQ().intValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getX",
        args = {}
    )
    public final void testGetX() {
        DSAPrivateKeySpec dpks = new DSAPrivateKeySpec(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigInteger("3"),
                new BigInteger("4"));
        assertEquals(1, dpks.getX().intValue());
    }
}
