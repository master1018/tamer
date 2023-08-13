@TestTargetClass(IndexOutOfBoundsException.class) 
public class IndexOutOfBoundsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IndexOutOfBoundsException",
        args = {}
    )
    public void test_Constructor() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IndexOutOfBoundsException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
