@TestTargetClass(ArrayIndexOutOfBoundsException.class) 
public class ArrayIndexOutOfBoundsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {int.class}
    )
    public void test_ConstructorI() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(-1);
        assertNotNull(e.getMessage());
        assertTrue("Unable to find index value in 'message' property.", e.getMessage().indexOf(
                "-1", 0) >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {}
    )
    public void test_Constructor() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
