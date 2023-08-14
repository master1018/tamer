@TestTargetClass(ParsePosition.class) 
public class ParsePositionTest extends junit.framework.TestCase {
    ParsePosition pp;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ParsePosition",
        args = {int.class}
    )
    public void test_ConstructorI() {
        ParsePosition pp1 = new ParsePosition(Integer.MIN_VALUE);
        assertTrue("Initialization failed.",
                pp1.getIndex() == Integer.MIN_VALUE);
        assertEquals("Initialization failed.", -1, pp1.getErrorIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        ParsePosition pp2 = new ParsePosition(43);
        pp2.setErrorIndex(56);
        assertTrue("equals failed.", !pp.equals(pp2));
        pp.setErrorIndex(56);
        pp.setIndex(43);
        assertTrue("equals failed.", pp.equals(pp2));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getErrorIndex",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setErrorIndex",
            args = {int.class}
        )
    })
    public void test_getErrorIndex() {
        pp.setErrorIndex(56);
        assertEquals("getErrorIndex failed.", 56, pp.getErrorIndex());
        pp.setErrorIndex(Integer.MAX_VALUE);
        assertEquals("getErrorIndex failed.", Integer.MAX_VALUE, 
                pp.getErrorIndex()); 
        assertEquals("getErrorIndex failed.", Integer.MAX_VALUE, 
                pp.getErrorIndex());         
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIndex",
        args = {}
    )
    public void test_getIndex() {
        assertTrue("getIndex failed.", pp.getIndex() == Integer.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        ParsePosition pp1 = new ParsePosition(0);
        ParsePosition pp2 = new ParsePosition(0);
        assertTrue("hashCode returns non equal hash codes for equal objects.", 
                pp1.hashCode() == pp2.hashCode());
        pp1.setIndex(Integer.MAX_VALUE);
        assertTrue("hashCode returns equal hash codes for non equal objects.", 
                pp1.hashCode() != pp2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setErrorIndex",
        args = {int.class}
    )
    public void test_setErrorIndexI() {
        pp.setErrorIndex(4564);
        assertEquals("setErrorIndex failed.", 4564, pp.getErrorIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIndex",
        args = {int.class}
    )
    public void test_setIndexI() {
        pp.setIndex(4564);
        assertEquals("setErrorIndex failed.", 4564, pp.getIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertNotNull("toString returns null.", pp.toString());
    }
    protected void setUp() {
        pp = new ParsePosition(Integer.MAX_VALUE);
    }
    protected void tearDown() {
    }
}
