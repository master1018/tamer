@TestTargetClass(java.nio.ByteBuffer.class)
public class WrappedByteBufferTest extends ByteBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = ByteBuffer.wrap(new byte[BUFFER_LENGTH]);
        loadTestData1(buf);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException, NullPointerException.",
        method = "wrap",
        args = {byte[].class, int.class, int.class}
    )
    public void testWrappedByteBuffer_IllegalArg() {
        byte array[] = new byte[BUFFER_LENGTH];
        try {
            ByteBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap(array, BUFFER_LENGTH + 1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap(array, 0, BUFFER_LENGTH + 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ByteBuffer.wrap((byte[])null, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
    }
}
