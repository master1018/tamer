@TestTargetClass(OutOfMemoryError.class) 
public class OutOfMemoryErrorTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutOfMemoryError",
        args = {}
    )
    public void test_Constructor() {
        Error e = new OutOfMemoryError();
        assertNull(e.getCause());
        assertNull(e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutOfMemoryError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        Error e = new OutOfMemoryError(null);
        assertNull(e.getMessage());
        assertNull(e.getCause());
        e= new OutOfMemoryError("msg");
        assertEquals("msg", e.getMessage());
        assertNull(e.getCause());
    }
}
