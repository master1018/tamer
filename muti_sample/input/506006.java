@TestTargetClass(java.nio.DoubleBuffer.class)
public class WrappedDoubleBufferTest extends DoubleBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = DoubleBuffer.wrap(new double[BUFFER_LENGTH]);
        loadTestData1(buf);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {double[].class, int.class, int.class}
    )
    public void testWrappedDoubleuffer_IllegalArg() {
        double array[] = new double[20];
        try {
            DoubleBuffer.wrap(array, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap(array, 21, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap(array, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DoubleBuffer.wrap((double[])null, -1, 0);
            fail("Should throw NPE"); 
        } catch (NullPointerException e) {
        }
        DoubleBuffer buf = DoubleBuffer.wrap(array, 2, 16);
        assertEquals(buf.position(), 2);
        assertEquals(buf.limit(), 18);
        assertEquals(buf.capacity(), 20);
    }
}
