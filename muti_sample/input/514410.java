public class JniInstanceTest extends TestCase {
    private InstanceNonce target;
    @Override
    protected void setUp() {
        target = new InstanceNonce();
    }
    public void test_nop() {
        target.nop();
    }
    public void test_returnBoolean() {
        assertEquals(false, target.returnBoolean());
    }
    public void test_returnByte() {
        assertEquals(123, target.returnByte());
    }
    public void test_returnShort() {
        assertEquals(-12345, target.returnShort());
    }
    public void test_returnChar() {
        assertEquals(34567, target.returnChar());
    }
    public void test_returnInt() {
        assertEquals(12345678, target.returnInt());
    }
    public void test_returnLong() {
        assertEquals(-1098765432109876543L, target.returnLong());
    }
    public void test_returnFloat() {
        assertEquals(-98765.4321F, target.returnFloat());
    }
    public void test_returnDouble() {
        assertEquals(12345678.9, target.returnDouble());
    }
    public void test_returnNull() {
        assertNull(target.returnNull());
    }
    public void test_returnString() {
        assertEquals("blort", target.returnString());
    }
    public void test_returnShortArray() {
        short[] array = target.returnShortArray();
        assertSame(short[].class, array.getClass());
        assertEquals(3, array.length);
        assertEquals(10, array[0]);
        assertEquals(20, array[1]);
        assertEquals(30, array[2]);
    }
    public void test_returnStringArray() {
        String[] array = target.returnStringArray();
        assertSame(String[].class, array.getClass());
        assertEquals(100, array.length);
        assertEquals("blort", array[0]);
        assertEquals(null,    array[1]);
        assertEquals("zorch", array[50]);
        assertEquals("fizmo", array[99]);
    }
    public void test_returnThis() {
        assertSame(target, target.returnThis());
    }
    public void test_takeBoolean() {
        assertTrue(target.takeBoolean(false));
    }
    public void test_takeByte() {
        assertTrue(target.takeByte((byte) -99));
    }
    public void test_takeShort() {
        assertTrue(target.takeShort((short) 19991));
    }
    public void test_takeChar() {
        assertTrue(target.takeChar((char) 999));
    }
    public void test_takeInt() {
        assertTrue(target.takeInt(-999888777));
    }
    public void test_takeLong() {
        assertTrue(target.takeLong(999888777666555444L));
    }
    public void test_takeFloat() {
        assertTrue(target.takeFloat(-9988.7766F));
    }
    public void test_takeDouble() {
        assertTrue(target.takeDouble(999888777.666555));
    }
    public void test_takeNull() {
        assertTrue(target.takeNull(null));
    }
    public void test_takeString() {
        assertTrue(target.takeString("fuzzbot"));
    }
    public void test_takeThis() {
        assertTrue(target.takeThis(target));
    }
    public void test_takeIntLong() {
        assertTrue(target.takeIntLong(914, 9140914091409140914L));
    }
    public void test_takeLongInt() {
        assertTrue(target.takeLongInt(-4321L, 12341234));
    }
    public void test_takeOneOfEach() {
        assertTrue(target.takeOneOfEach((boolean) false, (byte) 1,
                        (short) 2, (char) 3, (int) 4, 5L, "six", 7.0f, 8.0,
                        new int[] { 9, 10 }));
    }
    public void test_takeCoolHandLuke() {
        assertTrue(target.takeCoolHandLuke(1, 2, 3, 4, 5, 6, 7, 8, 9,
                        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                        20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                        30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                        40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                        50));
    }
}
