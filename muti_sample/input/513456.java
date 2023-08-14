@TestTargetClass(IllegalMonitorStateException.class) 
public class IllegalMonitorStateExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalMonitorStateException",
        args = {}
    )
    public void test_Constructor() {
        IllegalMonitorStateException e = new IllegalMonitorStateException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalMonitorStateException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalMonitorStateException e = new IllegalMonitorStateException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
