@TestTargetClass(ClassNotFoundException.class) 
public class ClassNotFoundExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {}
    )
    public void test_Constructor() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ClassNotFoundException e = new ClassNotFoundException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        String testMessage = "Test Message";
        Throwable thr = new Throwable();
        ClassNotFoundException cnfe = new ClassNotFoundException(testMessage, thr);
        assertEquals(testMessage, cnfe.getMessage());
        assertEquals(thr, cnfe.getException());
        cnfe = new ClassNotFoundException(null, thr);
        assertNull(cnfe.getMessage());
        assertEquals(thr, cnfe.getException());
        cnfe = new ClassNotFoundException(testMessage, null);
        assertNull(cnfe.getException());
        assertEquals(testMessage, cnfe.getMessage());
        cnfe = new ClassNotFoundException(null, null);
        assertNull(cnfe.getMessage());
        assertNull(cnfe.getException());
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getCause());
        e = new ClassNotFoundException("Message");
        assertNull(e.getCause());
        NullPointerException cause = new NullPointerException();
        Throwable thr = new Throwable(cause);
        e = new ClassNotFoundException("Message", thr);
        assertEquals(thr, e.getCause());
        e = new ClassNotFoundException("Message", null);
        assertEquals(null, e.getCause());       
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void test_getException() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getException());
        e = new ClassNotFoundException("Message");
        assertNull(e.getException());
        NullPointerException cause = new NullPointerException();
        Throwable thr = new Throwable(cause);
        e = new ClassNotFoundException("Message", thr);
        assertEquals(thr, e.getException());
        e = new ClassNotFoundException("Message", null);
        assertEquals(null, e.getException());       
    }
}
