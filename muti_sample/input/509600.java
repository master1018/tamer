@TestTargetClass(IllegalThreadStateException.class) 
public class IllegalThreadStateExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalThreadStateException",
        args = {}
    )
    public void test_Constructor() {
        IllegalThreadStateException e = new IllegalThreadStateException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalThreadStateException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalThreadStateException e = new IllegalThreadStateException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
