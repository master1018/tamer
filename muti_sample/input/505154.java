@TestTargetClass(BigInteger.class)
public class BigIntegerToStringTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadixOutOfRange() {
        String value = "442429234853876401";
        int radix = 10;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(45);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix2Neg() {
        String value = "-101001100010010001001010101110000101010110001010010101010101010101010101010101010101010101010010101";
        int radix = 2;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix2Pos() {
        String value = "101000011111000000110101010101010101010001001010101010101010010101010101010000100010010";
        int radix = 2;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix10Neg() {
        String value = "-2489756308572364789878394872984";
        int radix = 16;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix10Pos() {
        String value = "2387627892347567398736473476";
        int radix = 16;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix16Neg() {
        String value = "-287628a883451b800865c67e8d7ff20";
        int radix = 16;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix16Pos() {
        String value = "287628a883451b800865c67e8d7ff20";
        int radix = 16;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix24Neg() {
        String value = "-287628a88gmn3451b8ijk00865c67e8d7ff20";
        int radix = 24;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix24Pos() {
        String value = "287628a883451bg80ijhk0865c67e8d7ff20";
        int radix = 24;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix36Neg() {
        String value = "-uhguweut98iu4h3478tq3985pq98yeiuth33485yq4aiuhalai485yiaehasdkr8tywi5uhslei8";
        int radix = 36;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {int.class}
    )
    public void testRadix36Pos() {
        String value = "23895lt45y6vhgliuwhgi45y845htsuerhsi4586ysuerhtsio5y68peruhgsil4568ypeorihtse48y6";
        int radix = 36;
        BigInteger aNumber = new BigInteger(value, radix);
        String result = aNumber.toString(radix);
        assertTrue(result.equals(value));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString1() {
        String s = "0000000000";
        BigInteger bi = new BigInteger(s);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, "0");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString2() {
        String s = "1234567890987654321";
        BigInteger bi = new BigInteger(s);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString3() {
        String s = "-1234567890987654321";
        BigInteger bi = new BigInteger(s);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString4() {
        String s = "12345678901234";
        long l = 12345678901234L;
        BigInteger bi = BigInteger.valueOf(l);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString5() {
        String s = "-12345678901234";
        long l = -12345678901234L;
        BigInteger bi = BigInteger.valueOf(l);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        byte aBytes[] = {
                12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91
        };
        String s = "247856948764430159964673417020251";
        BigInteger bi = new BigInteger(aBytes);
        String sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
        byte aBytes_[] = {
                -12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91
        };
        s = "-238920881723209930210060613844133";
        bi = new BigInteger(aBytes_);
        sBI = bi.toString();
        assertEquals("toString method returns incorrect value instead of " + s, sBI, s);
    }
}
