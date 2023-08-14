@TestTargetClass(NumberFormatException.class) 
public class NumberFormatExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NumberFormatException",
        args = {}
    )
    public void test_Constructor() {
        NumberFormatException e = new NumberFormatException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NumberFormatException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NumberFormatException e = new NumberFormatException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
