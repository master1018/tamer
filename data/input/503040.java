@TestTargetClass(java.nio.IntBuffer.class)
public class DirectIntBufferTest extends IntBufferTest {
    public void setUp(){
        capacity = BUFFER_LENGTH;
        buf = ByteBuffer.allocateDirect(BUFFER_LENGTH*4).asIntBuffer();
        loadTestData1(buf);
        baseBuf = buf;
    }
    public void tearDown(){
        buf = null;
        baseBuf = null;
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "put",
            args = {int[].class, int.class, int.class}
    )
    public void testPutWhenOffsetIsNonZero() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        int[] source = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        intBuffer.put(source, 2, 2);
        intBuffer.put(source, 4, 2);
        assertEquals(4, intBuffer.get(0));
        assertEquals(5, intBuffer.get(1));
        assertEquals(6, intBuffer.get(2));
        assertEquals(7, intBuffer.get(3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies hasArray method for direct IntBuffer.",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies array method for direct IntBuffer.",
        method = "array",
        args = {}
    )
    public void testArray() {
        try {
            buf.array();
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies arrayOffset method for direct IntBuffer.",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isDirect method for direct IntBuffer.",
        method = "isDirect",
        args = {}
    )
    public void testIsDirect() {
        assertTrue(buf.isDirect());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies order method for direct IntBuffer.",
        method = "order",
        args = {}
    )
    public void testOrder() {
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test for IntToByteBufferAdapter",
        clazz = ByteBuffer.class,
        method = "asIntBuffer",
        args = {}
    )
    public void testRangeChecks() {
        int[] myInts = new int[BUFFER_LENGTH];
        for (int i = 0; i < BUFFER_LENGTH; i++) {
            myInts[i] = 1000 + i;
        }
        buf.position(0);
        buf.put(myInts, 0, BUFFER_LENGTH);
        buf.position(0);
        buf.put(myInts, 0, BUFFER_LENGTH);
        try {
            buf.put(myInts, 0, 1); 
            fail("BufferOverflowException expected but not thrown");
        } catch (BufferOverflowException boe) {
        }
        try {
            buf.position(0);
            buf.put(myInts, 0, BUFFER_LENGTH + 1); 
            fail("BufferOverflowException expected but not thrown");
        } catch (IndexOutOfBoundsException ioobe) {
        }
        try {
            buf.position(BUFFER_LENGTH - 1);
            buf.put(myInts, 0, 2); 
            fail("BufferOverflowException expected but not thrown");
        } catch (BufferOverflowException boe) {
        }
    }
}
