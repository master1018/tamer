@TestTargetClass(RSAPrivateKey.class)
public class RSAPrivateKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateExponent",
        args = {}
    )
    public void test_getPrivateExponent() throws Exception {
        KeyFactory gen = KeyFactory.getInstance("RSA");
        final BigInteger n = BigInteger.valueOf(3233);
        final BigInteger d = BigInteger.valueOf(2753);
        RSAPrivateKey key = (RSAPrivateKey) gen.generatePrivate(new RSAPrivateKeySpec(
                n, d));
        assertEquals("invalid private exponent", d, key.getPrivateExponent());
    }
}
