@TestTargetClass(java.nio.ShortBuffer.class)
public class ReadOnlyShortBufferTest extends ShortBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isReadOnly method for read only ShortBuffer.",
        method = "isReadOnly",
        args = {}
    )
    public void testIsReadOnly() {
        assertTrue(buf.isReadOnly());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies hasArray method for read only ShortBuffer.",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "array",
        args = {}
    )
    public void testArray() {
        try {
            buf.array();
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        ShortBuffer duplicate = buf.duplicate();
        assertEquals(buf.hashCode(), duplicate.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "compact",
        args = {}
    )
    public void testCompact() {
        try {
            buf.compact();
            fail("Should throw Exception"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "put",
        args = {short.class}
    )
    public void testPutshort() {
        try {
            buf.put((short)0);
            fail("Should throw Exception"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "put",
        args = {short[].class}
    )
    public void testPutshortArray() {
        short array[] = new short[1];
        try {
            buf.put(array);
            fail("Should throw Exception"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put((short[]) null);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "put",
        args = {short[].class, int.class, int.class}
    )
    public void testPutshortArrayintint() {
        short array[] = new short[1];
        try {
            buf.put(array, 0, array.length);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put((short[]) null, 0, 1);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put(new short[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put(array, -1, array.length);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "put",
        args = {java.nio.ShortBuffer.class}
    )
    public void testPutShortBuffer() {
        ShortBuffer other = ShortBuffer.allocate(1);
        try {
            buf.put(other);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put((ShortBuffer) null);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put(buf);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException.",
        method = "put",
        args = {int.class, short.class}
    )
    public void testPutintshort() {
        try {
            buf.put(0, (short) 0);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put(-1, (short) 0);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
    }
}
