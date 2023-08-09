@TestTargetClass(ECParameterSpec.class)
public class ECParameterSpecTest extends TestCase {
    EllipticCurve curve;
    ECPoint ecpoint;
    ECParameterSpec ecps;
    protected void setUp() throws Exception {
        super.setUp();
        curve = new EllipticCurve(new ECFieldF2m(2), BigInteger.valueOf(1),
                BigInteger.valueOf(1));
        ecpoint = new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(1));
        ecps = new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), 1);
    }
    protected void tearDown() throws Exception {
        curve = null;
        ecpoint = null;
        ecps = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ECParameterSpec",
        args = {java.security.spec.EllipticCurve.class, java.security.spec.ECPoint.class, java.math.BigInteger.class, int.class}
    )
    public void test_constructorLjava_security_spec_EllipticCurveLjava_security_spec_ECPointLjava_math_BigIntegerI() {
        assertEquals("wrong cofactor was returned", 1, ecps.getCofactor());
        assertEquals("wrong elliptic curve", curve, ecps.getCurve());
        assertEquals("wrong generator was returned", ecpoint, ecps
                .getGenerator());
        assertEquals("wrong order was reteurned", BigInteger.valueOf(1), ecps
                .getOrder());
        try {
            new ECParameterSpec(null, ecpoint, BigInteger.valueOf(1), 1);
            fail("NullPointerException exception has not been thrown");
        } catch (NullPointerException e) {
        }
        try {
            new ECParameterSpec(curve, null, BigInteger.valueOf(1), 1);
            fail("NullPointerException exception has not been thrown");
        } catch (NullPointerException e) {
        }
        try {
            new ECParameterSpec(curve, ecpoint, null, 1);
            fail("NullPointerException exception has not been thrown");
        } catch (NullPointerException e) {
        }
        try {
            new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(-1), 1);
            fail("IllegalArgumentException exception has not been thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), -1);
            fail("IllegalArgumentException exception has not been thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCurve",
        args = {}
    )
    public void test_GetCurve() {
        assertEquals("wrong elliptic curve", curve, ecps.getCurve());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getGenerator",
        args = {}
    )
    public void test_GetGenerator() {
        assertEquals("wrong generator was returned", ecpoint, ecps
                .getGenerator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOrder",
        args = {}
    )
    public void test_GetOrder() {
        assertEquals("wrong order was reteurned", BigInteger.valueOf(1), ecps
                .getOrder());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCofactor",
        args = {}
    )
    public void test_GetCofactor() {
        assertEquals("wrong cofactor was returned", 1, ecps.getCofactor());
    }
}
