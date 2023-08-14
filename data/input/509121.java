@TestTargetClass(RSAPrivateCrtKey.class)
public class RSAPrivateCrtKeyTest extends TestCase {
    RSAPrivateCrtKey key = null;
    protected void setUp() throws Exception {
        super.setUp();
        KeyFactory gen = KeyFactory.getInstance("RSA");
        key = (RSAPrivateCrtKey) gen.generatePrivate(Util.rsaCrtParam);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCrtCoefficient",
        args = {}
    )
    public void test_getCrtCoefficient() {
        assertEquals("invalid CRT coefficient",
                Util.rsaCrtParam.getCrtCoefficient(), key.getCrtCoefficient());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentP",
        args = {}
    )
    public void test_getPrimeExponentP() {
        assertEquals("invalid prime exponent P",
                Util.rsaCrtParam.getPrimeExponentP(), key.getPrimeExponentP());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentQ",
        args = {}
    )
    public void test_getPrimeExponentQ() {
        assertEquals("invalid prime exponent Q",
                Util.rsaCrtParam.getPrimeExponentQ(), key.getPrimeExponentQ());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeP",
        args = {}
    )
    public void test_getPrimeP() {
        assertEquals("invalid prime P",
                Util.rsaCrtParam.getPrimeP(), key.getPrimeP());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeQ",
        args = {}
    )
    public void test_getPrimeQ() {
        assertEquals("invalid prime Q",
                Util.rsaCrtParam.getPrimeQ(), key.getPrimeQ());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public void test_getPublicExponent() {
        assertEquals("invalid public exponent",
                Util.rsaCrtParam.getPublicExponent(), key.getPublicExponent());
    }
    protected void tearDown() throws Exception {
        key = null;
        super.tearDown();
    }
}
