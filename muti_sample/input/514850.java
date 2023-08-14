@TestTargetClass(RSAPublicKey.class)
public class RSAPublicKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public void test_getPublicExponent() throws Exception {
        KeyFactory gen = KeyFactory.getInstance("RSA");
        final BigInteger n = BigInteger.valueOf(3233);
        final BigInteger e = BigInteger.valueOf(17);
        RSAPublicKey key = (RSAPublicKey) gen.generatePublic(new RSAPublicKeySpec(
                n, e));
        assertEquals("invalid public exponent", e, key.getPublicExponent());
    }
}
