@TestTargetClass(org.apache.harmony.luni.platform.OSMemory.class)
public class OSMemoryTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "memset",
        args = {}
    )
    public void testMemset() {
        IMemorySystem memory = Platform.getMemorySystem();
        int byteCount = 32;
        int ptr = memory.malloc(byteCount);
        try {
            memory.setByte(ptr, (byte) 1);
            assertEquals((byte) 1, memory.getByte(ptr));
            memory.memset(ptr, (byte) 0, byteCount);
            assertBytesEqual((byte) 0, ptr, byteCount);
            memory.memset(ptr, (byte) 1, byteCount);
            assertBytesEqual((byte) 1, ptr, byteCount);
        } finally {
            memory.free(ptr);
        }
    }
    void assertBytesEqual(byte value, int ptr, int byteCount) {
        IMemorySystem memory = Platform.getMemorySystem();
        for (int i = 0; i < byteCount; ++i) {
            assertEquals(value, memory.getByte(ptr + i));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setIntArray",
        args = {}
    )
    public void testSetIntArray() {
        IMemorySystem memory = Platform.getMemorySystem();
        int[] values = { 3, 7, 31, 127, 8191, 131071, 524287, 2147483647 };
        int[] swappedValues = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            swappedValues[i] = swapInt(values[i]);
        }
        int scale = ICommonDataTypes.SIZEOF_JINT;
        int ptr = memory.malloc(scale * values.length);
        try {
            memory.memset(ptr, (byte) 0, scale * values.length);
            memory.setIntArray(ptr, values, 0, values.length, false);
            assertIntsEqual(values, ptr);
            memory.memset(ptr, (byte) 0, scale * values.length);
            memory.setIntArray(ptr, values, 0, values.length, true);
            assertIntsEqual(swappedValues, ptr);
            memory.memset(ptr, (byte) 0, scale * values.length);
            for (int i = 0; i < values.length; ++i) {
                memory.setIntArray(ptr + i * scale, values, i, 1, true);
            }
            assertIntsEqual(swappedValues, ptr);
        } finally {
            memory.free(ptr);
        }
    }
    private void assertIntsEqual(int[] expectedValues, int ptr) {
        IMemorySystem memory = Platform.getMemorySystem();
        for (int i = 0; i < expectedValues.length; ++i) {
            assertEquals(expectedValues[i], memory.getInt(ptr + ICommonDataTypes.SIZEOF_JINT * i));
        }
    }
    private static int swapInt(int n) {
        return (((n >>  0) & 0xff) << 24) |
               (((n >>  8) & 0xff) << 16) |
               (((n >> 16) & 0xff) <<  8) |
               (((n >> 24) & 0xff) <<  0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setShortArray",
        args = {}
    )
    public void testSetShortArray() {
        IMemorySystem memory = Platform.getMemorySystem();
        short[] values = { 0x0001, 0x0020, 0x0300, 0x4000 };
        short[] swappedValues = { 0x0100, 0x2000, 0x0003, 0x0040 };
        int scale = ICommonDataTypes.SIZEOF_JSHORT;
        int ptr = memory.malloc(scale * values.length);
        try {
            memory.memset(ptr, (byte) 0, scale * values.length);
            memory.setShortArray(ptr, values, 0, values.length, false);
            assertShortsEqual(values, ptr);
            memory.memset(ptr, (byte) 0, scale * values.length);
            memory.setShortArray(ptr, values, 0, values.length, true);
            assertShortsEqual(swappedValues, ptr);
            memory.memset(ptr, (byte) 0, scale * values.length);
            for (int i = 0; i < values.length; ++i) {
                memory.setShortArray(ptr + i * scale, values, i, 1, true);
            }
            assertShortsEqual(swappedValues, ptr);
        } finally {
            memory.free(ptr);
        }
    }
    private void assertShortsEqual(short[] expectedValues, int ptr) {
        IMemorySystem memory = Platform.getMemorySystem();
        for (int i = 0; i < expectedValues.length; ++i) {
            assertEquals(expectedValues[i], memory.getShort(ptr + ICommonDataTypes.SIZEOF_JSHORT * i));
        }
    }
    private static short swapShort(short n) {
        return (short) ((((n >>  0) & 0xff) << 8) | (((n >>  8) & 0xff) << 0));
    }
}
