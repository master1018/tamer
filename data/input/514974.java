@TestTargetClass(StringIndexOutOfBoundsException.class) 
public class StringIndexOutOfBoundsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringIndexOutOfBoundsException",
        args = {}
    )
    public void test_Constructor() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringIndexOutOfBoundsException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringIndexOutOfBoundsException",
        args = {int.class}
    )
    public void test_ConstructorLint() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException(0);
        assertTrue(e.getMessage().contains("0"));
    }
}
