@TestTargetClass(java.nio.CharBuffer.class)
public class HeapCharBufferTest extends CharBufferTest {
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
    public void testAllocatedCharBuffer_IllegalArg() {
        try {
            CharBuffer.allocate(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
    }
}
