@TestTargetClass(java.nio.FloatBuffer.class)
public class WrappedFloatBufferTest extends FloatBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = FloatBuffer.wrap(new float[BUFFER_LENGTH]);
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
        args = {float[].class, int.class, int.class}
    )
    public void testWrappedFloatBuffer_IllegalArg() {
        float array[] = new float[20];
        try {
            FloatBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap(array, 21, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap(array, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            FloatBuffer.wrap((float[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
        FloatBuffer buf = FloatBuffer.wrap(array, 2, 16);
        assertEquals(buf.position(), 2);
        assertEquals(buf.limit(), 18);
        assertEquals(buf.capacity(), 20);
    }
}
