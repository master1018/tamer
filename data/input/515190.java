@TestTargetClass(StringTokenizer.class) 
public class StringTokenizerTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringTokenizer",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            new StringTokenizer(null);
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringTokenizer",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        StringTokenizer st = new StringTokenizer("This:is:a:test:String", ":");
        assertTrue("Created incorrect tokenizer", st.countTokens() == 5
                && (st.nextElement().equals("This")));
        st = new StringTokenizer("This:is:a:test:String", null);
        try {
            new StringTokenizer(null, ":");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringTokenizer",
        args = {java.lang.String.class, java.lang.String.class, boolean.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_StringZ() {
        StringTokenizer st = new StringTokenizer("This:is:a:test:String", ":",
                true);
        st.nextElement();
        assertTrue("Created incorrect tokenizer", st.countTokens() == 8
                && (st.nextElement().equals(":")));
        st = new StringTokenizer("This:is:a:test:String", null, true);
        st = new StringTokenizer("This:is:a:test:String", null, false);
        try {
            new StringTokenizer(null, ":", true);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "countTokens",
        args = {}
    )
    public void test_countTokens() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        assertEquals("Incorrect token count returned", 5, st.countTokens());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasMoreElements",
        args = {}
    )
    public void test_hasMoreElements() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        st.nextElement();
        assertTrue("hasMoreElements returned incorrect value", st
                .hasMoreElements());
        st.nextElement();
        st.nextElement();
        st.nextElement();
        st.nextElement();
        assertTrue("hasMoreElements returned incorrect value", !st
                .hasMoreElements());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasMoreTokens",
        args = {}
    )
    public void test_hasMoreTokens() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        for (int counter = 0; counter < 5; counter++) {
            assertTrue(
                    "StringTokenizer incorrectly reports it has no more tokens",
                    st.hasMoreTokens());
            st.nextToken();
        }
        assertTrue("StringTokenizer incorrectly reports it has more tokens",
                !st.hasMoreTokens());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "nextElement",
        args = {}
    )
    public void test_nextElement() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        assertEquals("nextElement returned incorrect value", "This", ((String) st
                .nextElement()));
        assertEquals("nextElement returned incorrect value", "is", ((String) st
                .nextElement()));
        assertEquals("nextElement returned incorrect value", "a", ((String) st
                .nextElement()));
        assertEquals("nextElement returned incorrect value", "test", ((String) st
                .nextElement()));
        assertEquals("nextElement returned incorrect value", "String", ((String) st
                .nextElement()));
        try {
            st.nextElement();
            fail(
                    "nextElement failed to throw a NoSuchElementException when it should have been out of elements");
        } catch (NoSuchElementException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "nextToken",
        args = {}
    )
    public void test_nextToken() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        assertEquals("nextToken returned incorrect value", 
                "This", st.nextToken());
        assertEquals("nextToken returned incorrect value", 
                "is", st.nextToken());
        assertEquals("nextToken returned incorrect value", 
                "a", st.nextToken());
        assertEquals("nextToken returned incorrect value", 
                "test", st.nextToken());
        assertEquals("nextToken returned incorrect value", 
                "String", st.nextToken());
        try {
            st.nextToken();
            fail(
                    "nextToken failed to throw a NoSuchElementException when it should have been out of elements");
        } catch (NoSuchElementException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "nextToken",
        args = {java.lang.String.class}
    )
    public void test_nextTokenLjava_lang_String() {
        StringTokenizer st = new StringTokenizer("This is a test String");
        assertEquals("nextToken(String) returned incorrect value with normal token String",
                "This", st.nextToken(" "));
        assertEquals("nextToken(String) returned incorrect value with custom token String",
                " is a ", st.nextToken("tr"));
        assertEquals("calling nextToken() did not use the new default delimiter list",
                "es", st.nextToken());
        st = new StringTokenizer("This:is:a:test:String", " ");
        assertTrue(st.nextToken(":").equals("This"));
        assertTrue(st.nextToken(":").equals("is"));
        assertTrue(st.nextToken(":").equals("a"));
        assertTrue(st.nextToken(":").equals("test"));
        assertTrue(st.nextToken(":").equals("String"));
        try {
            st.nextToken(":");
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
        try {
            st.nextToken(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
