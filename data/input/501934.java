@TestTargetClass(URISyntaxException.class) 
public class URISyntaxExceptionTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "URISyntaxException",
            args = {java.lang.String.class, java.lang.String.class, int.class}
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
                method = "getInput",
                args = {}
        ), 
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                method = "getReason",
                args = {}
        )
    })
    public void test_ConstructorLjava_lang_StringLjava_lang_StringI() {
        try {
            new URISyntaxException(null, "problem", 2);
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        }
        try {
            new URISyntaxException("str", null, 2);
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        }
        try {
            new URISyntaxException("str", "problem", -2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        URISyntaxException e = new URISyntaxException("str", "problem", 2);
        assertEquals("returned incorrect reason", "problem", e.getReason());
        assertEquals("returned incorrect input", "str", e.getInput());
        assertEquals("returned incorrect index", 2, e.getIndex());
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                method = "URISyntaxException",
                args = {java.lang.String.class, java.lang.String.class}
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
                method = "getInput",
                args = {}
        ), 
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                method = "getReason",
                args = {}
        )
    })
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        try {
            new URISyntaxException(null, "problem");
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        }
        try {
            new URISyntaxException("str", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        }
        URISyntaxException e = new URISyntaxException("str", "problem");
        assertEquals("returned incorrect reason", "problem", e.getReason());
        assertEquals("returned incorrect input", "str", e.getInput());
        assertEquals("returned incorrect index", -1, e.getIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        Locale.setDefault(Locale.US);
        URISyntaxException e = new URISyntaxException("str", "problem", 3);
        assertEquals("Returned incorrect message", "problem at index 3: str", e
                .getMessage());
        e = new URISyntaxException("str", "problem");
        assertEquals("Returned incorrect message", "problem: str", e
                .getMessage());
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
    protected void doneSuite() {
    }
}
