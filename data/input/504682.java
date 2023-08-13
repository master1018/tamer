@TestTargetClass(RuntimeException.class) 
public class RuntimeExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimeException",
        args = {}
    )
    public void test_Constructor() {
        RuntimeException e = new RuntimeException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimeException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        RuntimeException e = new RuntimeException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimeException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_ConstructorLStringLThrowable() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        RuntimeException re = new RuntimeException(message, npe);
        assertEquals(message, re.getMessage());
        assertEquals(npe, re.getCause());
        re = new RuntimeException(null, npe);
        assertNull(re.getMessage());
        re = new RuntimeException(message, null);
        assertNull(re.getCause());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimeException",
        args = {java.lang.Throwable.class}
    )
    public void test_ConstructorLThrowable() {
        NullPointerException npe = new NullPointerException();
        RuntimeException re = new RuntimeException(npe);        
        assertEquals(npe, re.getCause());
        re = new RuntimeException((Throwable) null);
        assertNull(re.getCause());
    }
}
