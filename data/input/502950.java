@TestTargetClass(UnknownError.class) 
public class UnknownErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownError",
        args = {}
    )
    public void test_Constructor() {
        UnknownError e = new UnknownError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        UnknownError e = new UnknownError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
