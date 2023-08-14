@TestTargetClass(IncompatibleClassChangeError.class) 
public class IncompatibleClassChangeErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IncompatibleClassChangeError",
        args = {}
    )
    public void test_Constructor() {
        IncompatibleClassChangeError e = new IncompatibleClassChangeError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IncompatibleClassChangeError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IncompatibleClassChangeError e = new IncompatibleClassChangeError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
