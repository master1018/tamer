@TestTargetClass(CloneNotSupportedException.class) 
public class CloneNotSupportedExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CloneNotSupportedException",
        args = {}
    )
    public void test_Constructor() {
        CloneNotSupportedException e = new CloneNotSupportedException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CloneNotSupportedException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        CloneNotSupportedException e = new CloneNotSupportedException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
