@TestTargetClass(java.nio.ShortBuffer.class)
public class HeapShortBufferTest extends ShortBufferTest {
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
    public void testAllocatedShortBuffer_IllegalArg() {
        try {
            ShortBuffer.allocate(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
    }
}
