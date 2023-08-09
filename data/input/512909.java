@TestTargetClass(java.nio.LongBuffer.class)
public class HeapLongBufferTest extends LongBufferTest {
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
    public void testAllocatedLongBuffer_IllegalArg() {
        try {
            LongBuffer.allocate(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
    }
}
