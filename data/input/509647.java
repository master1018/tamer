@TestTargetClass(RSAOtherPrimeInfo.class)
public class RSAOtherPrimeInfoTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "RSAOtherPrimeInfo",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAOtherPrimeInfo01() {
        Object o =
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertTrue(o instanceof RSAOtherPrimeInfo);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAOtherPrimeInfo",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAOtherPrimeInfo02() {
        try {
            new RSAOtherPrimeInfo(null,
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAOtherPrimeInfo",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAOtherPrimeInfo03() {
        try {
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  null,
                                  BigInteger.valueOf(3L));
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAOtherPrimeInfo",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAOtherPrimeInfo04() {
        try {
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  null);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAOtherPrimeInfo",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAOtherPrimeInfo05() {
        try {
            new RSAOtherPrimeInfo(null,
                                  BigInteger.valueOf(2L),
                                  null);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCrtCoefficient",
        args = {}
    )
    public final void testGetCrtCoefficient() {
        RSAOtherPrimeInfo ropi = 
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(3L, ropi.getCrtCoefficient().longValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrime",
        args = {}
    )
    public final void testGetPrime() {
        RSAOtherPrimeInfo ropi = 
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(1L, ropi.getPrime().longValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getExponent",
        args = {}
    )
    public final void testGetExponent() {
        RSAOtherPrimeInfo ropi = 
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(2L, ropi.getExponent().longValue());
    }
}
