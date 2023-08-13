@TestTargetClass(BigInteger.class)
public class BigIntegerXorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testZeroPos() {
        String numA = "0";
        String numB = "27384627835298756289327365";
        String res  = "27384627835298756289327365";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testZeroNeg() {
        String numA = "0";
        String numB = "-27384627835298756289327365";
        String res =  "-27384627835298756289327365";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosZero() {
        String numA = "27384627835298756289327365";
        String numB = "0";
        String res = "27384627835298756289327365";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegPos() {
        String numA = "-27384627835298756289327365";
        String numB = "0";
        String res = "-27384627835298756289327365";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testZeroZero() {
        String numA = "0";
        String numB = "0";
        String res = "0";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testZeroOne() {
        String numA = "0";
        String numB = "1";
        String res = "1";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testOneOne() {
        String numA = "1";
        String numB = "1";
        String res = "0";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosSameLength() {
        String numA = "283746278342837476784564875684767";
        String numB = "293478573489347658763745839457637";
        String res = "71412358434940908477702819237626";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosFirstLonger() {
        String numA = "2837462783428374767845648748973847593874837948575684767";
        String numB = "293478573489347658763745839457637";
        String res = "2837462783428374767845615168483972194300564226167553530";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosFirstShorter() {
        String numA = "293478573489347658763745839457637";
        String numB = "2837462783428374767845648748973847593874837948575684767";
        String res = "2837462783428374767845615168483972194300564226167553530";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegSameLength() {
        String numA = "-283746278342837476784564875684767";
        String numB = "-293478573489347658763745839457637";
        String res = "71412358434940908477702819237626";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegFirstLonger() {
        String numA = "-2837462783428374767845648748973847593874837948575684767";
        String numB = "-293478573489347658763745839457637";
        String res = "2837462783428374767845615168483972194300564226167553530";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegFirstShorter() {
        String numA = "293478573489347658763745839457637";
        String numB = "2837462783428374767845648748973847593874837948575684767";
        String res = "2837462783428374767845615168483972194300564226167553530";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegSameLength() {
        String numA = "283746278342837476784564875684767";
        String numB = "-293478573489347658763745839457637";
        String res = "-71412358434940908477702819237628";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosSameLength() {
        String numA = "-283746278342837476784564875684767";
        String numB = "293478573489347658763745839457637";
        String res = "-71412358434940908477702819237628";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosFirstLonger() {
        String numA = "-2837462783428374767845648748973847593874837948575684767";
        String numB = "293478573489347658763745839457637";
        String res = "-2837462783428374767845615168483972194300564226167553532";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosFirstShorter() {
        String numA = "-293478573489347658763745839457637";
        String numB = "2837462783428374767845648748973847593874837948575684767";
        String res = "-2837462783428374767845615168483972194300564226167553532";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegFirstLonger() {
        String numA = "2837462783428374767845648748973847593874837948575684767";
        String numB = "-293478573489347658763745839457637";
        String res = "-2837462783428374767845615168483972194300564226167553532";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for xor operation.",
        method = "xor",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegFirstShorter() {
        String numA = "293478573489347658763745839457637";
        String numB = "-2837462783428374767845648748973847593874837948575684767";
        String res = "-2837462783428374767845615168483972194300564226167553532";
        BigInteger aNumber = new BigInteger(numA);
        BigInteger bNumber = new BigInteger(numB);
        BigInteger result = aNumber.xor(bNumber);
        assertTrue(res.equals(result.toString()));
    }
}
