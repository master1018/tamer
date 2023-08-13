@TestTargetClass(CoderResult.class)
public class CoderResultTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isError",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isMalformed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isOverflow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isUnderflow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isUnmappable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "length",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "throwException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "toString",
            args = {}
        )
    })
    public void testConstants() throws Exception {
        assertNotSame(CoderResult.OVERFLOW, CoderResult.UNDERFLOW);
        assertNotNull(CoderResult.OVERFLOW);
        assertFalse(CoderResult.OVERFLOW.isError());
        assertFalse(CoderResult.OVERFLOW.isMalformed());
        assertFalse(CoderResult.OVERFLOW.isUnderflow());
        assertFalse(CoderResult.OVERFLOW.isUnmappable());
        assertTrue(CoderResult.OVERFLOW.isOverflow());
        assertTrue(CoderResult.OVERFLOW.toString().indexOf("OVERFLOW") != -1);
        try {
            CoderResult.OVERFLOW.throwException();
            fail("Should throw BufferOverflowException");
        } catch (BufferOverflowException ex) {
        }
        try {
            CoderResult.OVERFLOW.length();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
        }
        assertNotNull(CoderResult.UNDERFLOW);
        assertFalse(CoderResult.UNDERFLOW.isError());
        assertFalse(CoderResult.UNDERFLOW.isMalformed());
        assertTrue(CoderResult.UNDERFLOW.isUnderflow());
        assertFalse(CoderResult.UNDERFLOW.isUnmappable());
        assertFalse(CoderResult.UNDERFLOW.isOverflow());
        assertTrue(CoderResult.UNDERFLOW.toString().indexOf("UNDERFLOW") != -1);
        try {
            CoderResult.UNDERFLOW.throwException();
            fail("Should throw BufferOverflowException");
        } catch (BufferUnderflowException ex) {
        }
        try {
            CoderResult.UNDERFLOW.length();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isError",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testIsError() {
        assertFalse(CoderResult.UNDERFLOW.isError());
        assertFalse(CoderResult.OVERFLOW.isError());
        assertTrue(CoderResult.malformedForLength(1).isError());
        assertTrue(CoderResult.unmappableForLength(1).isError());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isMalformed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testIsMalformed() {
        assertFalse(CoderResult.UNDERFLOW.isMalformed());
        assertFalse(CoderResult.OVERFLOW.isMalformed());
        assertTrue(CoderResult.malformedForLength(1).isMalformed());
        assertFalse(CoderResult.unmappableForLength(1).isMalformed());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isUnmappable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testIsUnmappable() {
        assertFalse(CoderResult.UNDERFLOW.isUnmappable());
        assertFalse(CoderResult.OVERFLOW.isUnmappable());
        assertFalse(CoderResult.malformedForLength(1).isUnmappable());
        assertTrue(CoderResult.unmappableForLength(1).isUnmappable());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isOverflow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testIsOverflow() {
        assertFalse(CoderResult.UNDERFLOW.isOverflow());
        assertTrue(CoderResult.OVERFLOW.isOverflow());
        assertFalse(CoderResult.malformedForLength(1).isOverflow());
        assertFalse(CoderResult.unmappableForLength(1).isOverflow());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isUnderflow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testIsUnderflow() {
        assertTrue(CoderResult.UNDERFLOW.isUnderflow());
        assertFalse(CoderResult.OVERFLOW.isUnderflow());
        assertFalse(CoderResult.malformedForLength(1).isUnderflow());
        assertFalse(CoderResult.unmappableForLength(1).isUnderflow());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "length",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testLength() {
        try {
            CoderResult.UNDERFLOW.length();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
        }
        try {
            CoderResult.OVERFLOW.length();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
        }
        assertEquals(CoderResult.malformedForLength(1).length(), 1);
        assertEquals(CoderResult.unmappableForLength(1).length(), 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "malformedForLength",
        args = {int.class}
    )
    public void testMalformedForLength() {
        assertNotNull(CoderResult.malformedForLength(Integer.MAX_VALUE));
        assertNotNull(CoderResult.malformedForLength(1));
        assertSame(CoderResult.malformedForLength(1), CoderResult
                .malformedForLength(1));
        assertNotSame(CoderResult.malformedForLength(1), CoderResult
                .unmappableForLength(1));
        assertNotSame(CoderResult.malformedForLength(2), CoderResult
                .malformedForLength(1));
        try {
            CoderResult.malformedForLength(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        try {
            CoderResult.malformedForLength(0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "unmappableForLength",
        args = {int.class}
    )
    public void testUnmappableForLength() {
        assertNotNull(CoderResult.unmappableForLength(Integer.MAX_VALUE));
        assertNotNull(CoderResult.unmappableForLength(1));
        assertSame(CoderResult.unmappableForLength(1), CoderResult
                .unmappableForLength(1));
        assertNotSame(CoderResult.unmappableForLength(2), CoderResult
                .unmappableForLength(1));
        try {
            CoderResult.unmappableForLength(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        try {
            CoderResult.unmappableForLength(0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "throwException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testThrowException() throws Exception {
        try {
            CoderResult.OVERFLOW.throwException();
            fail("Should throw BufferOverflowException");
        } catch (BufferOverflowException ex) {
        }
        try {
            CoderResult.UNDERFLOW.throwException();
            fail("Should throw BufferOverflowException");
        } catch (BufferUnderflowException ex) {
        }
        try {
            CoderResult.malformedForLength(1).throwException();
            fail("Should throw MalformedInputException");
        } catch (MalformedInputException ex) {
            assertEquals(ex.getInputLength(), 1);
        }
        try {
            CoderResult.unmappableForLength(1).throwException();
            fail("Should throw UnmappableCharacterException");
        } catch (UnmappableCharacterException ex) {
            assertEquals(ex.getInputLength(), 1);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedForLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableForLength",
            args = {int.class}
        )
    })
    public void testToString() throws Exception {
        assertTrue(CoderResult.OVERFLOW.toString().indexOf("OVERFLOW") != -1);
        assertTrue(CoderResult.UNDERFLOW.toString().indexOf("UNDERFLOW") != -1);
        assertTrue(CoderResult.malformedForLength(666).toString()
                .indexOf("666") != -1);
        assertTrue(CoderResult.unmappableForLength(666).toString().indexOf(
                "666") != -1);
    }
}
