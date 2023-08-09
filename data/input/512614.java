@TestTargetClass(ClassCastException.class) 
public class ClassCastExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassCastException",
        args = {}
    )
    public void test_Constructor() {
        ClassCastException e = new ClassCastException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassCastException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ClassCastException e = new ClassCastException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
