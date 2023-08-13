@TestTargetClass(RSAPrivateCrtKeySpec.class)
public class RSAPrivateCrtKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "RSAPrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPrivateCrtKeySpec01() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateCrtKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "RSAPrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPrivateCrtKeySpec02() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "RSAPrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPrivateCrtKeySpec03() {
        new RSAPrivateCrtKeySpec(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCrtCoefficient",
        args = {}
    )
    public final void testGetCrtCoefficient() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L));
        assertTrue(BigInteger.valueOf(5L).equals(ks.getCrtCoefficient()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentP",
        args = {}
    )
    public final void testGetPrimeExponentP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentP()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentQ",
        args = {}
    )
    public final void testGetPrimeExponentQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentQ()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeP",
        args = {}
    )
    public final void testGetPrimeP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeP()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeQ",
        args = {}
    )
    public final void testGetPrimeQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeQ()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public final void testGetPublicExponent() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPublicExponent()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getModulus",
        args = {}
    )
    public final void testGetModulus() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getModulus()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateExponent",
        args = {}
    )
    public final void testGetPrivateExponent() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrivateExponent()));
    }
}
