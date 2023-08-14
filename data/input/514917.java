@TestTargetClass(UnsatisfiedLinkError.class) 
public class UnsatisfiedLinkErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsatisfiedLinkError",
        args = {}
    )
    public void test_Constructor() {
        UnsatisfiedLinkError e = new UnsatisfiedLinkError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsatisfiedLinkError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        UnsatisfiedLinkError e = new UnsatisfiedLinkError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
