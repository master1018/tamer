@TestTargetClass(java.nio.ByteBuffer.class)
public class HeapByteBufferTest extends ByteBufferTest {
    protected void setUp() throws Exception {   
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "allocate",
        args = {int.class}
    )
    public void testAllocatedByteBuffer_IllegalArg() {
        try {
            ByteBuffer.allocate(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
    }
}
