@TestTargetClass(InternalError.class) 
public class InternalErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InternalError",
        args = {}
    )
    public void test_Constructor() {
        InternalError e = new InternalError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InternalError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InternalError e = new InternalError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
