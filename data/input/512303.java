@TestTargetClass(LinkageError.class) 
public class LinkageErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkageError",
        args = {}
    )
    public void test_Constructor() {
        LinkageError e = new LinkageError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkageError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        LinkageError e = new LinkageError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
