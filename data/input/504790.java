@TestTargetClass(NegativeArraySizeException.class) 
public class NegativeArraySizeExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NegativeArraySizeException",
        args = {}
    )
    public void test_Constructor() {
        NegativeArraySizeException e = new NegativeArraySizeException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NegativeArraySizeException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NegativeArraySizeException e = new NegativeArraySizeException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
