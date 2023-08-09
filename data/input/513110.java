@TestTargetClass(NoSuchMethodError.class) 
public class NoSuchMethodErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodError",
        args = {}
    )
    public void test_Constructor() {
        NoSuchMethodError e = new NoSuchMethodError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NoSuchMethodError e = new NoSuchMethodError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
