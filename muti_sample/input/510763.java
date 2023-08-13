@TestTargetClass(TypeNotPresentException.class) 
public class TypeNotPresentExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TypeNotPresentException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_constructorLjava_lang_StringLjava_lang_Throwable() {
        TypeNotPresentException e = new TypeNotPresentException(null, null);
        assertNotNull(e);
        String m = e.getMessage();
        assertNotNull(m);
        e = new TypeNotPresentException(getClass().getName(), null);
        assertNotNull(e);
        m = e.getMessage();
        assertNotNull(m);
        NullPointerException npe = new NullPointerException();
        e = new TypeNotPresentException(getClass().getName(), npe);
        assertNotNull(e.getMessage());
        assertSame(npe, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "typeName",
        args = {}
    )
    public void test_typeName() {
        TypeNotPresentException e = new TypeNotPresentException(null, null);
        assertNull(e.typeName());
        e = new TypeNotPresentException(getClass().getName(), null);
        assertEquals(getClass().getName(), e.typeName());
    }
}
