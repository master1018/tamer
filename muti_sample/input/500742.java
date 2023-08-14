@TestTargetClass(InstantiationError.class) 
public class InstantiationErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InstantiationError",
        args = {}
    )
    public void test_Constructor() {
        InstantiationError e = new InstantiationError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InstantiationError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InstantiationError e = new InstantiationError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
