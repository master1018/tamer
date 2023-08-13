@TestTargetClass(ExceptionInInitializerError.class) 
public class ExceptionInInitializerErrorTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        )
    })
    public void test_Constructor() {
        ExceptionInInitializerError e = new ExceptionInInitializerError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_String() {
        ExceptionInInitializerError e = new ExceptionInInitializerError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {java.lang.Throwable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getException",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException("fixture");
        ExceptionInInitializerError e = new ExceptionInInitializerError(npe);
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertSame(npe, e.getException());
        assertSame(npe, e.getCause());
    }
}
