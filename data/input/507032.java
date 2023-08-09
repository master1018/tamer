@TestTargetClass(ArithmeticException.class) 
public class ArithmeticExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArithmeticException",
        args = {}
    )
    public void test_Constructor() {
        ArithmeticException e = new ArithmeticException();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArithmeticException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ArithmeticException e = new ArithmeticException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
