@TestTargetClass(NoSuchFieldException.class) 
public class NoSuchFieldExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchFieldException",
        args = {}
    )
    public void test_Constructor() {
        NoSuchFieldException e = new NoSuchFieldException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchFieldException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NoSuchFieldException e = new NoSuchFieldException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
