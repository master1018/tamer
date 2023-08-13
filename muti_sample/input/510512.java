@TestTargetClass(NullPointerException.class) 
public class NullPointerExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NullPointerException",
        args = {}
    )
    public void test_Constructor() {
        NullPointerException e = new NullPointerException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NullPointerException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NullPointerException e = new NullPointerException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
