@TestTargetClass(RSAMultiPrimePrivateCrtKeySpec.class)
public class RSAMultiPrimePrivateCrtKeySpecTest extends TestCase {
    private static final RSAOtherPrimeInfo[] opi = new RSAOtherPrimeInfo[] {
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
    };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec01() {
        KeySpec ks = new RSAMultiPrimePrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                opi);
        assertTrue(ks instanceof RSAMultiPrimePrivateCrtKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec02() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec03() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec04() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec05() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec06() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec07() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec08() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    BigInteger.ONE,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec09() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null,
                    opi);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null otherPrimeInfo.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec10() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                null);
        } catch (Exception e) {
            fail("Unexpected exception is thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec11() {
        try {
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    new RSAOtherPrimeInfo[0]);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor using valid parameters. Constructed object must be instance of RSAPrivateKeySpec.",
        method = "RSAMultiPrimePrivateCrtKeySpec",
        args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
    )
    public final void testRSAMultiPrimePrivateCrtKeySpec12() {
        KeySpec ks = new RSAMultiPrimePrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                opi);
        assertTrue(ks instanceof RSAPrivateKeySpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCrtCoefficient",
        args = {}
    )
    public final void testGetCrtCoefficient() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getCrtCoefficient()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentP",
        args = {}
    )
    public final void testGetPrimeExponentP() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPrimeExponentP()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeExponentQ",
        args = {}
    )
    public final void testGetPrimeExponentQ() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPrimeExponentQ()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeP",
        args = {}
    )
    public final void testGetPrimeP() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPrimeP()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrimeQ",
        args = {}
    )
    public final void testGetPrimeQ() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPrimeQ()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public final void testGetPublicExponent() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPublicExponent()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getOtherPrimeInfo",
        args = {}
    )
    public final void testGetOtherPrimeInfo01() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(checkOtherPrimeInfo(ks.getOtherPrimeInfo()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getOtherPrimeInfo returns null if there are only two prime factors.",
        method = "getOtherPrimeInfo",
        args = {}
    )
    public final void testGetOtherPrimeInfo02() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    null);
        assertNull(ks.getOtherPrimeInfo());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that internal state of the object can not be modified by modifying initial array.",
            method = "RSAMultiPrimePrivateCrtKeySpec",
            args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that internal state of the object can not be modified by modifying initial array.",
            method = "getOtherPrimeInfo",
            args = {}
        )
    })
    public final void testIsStatePreserved1() {
        RSAOtherPrimeInfo[] opi1 = opi.clone();
        RSAMultiPrimePrivateCrtKeySpec ks = new RSAMultiPrimePrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                opi1);
        opi1[2] = new RSAOtherPrimeInfo(BigInteger.ZERO,
                                        BigInteger.ZERO,
                                        BigInteger.ZERO);
        assertTrue(checkOtherPrimeInfo(ks.getOtherPrimeInfo()));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that internal state of the object can not be modified using array reference returned by getOtherPrimeInfo() method.",
            method = "RSAMultiPrimePrivateCrtKeySpec",
            args = {java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.math.BigInteger.class, java.security.spec.RSAOtherPrimeInfo[].class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that internal state of the object can not be modified using array reference returned by getOtherPrimeInfo() method.",
            method = "getOtherPrimeInfo",
            args = {}
        )
    })
    public final void testIsStatePreserved2() {
        RSAOtherPrimeInfo[] opi1 = opi.clone();
        RSAMultiPrimePrivateCrtKeySpec ks = new RSAMultiPrimePrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                opi1);
        RSAOtherPrimeInfo[] ret = ks.getOtherPrimeInfo();
        ret[2] = new RSAOtherPrimeInfo(BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO);
        assertTrue(checkOtherPrimeInfo(ks.getOtherPrimeInfo()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getModulus",
        args = {}
    )
    public final void testGetModulus() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getModulus()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateExponent",
        args = {}
    )
    public final void testGetPrivateExponent() {
        RSAMultiPrimePrivateCrtKeySpec ks =
            new RSAMultiPrimePrivateCrtKeySpec(
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    BigInteger.ONE,
                    opi);
        assertTrue(BigInteger.ONE.equals(ks.getPrivateExponent()));
    }
    private boolean checkOtherPrimeInfo(RSAOtherPrimeInfo[] toBeChecked) {
        if (toBeChecked == null || toBeChecked.length != opi.length) {
            return false;
        }
        for (int i=0; i<opi.length; i++) {
            if (opi[i].getPrime().equals(toBeChecked[i].getPrime()) &&
                opi[i].getExponent().equals(toBeChecked[i].getExponent()) &&
                opi[i].getCrtCoefficient().equals(toBeChecked[i].getCrtCoefficient())) {
                continue;
            }
            return false;
        }
        return true;
    }
}
