@TestTargetClass(RSAPublicKeySpec.class)
public class RSAPublicKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "RSAPublicKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPublicKeySpec01() {
        KeySpec ks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        assertTrue(ks instanceof RSAPublicKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "RSAPublicKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPublicKeySpec02() {
        KeySpec ks =
            new RSAPublicKeySpec(null, null);
        assertTrue(ks instanceof RSAPublicKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getModulus",
        args = {}
    )
    public final void testGetModulus() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getModulus()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public final void testGetPublicExponent() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(3L),
                                 BigInteger.valueOf(1234567890L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getPublicExponent()));
    }
}
