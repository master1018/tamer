@TestTargetClass(java.nio.ShortBuffer.class)
public class WrappedShortBufferTest extends ShortBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = ShortBuffer.wrap(new short[BUFFER_LENGTH]);
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
        args = {short[].class, int.class, int.class}
    )
    public void testWrappedShortBuffer_IllegalArg() {
        short array[] = new short[20];
        try {
            ShortBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap(array, 21, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap(array, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ShortBuffer.wrap((short[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
        ShortBuffer buf = ShortBuffer.wrap(array, 2, 16);
        assertEquals(buf.position(), 2);
        assertEquals(buf.limit(), 18);
        assertEquals(buf.capacity(), 20);
    }
}
