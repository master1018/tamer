@TestTargetClass(ParseException.class) 
public class ParseExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ParseException",
        args = {java.lang.String.class, int.class}
    )
    public void test_ConstructorLjava_lang_StringI() {
        try {
            DateFormat df = DateFormat.getInstance();
            df.parse("HelloWorld");
            fail("ParseException not created/thrown.");
        } catch (ParseException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getErrorOffset",
        args = {}
    )
    public void test_getErrorOffset() {
        try {
            DateFormat df = DateFormat.getInstance();
            df.parse("1999HelloWorld");
        } catch (ParseException e) {
            assertEquals("getErrorOffsetFailed.", 4, e.getErrorOffset());
        }
    }
}
