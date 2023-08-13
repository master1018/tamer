@TestTargetClass(NoSuchFieldError.class) 
public class NoSuchFieldErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchFieldError",
        args = {}
    )
    public void test_Constructor() {
        NoSuchFieldError e = new NoSuchFieldError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchFieldError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NoSuchFieldError e = new NoSuchFieldError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
