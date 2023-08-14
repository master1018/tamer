@TestTargetClass(DSAPublicKeySpec.class)
public class DSAPublicKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DSAPublicKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testDSAPublicKeySpec() {
        KeySpec ks = new DSAPublicKeySpec(
                new BigInteger("1"), 
                new BigInteger("2"), 
                new BigInteger("3"), 
                new BigInteger("4"));
        assertTrue(ks instanceof DSAPublicKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getG",
        args = {}
    )
    public final void testGetG() {
        DSAPublicKeySpec dpks = new DSAPublicKeySpec(
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
        DSAPublicKeySpec dpks = new DSAPublicKeySpec(
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
        DSAPublicKeySpec dpks = new DSAPublicKeySpec(
                new BigInteger("1"), 
                new BigInteger("2"), 
                new BigInteger("3"), 
                new BigInteger("4"));
        assertEquals(3, dpks.getQ().intValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getY",
        args = {}
    )
    public final void testGetY() {
        DSAPublicKeySpec dpks = new DSAPublicKeySpec(
                new BigInteger("1"), 
                new BigInteger("2"), 
                new BigInteger("3"), 
                new BigInteger("4"));
        assertEquals(1, dpks.getY().intValue());
    }
}
