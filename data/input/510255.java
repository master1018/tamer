@TestTargetClass(InterruptedException.class) 
public class InterruptedExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InterruptedException",
        args = {}
    )
    public void test_Constructor() {
        InterruptedException e = new InterruptedException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InterruptedException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InterruptedException e = new InterruptedException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
