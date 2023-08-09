@TestTargetClass(RSAKey.class)
public class RSAKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getModulus",
        args = {}
    )
    public void test_getModulus() throws Exception {
        KeyFactory gen = KeyFactory.getInstance("RSA");
        final BigInteger n = BigInteger.valueOf(3233);
        final BigInteger d = BigInteger.valueOf(2753);
        final BigInteger e = BigInteger.valueOf(17);
        RSAKey key = null; 
        key = (RSAKey) gen.generatePrivate(new RSAPrivateKeySpec(n, d));
        assertEquals("invalid modulus", n, key.getModulus());
        key = (RSAKey) gen.generatePublic(new RSAPublicKeySpec(n, e));
        assertEquals("invalid modulus", n, key.getModulus());
    }
}
