@TestTargetClass(java.nio.CharBuffer.class)
public class WrappedCharBufferTest1 extends CharBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = CharBuffer.wrap(new char[BUFFER_LENGTH]);
        loadTestData1(buf);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        baseBuf = null;
        buf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException, NullPointerException.",
        method = "wrap",
        args = {java.lang.CharSequence.class, int.class, int.class}
    )
    public void testWrappedCharBuffer_IllegalArg() {
        char array[] = new char[BUFFER_LENGTH];
        try {
            CharBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(array, BUFFER_LENGTH + 1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(array, 0, BUFFER_LENGTH + 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap((char[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
    }
}
