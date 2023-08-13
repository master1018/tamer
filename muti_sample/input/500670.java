@TestTargetClass(IllegalAccessException.class) 
public class IllegalAccessExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalAccessException",
        args = {}
    )
    public void test_Constructor() {
        IllegalAccessException e = new IllegalAccessException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalAccessException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalAccessException e = new IllegalAccessException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
