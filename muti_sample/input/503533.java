public class JniStaticTest extends TestCase {
    public void test_nop() {
        StaticNonce.nop();
    }
    public void test_returnBoolean() {
        assertEquals(true, StaticNonce.returnBoolean());
    }
    public void test_returnByte() {
        assertEquals(123, StaticNonce.returnByte());
    }
    public void test_returnShort() {
        assertEquals(-12345, StaticNonce.returnShort());
    }
    public void test_returnChar() {
        assertEquals(34567, StaticNonce.returnChar());
    }
    public void test_returnInt() {
        assertEquals(12345678, StaticNonce.returnInt());
    }
    public void test_returnLong() {
        assertEquals(-1098765432109876543L, StaticNonce.returnLong());
    }
    public void test_returnFloat() {
        assertEquals(-98765.4321F, StaticNonce.returnFloat());
    }
    public void test_returnDouble() {
        assertEquals(12345678.9, StaticNonce.returnDouble());
    }
    public void test_returnNull() {
        assertNull(StaticNonce.returnNull());
    }
    public void test_returnString() {
        assertEquals("blort", StaticNonce.returnString());
    }
    public void test_returnShortArray() {
        short[] array = StaticNonce.returnShortArray();
        assertSame(short[].class, array.getClass());
        assertEquals(3, array.length);
        assertEquals(10, array[0]);
        assertEquals(20, array[1]);
        assertEquals(30, array[2]);
    }
    public void test_returnStringArray() {
        String[] array = StaticNonce.returnStringArray();
        assertSame(String[].class, array.getClass());
        assertEquals(100, array.length);
        assertEquals("blort", array[0]);
        assertEquals(null,    array[1]);
        assertEquals("zorch", array[50]);
        assertEquals("fizmo", array[99]);
    }
    public void test_returnThisClass() {
        assertSame(StaticNonce.class, StaticNonce.returnThisClass());
    }
    public void test_returnInstance() {
        StaticNonce nonce = StaticNonce.returnInstance();
        assertSame(StaticNonce.class, nonce.getClass());
    }
    public void test_takeBoolean() {
        assertTrue(StaticNonce.takeBoolean(true));
    }
    public void test_takeByte() {
        assertTrue(StaticNonce.takeByte((byte) -99));
    }
    public void test_takeShort() {
        assertTrue(StaticNonce.takeShort((short) 19991));
    }
    public void test_takeChar() {
        assertTrue(StaticNonce.takeChar((char) 999));
    }
    public void test_takeInt() {
        assertTrue(StaticNonce.takeInt(-999888777));
    }
    public void test_takeLong() {
        assertTrue(StaticNonce.takeLong(999888777666555444L));
    }
    public void test_takeFloat() {
        assertTrue(StaticNonce.takeFloat(-9988.7766F));
    }
    public void test_takeDouble() {
        assertTrue(StaticNonce.takeDouble(999888777.666555));
    }
    public void test_takeNull() {
        assertTrue(StaticNonce.takeNull(null));
    }
    public void test_takeString() {
        assertTrue(StaticNonce.takeString("fuzzbot"));
    }
    public void test_takeThisClass() {
        assertTrue(StaticNonce.takeThisClass(StaticNonce.class));
    }
    public void test_takeIntLong() {
        assertTrue(StaticNonce.takeIntLong(914, 9140914091409140914L));
    }
    public void test_takeLongInt() {
        assertTrue(StaticNonce.takeLongInt(-4321L, 12341234));
    }
    public void test_takeOneOfEach() {
        assertTrue(StaticNonce.takeOneOfEach((boolean) false, (byte) 1,
                        (short) 2, (char) 3, (int) 4, 5L, "six", 7.0f, 8.0,
                        new int[] { 9, 10 }));
    }
    public void test_takeCoolHandLuke() {
        assertTrue(StaticNonce.takeCoolHandLuke(1, 2, 3, 4, 5, 6, 7, 8, 9,
                        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                        20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                        30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                        40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                        50));
    }
}
