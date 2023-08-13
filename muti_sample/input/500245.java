@TestTargetClass(java.nio.Buffer.class)
public abstract class AbstractBufferTest extends TestCase {
    protected Buffer baseBuf;
    protected int capacity;
    protected void setUp() throws Exception{
        super.setUp();
        capacity = 10;
        baseBuf = ByteBuffer.allocate(10);
    }
    protected void tearDown() throws Exception{
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "capacity",
        args = {}
    )
    public void testCapacity() {
        assertTrue(0 <= baseBuf.position() && baseBuf.position() <= baseBuf.limit()
                && baseBuf.limit() <= baseBuf.capacity());
        assertEquals(capacity, baseBuf.capacity());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void testClear() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        Buffer ret = baseBuf.clear();
        assertSame(ret, baseBuf);
        assertEquals(0, baseBuf.position());
        assertEquals(baseBuf.limit(), baseBuf.capacity());
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "flip",
        args = {}
    )
    public void testFlip() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        baseBuf.mark();
        Buffer ret = baseBuf.flip();
        assertSame(ret, baseBuf);
        assertEquals(0, baseBuf.position());
        assertEquals(oldPosition, baseBuf.limit());
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hasRemaining",
        args = {}
    )
    public void testHasRemaining() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        assertEquals(baseBuf.hasRemaining(), baseBuf.position() < baseBuf.limit());
        baseBuf.position(baseBuf.limit());
        assertFalse(baseBuf.hasRemaining());
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Abstract method.",
        method = "isReadOnly",
        args = {}
    )
    public abstract void testIsReadOnly();
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "limit",
        args = {}
    )
    public void testLimit() {
        assertTrue(0 <= baseBuf.position() && baseBuf.position() <= baseBuf.limit()
                && baseBuf.limit() <= baseBuf.capacity());
        assertEquals(capacity, baseBuf.limit());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "limit",
        args = {int.class}
    )
    public void testLimitint() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        Buffer ret = baseBuf.limit(baseBuf.limit());
        assertSame(ret, baseBuf);
        baseBuf.mark();
        baseBuf.limit(baseBuf.capacity());
        assertEquals(baseBuf.limit(), baseBuf.capacity());
        assertEquals(oldPosition, baseBuf.position());
        baseBuf.reset();
        assertTrue("The buffer capacity was 0", baseBuf.capacity() > 0);
        baseBuf.limit(baseBuf.capacity());
        baseBuf.position(baseBuf.capacity());
        baseBuf.mark();
        baseBuf.limit(baseBuf.capacity() - 1);
        assertEquals(baseBuf.limit(), baseBuf.position());
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        try {
            baseBuf.limit(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
        try {
            baseBuf.limit(baseBuf.capacity() + 1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "mark",
        args = {}
    )
    public void testMark() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        Buffer ret = baseBuf.mark();
        assertSame(ret, baseBuf);
        baseBuf.mark();
        baseBuf.position(baseBuf.limit());
        baseBuf.reset();
        assertEquals(oldPosition, baseBuf.position());
        baseBuf.mark();
        baseBuf.position(baseBuf.limit());
        baseBuf.reset();
        assertEquals(oldPosition, baseBuf.position());
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void testPosition() {
        assertTrue(0 <= baseBuf.position() && baseBuf.position() <= baseBuf.limit()
                && baseBuf.limit() <= baseBuf.capacity());
        assertEquals(0, baseBuf.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {int.class}
    )
    public void testPositionint() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        try {
            baseBuf.position(-1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
        try {
            baseBuf.position(baseBuf.limit() + 1);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
        baseBuf.mark();
        baseBuf.position(baseBuf.position());
        baseBuf.reset();
        assertEquals(oldPosition, baseBuf.position());
        baseBuf.position(0);
        assertEquals(0, baseBuf.position());
        baseBuf.position(baseBuf.limit());
        assertEquals(baseBuf.limit(), baseBuf.position());
        assertTrue("The buffer capacity was 0.", baseBuf.capacity() > 0);
        baseBuf.limit(baseBuf.capacity());
        baseBuf.position(baseBuf.limit());
        baseBuf.mark();
        baseBuf.position(baseBuf.limit() - 1);
        assertEquals(baseBuf.limit() - 1, baseBuf.position());
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        Buffer ret = baseBuf.position(0);
        assertSame(ret, baseBuf);
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "remaining",
        args = {}
    )
    public void testRemaining() {
        assertEquals(baseBuf.remaining(), baseBuf.limit() - baseBuf.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void testReset() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        baseBuf.mark();
        baseBuf.position(baseBuf.limit());
        baseBuf.reset();
        assertEquals(oldPosition, baseBuf.position());
        baseBuf.mark();
        baseBuf.position(baseBuf.limit());
        baseBuf.reset();
        assertEquals(oldPosition, baseBuf.position());
        Buffer ret = baseBuf.reset();
        assertSame(ret, baseBuf);
        baseBuf.clear();
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "rewind",
        args = {}
    )
    public void testRewind() {
        int oldPosition = baseBuf.position();
        int oldLimit = baseBuf.limit();
        Buffer ret = baseBuf.rewind();
        assertEquals(0, baseBuf.position());
        assertSame(ret, baseBuf);
        try {
            baseBuf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        baseBuf.limit(oldLimit);
        baseBuf.position(oldPosition);
    }
}
