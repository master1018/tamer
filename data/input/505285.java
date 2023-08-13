@TestTargetClass(java.nio.IntBuffer.class)
public class WrappedIntBufferTest extends IntBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = IntBuffer.wrap(new int[BUFFER_LENGTH]);
        loadTestData1(buf);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        baseBuf = null;
        buf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {int[].class, int.class, int.class}
    )
    public void testWrappedIntBuffer_IllegalArg() {
        int array[] = new int[20];
        try {
            IntBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap(array, 21, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap(array, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            IntBuffer.wrap((int[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
        IntBuffer buf = IntBuffer.wrap(array, 2, 16);
        assertEquals(buf.position(), 2);
        assertEquals(buf.limit(), 18);
        assertEquals(buf.capacity(), 20);
    }
}
