@TestTargetClass(NoSuchMethodException.class) 
public class NoSuchMethodExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodException",
        args = {}
    )
    public void test_Constructor() {
        NoSuchMethodException e = new NoSuchMethodException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NoSuchMethodException e = new NoSuchMethodException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
