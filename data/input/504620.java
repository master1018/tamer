@TestTargetClass(InstantiationException.class) 
public class InstantiationExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InstantiationException",
        args = {}
    )
    public void test_Constructor() {
        InstantiationException e = new InstantiationException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InstantiationException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InstantiationException e = new InstantiationException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
