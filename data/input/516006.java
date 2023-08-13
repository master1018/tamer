@TestTargetClass(ECPrivateKeySpec.class)
public class ECPrivateKeySpecTest extends TestCase {
    BigInteger s;
    ECParameterSpec ecparams;
    ECPrivateKeySpec ecpks;
    protected void setUp() throws Exception {
        super.setUp();
        ECPoint ecpoint = new ECPoint(BigInteger.valueOf(1), BigInteger
                .valueOf(1));
        EllipticCurve curve = new EllipticCurve(new ECFieldF2m(2), BigInteger
                .valueOf(1), BigInteger.valueOf(1));
        s = BigInteger.valueOf(1);
        ecparams = new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), 1);
        ecpks = new ECPrivateKeySpec(s, ecparams);
    }
    protected void tearDown() throws Exception {
        s = null;
        ecparams = null;
        ecpks = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ECPrivateKeySpec",
        args = {java.math.BigInteger.class, java.security.spec.ECParameterSpec.class}
    )
    public void test_constructorLjava_math_BigIntegerLjava_security_spec_ECParameterSpec() {
        assertEquals("wrong private value", s, ecpks.getS());
        assertEquals("wrong parameters", ecparams, ecpks.getParams());
        try {
            new ECPrivateKeySpec(null, ecparams);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
        }
        try {
            new ECPrivateKeySpec(s, null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getS",
        args = {}
    )
    public void test_GetS() {
        assertEquals("wrong private value", s, ecpks.getS());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParams",
        args = {}
    )
    public void test_GetParams() {
        assertEquals("wrong parameters", ecparams, ecpks.getParams());
    }
}
