@TestTargetClass(ECPoint.class)
public class ECPointTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive cases.",
        method = "ECPoint",
        args = {java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testECPoint01() {
        new ECPoint(BigInteger.ZERO, BigInteger.ZERO);
        new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(-23456L));
        new ECPoint(BigInteger.valueOf(123456L), BigInteger.valueOf(123456L));
        new ECPoint(BigInteger.valueOf(-56L), BigInteger.valueOf(234L));
        new ECPoint(BigInteger.valueOf(3456L), BigInteger.valueOf(-2344L));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exceptions.",
        method = "ECPoint",
        args = {java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testECPoint02() {
        try {
            new ECPoint(null, BigInteger.ZERO);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new ECPoint(BigInteger.ZERO, null);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new ECPoint(null, null);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getAffineX",
        args = {}
    )
    public final void testGetAffineX01() {
        BigInteger x = BigInteger.valueOf(-23456L);
        ECPoint p = new ECPoint(x, BigInteger.valueOf(23456L));
        BigInteger xRet = p.getAffineX();
        assertEquals(x, xRet);
        assertSame(x, xRet);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getAffineX returns null for POINT_INFINITY.",
        method = "getAffineX",
        args = {}
    )
    public final void testGetAffineX02() {
        assertNull(ECPoint.POINT_INFINITY.getAffineX());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "getAffineY",
        args = {}
    )
    public final void testGetAffineY01() {
        BigInteger y =  BigInteger.valueOf(23456L);
        ECPoint p = new ECPoint(BigInteger.valueOf(-23456L), y);
        BigInteger yRet = p.getAffineY();
        assertEquals(y, yRet);
        assertSame(y, yRet);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getAffineY ruturns null for POINT_INFINITY.",
        method = "getAffineY",
        args = {}
    )
    public final void testGetAffineY02() {
        assertNull(ECPoint.POINT_INFINITY.getAffineY());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive cases.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        ECPoint p2=null, p1 =
            new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        assertTrue(p1.equals(p1));
        p1 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertTrue(p1.equals(p2) && p2.equals(p1));
        p1 = ECPoint.POINT_INFINITY;
        p2 = ECPoint.POINT_INFINITY;
        assertTrue(p1.equals(p2) && p2.equals(p1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative cases.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject02() {
        ECPoint p2=null, p1 =
            new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        assertFalse(p1.equals(p2));
        p1 = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertFalse(p1.equals(p2) || p2.equals(p1));
        p1 = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ZERO);
        assertFalse(p1.equals(p2) || p2.equals(p1));
        p1 = ECPoint.POINT_INFINITY;
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ZERO);
        assertFalse(p1.equals(p2) || p2.equals(p1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode01() {
        ECPoint f = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
        hc = ECPoint.POINT_INFINITY.hashCode();
        assertTrue(hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode02() {
        ECPoint p1 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        ECPoint p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
