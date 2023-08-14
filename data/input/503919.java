@TestTargetClass(ArrayStoreException.class) 
public class ArrayStoreExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayStoreException",
        args = {}
    )
    public void test_Constructor() {
        ArrayStoreException e = new ArrayStoreException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayStoreException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ArrayStoreException e = new ArrayStoreException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
