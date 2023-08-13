@TestTargetClass(java.nio.FloatBuffer.class)
public class HeapFloatBufferTest extends FloatBufferTest {
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
    public void testAllocatedFloatBuffer_IllegalArg() {
        try {
            FloatBuffer.allocate(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
    }
}
