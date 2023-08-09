@TestTargetClass(ECPublicKeySpec.class)
public class ECPublicKeySpecTest extends TestCase {
    ECPoint w;
    ECParameterSpec params;
    ECPublicKeySpec ecpks;
    protected void setUp() throws Exception {
        super.setUp();
        ECPoint ecpoint = new ECPoint(BigInteger.valueOf(1), BigInteger
                .valueOf(1));
        EllipticCurve curve = new EllipticCurve(new ECFieldF2m(2), BigInteger
                .valueOf(1), BigInteger.valueOf(1));
        w = new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(1));
        params = new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), 1);
        ecpks = new ECPublicKeySpec(w, params);
    }
    protected void tearDown() throws Exception {
        w = null;
        params = null;
        ecpks = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ECPublicKeySpec",
        args = {java.security.spec.ECPoint.class, java.security.spec.ECParameterSpec.class}
    )
    public final void test_constructorLjava_security_spec_ECPointLjava_security_spec_ECParameterSpec() {
        assertEquals("wrong params value", params, ecpks.getParams());
        assertEquals("wrong w value", w, ecpks.getW());
        try {
            new ECPublicKeySpec(null, params);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
        }
        try {
            new ECPublicKeySpec(w, null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getW",
        args = {}
    )
    public final void testGetW() {
        assertEquals("wrong w value", w, ecpks.getW());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParams",
        args = {}
    )
    public final void testGetParams() {
        assertEquals("wrong params value", params, ecpks.getParams());
    }
}
