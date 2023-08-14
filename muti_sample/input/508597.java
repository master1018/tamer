@TestTargetClass(IllegalAccessError.class) 
public class IllegalAccessErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalAccessError",
        args = {}
    )
    public void test_Constructor() {
        IllegalAccessError e = new IllegalAccessError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalAccessError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalAccessError e = new IllegalAccessError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
