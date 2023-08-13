@TestTargetClass(NoClassDefFoundError.class) 
public class NoClassDefFoundErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoClassDefFoundError",
        args = {}
    )
    public void test_Constructor() {
        NoClassDefFoundError e = new NoClassDefFoundError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoClassDefFoundError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NoClassDefFoundError e = new NoClassDefFoundError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
