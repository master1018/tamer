@TestTargetClass(PatternSyntaxException.class)
public class PatternSyntaxExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "PatternSyntaxException",
            args = {java.lang.String.class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPattern",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getIndex",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        )
    })          
    public void testPatternSyntaxException() {
        PatternSyntaxException e = new PatternSyntaxException("Foo", "Bar", 0);
        assertEquals("Foo", e.getDescription());
        assertEquals("Bar", e.getPattern());
        assertEquals(0, e.getIndex());
        String s = e.getMessage();
        assertTrue(s.contains("Foo"));
        assertTrue(s.contains("Bar"));
        assertTrue(s.contains("0"));
        e = new PatternSyntaxException(null, "Bar", 0);
        assertEquals(null, e.getDescription());
        assertEquals("Bar", e.getPattern());
        assertEquals(0, e.getIndex());
        s = e.getMessage();
        assertFalse(s.contains("Foo"));
        assertTrue(s.contains("Bar"));
        assertTrue(s.contains("0"));
        e = new PatternSyntaxException("Foo", null, 0);
        assertEquals("Foo", e.getDescription());
        assertEquals(null, e.getPattern());
        assertEquals(0, e.getIndex());
        s = e.getMessage();
        assertTrue(s.contains("Foo"));
        assertFalse(s.contains("Bar"));
        assertTrue(s.contains("0"));
        e = new PatternSyntaxException(null, null, 0);
        assertEquals(null, e.getDescription());
        assertEquals(null, e.getPattern());
        assertEquals(0, e.getIndex());
        s = e.getMessage();
        assertFalse(s.contains("Foo"));
        assertFalse(s.contains("Bar"));
        assertTrue(s.contains("0"));
        e = new PatternSyntaxException("Foo", "Bar", -1);
        assertEquals(-1, e.getIndex());
        s = e.getMessage();
        assertFalse(s.contains("^"));
        e = new PatternSyntaxException("Foo", null, 0);
        assertEquals(0, e.getIndex());
        s = e.getMessage();
        assertFalse(s.contains("^"));
    }
}
