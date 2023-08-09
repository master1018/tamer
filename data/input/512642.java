@TestTargetClass(RSAMultiPrimePrivateCrtKey.class)
public class RSAMultiPrimePrivateCrtKeyTest extends TestCase {
    private static final RSAOtherPrimeInfo[] opi = new RSAOtherPrimeInfo[] {
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
    };
    private final BigInteger publicExponent = BigInteger.ONE;
    private final BigInteger primeExponentP = BigInteger.ONE;
    private final BigInteger primeExponentQ = BigInteger.ONE;
    private final BigInteger primeP = BigInteger.ONE;
    private final BigInteger primeQ = BigInteger.ONE;
    private final BigInteger crtCoefficient = BigInteger.ONE;
    class RSAMulti extends RSAMultiPrimePrivateCrtKeyImpl {
        public RSAMulti(BigInteger publicExp,
                        BigInteger primeExpP, 
                        BigInteger primeExpQ, 
                        BigInteger prP, 
                        BigInteger prQ, 
                        BigInteger crtCft, 
                        RSAOtherPrimeInfo[] otherPrmInfo) {
            super(publicExp, primeExpP, primeExpQ, prP, prQ, crtCft, otherPrmInfo);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCrtCoefficient",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrimeExponentP",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrimeExponentQ",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrimeP",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrimeQ",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPublicExponent",
            args = {}
        )
    })
    public void test_RSAMultiPrimePrivateCrtKey() {
        RSAMulti rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                                     primeP, primeQ, crtCoefficient, opi);
        try {
            assertEquals(rsam.getCrtCoefficient(), crtCoefficient);
            assertEquals(rsam.getPrimeExponentP(), primeExponentP);
            assertEquals(rsam.getPrimeExponentQ(), primeExponentQ);
            assertEquals(rsam.getPrimeP(), primeP);
            assertEquals(rsam.getPrimeQ(), primeQ);
            assertEquals(rsam.getPublicExponent(), publicExponent);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOtherPrimeInfo",
        args = {}
    )
    public void test_getOtherPrimeInfo() {
        RSAMulti rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                                     primeP, primeQ, crtCoefficient, null);
        try {
            assertNull("Object RSAOtherPrimeInfo is not NULL", rsam.getOtherPrimeInfo());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                            primeP, primeQ, crtCoefficient, opi);
        try {
            assertEquals(rsam.getOtherPrimeInfo(), opi);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}