@TestTargetClass(java.nio.DoubleBuffer.class)
public class DirectDoubleBufferTest extends DoubleBufferTest {
    public void setUp(){
        capacity = BUFFER_LENGTH;
        buf = ByteBuffer.allocateDirect(BUFFER_LENGTH*8).asDoubleBuffer();
        loadTestData1(buf);
        baseBuf = buf;
    }
    public void tearDown(){
        buf = null;
        baseBuf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies hasArray method for direct DoubleBuffer.",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies array method for direct DoubleBuffer.",
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
        notes = "Verifies arrayOffset method for direct DoubleBuffer.",
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
        notes = "Verifies isDirect method for direct DoubleBuffer.",
        method = "isDirect",
        args = {}
    )
    public void testIsDirect() {
        assertTrue(buf.isDirect());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies order method for direct DoubleBuffer.",
        method = "order",
        args = {}
    )
    public void testOrder() {
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order());
    }
}
