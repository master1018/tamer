@TestTargetClass(StackOverflowError.class) 
public class StackOverflowErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StackOverflowError",
        args = {}
    )
    public void test_Constructor() {
        StackOverflowError e = new StackOverflowError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StackOverflowError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        StackOverflowError e = new StackOverflowError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
