@TestTargetClass(VerifyError.class) 
public class VerifyErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "VerifyError",
        args = {}
    )
    public void test_Constructor() {
        VerifyError e = new VerifyError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "VerifyError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        VerifyError e = new VerifyError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
