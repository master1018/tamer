@TestTargetClass(java.nio.LongBuffer.class)
public class WrappedLongBufferTest extends LongBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = LongBuffer.wrap(new long[BUFFER_LENGTH]);
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
        args = {long[].class, int.class, int.class}
    )
    public void testWrappedLongBuffer_IllegalArg() {
        long array[] = new long[20];
        try {
            LongBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap(array, 21, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap(array, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            LongBuffer.wrap((long[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
        LongBuffer buf = LongBuffer.wrap(array, 2, 16);
        assertEquals(buf.position(), 2);
        assertEquals(buf.limit(), 18);
        assertEquals(buf.capacity(), 20);
    }
}
