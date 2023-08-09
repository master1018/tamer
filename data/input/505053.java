@TestTargetClass(ECFieldFp.class)
public class ECFieldFpTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECFieldFp",
        args = {java.math.BigInteger.class}
    )
    public final void testECFieldFp01() {
        new ECFieldFp(BigInteger.valueOf(23L));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECFieldFp",
        args = {java.math.BigInteger.class}
    )
    public final void testECFieldFp02() {
        new ECFieldFp(BigInteger.valueOf(21L));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECFieldFp",
        args = {java.math.BigInteger.class}
    )
    public final void testECFieldFp03() {
        try {
            new ECFieldFp(BigInteger.valueOf(-1L)); 
            fail(getName() +
                    " FAILED: expected exception has not been thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECFieldFp",
        args = {java.math.BigInteger.class}
    )
    public final void testECFieldFp04() {
        try {
            new ECFieldFp(BigInteger.valueOf(0L));
            fail(getName() +
                    " FAILED: expected exception has not been thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECFieldFp",
        args = {java.math.BigInteger.class}
    )
    public final void testECFieldFp05() {
        try {
            new ECFieldFp(null);
            fail(getName() +
                    " FAILED: expected exception has not been thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode01() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode02() {
        assertTrue(new ECFieldFp(BigInteger.valueOf(23L)).hashCode() ==
                   new ECFieldFp(BigInteger.valueOf(23L)).hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFieldSize",
        args = {}
    )
    public final void testGetFieldSize() {
        assertEquals(5, new ECFieldFp(BigInteger.valueOf(23L)).getFieldSize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getP",
        args = {}
    )
    public final void testGetP() {
        BigInteger p = BigInteger.valueOf(23L);
        assertTrue(p.equals(new ECFieldFp(p).getP()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        ECFieldFp obj = new ECFieldFp(BigInteger.valueOf(23L));
        assertTrue(obj.equals(obj));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject02() {
        assertFalse(new ECFieldFp(BigInteger.valueOf(23L)).equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject03() {
        assertFalse(new ECFieldFp(BigInteger.valueOf(23L)).equals(new Object()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject04() {
        assertTrue(new ECFieldFp(BigInteger.valueOf(23L)).equals(
                   new ECFieldFp(BigInteger.valueOf(23L))));
    }
}
