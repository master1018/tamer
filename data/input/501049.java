@TestTargetClass(RSAPrivateKeySpec.class)
public class RSAPrivateKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RSAPrivateKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testRSAPrivateKeySpec() {
        KeySpec ks = new RSAPrivateKeySpec(BigInteger.valueOf(1234567890L),
                                           BigInteger.valueOf(3L));
        assertTrue(ks instanceof RSAPrivateKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getModulus",
        args = {}
    )
    public final void testGetModulus() {
        RSAPrivateKeySpec rpks =
            new RSAPrivateKeySpec(BigInteger.valueOf(1234567890L),
                                  BigInteger.valueOf(3L));
        assertEquals(1234567890L, rpks.getModulus().longValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateExponent",
        args = {}
    )
    public final void testGetPrivateExponent() {
        RSAPrivateKeySpec rpks =
            new RSAPrivateKeySpec(BigInteger.valueOf(1234567890L),
                                  BigInteger.valueOf(3L));
        assertEquals(3L, rpks.getPrivateExponent().longValue());
    }
}
