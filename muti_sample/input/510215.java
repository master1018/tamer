@TestTargetClass(EllipticCurve.class)
public class EllipticCurveTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive cases.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class, byte[].class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray01() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        new EllipticCurve(f, a, b, seed);
        ECFieldF2m f1 = new ECFieldF2m(5);
        a = BigInteger.ZERO;
        b = BigInteger.valueOf(23L);
        new EllipticCurve(f1, a, b, seed);
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = BigInteger.valueOf(19L);
        seed = null;
        new EllipticCurve(f, a, b, seed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class, byte[].class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray02() {
        ECFieldFp f = null;
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = null;
        b = BigInteger.valueOf(19L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = null;
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class, byte[].class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray03() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.valueOf(24L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(1L);
        b = BigInteger.valueOf(23L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1.1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(24L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(25L);
        b = BigInteger.valueOf(240L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class, byte[].class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray04() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(32L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(32L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(32L);
        b = BigInteger.valueOf(43L);
        seed = new byte[24];
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that byte array of EllipticCurve can't be modified",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class, byte[].class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray05() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(0L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        byte[] seedCopy = seed.clone();
        EllipticCurve c = new EllipticCurve(f, a, b, seedCopy);
        seedCopy[0] = (byte) 1;
        assertTrue(Arrays.equals(seed, c.getSeed()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigInteger01() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        new EllipticCurve(f, a, b);
        ECFieldF2m f1 = new ECFieldF2m(5);
        a = BigInteger.ZERO;
        b = BigInteger.valueOf(23L);
        new EllipticCurve(f1, a, b);
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = BigInteger.valueOf(19L);
        new EllipticCurve(f, a, b);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigInteger02() {
        ECFieldFp f = null;
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = null;
        b = BigInteger.valueOf(19L);
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = null;
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigInteger03() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.valueOf(24L);
        BigInteger b = BigInteger.valueOf(19L);
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(23L);
        b = BigInteger.valueOf(19L);
        try {
            new EllipticCurve(f, a, b);
            fail("#1.1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(24L);
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(25L);
        b = BigInteger.valueOf(240L);
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigInteger04() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(32L);
        BigInteger b = BigInteger.valueOf(19L);
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(32L);
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(32L);
        b = BigInteger.valueOf(43L);
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getA",
        args = {}
    )
    public final void testGetA() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(a, c.getA());
        assertSame(a, c.getA());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test.",
        method = "EllipticCurve",
        args = {java.security.spec.ECField.class, java.math.BigInteger.class, java.math.BigInteger.class}
    )
    public final void testEllipticCurveECFieldBigIntegerBigInteger05() {
        EllipticCurve ec = new EllipticCurve(new testECField(), BigInteger
                .valueOf(4L), BigInteger.ONE);
        assertEquals("incorrect a", ec.getA(), BigInteger.valueOf(4L));
        assertEquals("incorrect b", ec.getB(), BigInteger.ONE);
        assertEquals("incorrect size", ec.getField().getFieldSize(), 2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getB",
        args = {}
    )
    public final void testGetB() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(b, c.getB());
        assertSame(b, c.getB());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getField",
        args = {}
    )
    public final void testGetField() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(f, c.getField());
        assertSame(f, c.getField());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getSeed",
        args = {}
    )
    public final void testGetSeed01() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed);
        byte[] seedRet = c.getSeed();
        assertNotNull(seedRet);
        assertTrue(Arrays.equals(seed, seedRet));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that modification of byte array  doesn't change internal state of test object.",
        method = "getSeed",
        args = {}
    )
    public final void testGetSeed02() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed.clone());
        byte[] seedRet = c.getSeed();
        seedRet[0] = (byte) 1;
        assertTrue(Arrays.equals(seed, c.getSeed()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that repeated calls of getSeed method must return different refs.",
        method = "getSeed",
        args = {}
    )
    public final void testGetSeed03() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed);
        c.getSeed();
        assertNotSame(c.getSeed(), c.getSeed());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test.",
        method = "getSeed",
        args = {}
    )
    public final void testGetSeed04() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        assertNull(new EllipticCurve(f, a, a).getSeed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        EllipticCurve c2 = null, c1 = new EllipticCurve(new ECFieldFp(
                BigInteger.valueOf(23L)), BigInteger.ONE, BigInteger
                .valueOf(19L));
        assertTrue(c1.equals(c1));
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L));
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L));
        assertTrue(c1.equals(c2) && c2.equals(c1));
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24]);
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L), new byte[24]);
        assertTrue(c1.equals(c2) && c2.equals(c1));
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24]);
        MyEllipticCurve c3 = new MyEllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]);
        assertTrue(c1.equals(c3) && c3.equals(c1));
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L));
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L), null);
        assertTrue(c1.equals(c2) && c2.equals(c1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that several calls of hashCode method for the same objects return the same values.",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode01() {
        int hc = 0;
        EllipticCurve f = new EllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]);
        hc = f.hashCode();
        assertTrue(hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode02() {
        assertEquals(new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24])
                .hashCode(), new EllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]).hashCode());
    }
    class testECField implements ECField {
        public int getFieldSize() {
            return 2;
        }
    }
    private static class MyEllipticCurve extends EllipticCurve {
        MyEllipticCurve(ECField f, BigInteger a, BigInteger b, byte[] seed) {
            super(f, a, b, seed);
        }
    }
}
